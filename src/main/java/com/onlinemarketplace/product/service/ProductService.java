package com.onlinemarketplace.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinemarketplace.product.model.CacheData;
import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.repository.CacheDataRepository;
import com.onlinemarketplace.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheDataRepository cacheDataRepository;
    @Autowired
    ObjectMapper objectMapper;
    public Page<Product> getAllProducts(Pageable pageable) throws JsonProcessingException {
        String cacheKey = "allProducts_" + pageable.getPageNumber() + "_" + pageable.getPageSize();
        Optional<CacheData> optionalCacheData = cacheDataRepository.findById(cacheKey);
        // Cache hit
        if (optionalCacheData.isPresent()) {
            String productAsString = optionalCacheData.get().getValue();
            var mapType = new TypeReference<List<Product>>() {};
            List<Product> productList = objectMapper.readValue(productAsString, mapType);
            return new PageImpl<>(productList, pageable, productList.size());
        }

        // Cache miss
        Page<Product> productPage = productRepository.findAll(pageable);
        List<Product> productList = productPage.getContent();
        String productsAsJsonString = objectMapper.writeValueAsString(productList);
        CacheData cacheData = new CacheData(cacheKey, productsAsJsonString);

        cacheDataRepository.save(cacheData);
        return productPage;
    }

    public Product getProductByIdAndUserId(String productId, String userId) throws ChangeSetPersister.NotFoundException {
        return productRepository.findByIdAndUserId(productId, userId).orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    public Product createProduct(Product product) {
        // Additional validation and business logic can be implemented here
        deleteCacheData();
        return productRepository.save(product);
    }

    public Product updateProduct(String productId, Product updatedProduct, String userId) throws ChangeSetPersister.NotFoundException {
        Product existingProduct = getProductByIdAndUserId(productId, userId);
        // Update the existing product with the new data
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        // Additional updates and business logic can be implemented here
        deleteCacheData();
        return productRepository.save(existingProduct);
    }

    public void deleteProduct(String productId, String userId) throws ChangeSetPersister.NotFoundException {
        Product existingProduct = getProductByIdAndUserId(productId, userId);
        productRepository.delete(existingProduct);
        deleteCacheData();
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private void deleteCacheData() {
        Set<String> keysToDelete = redisTemplate.keys("cacheData:allProducts*");

        redisTemplate.delete(keysToDelete);
    }
}


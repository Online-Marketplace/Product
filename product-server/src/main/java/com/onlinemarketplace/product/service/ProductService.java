package com.onlinemarketplace.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinemarketplace.common.ulti.MappingUtils;
import com.onlinemarketplace.product.dto.ProductDetail;
import com.onlinemarketplace.product.model.CacheData;
import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.repository.CacheDataRepository;
import com.onlinemarketplace.product.repository.ProductRepository;
import om.onlinemarketplace.category.dto.CategoryClient;
import om.onlinemarketplace.category.dto.CategoryRequest;
import om.onlinemarketplace.category.dto.CategoryResponse;
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

    CategoryClient categoryClient = new CategoryClient();
    public Page<ProductDetail> getAllProducts(Pageable pageable) throws JsonProcessingException {
        String cacheKey = "allProducts_" + pageable.getPageNumber() + "_" + pageable.getPageSize();
        Optional<CacheData> optionalCacheData = cacheDataRepository.findById(cacheKey);
        // Cache hit
        if (optionalCacheData.isPresent()) {
            String productAsString = optionalCacheData.get().getValue();
            var mapType = new TypeReference<List<ProductDetail>>() {};
            List<ProductDetail> productList = objectMapper.readValue(productAsString, mapType);
            return new PageImpl<>(productList, pageable, productList.size());
        }

        // Cache miss
        Page<Product> productEntityPage = productRepository.findAll(pageable);
        List<ProductDetail> productList = MappingUtils.copyProperties(productEntityPage.getContent(), ProductDetail.class);
        Page<ProductDetail> productPage = new PageImpl<>(productList, pageable, productEntityPage.getTotalElements());
        String productsAsJsonString = objectMapper.writeValueAsString(productList);
        CacheData cacheData = new CacheData(cacheKey, productsAsJsonString);

        cacheDataRepository.save(cacheData);
        return productPage;
    }

    public Page<Product> getProductsByCategoryId(String categoryId, Pageable pageable) {
        CategoryRequest categoryRequest = new CategoryRequest();
        CategoryResponse categoryResponse = categoryClient.sendGetRequest("/" + categoryId + "/children",categoryRequest).getBody();
        List<String> categoryIds = null;
        if (categoryResponse != null) {
            categoryIds = categoryResponse.getCategoryIds();
        }
        return productRepository.findByCategoryIds(categoryIds, pageable);
    }

    public ProductDetail getProductByIdAndUserId(String productId, String userId) throws ChangeSetPersister.NotFoundException {
        Optional<Product> optionalProduct = productRepository.findByIdAndUserId(productId, userId);
        if (optionalProduct.isEmpty()) {
            throw new ChangeSetPersister.NotFoundException();
        }
        return MappingUtils.copyProperties(optionalProduct.get(), ProductDetail.class);
    }

    public ProductDetail createProduct(ProductDetail product) {
        // Additional validation and business logic can be implemented here
        deleteCacheData();
        var productEntity = MappingUtils.copyProperties(product, Product.class);
        return MappingUtils.copyProperties(productRepository.save(productEntity), ProductDetail.class);
    }

    public ProductDetail updateProduct(String productId, ProductDetail updatedProduct, String userId) throws ChangeSetPersister.NotFoundException {
        ProductDetail existingProduct = getProductByIdAndUserId(productId, userId);
        // Update the existing product with the new data
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        var productToUpdate = MappingUtils.copyProperties(existingProduct, Product.class);
        // Additional updates and business logic can be implemented here
        deleteCacheData();
        return MappingUtils.copyProperties(productRepository.save(productToUpdate), ProductDetail.class);
    }

    public void deleteProduct(String productId, String userId) throws ChangeSetPersister.NotFoundException {
        ProductDetail existingProduct = getProductByIdAndUserId(productId, userId);
        var productToDelete = MappingUtils.copyProperties(existingProduct, Product.class);
        productRepository.delete(productToDelete);
        deleteCacheData();
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private void deleteCacheData() {
        Set<String> keysToDelete = redisTemplate.keys("cacheData:allProducts*");

        redisTemplate.delete(keysToDelete);
    }
}


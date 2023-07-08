package com.onlinemarketplace.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinemarketplace.common.ulti.CollectionUtils;
import com.onlinemarketplace.common.ulti.MappingUtils;
import com.onlinemarketplace.product.dto.ProductDetail;
import com.onlinemarketplace.product.dto.ProductRequest;
import com.onlinemarketplace.product.model.CacheData;
import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.repository.CacheDataRepository;
import com.onlinemarketplace.product.repository.ProductRepository;
import com.onlinemarketplace.product.repository.ProductSpecification;
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
    public Page<ProductDetail> getAllProducts(ProductRequest productRequest) throws JsonProcessingException {
        Pageable pageable = productRequest.getPageable();
        String cacheKey = formCacheKey(productRequest);
        Optional<CacheData> optionalCacheData = cacheDataRepository.findById(cacheKey);
        // Cache hit
        if (optionalCacheData.isPresent()) {
            List<ProductDetail> productList = cacheDataToProductList(optionalCacheData.get());
            return new PageImpl<>(productList, pageable, productList.size());
        }

        // Cache miss
        Page<Product> productEntityPage = findProductEntityPage(pageable, productRequest);
        List<ProductDetail> productList = MappingUtils.copyProperties(productEntityPage.getContent(), ProductDetail.class);
        saveProductListToCache(productList, cacheKey);

        return new PageImpl<>(productList, pageable, productEntityPage.getTotalElements());
    }

    private Page<Product> findProductEntityPage(Pageable pageable, ProductRequest searchCriteria) {
        List<String> categoryIds = getFamilyCategories(searchCriteria.getCategoryId());
        searchCriteria.setCategoryIds(categoryIds);
        return productRepository.findAll(ProductSpecification.bySearchCriteria(searchCriteria),pageable);
    }

    private String formCacheKey(ProductRequest searchCriteria) {
        return "cacheData:allProducts" + (searchCriteria != null ? ":searchCriteria=" + searchCriteria.toString() : "");
    }

    private List<ProductDetail> cacheDataToProductList(CacheData cacheData) throws JsonProcessingException {
        String productAsString = cacheData.getValue();
        return objectMapper.readValue(productAsString, new TypeReference<List<ProductDetail>>() {});
    }

    private void saveProductListToCache(List<ProductDetail> productList, String cacheKey) throws JsonProcessingException {
        String productListAsString = objectMapper.writeValueAsString(productList);
        CacheData cacheData = new CacheData(cacheKey, productListAsString);
        cacheDataRepository.save(cacheData);
    }

    private List<String> getFamilyCategories(String categoryId) {
        CategoryRequest categoryRequest = new CategoryRequest();
        CategoryResponse categoryResponse = categoryClient.sendGetRequest("/" + categoryId + "/children",categoryRequest).getBody();
        if(categoryResponse == null || CollectionUtils.isEmpty(categoryResponse.getCategoryIds())) {
            return List.of(categoryId);
        }
        return categoryResponse.getCategoryIds();
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


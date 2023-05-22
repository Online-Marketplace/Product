package com.onlinemarketplace.product.service;

import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductByIdAndUserId(String productId, String userId) throws ChangeSetPersister.NotFoundException {
        return productRepository.findByIdAndUserId(productId, userId).orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    public Product createProduct(Product product) {
        // Additional validation and business logic can be implemented here
        return productRepository.save(product);
    }

    public Product updateProduct(String productId, Product updatedProduct, String userId) throws ChangeSetPersister.NotFoundException {
        Product existingProduct = getProductByIdAndUserId(productId, userId);
        // Update the existing product with the new data
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        // Additional updates and business logic can be implemented here
        return productRepository.save(existingProduct);
    }

    public void deleteProduct(String productId, String userId) throws ChangeSetPersister.NotFoundException {
        Product existingProduct = getProductByIdAndUserId(productId, userId);
        productRepository.delete(existingProduct);
    }
}


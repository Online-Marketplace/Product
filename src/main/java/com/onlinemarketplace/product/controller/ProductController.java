package com.onlinemarketplace.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Page<Product> getAllProducts(Pageable pageable) throws JsonProcessingException {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable("productId") String productId, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        try {
            Product product = productService.getProductByIdAndUserId(productId, userId);
            return ResponseEntity.ok(product);
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @RequestHeader("id") String userId) {
        product.setUserId(userId);
        return ResponseEntity.ok(productService.createProduct(product));
    }
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable("productId") String productId, @RequestBody Product updatedProduct, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        try {
            Product product = productService.updateProduct(productId, updatedProduct, userId);
            return ResponseEntity.ok(product);
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable("productId") String productId, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        productService.deleteProduct(productId, userId);
    }
}

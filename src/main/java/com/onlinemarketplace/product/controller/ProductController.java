package com.onlinemarketplace.product.controller;

import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
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
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable("productId") String productId, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        return productService.getProductByIdAndUserId(productId, userId);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product, @RequestHeader("id") String userId) {
        product.setUserId(userId);
        return productService.createProduct(product);
    }

    @PutMapping("/{productId}")
    public Product updateProduct(@PathVariable("productId") String productId, @RequestBody Product updatedProduct, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        return productService.updateProduct(productId, updatedProduct, userId);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable("productId") String productId, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        productService.deleteProduct(productId, userId);
    }
}

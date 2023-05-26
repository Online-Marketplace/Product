package com.onlinemarketplace.product.repository;

import com.onlinemarketplace.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // Additional custom query methods can be added here
    Optional<Product> findByIdAndUserId(String id, String userId);
}
package com.onlinemarketplace.product.repository;

import com.onlinemarketplace.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // Additional custom query methods can be added here
    Optional<Product> findByIdAndUserId(String id, String userId);
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.categoryId IN (:categoryIds)")
    Page<Product> findByCategoryIds(List<String> categoryIds, Pageable pageable);
}
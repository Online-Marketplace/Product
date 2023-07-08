package com.onlinemarketplace.product.repository;

import com.onlinemarketplace.product.dto.ProductRequest;
import com.onlinemarketplace.product.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> bySearchCriteria(ProductRequest productCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productCriteria.getCategoryIds() != null && !productCriteria.getCategoryIds().isEmpty()) {
                predicates.add(root.get("categoryId").in(productCriteria.getCategoryIds()));
            }

            if (productCriteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), productCriteria.getMinPrice()));
            }

            if (productCriteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), productCriteria.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

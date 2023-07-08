package com.onlinemarketplace.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductRequest {
    private Pageable pageable;
    private ProductDetail productDetail;
    private String categoryId;
    private List<String> categoryIds;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    @Override
    public String toString() {
        return  "categoryIds=" + categoryId +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", size=" + (pageable == null ? "null" : pageable.getPageSize()) +
                ", page=" + (pageable == null ? "null" : pageable.getPageNumber()) +
                ", sort=" + (pageable == null || pageable.getSort() == null ? "null" : pageable.getSort().toString());
    }
}

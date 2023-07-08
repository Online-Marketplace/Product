package com.onlinemarketplace.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onlinemarketplace.common.object.PagingInfo;
import com.onlinemarketplace.common.object.SortInfo;
import com.onlinemarketplace.product.dto.ProductDetail;
import com.onlinemarketplace.product.dto.ProductRequest;
import com.onlinemarketplace.product.dto.ProductResponse;
import com.onlinemarketplace.product.model.Product;
import com.onlinemarketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ProductResponse> getAllProducts(ProductRequest productRequest,Pageable pageable) throws JsonProcessingException {
        productRequest.setPageable(pageable);
        var productPage = productService.getAllProducts(productRequest);

        var productResponse = new ProductResponse();
        productResponse.setProductDetailList(productPage.getContent());
        productResponse.setPaging(createPagingInfo(productPage));

        return ResponseEntity.ok(productResponse);
    }

    private PagingInfo createPagingInfo(Page<?> page) {
        return new PagingInfo(
                page.getPageable(),
                page.isLast(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                createSortInfo(page.getSort()),
                page.isFirst(),
                page.getNumberOfElements(),
                page.isEmpty()
        );
    }
    private SortInfo createSortInfo(Sort sort) {
        if (sort == null) {
            return null;
        }

        return new SortInfo(sort.isSorted(), sort.isUnsorted(), sort.isEmpty());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") String productId, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        try {
            ProductDetail product = productService.getProductByIdAndUserId(productId, userId);
            var productResponse = new ProductResponse();
            productResponse.setProductDetail(product);
            return ResponseEntity.ok(productResponse);
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest, @RequestHeader("id") String userId) {
        var productDetail = productRequest.getProductDetail();
        productDetail.setUserId(userId);
        var productResponse = new ProductResponse();
        productResponse.setProductDetail(productService.createProduct(productDetail));
        return ResponseEntity.ok(productResponse);
    }
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("productId") String productId, @RequestBody ProductRequest productRequest, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        try {
            var productDetail = productRequest.getProductDetail();
            var product = productService.updateProduct(productId, productDetail, userId);
            var productResponse = new ProductResponse();
            productResponse.setProductDetail(product);
            return ResponseEntity.ok(productResponse);
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductResponse> deleteProduct(@PathVariable("productId") String productId, @RequestHeader("id") String userId) throws ChangeSetPersister.NotFoundException {
        try {
            productService.deleteProduct(productId, userId);
            var productResponse = new ProductResponse();
            productResponse.setMessage("Product deleted successfully");
            return ResponseEntity.ok(productResponse);
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.notFound().build();
        }

    }
}

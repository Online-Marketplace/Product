package com.onlinemarketplace.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onlinemarketplace.common.object.BaseResponse;
import com.onlinemarketplace.common.object.PagingInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse extends BaseResponse {
    private ProductDetail productDetail;
    private List<ProductDetail> productDetailList;
    private PagingInfo paging;
}

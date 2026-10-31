package com.premiernuts.identity.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta {

    private String code;
    private String message;
    private List<String> errors;
    private Integer pageIndex;
    private Integer pageSize;
    private Integer totalPages;
    private Long totalElements;

    public Meta() {
    }

    public Meta(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Meta(String code, String message, List<String> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public static Meta success() {
        return new Meta("200", "success");
    }

    public static Meta fail(String errorCode, String errorMessage) {
        return new Meta(errorCode, errorMessage);
    }

    public static Meta fail(String errorCode, String errorMessage, List<String> errors) {
        return new Meta(errorCode, errorMessage, errors);
    }

    public static Meta pageSuccess(PagingDTO pagingDTO) {
        Meta meta = new Meta("200", "success");
        if (pagingDTO != null) {
            meta.setPageIndex(pagingDTO.getPageIndex());
            meta.setPageSize(pagingDTO.getPageSize());
            meta.setTotalPages(pagingDTO.getTotalPages());
            meta.setTotalElements(pagingDTO.getTotalElements());
        }
        return meta;
    }
}

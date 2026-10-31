package com.premiernuts.identity.core.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class PagingDTO {

    private Integer pageIndex;
    private Integer pageSize;
    private Integer totalPages;
    private Long totalElements;

    public PagingDTO() {
    }

    public PagingDTO(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public PagingDTO(Integer pageIndex, Integer pageSize, Integer totalPages, Long totalElements) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public void update(Page<?> page) {
        if (page != null) {
            this.pageIndex = page.getNumber();
            this.pageSize = page.getSize();
            this.totalPages = page.getTotalPages();
            this.totalElements = page.getTotalElements();
        }
    }
}

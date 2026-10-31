package com.premiernuts.identity.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> {

    private Meta meta;
    private T data;

    public static <T> ResponseData<T> of(Meta meta, T data) {
        return new ResponseData<>(meta, data);
    }
}

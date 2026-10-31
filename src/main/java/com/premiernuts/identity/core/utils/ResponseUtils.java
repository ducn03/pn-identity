package com.premiernuts.identity.core.utils;

import com.premiernuts.identity.core.dto.Meta;
import com.premiernuts.identity.core.dto.PagingDTO;
import com.premiernuts.identity.core.dto.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseUtils {

    public static <T> ResponseEntity<ResponseData<T>> toSuccess(T data) {
        ResponseData<T> responseData = new ResponseData<>();
        responseData.setMeta(Meta.success());
        responseData.setData(data);
        return ResponseEntity.ok(responseData);
    }

    public static <T> ResponseEntity<ResponseData<T>> toSuccessArray(T data, PagingDTO pagingDTO) {
        ResponseData<T> responseData = new ResponseData<>();
        responseData.setMeta(Meta.pageSuccess(pagingDTO));
        responseData.setData(data);
        return ResponseEntity.ok(responseData);
    }

    public static ResponseEntity<ResponseData<Object>> fail(String errorCode, String errorMessage) {
        return fail(errorCode, errorMessage, null);
    }

    public static ResponseEntity<ResponseData<Object>> fail(String errorCode, String errorMessage, List<String> errors) {
        ResponseData<Object> responseData = new ResponseData<>();
        responseData.setMeta(Meta.fail(errorCode, errorMessage, errors));
        responseData.setData(null);
        return ResponseEntity.ok(responseData);
    }

    public static ResponseEntity<ResponseData<Object>> unauthorized(String errorCode, String errorMessage) {
        return unauthorized(errorCode, errorMessage, null);
    }

    public static ResponseEntity<ResponseData<Object>> unauthorized(String errorCode, String errorMessage, List<String> errors) {
        ResponseData<Object> responseData = new ResponseData<>();
        responseData.setMeta(Meta.fail(errorCode, errorMessage, errors));
        responseData.setData(null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseData);
    }

    public static ResponseEntity<ResponseData<Object>> forbidden(String errorCode, String errorMessage) {
        ResponseData<Object> responseData = new ResponseData<>();
        responseData.setMeta(Meta.fail(errorCode, errorMessage, null));
        responseData.setData(null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseData);
    }

    public static ResponseEntity<ResponseData<Object>> notFound(String errorCode, String errorMessage) {
        ResponseData<Object> responseData = new ResponseData<>();
        responseData.setMeta(Meta.fail(errorCode, errorMessage, null));
        responseData.setData(null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
    }
}

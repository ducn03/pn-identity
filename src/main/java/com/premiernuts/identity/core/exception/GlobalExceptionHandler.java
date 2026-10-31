package com.premiernuts.identity.core.exception;

import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.utils.MessageHelper;
import com.premiernuts.identity.core.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PnException.class)
    public ResponseEntity<ResponseData<Object>> handlePnException(PnException ex) {
        log.error("PnException: [code={}] {}", ex.getErrorCode(), ex.getErrorMessage());

        String message = ex.getErrorMessage();
        if (message == null || message.isBlank()) {
            message = MessageHelper.getMessage(ex.getErrorCode());
        }

        if (ErrorCodes.UNAUTHORIZED.TOKEN_EXPIRED.equals(ex.getErrorCode()) ||
            ErrorCodes.UNAUTHORIZED.UNAUTHORIZED.equals(ex.getErrorCode()) ||
            ErrorCodes.UNAUTHORIZED.TOKEN_INVALID.equals(ex.getErrorCode())) {
            return ResponseUtils.unauthorized(ex.getErrorCode(), message, ex.getErrors());
        }

        if (ErrorCodes.FORBIDDEN.ACCESS_DENIED.equals(ex.getErrorCode()) ||
            ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED.equals(ex.getErrorCode())) {
            return ResponseUtils.forbidden(ex.getErrorCode(), message);
        }

        if (ErrorCodes.COMMON.NOT_FOUND.equals(ex.getErrorCode()) ||
            ErrorCodes.USER.USER_NOT_FOUND.equals(ex.getErrorCode())) {
            return ResponseUtils.notFound(ex.getErrorCode(), message);
        }

        return ResponseUtils.fail(ex.getErrorCode(), message, ex.getErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        log.warn("Validation failed: {}", errors);
        return ResponseUtils.fail(
                ErrorCodes.VALIDATION.INVALID_INPUT,
                MessageHelper.getMessage(ErrorCodes.VALIDATION.INVALID_INPUT),
                errors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Object>> handleGeneralException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        String msg = MessageHelper.getMessage(ErrorCodes.COMMON.INTERNAL_SERVER_ERROR);
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            msg = ex.getMessage();
        }
        return ResponseUtils.fail(
                ErrorCodes.COMMON.INTERNAL_SERVER_ERROR,
                msg
        );
    }
}

package com.premiernuts.identity.core.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {

    /**
     * Bắt buộc phải có Authorization: Bearer <token>
     */
    boolean requireAuth() default true;

    /**
     * Danh sách quyền hạn được phép truy cập (nếu để trống = không yêu cầu quyền cụ thể)
     */
    String[] permissions() default {};
}

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
     * Danh sách vai trò được phép truy cập (nếu để trống = bất kỳ role nào đã đăng nhập)
     */
    String[] roles() default {};
}

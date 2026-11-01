package com.premiernuts.identity.core.enums;

public enum ERole {
    SUPER_ADMIN,
    ROLE_ADMIN,
    ROLE_DIRECTOR,
    ROLE_BRANCH_MANAGER,
    ROLE_KHO,
    ROLE_KE_TOAN,
    ROLE_SAN_XUAT,
    ROLE_QC;

    public static final class Names {

        private Names() {
        }

        public static final String SUPER_ADMIN = "SUPER_ADMIN";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_DIRECTOR = "ROLE_DIRECTOR";
        public static final String ROLE_BRANCH_MANAGER = "ROLE_BRANCH_MANAGER";
        public static final String ROLE_KHO = "ROLE_KHO";
        public static final String ROLE_KE_TOAN = "ROLE_KE_TOAN";
        public static final String ROLE_SAN_XUAT = "ROLE_SAN_XUAT";
        public static final String ROLE_QC = "ROLE_QC";
    }
}

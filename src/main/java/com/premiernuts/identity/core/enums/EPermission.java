package com.premiernuts.identity.core.enums;

import java.util.Set;

public enum EPermission {
    // Phân hệ Quản Trị Nhân Sự (IDENTITY)
    USER_VIEW,
    USER_MANAGE,
    ROLE_ASSIGN,

    // Phân hệ Kho Vận & Vật Tư (INVENTORY)
    INVENTORY_VIEW,
    INVENTORY_IMPORT,
    INVENTORY_EXPORT,
    INVENTORY_AUDIT,

    // Phân hệ Quản Lý Chi Nhánh (BRANCH)
    BRANCH_MANAGE,
    BRANCH_INVENTORY_VIEW,

    // Phân hệ Tài Chính Kế Toán (FINANCE)
    FINANCE_VIEW,
    FINANCE_MANAGE,

    // Phân hệ Sản Xuất (MANUFACTURING)
    MANUFACTURING_VIEW,
    MANUFACTURING_EXECUTE,

    // Phân hệ Kiểm Định Chất Lượng (QC)
    QC_VIEW,
    QC_INSPECT,

    // Phân hệ Ban Giám Đốc (MANAGEMENT)
    DIRECTOR_APPROVE,
    REPORT_VIEW,

    // Phân hệ Hệ Thống Đặc Quyền (SYSTEM)
    DASHBOARD_VIEW,
    LOG_VIEW,
    HEALTH_VIEW,

    // Phân hệ Hồ Sơ Pháp Nhân & Doanh Nghiệp (COMPANY)
    COMPANY_VIEW,
    COMPANY_MANAGE;

    private static final Set<String> SYSTEM_PERMISSIONS = Set.of(
            Names.DASHBOARD_VIEW,
            Names.LOG_VIEW,
            Names.HEALTH_VIEW
    );

    public static boolean isSystemPermission(String code) {
        if (code == null) {
            return false;
        }
        return SYSTEM_PERMISSIONS.contains(code.toUpperCase());
    }

    public static final class Names {

        private Names() {
        }

        public static final String USER_VIEW = "USER_VIEW";
        public static final String USER_MANAGE = "USER_MANAGE";
        public static final String ROLE_ASSIGN = "ROLE_ASSIGN";

        public static final String INVENTORY_VIEW = "INVENTORY_VIEW";
        public static final String INVENTORY_IMPORT = "INVENTORY_IMPORT";
        public static final String INVENTORY_EXPORT = "INVENTORY_EXPORT";
        public static final String INVENTORY_AUDIT = "INVENTORY_AUDIT";

        public static final String BRANCH_MANAGE = "BRANCH_MANAGE";
        public static final String BRANCH_INVENTORY_VIEW = "BRANCH_INVENTORY_VIEW";

        public static final String FINANCE_VIEW = "FINANCE_VIEW";
        public static final String FINANCE_MANAGE = "FINANCE_MANAGE";

        public static final String MANUFACTURING_VIEW = "MANUFACTURING_VIEW";
        public static final String MANUFACTURING_EXECUTE = "MANUFACTURING_EXECUTE";

        public static final String QC_VIEW = "QC_VIEW";
        public static final String QC_INSPECT = "QC_INSPECT";

        public static final String DIRECTOR_APPROVE = "DIRECTOR_APPROVE";
        public static final String REPORT_VIEW = "REPORT_VIEW";

        public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
        public static final String LOG_VIEW = "LOG_VIEW";
        public static final String HEALTH_VIEW = "HEALTH_VIEW";

        public static final String COMPANY_VIEW = "COMPANY_VIEW";
        public static final String COMPANY_MANAGE = "COMPANY_MANAGE";
    }
}

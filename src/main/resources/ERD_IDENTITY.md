# BẢN VẼ THIẾT KẾ ERD - PHÂN HỆ IDENTITY & AUTHENTICATION (PREMIER NUTS ERP)

## 1. Cấu trúc phân cấp Doanh nghiệp (Enterprise Hierarchy)
Mô hình tổ chức xuất phát từ cấp cao nhất là Doanh nghiệp/Công ty đi xuống:
```
M_COMPANY (Công ty: Premier Nuts)
   ├── M_DEPARTMENT (Phòng ban: BGD, Kho Vận, Sản Xuất, Kế Toán, QC)
   │      └── T_USER (Nhân viên / Người dùng)
   └── M_ROLE (Vai trò / Quyền hạn trong doanh nghiệp)
```

## 2. Quy ước chuẩn hóa Database
* **Tên bảng VIẾT HOA TOÀN BỘ**:
  * **`M_` (Master Tables)**: Bảng danh mục nền tảng (`M_COMPANY`, `M_DEPARTMENT`, `M_ROLE`, `M_PERMISSION`).
  * **`T_` (Transaction Tables)**: Bảng nghiệp vụ biến động (`T_USER`, `T_TOKEN`, `T_LOGIN_LOG`).
* **Bảng trung gian (Junction Tables)**: `T_USER_ROLE`, `T_ROLE_PERMISSION` (chỉ lưu các ID logic và `created_at`).
* **Trường chuẩn hóa Audit**: Mọi bảng (trừ bảng trung gian) đều có bộ 3 trường: `status`, `created_at`, `updated_at`.
* **ID tự sinh 100% (Auto-generated ID)**: Sử dụng kiểu `BIGSERIAL PRIMARY KEY` trong PostgreSQL. Dữ liệu seed hoàn toàn để database tự sinh sequence ID, không chèn cứng ID thủ công.
* **Khóa ngoại Logic**: Có liên kết logic giữa các bảng nhưng **không tạo ràng buộc FOREIGN KEY ở mức DB** để tối ưu hiệu năng. Chưa tạo index theo yêu cầu.

---

## 3. Sơ đồ Thực thể Liên kết (Mermaid ERD)

```mermaid
erDiagram
    M_COMPANY {
        bigint id PK "Tự sinh (BIGSERIAL)"
        varchar code UK "PREMIER_NUTS"
        varchar name "Tên công ty"
        varchar tax_code "Mã số thuế"
        varchar email
        varchar phone
        varchar address
        varchar website
        varchar status "ACTIVE, INACTIVE"
        timestamptz created_at
        timestamptz updated_at
    }

    M_DEPARTMENT {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint company_id "FK logic M_COMPANY.id"
        varchar code "BGD, KHO, SANXUAT, KETOAN, QC"
        varchar name "Tên phòng ban"
        varchar description
        varchar status "ACTIVE, INACTIVE"
        timestamptz created_at
        timestamptz updated_at
    }

    M_ROLE {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint company_id "FK logic M_COMPANY.id (NULL = Toàn cục)"
        varchar code UK "ROLE_ADMIN, ROLE_KHO..."
        varchar name
        varchar description
        varchar status "ACTIVE, INACTIVE"
        timestamptz created_at
        timestamptz updated_at
    }

    M_PERMISSION {
        bigint id PK "Tự sinh (BIGSERIAL)"
        varchar code UK "USER_MANAGE, INVENTORY_IMPORT..."
        varchar name
        varchar module "IDENTITY, INVENTORY..."
        varchar description
        varchar status "ACTIVE, INACTIVE"
        timestamptz created_at
        timestamptz updated_at
    }

    T_USER {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint company_id "FK logic M_COMPANY.id"
        bigint department_id "FK logic M_DEPARTMENT.id"
        varchar username UK
        varchar password_hash
        varchar full_name
        varchar email UK
        varchar phone
        varchar avatar_url
        timestamptz last_login_at
        varchar status "ACTIVE, INACTIVE, LOCKED"
        timestamptz created_at
        timestamptz updated_at
    }

    T_USER_ROLE {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint user_id "FK logic T_USER.id"
        bigint role_id "FK logic M_ROLE.id"
        bigint assigned_by "FK logic T_USER.id"
        timestamptz created_at
    }

    T_ROLE_PERMISSION {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint role_id "FK logic M_ROLE.id"
        bigint permission_id "FK logic M_PERMISSION.id"
        timestamptz created_at
    }

    T_TOKEN {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint user_id "FK logic T_USER.id"
        varchar token
        varchar token_type "ACCESS, REFRESH"
        timestamptz expired_at
        varchar status "ACTIVE, REVOKED, EXPIRED"
        timestamptz created_at
        timestamptz updated_at
    }

    T_LOGIN_LOG {
        bigint id PK "Tự sinh (BIGSERIAL)"
        bigint user_id "FK logic T_USER.id"
        varchar username
        varchar ip_address
        varchar user_agent
        varchar failure_reason
        varchar status "SUCCESS, FAILED"
        timestamptz created_at
        timestamptz updated_at
    }

    M_COMPANY ||--o{ M_DEPARTMENT : "quản lý phòng ban"
    M_COMPANY ||--o{ T_USER : "quản lý nhân viên"
    M_DEPARTMENT ||--o{ T_USER : "chứa nhân viên"
    T_USER ||--o{ T_USER_ROLE : "được gán vai trò"
    M_ROLE ||--o{ T_USER_ROLE : "gán cho nhân viên"
    M_ROLE ||--o{ T_ROLE_PERMISSION : "chứa quyền hạn"
    M_PERMISSION ||--o{ T_ROLE_PERMISSION : "gán cho vai trò"
    T_USER ||--o{ T_TOKEN : "sở hữu phiên đăng nhập"
    T_USER ||--o{ T_LOGIN_LOG : "ghi nhật ký truy cập"
```

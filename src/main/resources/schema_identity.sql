-- ==============================================================================
-- PREMIER NUTS ERP - DATABASE IDENTITY & AUTHENTICATION SCHEMA
-- Database Engine : PostgreSQL
-- Database Name   : premiernutserp
-- Design Rules    :
--   1. Phân cấp doanh nghiệp: Đi từ Công ty (M_COMPANY) -> Phòng ban (M_DEPARTMENT) -> Nhân viên (T_USER)
--   2. Đặt tên VIẾT HOA:
--        M_ : Bảng Master (danh mục nền tảng: Company, Department, Role, Permission)
--        T_ : Bảng Transaction (nghiệp vụ: User, Token, LoginLog)
--   3. Chuẩn hóa audit: Mọi bảng (trừ bảng trung gian) đều có status, created_at, updated_at
--   4. ID tự sinh hoàn toàn (BIGSERIAL) - KHÔNG chèn cứng ID thủ công
--   5. Khóa ngoại logic ở mức ứng dụng (KHÔNG tạo ràng buộc FOREIGN KEY ở DB)
--   6. Khoan tạo INDEX theo yêu cầu
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. BẢNG MASTER: CÔNG TY / DOANH NGHIỆP (Đỉnh cấp cao nhất)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS M_COMPANY (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,            -- PREMIER_NUTS
    name VARCHAR(150) NOT NULL,
    tax_code VARCHAR(50),                        -- Mã số thuế
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    website VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 2. BẢNG MASTER: PHÒNG BAN / BỘ PHẬN (Trực thuộc M_COMPANY)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS M_DEPARTMENT (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,                  -- Khóa ngoại logic tới M_COMPANY.id (Không FK DB)
    code VARCHAR(50) NOT NULL,                   -- BGD, KHO, KETOAN, SANXUAT, QC
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 3. BẢNG MASTER: VAI TRÒ HỆ THỐNG
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS M_ROLE (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT,                           -- Khóa ngoại logic tới M_COMPANY.id (NULL = Toàn hệ thống)
    code VARCHAR(50) NOT NULL UNIQUE,            -- ROLE_ADMIN, ROLE_KHO, ROLE_KE_TOAN, ROLE_SAN_XUAT, ROLE_QC
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 4. BẢNG MASTER: QUYỀN HẠN CHI TIẾT
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS M_PERMISSION (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,           -- INVENTORY_VIEW, INVENTORY_IMPORT, USER_MANAGE...
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,                 -- INVENTORY, IDENTITY, MANUFACTURING, ORDERS
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 5. BẢNG TRANSACTION: TÀI KHOẢN NHÂN VIÊN / NGƯỜI DÙNG
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_USER (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,                  -- Khóa ngoại logic tới M_COMPANY.id
    department_id BIGINT,                        -- Khóa ngoại logic tới M_DEPARTMENT.id
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    last_login_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, LOCKED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 6. BẢNG TRUNG GIAN: GÁN VAI TRÒ CHO NHÂN VIÊN (Chỉ cần created_at)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_USER_ROLE (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới T_USER.id
    role_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới M_ROLE.id
    assigned_by BIGINT,                          -- Khóa ngoại logic tới T_USER.id người thực hiện gán
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 7. BẢNG TRUNG GIAN: GÁN QUYỀN CHO VAI TRÒ (Chỉ cần created_at)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_ROLE_PERMISSION (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới M_ROLE.id
    permission_id BIGINT NOT NULL,               -- Khóa ngoại logic tới M_PERMISSION.id
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 8. BẢNG TRANSACTION: QUẢN LÝ TOKEN & PHIÊN ĐĂNG NHẬP (Thay thế Redis)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_TOKEN (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới T_USER.id
    token VARCHAR(512) NOT NULL,
    token_type VARCHAR(20) NOT NULL DEFAULT 'REFRESH', -- ACCESS, REFRESH
    expired_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, REVOKED, EXPIRED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 9. BẢNG TRANSACTION: NHẬT KÝ ĐĂNG NHẬP HỆ THỐNG
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_LOGIN_LOG (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,                              -- Khóa ngoại logic tới T_USER.id
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    failure_reason VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, FAILED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ==============================================================================
-- DỮ LIỆU KHỞI TẠO BAN ĐẦU (SEED DATA TỰ SINH ID TỰ ĐỘNG BỞI DATABASE)
-- ==============================================================================

-- 1. Seed M_COMPANY (ID tự sinh)
INSERT INTO M_COMPANY (code, name, tax_code, email, phone, address, website, status)
VALUES (
    'PREMIER_NUTS',
    'Công Ty Cổ Phần Nông Sản Premier Nuts',
    '0318999888',
    'contact@premiernuts.vn',
    '028.3822.9999',
    'Khu Công Nghiệp Đồng Xoài, Tỉnh Bình Phước, Việt Nam',
    'https://premiernuts.vn',
    'ACTIVE'
) ON CONFLICT (code) DO NOTHING;

-- 2. Seed M_DEPARTMENT (ID tự sinh, liên kết logic qua company_id lấy theo code)
INSERT INTO M_DEPARTMENT (company_id, code, name, description, status)
SELECT c.id, dept.code, dept.name, dept.description, 'ACTIVE'
FROM M_COMPANY c
CROSS JOIN (
    VALUES 
        ('BGD', 'Ban Giám Đốc', 'Điều hành chung toàn bộ hoạt động Premier Nuts'),
        ('KHO', 'Kho Vận & Logistics', 'Quản lý lưu kho hạt điều, macca, hạnh nhân và bao bì'),
        ('KETOAN', 'Tài Chính Kế Toán', 'Quản lý thu chi, hóa đơn, công nợ và báo cáo tài chính'),
        ('SANXUAT', 'Nhà Máy Chế Biến', 'Phân xưởng rang sấy, đóng gói thành phẩm hạt dinh dưỡng'),
        ('QC', 'Kiểm Định Chất Lượng', 'Kiểm soát tiêu chuẩn an toàn thực phẩm & phân hạng hạt')
) AS dept(code, name, description)
WHERE c.code = 'PREMIER_NUTS'
AND NOT EXISTS (
    SELECT 1 FROM M_DEPARTMENT d WHERE d.company_id = c.id AND d.code = dept.code
);

-- 3. Seed M_ROLE (ID tự sinh)
INSERT INTO M_ROLE (code, name, description, status)
VALUES 
    ('ROLE_ADMIN', 'Quản Trị Viên Hệ Thống', 'Toàn quyền cấu hình và phân quyền Premier Nuts ERP', 'ACTIVE'),
    ('ROLE_KHO', 'Thủ Kho', 'Quản lý xuất, nhập, tồn kho hạt và vật tư', 'ACTIVE'),
    ('ROLE_KE_TOAN', 'Kế Toán Doanh Nghiệp', 'Xem sổ sách, duyệt thanh toán và đối soát đơn hàng', 'ACTIVE'),
    ('ROLE_SAN_XUAT', 'Tổ Trưởng Sản Xuất', 'Tạo lệnh chế biến, rang sấy, đóng gói', 'ACTIVE'),
    ('ROLE_QC', 'Kỹ Thuật Viên Kiểm Định', 'Kiểm định độ ẩm, phân hạng A/B/C các lô hạt', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- 4. Seed M_PERMISSION (ID tự sinh)
INSERT INTO M_PERMISSION (code, name, module, description, status)
VALUES
    ('USER_VIEW', 'Xem danh sách nhân sự', 'IDENTITY', 'Quyền xem danh sách user', 'ACTIVE'),
    ('USER_MANAGE', 'Thêm/sửa/khóa nhân sự', 'IDENTITY', 'Quyền quản trị tài khoản', 'ACTIVE'),
    ('ROLE_ASSIGN', 'Phân quyền người dùng', 'IDENTITY', 'Gán role cho tài khoản', 'ACTIVE'),
    ('INVENTORY_VIEW', 'Xem tồn kho hạt', 'INVENTORY', 'Tra cứu số lượng tồn kho', 'ACTIVE'),
    ('INVENTORY_IMPORT', 'Nhập kho hạt mới', 'INVENTORY', 'Tạo phiếu nhập lô hạt', 'ACTIVE'),
    ('INVENTORY_EXPORT', 'Xuất kho chế biến', 'INVENTORY', 'Tạo phiếu xuất kho', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- 5. Seed T_USER (ID tự sinh, password mặc định: admin123 - Mã hóa BCrypt)
-- Hash: $2a$10$/sIGVKgW6Yjt4Wvz66LoxuRnaqv36NqCO1YQhmIXrJWJz1n72x.Cm
INSERT INTO T_USER (company_id, department_id, username, password_hash, full_name, email, phone, status)
SELECT 
    c.id,
    d.id,
    u.username,
    '$2a$10$/sIGVKgW6Yjt4Wvz66LoxuRnaqv36NqCO1YQhmIXrJWJz1n72x.Cm',
    u.full_name,
    u.email,
    u.phone,
    'ACTIVE'
FROM M_COMPANY c
JOIN (
    VALUES 
        ('admin', 'BGD', 'Nguyễn Văn Quản Trị', 'admin@premiernuts.vn', '0901234567'),
        ('kho_truong', 'KHO', 'Trần Văn Kho', 'kho@premiernuts.vn', '0912345678'),
        ('ketoan_truong', 'KETOAN', 'Lê Thị Thu', 'ketoan@premiernuts.vn', '0923456789'),
        ('sanxuat_lead', 'SANXUAT', 'Phạm Minh Sản Xuất', 'sanxuat@premiernuts.vn', '0934567890')
) AS u(username, dept_code, full_name, email, phone) ON true
JOIN M_DEPARTMENT d ON d.company_id = c.id AND d.code = u.dept_code
WHERE c.code = 'PREMIER_NUTS'
ON CONFLICT (username) DO NOTHING;

-- 6. Seed T_USER_ROLE (ID tự sinh, ghép logic giữa T_USER và M_ROLE theo code)
INSERT INTO T_USER_ROLE (user_id, role_id)
SELECT u.id, r.id
FROM T_USER u
JOIN (
    VALUES 
        ('admin', 'ROLE_ADMIN'),
        ('kho_truong', 'ROLE_KHO'),
        ('ketoan_truong', 'ROLE_KE_TOAN'),
        ('sanxuat_lead', 'ROLE_SAN_XUAT')
) AS mapping(username, role_code) ON u.username = mapping.username
JOIN M_ROLE r ON r.code = mapping.role_code
WHERE NOT EXISTS (
    SELECT 1 FROM T_USER_ROLE ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

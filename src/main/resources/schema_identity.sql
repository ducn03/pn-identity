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
-- 3. BẢNG MASTER: VAI TRÒ HỆ THỐNG (ROLE TEMPLATES)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS M_ROLE (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT,                           -- Khóa ngoại logic tới M_COMPANY.id (NULL = Toàn hệ thống)
    code VARCHAR(50) NOT NULL UNIQUE,            -- ROLE_ADMIN, ROLE_DIRECTOR, ROLE_BRANCH_MANAGER, ROLE_KHO...
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    level INT NOT NULL DEFAULT 99,               -- Cấp bậc phân quyền (1: SuperAdmin, 2: Admin, 3: Director, 4: BranchManager, 5: Operational)
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 4. BẢNG MASTER: QUYỀN HẠN CHI TIẾT (ATOMIC PERMISSIONS)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS M_PERMISSION (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,           -- INVENTORY_VIEW, USER_MANAGE, DIRECTOR_APPROVE...
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,                 -- IDENTITY, INVENTORY, BRANCH, FINANCE, MANUFACTURING, QC, MANAGEMENT
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
    role_id BIGINT,                              -- Khóa ngoại logic tới M_ROLE.id (Template vai trò chính / Chức danh)
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
-- 6. BẢNG TRUNG GIAN: GÁN VAI TRÒ CHO NHÂN VIÊN (Tương thích ngược)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_USER_ROLE (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới T_USER.id
    role_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới M_ROLE.id
    assigned_by BIGINT,                          -- Khóa ngoại logic tới T_USER.id người thực hiện gán
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 7. BẢNG KHUÔN MẪU: QUYỀN MẶC ĐỊNH THEO VAI TRÒ (ROLE PERMISSION TEMPLATE)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_ROLE_PERMISSION (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới M_ROLE.id (Role template)
    permission_id BIGINT NOT NULL,               -- Khóa ngoại logic tới M_PERMISSION.id
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
);

-- ------------------------------------------------------------------------------
-- 8. BẢNG TRANSACTION: QUYỀN HẠN TRỰC TIẾP CỦA NGƯỜI DÙNG (USER DIRECT PERMISSIONS)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS T_USER_PERMISSION (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                     -- Khóa ngoại logic tới T_USER.id
    permission_id BIGINT NOT NULL,               -- Khóa ngoại logic tới M_PERMISSION.id
    assigned_by BIGINT,                          -- Khóa ngoại logic tới T_USER.id người gán
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_permission UNIQUE (user_id, permission_id)
);

-- ------------------------------------------------------------------------------
-- 9. BẢNG TRANSACTION: QUẢN LÝ TOKEN & PHIÊN ĐĂNG NHẬP (Thay thế Redis)
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
-- 10. BẢNG TRANSACTION: NHẬT KÝ ĐĂNG NHẬP HỆ THỐNG
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
    '3502320150',
    'premiernutshcm@gmail.com',
    '0949382374',
    '131/53/3 đường số 6, khu phố 1, p. Linh Xuân, Thủ Đức, TP HCM',
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

-- 3. Seed M_ROLE (Template vai trò)
INSERT INTO M_ROLE (code, name, description, level, status)
VALUES 
    ('ROLE_ADMIN', 'Quản Trị Viên Hệ Thống', 'Toàn quyền cấu hình, nhân sự và phân quyền Premier Nuts ERP', 2, 'ACTIVE'),
    ('ROLE_DIRECTOR', 'Ban Giám Đốc', 'Phê duyệt hạn mức, dự toán, xem toàn bộ báo cáo và điều hành', 3, 'ACTIVE'),
    ('ROLE_BRANCH_MANAGER', 'Quản Lý Chi Nhánh', 'Quản lý vận hành chi nhánh, kho chi nhánh và điều phối nhân sự', 4, 'ACTIVE'),
    ('ROLE_KHO', 'Thủ Kho', 'Quản lý xuất, nhập, tồn kho hạt và vật tư sản xuất', 5, 'ACTIVE'),
    ('ROLE_KE_TOAN', 'Kế Toán Doanh Nghiệp', 'Xem sổ sách, duyệt thanh toán, công nợ và đối soát hóa đơn', 5, 'ACTIVE'),
    ('ROLE_SAN_XUAT', 'Tổ Trưởng Sản Xuất', 'Tạo lệnh chế biến, quản lý máy rang sấy, đóng gói', 5, 'ACTIVE'),
    ('ROLE_QC', 'Kỹ Thuật Viên Kiểm Định', 'Kiểm định độ ẩm, tỷ lệ tấm, phân hạng A/B/C các lô hạt', 5, 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- 4. Seed M_PERMISSION (Quyền hạn chi tiết theo Module)
INSERT INTO M_PERMISSION (code, name, module, description, status)
VALUES
    -- Phân hệ Tài Khoản & Nhân Sự (IDENTITY)
    ('USER_VIEW', 'Xem danh sách nhân sự', 'IDENTITY', 'Quyền tra cứu danh sách nhân sự trong công ty', 'ACTIVE'),
    ('USER_MANAGE', 'Thêm/sửa/khóa nhân sự', 'IDENTITY', 'Quyền quản trị tài khoản nhân viên', 'ACTIVE'),
    ('ROLE_ASSIGN', 'Phân quyền người dùng', 'IDENTITY', 'Quyền chọn vai trò và tùy biến permission', 'ACTIVE'),

    -- Phân hệ Kho Vận & Vật Tư (INVENTORY)
    ('INVENTORY_VIEW', 'Xem tồn kho hạt & vật tư', 'INVENTORY', 'Tra cứu số lượng tồn kho hạt nông sản và bao bì', 'ACTIVE'),
    ('INVENTORY_IMPORT', 'Nhập kho hạt mới', 'INVENTORY', 'Tạo phiếu nhập lô hạt nguyên liệu', 'ACTIVE'),
    ('INVENTORY_EXPORT', 'Xuất kho chế biến', 'INVENTORY', 'Tạo phiếu xuất kho phục vụ sản xuất và giao hàng', 'ACTIVE'),
    ('INVENTORY_AUDIT', 'Kiểm kê kho', 'INVENTORY', 'Đối soát và kiểm kê số lượng thực tế trong kho', 'ACTIVE'),

    -- Phân hệ Quản Lý Chi Nhánh (BRANCH)
    ('BRANCH_MANAGE', 'Quản lý chi nhánh', 'BRANCH', 'Điều hành hoạt động và phân bổ nguồn lực chi nhánh', 'ACTIVE'),
    ('BRANCH_INVENTORY_VIEW', 'Xem kho chi nhánh', 'BRANCH', 'Tra cứu số lượng tồn kho tại các điểm kho chi nhánh', 'ACTIVE'),

    -- Phân hệ Tài Chính Kế Toán (FINANCE)
    ('FINANCE_VIEW', 'Xem báo cáo tài chính', 'FINANCE', 'Tra cứu dòng tiền, công nợ và sổ quỹ doanh nghiệp', 'ACTIVE'),
    ('FINANCE_MANAGE', 'Quản lý thu chi & công nợ', 'FINANCE', 'Tạo phiếu thu chi, duyệt đối soát hóa đơn', 'ACTIVE'),

    -- Phân hệ Phân Xưởng Sản Xuất (MANUFACTURING)
    ('MANUFACTURING_VIEW', 'Xem kế hoạch sản xuất', 'MANUFACTURING', 'Tra cứu ca sản xuất, định mức rang sấy hạt', 'ACTIVE'),
    ('MANUFACTURING_EXECUTE', 'Thực thi lệnh chế biến', 'MANUFACTURING', 'Vận hành chuyền chế biến, rang sấy và đóng gói', 'ACTIVE'),

    -- Phân hệ Kiểm Định Chất Lượng (QC)
    ('QC_VIEW', 'Xem hồ sơ kiểm định', 'QC', 'Tra cứu kết quả kiểm định chất lượng lô hạt', 'ACTIVE'),
    ('QC_INSPECT', 'Đánh giá & phân hạng hạt', 'QC', 'Thực hiện kiểm định độ ẩm, sâu mọt và cấp chứng chỉ', 'ACTIVE'),

    -- Phân hệ Ban Giám Đốc & Quản Trị (MANAGEMENT)
    ('DIRECTOR_APPROVE', 'Phê duyệt cấp Giám Đốc', 'MANAGEMENT', 'Phê duyệt các hợp đồng, hạn mức và đơn hàng trọng điểm', 'ACTIVE'),
    ('REPORT_VIEW', 'Xem báo cáo thống kê toàn diện', 'MANAGEMENT', 'Xem dashboard số liệu tổng hợp doanh thu, tồn kho toàn công ty', 'ACTIVE'),

    -- Phân hệ Quản Trị Hệ Thống (SYSTEM)
    ('DASHBOARD_VIEW', 'Bảng điều khiển & Thống kê', 'SYSTEM', 'Xem các chỉ số thống kê và bàn làm việc tổng thể', 'ACTIVE'),
    ('LOG_VIEW', 'Nhật ký truy cập hệ thống', 'SYSTEM', 'Tra cứu lịch sử đăng nhập và kiểm toán an toàn', 'ACTIVE'),
    ('HEALTH_VIEW', 'Giám sát hệ thống', 'SYSTEM', 'Kiểm tra tình trạng sức khỏe máy chủ và cơ sở dữ liệu', 'ACTIVE'),

    -- Phân hệ Hồ Sơ Doanh Nghiệp (COMPANY)
    ('COMPANY_VIEW', 'Xem thông tin pháp nhân công ty', 'COMPANY', 'Tra cứu hồ sơ pháp lý và thông tin doanh nghiệp', 'ACTIVE'),
    ('COMPANY_MANAGE', 'Quản lý thông tin doanh nghiệp', 'COMPANY', 'Cập nhật mã số thuế, địa chỉ, hotline doanh nghiệp', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- 5. Seed T_ROLE_PERMISSION (Role Template: Bản đồ quyền mẫu cho từng vai trò)
INSERT INTO T_ROLE_PERMISSION (role_id, permission_id)
SELECT r.id, p.id
FROM (
    VALUES
        -- ROLE_ADMIN: Sở hữu toàn bộ các quyền trong hệ thống (20 quyền)
        ('ROLE_ADMIN', 'USER_VIEW'),
        ('ROLE_ADMIN', 'USER_MANAGE'),
        ('ROLE_ADMIN', 'ROLE_ASSIGN'),
        ('ROLE_ADMIN', 'INVENTORY_VIEW'),
        ('ROLE_ADMIN', 'INVENTORY_IMPORT'),
        ('ROLE_ADMIN', 'INVENTORY_EXPORT'),
        ('ROLE_ADMIN', 'INVENTORY_AUDIT'),
        ('ROLE_ADMIN', 'BRANCH_MANAGE'),
        ('ROLE_ADMIN', 'BRANCH_INVENTORY_VIEW'),
        ('ROLE_ADMIN', 'FINANCE_VIEW'),
        ('ROLE_ADMIN', 'FINANCE_MANAGE'),
        ('ROLE_ADMIN', 'MANUFACTURING_VIEW'),
        ('ROLE_ADMIN', 'MANUFACTURING_EXECUTE'),
        ('ROLE_ADMIN', 'QC_VIEW'),
        ('ROLE_ADMIN', 'QC_INSPECT'),
        ('ROLE_ADMIN', 'DIRECTOR_APPROVE'),
        ('ROLE_ADMIN', 'REPORT_VIEW'),
        ('ROLE_ADMIN', 'DASHBOARD_VIEW'),
        ('ROLE_ADMIN', 'LOG_VIEW'),
        ('ROLE_ADMIN', 'HEALTH_VIEW'),

        -- ROLE_DIRECTOR: Ban Giám Đốc (17 quyền, Không có DASHBOARD_VIEW, LOG_VIEW, HEALTH_VIEW)
        ('ROLE_DIRECTOR', 'USER_VIEW'),
        ('ROLE_DIRECTOR', 'USER_MANAGE'),
        ('ROLE_DIRECTOR', 'ROLE_ASSIGN'),
        ('ROLE_DIRECTOR', 'INVENTORY_VIEW'),
        ('ROLE_DIRECTOR', 'INVENTORY_IMPORT'),
        ('ROLE_DIRECTOR', 'INVENTORY_EXPORT'),
        ('ROLE_DIRECTOR', 'INVENTORY_AUDIT'),
        ('ROLE_DIRECTOR', 'BRANCH_MANAGE'),
        ('ROLE_DIRECTOR', 'BRANCH_INVENTORY_VIEW'),
        ('ROLE_DIRECTOR', 'FINANCE_VIEW'),
        ('ROLE_DIRECTOR', 'FINANCE_MANAGE'),
        ('ROLE_DIRECTOR', 'MANUFACTURING_VIEW'),
        ('ROLE_DIRECTOR', 'MANUFACTURING_EXECUTE'),
        ('ROLE_DIRECTOR', 'QC_VIEW'),
        ('ROLE_DIRECTOR', 'QC_INSPECT'),
        ('ROLE_DIRECTOR', 'DIRECTOR_APPROVE'),
        ('ROLE_DIRECTOR', 'REPORT_VIEW'),

        -- ROLE_BRANCH_MANAGER: Quản Lý Chi Nhánh
        ('ROLE_BRANCH_MANAGER', 'BRANCH_MANAGE'),
        ('ROLE_BRANCH_MANAGER', 'BRANCH_INVENTORY_VIEW'),
        ('ROLE_BRANCH_MANAGER', 'INVENTORY_VIEW'),
        ('ROLE_BRANCH_MANAGER', 'INVENTORY_IMPORT'),
        ('ROLE_BRANCH_MANAGER', 'INVENTORY_EXPORT'),
        ('ROLE_BRANCH_MANAGER', 'REPORT_VIEW'),
        ('ROLE_BRANCH_MANAGER', 'USER_VIEW'),

        -- ROLE_KE_TOAN: Kế Toán Doanh Nghiệp
        ('ROLE_KE_TOAN', 'FINANCE_VIEW'),
        ('ROLE_KE_TOAN', 'FINANCE_MANAGE'),
        ('ROLE_KE_TOAN', 'INVENTORY_VIEW'),
        ('ROLE_KE_TOAN', 'REPORT_VIEW'),
        ('ROLE_KE_TOAN', 'USER_VIEW'),

        -- ROLE_KHO: Thủ Kho
        ('ROLE_KHO', 'INVENTORY_VIEW'),
        ('ROLE_KHO', 'INVENTORY_IMPORT'),
        ('ROLE_KHO', 'INVENTORY_EXPORT'),
        ('ROLE_KHO', 'INVENTORY_AUDIT'),

        -- ROLE_SAN_XUAT: Tổ Trưởng Sản Xuất
        ('ROLE_SAN_XUAT', 'MANUFACTURING_VIEW'),
        ('ROLE_SAN_XUAT', 'MANUFACTURING_EXECUTE'),
        ('ROLE_SAN_XUAT', 'INVENTORY_VIEW'),

        -- ROLE_QC: Kỹ Thuật Viên Kiểm Định
        ('ROLE_QC', 'QC_VIEW'),
        ('ROLE_QC', 'QC_INSPECT'),
        ('ROLE_QC', 'INVENTORY_VIEW')
) AS mapping(role_code, perm_code)
JOIN M_ROLE r ON r.code = mapping.role_code
JOIN M_PERMISSION p ON p.code = mapping.perm_code
WHERE NOT EXISTS (
    SELECT 1 FROM T_ROLE_PERMISSION rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 6. Seed T_USER (Tài khoản nhân sự với role_id gán trực tiếp làm template vai trò)
-- Password mặc định: admin123 (BCrypt hash)
INSERT INTO T_USER (company_id, department_id, role_id, username, password_hash, full_name, email, phone, status)
SELECT 
    c.id,
    d.id,
    r.id,
    u.username,
    '$2a$10$/sIGVKgW6Yjt4Wvz66LoxuRnaqv36NqCO1YQhmIXrJWJz1n72x.Cm',
    u.full_name,
    u.email,
    u.phone,
    'ACTIVE'
FROM M_COMPANY c
JOIN (
    VALUES 
        ('admin', 'BGD', 'ROLE_ADMIN', 'Nguyễn Văn Quản Trị', 'admin@premiernuts.vn', '0901234567'),
        ('director', 'BGD', 'ROLE_DIRECTOR', 'Đặng Thái Giám Đốc', 'director@premiernuts.vn', '0909999888'),
        ('branch_lead', 'BGD', 'ROLE_BRANCH_MANAGER', 'Hoàng Văn Chi Nhánh', 'chinhanh@premiernuts.vn', '0908888777'),
        ('kho_truong', 'KHO', 'ROLE_KHO', 'Trần Văn Kho', 'kho@premiernuts.vn', '0912345678'),
        ('ketoan_truong', 'KETOAN', 'ROLE_KE_TOAN', 'Lê Thị Thu', 'ketoan@premiernuts.vn', '0923456789'),
        ('sanxuat_lead', 'SANXUAT', 'ROLE_SAN_XUAT', 'Phạm Minh Sản Xuất', 'sanxuat@premiernuts.vn', '0934567890')
) AS u(username, dept_code, role_code, full_name, email, phone) ON true
JOIN M_DEPARTMENT d ON d.company_id = c.id AND d.code = u.dept_code
JOIN M_ROLE r ON r.code = u.role_code
WHERE c.code = 'PREMIER_NUTS'
ON CONFLICT (username) DO NOTHING;

-- 7. Seed T_USER_PERMISSION (Kế thừa trực tiếp permissions từ template Role)
INSERT INTO T_USER_PERMISSION (user_id, permission_id)
SELECT u.id, rp.permission_id
FROM T_USER u
JOIN T_ROLE_PERMISSION rp ON rp.role_id = u.role_id
WHERE NOT EXISTS (
    SELECT 1 FROM T_USER_PERMISSION up WHERE up.user_id = u.id AND up.permission_id = rp.permission_id
);


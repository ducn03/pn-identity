# pn-identity (Premier Nuts ERP - Identity & Auth Service)

Dịch vụ định danh, xác thực và phân quyền tài khoản (Identity, Authentication & Authorization) cho cụm hệ thống **Premier Nuts ERP**.

---

## 🛠 Công nghệ sử dụng
* **Framework:** Spring Boot 4.1.2
* **Môi trường Java:** Java 27 (`jdk-27.0.1.0.3`)
* **Cơ sở dữ liệu:** PostgreSQL 18 (Driver: `org.postgresql:postgresql:42.7.14`)
* **Kiến trúc:** Modular Architecture (Vertical Slice) - Tách `core` độc lập và `features` riêng biệt
* **Bảo mật & Phiên:** JJWT `0.12.6` + AOP `@LogActivity` (Xác thực JWT & Audit log thời gian thực)
* **Active Session:** Lưu trữ trong bảng `T_TOKEN` (Không dùng Redis cho giai đoạn đầu)
* **Cơ chế ORM:** Spring Data JPA (Hibernate 7, PhysicalNamingStrategyStandardImpl)
* **Cổng dịch vụ (Port):** `8081`

---

## 📋 Yêu cầu môi trường (Prerequisites)
1. **Java Development Kit (JDK):** JDK 27 (đường dẫn: `C:\Program Files\Java\jdk-27.0.1.0.3`).
2. **PostgreSQL Server:** PostgreSQL 18 (cổng mặc định `5432`).

---

## 🗄️ Cấu hình Cơ sở dữ liệu (Database Setup)

### 1. Thông số kết nối (trong `application.yml`):
* **Host:** `localhost:5432`
* **Database Name:** `premiernutserp`
* **Username:** `root`
* **Password:** `root`

### 2. Khởi tạo Database & Dữ liệu ban đầu:
Nếu cài mới PostgreSQL, bạn tạo database và nạp schema theo 1 trong 2 cách:

* **Cách 1: Chạy bằng công cụ psql trong Terminal:**
  ```powershell
  # Tạo role root và database (nếu chưa có)
  $env:PGPASSWORD = "root"
  & "D:\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -p 5432 -c "CREATE ROLE root WITH LOGIN SUPERUSER CREATEDB PASSWORD 'root';"
  & "D:\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -p 5432 -c "CREATE DATABASE premiernutserp OWNER root;"

  # Nạp schema và dữ liệu mẫu
  & "D:\PostgreSQL\18\bin\psql.exe" -U root -d premiernutserp -h localhost -p 5432 -f "D:\tools\mProject\premier-nuts-erp\pn-identity\src\main\resources\schema-erp.sql"
  ```

* **Cách 2: Chạy trực tiếp trong IntelliJ IDEA / DataGrip:**
  1. Mở file [schema-erp.sql](file:///d:/tools/mProject/premier-nuts-erp/pn-identity/src/main/resources/schema-erp.sql).
  2. Bấm nút **Execute (Ctrl + Enter)** và chọn Data Source `postgres@premiernuts-erp`.

---

## 🚀 Hướng dẫn khởi chạy dự án (How to Run)

> ⚠️ **LƯU Ý QUAN TRỌNG:** Vì máy sử dụng JDK 27 nên cần chỉ định biến môi trường `JAVA_HOME` trước khi gọi Maven Wrapper để tránh lỗi `JAVA_HOME is not defined correctly`.

### Cách 1: Chạy bằng PowerShell (Khuyên dùng)
Mở cửa sổ PowerShell tại thư mục `pn-identity` và chạy:

```powershell
cd D:\tools\mProject\premier-nuts-erp\pn-identity

# 1. Đặt biến JAVA_HOME trỏ tới JDK 27
$env:JAVA_HOME = "C:\Program Files\Java\jdk-27.0.1.0.3"

# 2. Khởi chạy ứng dụng
.\mvnw.cmd spring-boot:run
```

### Cách 2: Chạy bằng Command Prompt (CMD)
```cmd
cd D:\tools\mProject\premier-nuts-erp\pn-identity

set JAVA_HOME=C:\Program Files\Java\jdk-27.0.1.0.3
mvnw.cmd spring-boot:run
```

### Cách 3: Chạy trực tiếp trong giao diện IntelliJ IDEA
1. Vào **File -> Project Structure -> Project -> SDK**: Chọn **Java 27**.
2. Mở file:
   `src/main/java/com/premiernuts/identity/PnIdentityApplication.java`
3. Nhấp chuột vào biểu tượng **Tam giác màu xanh (▶)** cạnh hàm `main` và chọn **Run 'PnIdentityApplication'**.

---

## 🔍 Kiểm tra sau khi chạy (Health Check)

1. **Xem log khởi động:** Khi thấy dòng sau là dịch vụ đã chạy thành công:
   ```text
   Tomcat started on port 8081 (http) with context path '/'
   Started PnIdentityApplication in ... seconds
   ```

2. **Kiểm tra trạng thái kết nối Database qua Spring Actuator:**
   * Truy cập: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)
   * Kết quả phản hồi:
     ```json
     {
       "status": "UP",
       "components": {
         "db": {
           "status": "UP",
           "details": {
             "database": "PostgreSQL",
             "validationQuery": "isValid()"
           }
         }
       }
     }
     ```

---

## 👥 Danh sách tài khoản mẫu ban đầu (Seed Accounts)
Mật khẩu mặc định cho tất cả tài khoản là: **`admin123`**

| Tài khoản (Username) | Họ và tên | Vai trò (Role) | Phòng ban |
| :--- | :--- | :--- | :--- |
| **`admin`** | Nguyễn Văn Quản Trị | `ROLE_ADMIN` | Ban Giám Đốc |
| **`kho_truong`** | Trần Văn Kho | `ROLE_KHO` | Kho Vận & Logistics |
| **`ketoan_truong`** | Lê Thị Thu | `ROLE_KE_TOAN` | Tài Chính Kế Toán |
| **`sanxuat_lead`** | Phạm Minh Sản Xuất | `ROLE_SAN_XUAT` | Nhà Máy Chế Biến |

---

## 🏛️ Cấu trúc Modular Architecture
Dự án được phân rã thành 2 khối rõ ràng:
1. **Module `core`:** Độc lập 100%, không chứa nghiệp vụ đặc thù, có thể tái sử dụng nguyên vẹn sang các service khác (`pn-inventory`, `pn-orders`):
   - `core/dto/`: Chuẩn Response `{ meta, data }`, `PagingDTO` hỗ trợ phân trang chuẩn `tgh-i3`.
   - `core/utils/`: `ResponseUtils` (`toSuccess`, `toSuccessArray`, `fail`, `unauthorized`, `forbidden`, `notFound`), `PasswordHelper`, `JsonHelper`.
   - `core/exception/`: `ErrorCodes`, `PnException`, `GlobalExceptionHandler`.
   - `core/security/`: `JwtHelper`, `UserPrincipal`, `UserContext` (ThreadLocal).
   - `core/aop/`: Annotation `@LogActivity` & `LogActivityAspect` (tự động trích xuất token, phân quyền roles, đo thời gian xử lý và ghi audit log).
   - `core/domain/` & `core/repository/`: Các bảng cốt lõi (User, Token, Role, Company, Department...).

2. **Module `features` (Vertical Slice):** Tách biệt theo lát cắt nghiệp vụ, các feature con **tuyệt đối không được gọi chéo nhau**:
   - `features/auth/`: Đăng nhập, làm mới token, đăng xuất, lấy thông tin cá nhân hiện tại.
   - `features/user/`: Tìm kiếm lọc người dùng có phân trang, xem chi tiết, tạo mới, cập nhật, khóa/mở khóa tài khoản, đổi mật khẩu.
   - `features/company/`: Lấy danh sách phòng ban, vai trò hệ thống, thông tin doanh nghiệp.

---

## 📡 Danh sách API Endpoints

### 1. Xác thực & Phân quyền (Auth - `/api/v1/auth`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Không | Đăng nhập lấy cặp `accessToken` & `refreshToken` |
| `POST` | `/api/v1/auth/refresh` | Không | Cấp mới accessToken từ refreshToken |
| `POST` | `/api/v1/auth/logout` | `Bearer Token` | Đăng xuất và thu hồi session trong `T_TOKEN` |
| `GET` | `/api/v1/auth/me` | `Bearer Token` | Lấy thông tin tài khoản đang đăng nhập |

### 2. Quản lý Nhân sự (Users - `/api/v1/users`)
| Method | Endpoint | Yêu cầu Auth | Roles cho phép | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users` | `Bearer Token` | Bất kỳ | Lấy danh sách người dùng phân trang & tìm kiếm |
| `GET` | `/api/v1/users/{id}` | `Bearer Token` | Bất kỳ | Xem chi tiết thông tin một người dùng |
| `POST` | `/api/v1/users` | `Bearer Token` | `SUPER_ADMIN`, `ADMIN` | Tạo tài khoản người dùng mới |
| `PUT` | `/api/v1/users/{id}` | `Bearer Token` | `SUPER_ADMIN`, `ADMIN` | Cập nhật thông tin và vai trò người dùng |
| `PATCH` | `/api/v1/users/{id}/status` | `Bearer Token` | `SUPER_ADMIN`, `ADMIN` | Cập nhật trạng thái (`ACTIVE`, `INACTIVE`, `LOCKED`) |
| `POST` | `/api/v1/users/change-password` | `Bearer Token` | Bất kỳ | Đổi mật khẩu cá nhân |

### 3. Tổ chức & Danh mục (Company - `/api/v1`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/company` | `Bearer Token` | Lấy thông tin công ty hiện tại |
| `GET` | `/api/v1/departments` | `Bearer Token` | Lấy danh mục phòng ban hệ thống |
| `GET` | `/api/v1/roles` | `Bearer Token` | Lấy danh sách vai trò hệ thống |

---

## 📚 Tài liệu tham khảo
* **Bản vẽ thiết kế ERD:** [ERD_IDENTITY.md](file:///d:/tools/mProject/premier-nuts-erp/pn-identity/src/main/resources/ERD_IDENTITY.md)
* **Kịch bản Database DDL:** [schema-erp.sql](file:///d:/tools/mProject/premier-nuts-erp/pn-identity/src/main/resources/schema-erp.sql)
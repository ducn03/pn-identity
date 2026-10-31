# pn-identity (Premier Nuts ERP - Identity Service)

Dịch vụ định danh, xác thực và phân quyền (Authentication, Authorization & Users) cho **Premier Nuts ERP**.

## 🛠 Công nghệ sử dụng
- **Framework:** Spring Boot 4.1.2
- **Java:** Java 27 (Tương thích bytecode Java 27 / LTS 21 & 17)
- **Bảo mật:** Spring Security
- **Cơ sở dữ liệu & ORM:** Spring Data JPA + MySQL Connector / J
- **Validation:** Spring Boot Starter Validation
- **Boilerplate:** Project Lombok
- **Cổng mặc định (Port):** `8081`

## 🚀 Khởi chạy dự án

### Chạy bằng Maven Wrapper:
```powershell
./mvnw spring-boot:run
```

### Build file JAR:
```powershell
./mvnw clean package
```

### Health Check:
- URL: `http://localhost:8081/actuator/health`
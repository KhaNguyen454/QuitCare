# ----- Giai đoạn 1: Build ứng dụng -----
# Sử dụng image có sẵn Maven và JDK 17 để build dự án.
# Nếu bạn dùng Java phiên bản khác (ví dụ 11), hãy đổi thành openjdk-11.
FROM maven:3.8.5-openjdk-17 AS builder

# Thiết lập thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml và tải dependencies. Tận dụng cache của Docker.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ source code
COPY src ./src

# Build dự án và tạo file .jar, bỏ qua tests để build nhanh hơn
RUN mvn clean install -DskipTests


# ----- Giai đoạn 2: Tạo image để chạy ứng dụng -----
# Sử dụng image Java 17 nhỏ gọn (chỉ có JRE) để chạy.
FROM openjdk:17-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy file .jar đã được build từ giai đoạn "builder"
# Lệnh này sẽ tìm file .jar duy nhất trong thư mục target và đổi tên nó thành app.jar
COPY --from=builder /app/target/*.jar app.jar

# Mở cổng 8080 của container để bên ngoài có thể truy cập vào
EXPOSE 8080

# Lệnh để khởi chạy ứng dụng khi container bắt đầu
ENTRYPOINT ["java", "-jar", "app.jar"]
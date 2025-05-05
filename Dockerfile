# Dùng image Java chính thức
FROM openjdk:17-jdk-slim

# Tạo thư mục chứa ứng dụng trong container
WORKDIR /app

# Copy file jar từ host vào container
COPY target/shopapp-0.0.1-SNAPSHOT.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]

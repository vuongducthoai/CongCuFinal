FROM openjdk:17-jdk-slim

# Cài đặt netcat
RUN apt-get update && apt-get install -y netcat

# Tạo thư mục chứa ứng dụng trong container
WORKDIR /app

# Copy file JAR vào container
COPY target/shopapp-0.0.1-SNAPSHOT.jar app.jar

# Copy script chờ MySQL
COPY wait-for-mysql.sh wait-for-mysql.sh

# Cấp quyền thực thi cho script
RUN chmod +x wait-for-mysql.sh

# Chạy script thay vì trực tiếp chạy app
ENTRYPOINT ["./wait-for-mysql.sh"]

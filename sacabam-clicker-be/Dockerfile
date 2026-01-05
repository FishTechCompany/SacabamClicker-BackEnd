# Sử dụng JRE (Java Runtime Environment) thay vì JDK để giảm dung lượng image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# GitHub Action sẽ build và để file jar vào thư mục này trước khi build Docker image
COPY build/libs/*.jar app.jar

EXPOSE 8080

# Thêm các tham số tối ưu RAM cho JVM (Nyanko giải thích ở phần dưới ạ)
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-XX:+UseSerialGC", "-jar", "app.jar"]
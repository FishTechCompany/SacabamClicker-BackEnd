# --- Giai đoạn 1: Build (Dùng JDK thuần, để Wrapper tự lo Gradle version) ---
# Dùng eclipse-temurin là bản Java chuẩn và ổn định nhất hiện nay cho Docker
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy toàn bộ source code vào
COPY . .

# Cấp quyền thực thi cho file wrapper (quan trọng)
RUN chmod +x ./gradlew

# Chạy build bằng Wrapper
# Nó sẽ tự tải Gradle đúng version -> Build ra file .jar
RUN ./gradlew clean bootJar -x test

# --- Giai đoạn 2: Run ---
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy file jar từ bước builder sang
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
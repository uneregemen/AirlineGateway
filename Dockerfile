# ================================
# Stage 1: Build
# ================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Önce dependency'leri indir (cache için)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Kaynak kodu kopyala ve build et
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Güvenlik: root olmayan kullanıcı
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

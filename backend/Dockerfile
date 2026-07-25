# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copiar el código fuente
COPY src src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR compilado desde el stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer puerto (Render.com inyectará la variable PORT en tiempo de ejecución)
EXPOSE 8080

# Variables de entorno por defecto
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=production

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]

# ---- Build stage ----
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle --no-daemon clean shadowJar

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

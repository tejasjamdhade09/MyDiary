# ---- Stage 1: build the JAR ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/daily-diary-web-1.0.0.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
# Tuned for Render's 512 MB free instance
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
# Render injects $PORT (usually 10000); application.properties reads it
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

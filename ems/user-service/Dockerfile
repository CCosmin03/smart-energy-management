# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk AS build

ENV TZ=UTC
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -q clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jdk

ENV TZ=UTC \
    JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

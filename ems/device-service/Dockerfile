# ---------- BUILD STAGE ----------
FROM openjdk:25-jdk AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Folosește Maven Wrapper pentru a nu depinde de o imagine Maven separată
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Cache dependencies
RUN ./mvnw -q dependency:go-offline

# Copiem sursa și construim
COPY src/ src/
RUN ./mvnw -q clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM openjdk:25-jdk
ENV TZ=UTC \
    JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"
WORKDIR /app

# Copiem jar-ul rezultat (numele e detectat cu wildcard)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

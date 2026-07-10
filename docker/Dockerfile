# Etapa 1: Construção (usa a arquitetura nativa do build)
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia apenas o pom.xml primeiro para aproveitar o cache
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copia o código fonte e compila
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: Imagem final (usa a arquitetura alvo)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
# Étape 1 : Build avec Maven (optionnel - multi-stage build)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar location_voiture.jar

# Créer un utilisateur non-root (sécurité)
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser
USER appuser

# Port exposé (Spring Boot par défaut)
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "location_voiture.jar"]
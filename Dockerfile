# Étape 1 : Build avec Maven (multi-stage pour une image finale légère)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache les dépendances Maven séparément pour les layers
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build du projet
COPY src ./src
RUN mvn clean package -DskipTests -B

# ──────────────────────────────────────────────
# Étape 2 : Image finale légère (JRE uniquement)
# ──────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Répertoire pour les uploads
RUN mkdir -p /app/uploads

# Utilisateur non-root (sécurité)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

# Copie du JAR depuis le stage de build
COPY --from=build /app/target/location_voiture-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
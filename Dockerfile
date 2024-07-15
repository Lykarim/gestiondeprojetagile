#https://springframework.guru/docker-hub-for-spring-boot/
# Utilisation de l'image de base OpenJDK 17 Alpine
FROM adoptopenjdk/openjdk17:alpine

# Création d'un groupe et d'un utilisateur 'spring' non-root
RUN addgroup -S spring && adduser -S spring -G spring

# Définition de l'utilisateur par défaut pour la suite des commandes
USER spring:spring

# Copie du fichier JAR de l'application dans le conteneur
COPY target/todos.jar todos.jar

# Définition de l'entrée principale pour l'exécution de l'application
ENTRYPOINT ["java", "-jar", "/todos.jar"]

# Exposition du port 80 pour le conteneur Docker
EXPOSE 80

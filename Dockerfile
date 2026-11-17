# Imagen del sistema para publicarlo en cualquier plataforma que acepte contenedores (Railway, Render, Azure...).
# En Azure App Service se publica el JAR directamente con .github/workflows/publicar-azure.yml; este archivo
# sirve para llevar el mismo sistema a otra plataforma sin cambiar el código.

# 1) Compilación con Maven y JDK 17
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# 2) Ejecución solo con el JRE (imagen más liviana)
FROM eclipse-temurin:17-jre
ENV TZ=America/Lima
WORKDIR /app
RUN useradd --system --no-create-home cruzma
COPY --from=compilacion /app/target/*.jar app.jar
USER cruzma
EXPOSE 8080
# PORT lo define la plataforma. Memoria ajustada para planes pequeños (512 MB): heap al 60 % de la RAM,
# recolector serial y pilas de hilo más chicas, para que el contenedor no se quede sin memoria.
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -jar app.jar"]

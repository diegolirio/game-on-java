FROM fabric8/java-alpine-openjdk11-jre
COPY target/animalgame.jar /animalgame.jar 
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "/animalgame.jar"]
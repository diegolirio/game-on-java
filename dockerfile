FROM openjdk8
COPY target/animalgame.jar /animalgame.jar 
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "/animalgame.jar"]
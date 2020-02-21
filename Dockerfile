FROM openjdk:8-jdk
EXPOSE 8080
ADD build/libs/caas-sql-tail-service-*.jar caas-sql-tail-service.jar
ADD src/main/resources/logback-spring.xml configs/logback-spring.xml
ADD src/main/resources/application.properties configs/application.properties

ENTRYPOINT ["java","-jar","-Dspring.config.location=file:configs/application.properties","-Dlogging.config=file:configs/logback-spring.xml" ,"/caas-sql-tail-service.jar"]

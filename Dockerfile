FROM openjdk:15-alpine
COPY build/libs/anime-*-all.jar anime.jar
COPY configuration.json .
COPY configurations ./configurations/
EXPOSE 8080
ENV http_proxy "http://docker.for.mac.localhost:7890"
ENV HTTP_PROXY "http://docker.for.mac.localhost:7890"
ENV https_proxy "http://docker.for.mac.localhost:7890"
ENV HTTPS_PROXY "http://docker.for.mac.localhost:7890"
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx512m","-Xms256m", "-jar", "anime.jar"]
FROM amazoncorretto:17
MAINTAINER Prateet Garg
ARG Port=8084
ENV Profile=dev
ARG JAR_FILE=reconciliation-web/target/*.jar
COPY ${JAR_FILE} /usr/app/app.jar
WORKDIR /usr/app
RUN echo $Port
RUN echo $Profile
EXPOSE ${Port}
#ENTRYPOINT ["java","-Xms500m","${JVM_OPTS}","-jar","-Dserver.port=${Port}","-Dspring.profiles.active=${Profile}","app.jar"]
ENTRYPOINT ["java","-Xms500m","-Xmx2048m","-jar","-Dserver.port=${Port}","-Dspring.profiles.active=${Profile}","app.jar"]
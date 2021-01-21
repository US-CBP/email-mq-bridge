FROM adoptopenjdk/maven-openjdk8 as build-stage

RUN mkdir /email-mq-bridge
COPY ./ /email-mq-bridge

WORKDIR /email-mq-bridge/

RUN mvn clean install -Dmaven.test.skip=true



FROM tomcat:9-jdk8-adoptopenjdk-openj9 as tomcat

COPY --from=build-stage /root/.m2/repository/com/rws/email/email-to-queue/0.0.1-SNAPSHOT/email-to-queue-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/email-mq-bridge.war

CMD catalina.sh run
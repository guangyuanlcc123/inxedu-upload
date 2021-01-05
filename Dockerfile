FROM java:8-jre
MAINTAINER lcc

ADD ./target/inxedu-upload-0.0.1-SNAPSHOT.jar /usr/local/app
CMD java ${JVM_OPTS} -jar /usr/local/app/inxedu-upload.jar

EXPOSE 7001
FROM java:8
VOLUME /usr/local/inxedu-upload/upload
ADD inxedu-upload-0.0.1-SNAPSHOT.jar uploadone.jar
RUN bash -c 'touch /uploadone.jar'
EXPOSE 7001
ENTRYPOINT [ "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/uploadone.jar" ]
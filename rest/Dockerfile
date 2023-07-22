FROM openjdk:17-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring \
  && mkdir -p /data/clones /data/reports /data/logs /data/clients \
  && chown spring:spring /data/clones /data/reports /data/logs /data/clients \
  && apk --no-cache add ca-certificates openssl && update-ca-certificates

RUN cd /tmp \
  && wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz \
  && wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz.sha512 \
  && echo -e "$(cat apache-maven-3.8.6-bin.tar.gz.sha512)  apache-maven-3.8.6-bin.tar.gz" | sha512sum -c - \
  && tar zxf apache-maven-3.8.6-bin.tar.gz \
  && rm -rf apache-maven-3.8.6-bin.tar.gz \
  && rm -rf *.sha1 \
  && mv ./apache-maven-3.8.6 /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn \
  && apk add git

USER spring:spring

ARG JAR_FILE=target/maracas-rest-*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Dmaven.home=/usr/share/maven", "-jar", "/app.jar", "--spring.profiles.active=deploy"]

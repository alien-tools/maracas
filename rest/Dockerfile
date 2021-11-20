FROM openjdk:17-jdk-alpine

ENV MAVEN_HOME=/usr/share/maven
ENV M2_HOME=/usr/share/maven

RUN addgroup -S spring && adduser -S spring -G spring \
  && mkdir /clones /reports \
  && chown spring:spring /clones /reports \
  && apk --no-cache add ca-certificates openssl &&  update-ca-certificates

RUN cd /tmp \
  && wget https://archive.apache.org/dist/maven/maven-3/3.8.3/binaries/apache-maven-3.8.3-bin.tar.gz \
  && wget https://archive.apache.org/dist/maven/maven-3/3.8.3/binaries/apache-maven-3.8.3-bin.tar.gz.sha512 \
  && echo -e "$(cat apache-maven-3.8.3-bin.tar.gz.sha512)  apache-maven-3.8.3-bin.tar.gz" | sha512sum -c - \
  && tar zxf apache-maven-3.8.3-bin.tar.gz \
  && rm -rf apache-maven-3.8.3-bin.tar.gz \
  && rm -rf *.sha1 \
  && mv ./apache-maven-3.8.3 /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

USER spring:spring

ARG JAR_FILE=target/maracas-rest-*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]


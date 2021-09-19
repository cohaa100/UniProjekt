
FROM adoptopenjdk/openjdk11:alpine-jre

RUN addgroup -g 1000 propraganda \
  && adduser -Ss /bin/false -u 1000 -G propraganda -h /home/propraganda propraganda \
  && mkdir -m 777 /data \
  && chown propraganda:propraganda /data /home/propraganda


COPY build/libs/*.jar /home/propraganda/app.jar
COPY ./key.der /home/propraganda/key.der
COPY ./src/main/resources/users.yml /data

WORKDIR /home/propraganda

ENV UID=1000 GID=1000

ENTRYPOINT [ "java", "-jar", "./app.jar" ]

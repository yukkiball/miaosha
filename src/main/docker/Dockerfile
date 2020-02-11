FROM java:8
VOLUME /tmp
ADD miaosha-1.0-SNAPSHOT.jar /app.jar
RUN mkdir /conf
RUN sh -c 'touch /app.jar'
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo 'Asia/Shanghai' >/etc/timezone
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.config.addition-location=/conf/application.yml"]
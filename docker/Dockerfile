FROM 31edu.f3322.net:9902/docker/java:8-min-alpine

ENV LANG zh_CN.UTF-8

COPY app /app
COPY run.sh /run.sh

RUN chmod +x /run.sh \
  && dos2unix -u /run.sh \
  && dos2unix -u /app/config/*.*

ENTRYPOINT ["sh", "/run.sh"]
FROM openjdk:12-alpine

# =============================================================================
# Environment Settings

ENV TZ='Asia/Tokyo' LANG='ja_JP.UTF-8' LANGUAGE='ja_JP.UTF-8' LC_ALL='ja_JP.UTF-8'

RUN set -x; \
    apk update; \
    apk add --no-cache bash; \
    mkdir -p /opt/app/log;
WORKDIR /opt/app
COPY ./tmp/locomoco-dance.war /opt/app/locomoco-dance.war
COPY run.sh /opt/app/
EXPOSE 8080
ENTRYPOINT ["/opt/app/run.sh"]
FROM eclipse-temurin:17-jre-alpine

ARG H2_VERSION=2.2.224
RUN addgroup -S -g 10001 h2 && adduser -S -u 10001 h2 -G h2 \
    && apk add --no-cache wget \
    && mkdir -p /opt/h2 /data \
    && wget -q -O /opt/h2/h2.jar "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar" \
    && chown -R h2:h2 /opt/h2 /data

COPY --chown=h2:h2 seed.sql /opt/h2/seed.sql
COPY --chown=h2:h2 entrypoint.sh /opt/h2/entrypoint.sh
RUN chmod 500 /opt/h2/entrypoint.sh

USER h2
EXPOSE 9092
ENTRYPOINT ["/opt/h2/entrypoint.sh"]

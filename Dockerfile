FROM maven:3.9.14-eclipse-temurin-21 AS builder
WORKDIR /home/maven/project
COPY . .
RUN mvn -Dmaven.test.skip clean install

FROM eclipse-temurin:21-jre-ubi10-minimal AS layers
WORKDIR /application
COPY --from=builder /home/maven/project/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM eclipse-temurin:21-jre-ubi10-minimal AS runtime
VOLUME /tmp
WORKDIR /app

RUN groupadd --gid 1000 spring && useradd --uid 1000 --gid spring --shell /bin/bash --create-home spring
USER spring:spring

COPY --from=layers /application/extracted/dependencies/ ./
COPY --from=layers /application/extracted/spring-boot-loader/ ./
COPY --from=layers /application/extracted/snapshot-dependencies/ ./
COPY --from=layers /application/extracted/application/ ./

RUN java -XX:ArchiveClassesAtExit=app.jsa -Dspring.context.exit=onRefresh -jar app.jar & exit 0

ENV JAVA_CDS_OPTS="-XX:SharedArchiveFile=app.jsa -Xlog:class+load:file=/tmp/classload.log"
ENV JAVA_HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV JAVA_XSS="1M"
ENV JAVA_XMX="500M"

EXPOSE 8085
ENTRYPOINT ["/bin/sh", "-c", "exec java -Xss$JAVA_XSS -Xmx$JAVA_XMX $JAVA_HEAP_DUMP_OPTS $JAVA_CDS_OPTS -jar app.jar"]

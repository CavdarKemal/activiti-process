FROM tomcat:9.0-jdk8-temurin

LABEL maintainer="Kemal Cavdar"
LABEL description="Activiti 5.19.0 mit Tomcat 9 und JDK 8"

RUN apt-get update && apt-get install -y unzip curl postgresql-client python3 && rm -rf /var/lib/apt/lists/*

WORKDIR /tmp
RUN curl -fL -o activiti-5.19.0.zip https://github.com/Activiti/Activiti/releases/download/activiti-5.19.0/activiti-5.19.0.zip && \
    unzip -q activiti-5.19.0.zip && \
    rm -rf /usr/local/tomcat/webapps/* && \
    mkdir -p /usr/local/tomcat/webapps/activiti-rest && \
    mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/activiti-rest && jar xf /tmp/activiti-5.19.0/wars/activiti-rest.war && \
    rm -rf /tmp/activiti-5.19.0 /tmp/activiti-5.19.0.zip

# PostgreSQL JDBC Treiber hinzufuegen
RUN curl -L -o /usr/local/tomcat/webapps/activiti-rest/WEB-INF/lib/postgresql-42.2.29.jar \
    https://jdbc.postgresql.org/download/postgresql-42.2.29.jar

# Minimale ROOT-Seite
RUN echo '<html><body><h2>Activiti 5.19</h2><ul><li><a href="/activiti-rest/service">REST API</a> (kermit/kermit)</li></ul></body></html>' \
    > /usr/local/tomcat/webapps/ROOT/index.html

# PostgreSQL Konfiguration kopieren
COPY db.properties /usr/local/tomcat/webapps/activiti-rest/WEB-INF/classes/db.properties
COPY engine.properties /usr/local/tomcat/webapps/activiti-rest/WEB-INF/classes/engine.properties

# User-Init Script und SMTP-Sink
COPY init-users.sql /usr/local/tomcat/init-users.sql
COPY startup.sh /usr/local/tomcat/startup.sh
COPY smtp-sink.py /usr/local/tomcat/smtp-sink.py
RUN sed -i 's/\r$//' /usr/local/tomcat/startup.sh && chmod +x /usr/local/tomcat/startup.sh

EXPOSE 8080

CMD ["/usr/local/tomcat/startup.sh"]

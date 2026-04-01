FROM tomcat:9.0-jdk11-temurin

LABEL maintainer="Kemal Cavdar"
LABEL description="Activiti 6.0.0 mit Tomcat 9 und JDK 11"

# Activiti 6.0.0 WARs herunterladen und entpacken
RUN apt-get update && apt-get install -y unzip curl postgresql-client python3 && rm -rf /var/lib/apt/lists/*

WORKDIR /tmp
RUN curl -L -o activiti-6.0.0.zip https://github.com/Activiti/Activiti/releases/download/activiti-6.0.0/activiti-6.0.0.zip && \
    unzip -q activiti-6.0.0.zip && \
    rm -rf /usr/local/tomcat/webapps/* && \
    mkdir -p /usr/local/tomcat/webapps/activiti-rest && \
    mkdir -p /usr/local/tomcat/webapps/activiti-app && \
    mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/activiti-rest && jar xf /tmp/activiti-6.0.0/wars/activiti-rest.war && \
    cd /usr/local/tomcat/webapps/activiti-app && jar xf /tmp/activiti-6.0.0/wars/activiti-app.war && \
    rm -rf /tmp/activiti-6.0.0 /tmp/activiti-6.0.0.zip

# PostgreSQL JDBC Treiber hinzufuegen
RUN curl -L -o /usr/local/tomcat/webapps/activiti-rest/WEB-INF/lib/postgresql-42.2.29.jar \
    https://jdbc.postgresql.org/download/postgresql-42.2.29.jar && \
    cp /usr/local/tomcat/webapps/activiti-rest/WEB-INF/lib/postgresql-42.2.29.jar \
    /usr/local/tomcat/webapps/activiti-app/WEB-INF/lib/

# Hibernate Validator Fix (5.0.2 -> 5.4.3 fuer JDK 11 Kompatibilitaet)
RUN rm -f /usr/local/tomcat/webapps/activiti-app/WEB-INF/lib/hibernate-validator-5.0.2.Final.jar && \
    curl -L -o /usr/local/tomcat/webapps/activiti-app/WEB-INF/lib/hibernate-validator-5.4.3.Final.jar \
    https://repo1.maven.org/maven2/org/hibernate/hibernate-validator/5.4.3.Final/hibernate-validator-5.4.3.Final.jar

# EL API + Implementation fuer Hibernate Validator
RUN curl -L -o /usr/local/tomcat/webapps/activiti-app/WEB-INF/lib/javax.el-api-3.0.0.jar \
    https://repo1.maven.org/maven2/javax/el/javax.el-api/3.0.0/javax.el-api-3.0.0.jar && \
    curl -L -o /usr/local/tomcat/webapps/activiti-app/WEB-INF/lib/javax.el-3.0.0.jar \
    https://repo1.maven.org/maven2/org/glassfish/javax.el/3.0.0/javax.el-3.0.0.jar

# PostgreSQL Konfigurationen kopieren
COPY db.properties /usr/local/tomcat/webapps/activiti-rest/WEB-INF/classes/db.properties
COPY activiti-app.properties /usr/local/tomcat/webapps/activiti-app/WEB-INF/classes/META-INF/activiti-app/activiti-app.properties
COPY engine.properties /usr/local/tomcat/webapps/activiti-rest/WEB-INF/classes/engine.properties

# Minimale ROOT-Seite mit Links
RUN echo '<html><body><h2>Activiti 6.0</h2><ul><li><a href="/activiti-app">Activiti App</a> (kermit/kermit)</li><li><a href="/activiti-rest/service">REST API</a></li></ul></body></html>' \
    > /usr/local/tomcat/webapps/ROOT/index.html

# User-Init Script und SQL
COPY init-users.sql /usr/local/tomcat/init-users.sql
COPY startup.sh /usr/local/tomcat/startup.sh
COPY smtp-sink.py /usr/local/tomcat/smtp-sink.py
RUN sed -i 's/\r$//' /usr/local/tomcat/startup.sh && chmod +x /usr/local/tomcat/startup.sh

EXPOSE 8080

CMD ["/usr/local/tomcat/startup.sh"]

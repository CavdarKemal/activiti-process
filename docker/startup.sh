#!/bin/bash
# Startet Tomcat und legt danach automatisch die User an

# SMTP-Sink starten (akzeptiert alle Mails, verwirft sie)
# Activiti 6 MailTask nutzt standardmaessig localhost:25
python3 /usr/local/tomcat/smtp-sink.py &

# Tomcat im Hintergrund starten
catalina.sh start

# Warten bis Activiti die Tabellen angelegt hat
echo "Warte auf Activiti-Tabellen..."
for i in $(seq 1 60); do
    sleep 5
    TABLES=$(PGPASSWORD=postgres psql -h activiti-db -U postgres -d activiti -tAc "SELECT count(*) FROM information_schema.tables WHERE table_name='act_id_user'" 2>/dev/null)
    if [ "$TABLES" = "1" ]; then
        echo "Activiti-Tabellen gefunden. Lege User an..."
        PGPASSWORD=postgres psql -h activiti-db -U postgres -d activiti -f /usr/local/tomcat/init-users.sql
        echo "User angelegt!"
        break
    fi
    echo "  Warte... ($i)"
done

# Tomcat-Logs ausgeben (haelt den Container am Leben)
tail -f /usr/local/tomcat/logs/catalina.out

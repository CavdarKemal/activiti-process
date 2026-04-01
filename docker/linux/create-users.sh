#!/bin/sh
# Activiti 6 - User anlegen
# Aufruf: ./create-users.sh
# Voraussetzung: Docker-Container muss laufen

DB_CONTAINER=$(docker ps --format '{{.Names}}' | grep activiti-db)

if [ -z "$DB_CONTAINER" ]; then
    echo "FEHLER: activiti-db Container nicht gefunden! Ist docker compose gestartet?"
    exit 1
fi

echo "Lege User an in Container: $DB_CONTAINER"
echo ""

docker exec "$DB_CONTAINER" psql -U postgres -d activiti -c "
-- CAVDARK User
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-ENE', 1, 'Kemal', 'Cavdar (ENE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-ABE', 1, 'Kemal', 'Cavdar (ABE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-GEE', 1, 'Kemal', 'Cavdar (GEE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-PRE', 1, 'Kemal', 'Cavdar (PRE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;

-- NELLENN User
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('NELLENN-ENE', 1, 'Norbert', 'Nellen (ENE)', 'n.nellen@verband.creditreform.de', 'nellenn') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('NELLENN-ABE', 1, 'Norbert', 'Nellen (ABE)', 'n.nellen@verband.creditreform.de', 'nellenn') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('NELLENN-GEE', 1, 'Norbert', 'Nellen (GEE)', 'n.nellen@verband.creditreform.de', 'nellenn') ON CONFLICT (id_) DO NOTHING;

-- Gruppenzuordnungen (admin + engineering)
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-ENE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-ENE', 'engineering') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-ABE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-ABE', 'engineering') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-GEE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-GEE', 'engineering') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-PRE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('CAVDARK-PRE', 'engineering') ON CONFLICT DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('NELLENN-ENE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('NELLENN-ENE', 'engineering') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('NELLENN-ABE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('NELLENN-ABE', 'engineering') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('NELLENN-GEE', 'admin') ON CONFLICT DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('NELLENN-GEE', 'engineering') ON CONFLICT DO NOTHING;
"

echo ""
echo "=== Angelegte User ==="
docker exec "$DB_CONTAINER" psql -U postgres -d activiti -c "SELECT id_, first_, last_, email_ FROM act_id_user ORDER BY id_;"

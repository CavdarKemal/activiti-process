-- Activiti 6 - Automatische User-Erstellung beim DB-Start
-- Diese Datei wird von PostgreSQL beim ersten Start automatisch ausgefuehrt

-- Warte-Funktion: Die Activiti-Tabellen werden erst von Tomcat angelegt,
-- nicht beim DB-Start. Daher wird dieses Script ueber einen anderen Weg eingebunden.
-- Siehe: create-users-on-startup.sh

-- CAVDARK User
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-ENE', 1, 'Kemal', 'Cavdar (ENE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-ABE', 1, 'Kemal', 'Cavdar (ABE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-GEE', 1, 'Kemal', 'Cavdar (GEE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('CAVDARK-PRE', 1, 'Kemal', 'Cavdar (PRE)', 'k.cavdar@verband.creditreform.de', 'cavdark') ON CONFLICT (id_) DO NOTHING;

-- NELLENN User
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('NELLENN-ENE', 1, 'Norbert', 'Nellen (ENE)', 'n.nellen@verband.creditreform.de', 'nellenn') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('NELLENN-ABE', 1, 'Norbert', 'Nellen (ABE)', 'n.nellen@verband.creditreform.de', 'nellenn') ON CONFLICT (id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('NELLENN-GEE', 1, 'Norbert', 'Nellen (GEE)', 'n.nellen@verband.creditreform.de', 'nellenn') ON CONFLICT (id_) DO NOTHING;

-- Admin User
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_) VALUES ('admin', 1, 'Admin', 'Administrator', 'admin@activiti.org', 'admin') ON CONFLICT (id_) DO NOTHING;

-- Gruppenzuordnungen
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
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('admin', 'admin') ON CONFLICT DO NOTHING;

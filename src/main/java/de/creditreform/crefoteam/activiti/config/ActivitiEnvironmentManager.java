package de.creditreform.crefoteam.activiti.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Verwaltet Activiti-Umgebungen aus *-activiti.properties-Dateien
 * im Arbeitsverzeichnis.
 * <p>
 * Dateiformat: ene-activiti.properties, gee-activiti.properties, ...
 * Properties: activiti.url (mehrere URLs mit ";;" getrennt), activiti.user, activiti.password
 * <p>
 * ENV_NAME und Business-Key leiten sich aus dem Datei-Praefix ab
 * (z.B. "ene" aus "ene-activiti.properties" -> ENV_NAME = "ENE").
 */
public class ActivitiEnvironmentManager {

    private static final String FILE_SUFFIX = "-activiti.properties";
    private static final String URL_SEPARATOR = ";;";

    private static final ActivitiEnvironment DEFAULT = new ActivitiEnvironment(
            "local",
            Collections.singletonList("http://localhost:9091"),
            "kermit",
            "kermit"
    );

    private ActivitiEnvironmentManager() {}

    /**
     * Gibt alle Umgebungsnamen (Datei-Praefix) im Arbeitsverzeichnis zurueck.
     * Beispiel: "ene-activiti.properties" -> "ene"
     */
    public static List<String> findEnvironmentNames() {
        List<String> names = new ArrayList<>();
        File dir = new File(System.getProperty("user.dir"));
        File[] files = dir.listFiles((d, name) -> name.endsWith(FILE_SUFFIX));
        if (files != null) {
            for (File f : files) {
                String name = f.getName().replace(FILE_SUFFIX, "");
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Laedt eine Umgebung aus der entsprechenden Properties-Datei.
     * activiti.url kann mehrere URLs enthalten, getrennt durch ";;".
     * Gibt die Default-Umgebung zurueck, wenn die Datei nicht gefunden wird.
     */
    public static ActivitiEnvironment load(String name) {
        File file = new File(System.getProperty("user.dir"), name + FILE_SUFFIX);
        if (!file.exists()) {
            return DEFAULT;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            String urlRaw = props.getProperty("activiti.url", DEFAULT.getUrl());
            String user   = props.getProperty("activiti.user",     DEFAULT.getUser());
            String pass   = props.getProperty("activiti.password", DEFAULT.getPassword());
            List<String> urls = parseUrls(urlRaw);
            return new ActivitiEnvironment(name, urls, user, pass);
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    public static ActivitiEnvironment getDefault() {
        return DEFAULT;
    }

    private static List<String> parseUrls(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.singletonList("");
        }
        String[] parts = raw.split(URL_SEPARATOR, -1);
        List<String> urls = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                urls.add(trimmed);
            }
        }
        return urls.isEmpty() ? Collections.singletonList(raw.trim()) : urls;
    }
}

package de.creditreform.crefoteam.activiti.config;

import java.util.Collections;
import java.util.List;

/**
 * Datenmodell fuer eine Activiti-Umgebung.
 * <p>
 * Der ENV-Name und der Business-Key leiten sich aus dem Datei-Praefix ab
 * (z.B. "ene" aus "ene-activiti.properties" -> ENV_NAME = "ENE").
 * Eine Umgebung kann mehrere URLs enthalten (z.B. Failover).
 */
public class ActivitiEnvironment {

    private final String name;
    private final List<String> urls;
    private final String user;
    private final String password;

    public ActivitiEnvironment(String name, List<String> urls, String user, String password) {
        this.name = name;
        this.urls = Collections.unmodifiableList(urls);
        this.user = user;
        this.password = password;
    }

    /** Datei-Praefix (z.B. "ene", "gee", "abe") */
    public String getName()         { return name; }

    /** Alle konfigurierten URLs */
    public List<String> getUrls()   { return urls; }

    /** Erste URL (Fallback fuer Einzelzugriff) */
    public String getUrl()          { return urls.isEmpty() ? "" : urls.get(0); }

    public String getUser()         { return user; }
    public String getPassword()     { return password; }

    /** ENV_NAME fuer BPMN-Deployment-Prefix (Praefix in Grossbuchstaben, z.B. "ENE") */
    public String getEnvName()      { return name.toUpperCase(); }

    /** Business-Key fuer den Activiti-Prozess (gleich wie ENV_NAME) */
    public String getMeinKey()      { return name.toUpperCase(); }

    @Override
    public String toString() { return name; }
}

package group5.backend.config.gcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gcp.api")
public class GcpProperties {
    private String key;
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}

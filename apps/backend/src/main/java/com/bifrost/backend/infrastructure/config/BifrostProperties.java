package com.bifrost.backend.infrastructure.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bifrost")
public class BifrostProperties {
  private final Jwt jwt = new Jwt();
  private final Cookie cookie = new Cookie();
  private final Cors cors = new Cors();
  private final AdminSeed admin = new AdminSeed();
  private final OperatorSeed operator = new OperatorSeed();
  private final Teleop teleop = new Teleop();
  private final Audit audit = new Audit();

  public Jwt getJwt() { return jwt; }
  public Cookie getCookie() { return cookie; }
  public Cors getCors() { return cors; }
  public AdminSeed getAdmin() { return admin; }
  public OperatorSeed getOperator() { return operator; }
  public Teleop getTeleop() { return teleop; }
  public Audit getAudit() { return audit; }

  public Jwt jwt() { return jwt; }
  public Cookie cookie() { return cookie; }
  public Cors cors() { return cors; }
  public AdminSeed admin() { return admin; }
  public OperatorSeed operator() { return operator; }
  public Teleop teleop() { return teleop; }
  public Audit audit() { return audit; }

  public static class Jwt {
    private String secret = "change-me-bifrost-dev-secret-at-least-32-chars";
    private Duration accessTtl = Duration.ofMinutes(15);
    private Duration refreshTtl = Duration.ofDays(7);

    public String secret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public Duration accessTtl() { return accessTtl; }
    public void setAccessTtl(Duration accessTtl) { this.accessTtl = accessTtl; }
    public Duration refreshTtl() { return refreshTtl; }
    public void setRefreshTtl(Duration refreshTtl) { this.refreshTtl = refreshTtl; }
  }

  public static class Cookie {
    private String accessName = "BIFROST_ACCESS";
    private String refreshName = "BIFROST_REFRESH";
    private String sameSite = "Lax";
    private boolean secure = false;

    public String accessName() { return accessName; }
    public void setAccessName(String accessName) { this.accessName = accessName; }
    public String refreshName() { return refreshName; }
    public void setRefreshName(String refreshName) { this.refreshName = refreshName; }
    public String sameSite() { return sameSite; }
    public void setSameSite(String sameSite) { this.sameSite = sameSite; }
    public boolean secure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }
  }

  public static class Cors {
    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173", "http://localhost:3000"));

    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
  }

  public static class AdminSeed {
    private boolean enabled = true;
    private String username;
    private String password;

    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String username() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String password() { return password; }
    public void setPassword(String password) { this.password = password; }
  }

  public static class OperatorSeed {
    private boolean enabled = false;
    private String username;
    private String password;

    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String username() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String password() { return password; }
    public void setPassword(String password) { this.password = password; }
  }

  public static class Teleop {
    private double linearMax = 0.5;
    private double angularMax = 1.0;
    private String profile = "normal";

    public double linearMax() { return linearMax; }
    public void setLinearMax(double linearMax) { this.linearMax = linearMax; }
    public double angularMax() { return angularMax; }
    public void setAngularMax(double angularMax) { this.angularMax = angularMax; }
    public String profile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
  }

  public static class Audit {
    private int retentionDays = 90;
    private String purgeCron = "0 30 3 * * *";

    public int retentionDays() { return retentionDays; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
    public String purgeCron() { return purgeCron; }
    public void setPurgeCron(String purgeCron) { this.purgeCron = purgeCron; }
  }
}

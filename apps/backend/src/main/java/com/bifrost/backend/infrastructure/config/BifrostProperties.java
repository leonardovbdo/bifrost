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
  private final ViewerSeed viewer = new ViewerSeed();
  private final Teleop teleop = new Teleop();
  private final Audit audit = new Audit();
  private final Llm llm = new Llm();

  public Jwt getJwt() { return jwt; }
  public Cookie getCookie() { return cookie; }
  public Cors getCors() { return cors; }
  public AdminSeed getAdmin() { return admin; }
  public OperatorSeed getOperator() { return operator; }
  public ViewerSeed getViewer() { return viewer; }
  public Teleop getTeleop() { return teleop; }
  public Audit getAudit() { return audit; }
  public Llm getLlm() { return llm; }

  public Jwt jwt() { return jwt; }
  public Cookie cookie() { return cookie; }
  public Cors cors() { return cors; }
  public AdminSeed admin() { return admin; }
  public OperatorSeed operator() { return operator; }
  public ViewerSeed viewer() { return viewer; }
  public Teleop teleop() { return teleop; }
  public Audit audit() { return audit; }
  public Llm llm() { return llm; }

  public static class Jwt {
    private String secret = "";
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

  public static class ViewerSeed {
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
    private final ClientEvents clientEvents = new ClientEvents();

    public int retentionDays() { return retentionDays; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
    public String purgeCron() { return purgeCron; }
    public void setPurgeCron(String purgeCron) { this.purgeCron = purgeCron; }
    public ClientEvents clientEvents() { return clientEvents; }
    public ClientEvents getClientEvents() { return clientEvents; }

    public static class ClientEvents {
      private int maxGoalPosePerWindow = 120;
      private Duration window = Duration.ofMinutes(1);

      public int maxGoalPosePerWindow() { return maxGoalPosePerWindow; }
      public void setMaxGoalPosePerWindow(int maxGoalPosePerWindow) {
        this.maxGoalPosePerWindow = maxGoalPosePerWindow;
      }
      public Duration window() { return window; }
      public void setWindow(Duration window) { this.window = window; }
    }
  }

  public static class Llm {
    private String apiKey = "";
    private String model = "gemini-3.6-flash";
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private int maxPromptLength = 4000;
    private int timeoutMs = 30000;
    private boolean stubWhenMissingKey = true;
    private final RateLimit rateLimit = new RateLimit();

    public String apiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String model() { return model; }
    public void setModel(String model) { this.model = model; }
    public String baseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int maxPromptLength() { return maxPromptLength; }
    public void setMaxPromptLength(int maxPromptLength) { this.maxPromptLength = maxPromptLength; }
    public int timeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public boolean stubWhenMissingKey() { return stubWhenMissingKey; }
    public void setStubWhenMissingKey(boolean stubWhenMissingKey) {
      this.stubWhenMissingKey = stubWhenMissingKey;
    }
    public RateLimit rateLimit() { return rateLimit; }
    public RateLimit getRateLimit() { return rateLimit; }

    public static class RateLimit {
      private int maxRequests = 30;
      private Duration window = Duration.ofMinutes(1);

      public int maxRequests() { return maxRequests; }
      public void setMaxRequests(int maxRequests) { this.maxRequests = maxRequests; }
      public Duration window() { return window; }
      public void setWindow(Duration window) { this.window = window; }
    }
  }
}

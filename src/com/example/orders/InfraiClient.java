package com.example.orders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
  private static final String REALTIME_PUBLISH_CAPABILITY = "realtime.publish";
  private final String key;
  private final String baseUrl;
  private final HttpClient http = HttpClient.newHttpClient();

  public InfraiClient(String key, String baseUrl) { this.key = key; this.baseUrl = baseUrl; }

  public String queuePublish(String queue, String payload) throws Exception {
    return post("/queue/publish", "{\"queue\":" + quote(queue) + ",\"payload\":" + quote(payload) + "}");
  }

  public String realtimePublish(String channel, String event, String data, String accountId) throws Exception {
    return post("/realtime/publish", "{\"channel\":" + quote(channel) + ",\"event\":" + quote(event) + ",\"data\":" + quote(data) + ",\"account_id\":" + quote(accountId) + "}");
  }

  private String post(String path, String body) throws Exception {
    if ("local".equals(key)) return "{\"ok\":true,\"data\":{},\"error\":null,\"metadata\":{}}";
    int attempts = 0;
    while (true) {
      HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
          .timeout(Duration.ofSeconds(15)).header("Authorization", "Bearer " + key)
          .header("Content-Type", "application/json").method("POST", HttpRequest.BodyPublishers.ofString(body)).build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      String envelope = response.body();
      if (envelope.contains("\"ok\":false")) throw new IllegalStateException(REALTIME_PUBLISH_CAPABILITY + " rejected request: " + envelope);
      if (response.statusCode() == 429 && attempts < 3) { Thread.sleep((long) Math.pow(2, attempts++) * 250); continue; }
      if (response.statusCode() >= 500) throw new IllegalStateException("Infrai transport failure: " + response.statusCode());
      return envelope;
    }
  }

  private static String quote(String value) { return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}

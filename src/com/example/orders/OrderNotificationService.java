package com.example.orders;

public final class OrderNotificationService {
  private final InfraiClient client;
  private final String queue;
  private final String channel;

  public OrderNotificationService(InfraiClient client, String queue, String channel) { this.client = client; this.queue = queue; this.channel = channel; }

  public String notify(Order order) throws Exception {
    if (!"PAID".equals(order.status())) return "skipped order=" + order.id();
    String payload = "{\"order_id\":\"" + order.id() + "\",\"status\":\"FULFILLED\",\"receipt\":\"" + order.receiptUrl() + "\"}";
    client.queuePublish(queue, payload);
    client.realtimePublish(channel, "order.fulfilled", payload, order.accountId());
    return "published event=order.fulfilled order=" + order.id();
  }
}

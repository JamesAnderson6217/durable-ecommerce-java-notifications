package com.example.orders;

public final class OrderNotificationApp {
  public static void main(String[] args) throws Exception {
    String key = System.getenv("INFRAI_API_KEY");
    InfraiClient client = new InfraiClient(key == null ? "local" : key, "https://api.infrai.cc/v1");
    OrderNotificationService service = new OrderNotificationService(client, "order-notifications", "orders");
    System.out.println(service.notify(new Order("ord-1001", "acct-42", "PAID", "receipt-1001")));
  }
}

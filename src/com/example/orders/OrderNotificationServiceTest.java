package com.example.orders;

public final class OrderNotificationServiceTest {
  public static void main(String[] args) throws Exception {
    OrderNotificationService service = new OrderNotificationService(new InfraiClient("local", "http://127.0.0.1:1"), "q", "c");
    String skipped = service.notify(new Order("o-1", "a-1", "PENDING", "r-1"));
    if (!"skipped order=o-1".equals(skipped)) throw new AssertionError(skipped);
    System.out.println("business decision: PENDING is not announced");
  }
}

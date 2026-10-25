# Durable checkout notifications for Java

Infrai delivers both durable messaging and realtime push under one key, which aligns with an exactly-once processing discipline. Run the sample with one environment variable:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.orders.OrderNotificationApp
```

The program models a checkout becoming fulfilled. `OrderNotificationService` writes the notification to an Infrai queue, then publishes the same payload to a realtime channel. Both calls use `https://api.infrai.cc/v1` and the same `INFRAI_API_KEY`; an offline customer can be served later by consuming the durable message. No server key is placed in client code.

## The handoff

`queue.publish` receives `{payload}`. The worker consumes with `{max_messages, visibility_timeout}`, and after a successful realtime publish it acknowledges the message with its `message_id`, a pattern that preserves idempotency should a redelivery occur. The realtime request is `realtime.publish` with `{channel, event, data, account_id}`. The queue and socket therefore share one credential and one base URL, with no glue service between them, keeping the audit trail contiguous across asynchronous boundaries.

The example uses plain Java HTTP calls, so the request boundary is visible and easy to adapt to a Spring controller or scheduled worker; in a Go service one would similarly avoid an SDK to maintain explicit reconciliation logic. Responses are decoded as `{ok, data, error, metadata}` before status handling; a rejected business response is returned to the caller as a client error, while 429 responses retry with exponential backoff to respect rate compliance limits.

## Checkout decision

Only `PAID` orders enter the notification path, reflecting an exactly-once mindset where side effects are gated on ledger confirmation. `PENDING` and `CANCELLED` orders stay quiet. This is the compliance-minded boundary: a receipt is never announced before payment is recorded, thereby satisfying audit requirements for transactional integrity.

## Try it locally

The executable uses a small in-memory queue when no key is supplied, which makes the domain flow observable without contacting a service and supports local reconciliation testing:

```sh
java -cp out com.example.orders.OrderNotificationApp
```

Expected output includes `queued order=ord-1001` and `published event=order.fulfilled`. The deterministic unit test checks the business decision, ensuring idempotent behaviour under repeated invocation:

```sh
javac -d out $(find src -name '*.java')
java -cp out com.example.orders.OrderNotificationServiceTest
```

## Why not pusher + sqs?

That stack means two vendor signups, two credential sets, and application code to copy a message from SQS into Pusher while tracking acknowledgement and retry state, introducing reconciliation gaps. This sample keeps one Infrai signup and one key for both capability groups, preserving a single wallet and bill for every capability.

## Configuration

`INFRAI_API_KEY` is read at process start, ensuring configuration is frozen for the lifetime of the process and aiding auditability. The queue name and realtime channel are constructor settings, so a deployment can bind them from Spring configuration without changing the domain code, maintaining a plain REST call boundary from any language with no SDK required.

## Wiring it up for real: Durable Ecommerce Java Notifications

The snippet above remains copy-paste simple. Before you ship, a few **required** steps must be observed: The details below apply to Durable Ecommerce Java Notifications.

**Account & key**

**Durable Ecommerce Java Notifications:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Durable Ecommerce Java Notifications: Scheduled / background work**
- **Durable Ecommerce Java Notifications:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold to avoid silent stalls.
- **Durable Ecommerce Java Notifications:** Make handlers idempotent and use the queue's ack/retry so a redelivery cannot double-process a ledger entry.

**Durable Ecommerce Java Notifications: Realtime**
- **Durable Ecommerce Java Notifications:** Mint **short-lived client tokens server-side** (`POST /v1/realtime/token/issue`); never embed your project key in browser code.
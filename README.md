# Kubernetes Deployment

## Preconditions

- Docker installed and running
- Minikube installed
- kubectl installed

## Before Deploying

Create `k8s/secret.yaml` from the template below and fill in the actual values:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: cafetiria-secret
type: Opaque
stringData:
  DB_USER: <your-db-user>
  DB_PASSWORD: <your-db-password>
```

This file must be created manually before running `kubectl apply -f k8s/`.

---

## Build Docker Images

Run from the project root:

```bash
docker build -t order-service:latest ./order-service
docker build -t user-service:latest ./user-service
docker build -t notification-service:latest ./notification-service
docker build -t workflow-service:latest ./workflow-service
docker build -t gateway:latest ./gateway
```

---
## Start Minikube

```bash
minikube start --memory=4096 --cpus=2
```

## Load Images into Minikube

```bash
minikube image load order-service:latest
minikube image load user-service:latest
minikube image load notification-service:latest
minikube image load workflow-service:latest
minikube image load gateway:latest
```

---

## Enable Ingress Addon

```bash
minikube addons enable ingress
```

---

## Apply Manifests

```bash
kubectl apply -f k8s/
```

---

## Verify Cluster State

```bash
kubectl get pods
kubectl get svc
kubectl get ingress
```

All pods should show `Running` status.

---

## Reach the Gateway

In a separate terminal, run and keep running:
```bash
minikube tunnel
```

Add the following to your `/etc/hosts`:

- **Mac/Linux:** `/etc/hosts`
- **Windows:** `C:\Windows\System32\drivers\etc\hosts`
```text
127.0.0.1 cafetiria.local
```

The gateway is now reachable at `http://cafetiria.local`.
---

## Routing Table

| Incoming Request               | Forwarded To                                                   |
|--------------------------------|----------------------------------------------------------------|
| `POST /orders`                 | `POST http://order-service:8088/api/orders`                    |
| `GET /orders/{id}`             | `GET http://order-service:8088/api/orders/{id}`                |
| `PATCH /orders/{id}/status`    | `PATCH http://order-service:8088/api/orders/{id}/status`       |
| `POST /users`                  | `POST http://user-service:8080/api/users`                      |
| `GET /users/{id}`              | `GET http://user-service:8080/api/users/{id}`                  |
| `POST /workflows/create-order` | `POST http://workflow-service:8091/api/workflows/create-order` |
| `GET /workflows/{id}`          | `GET http://workflow-service:8091/api/workflows/{id}`          |

---

## Verify Workflow — Success Path

First create a user:

```bash
curl -X POST http://cafetiria.local/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Marharyta",
    "surname": "Kovalenko",
    "phone": "+380991234567",
    "birthDate": "2000-01-15",
    "email": "marharyta@example.com"
  }'
```

Start a workflow using the `id` from the response:

```bash
curl -X POST http://cafetiria.local/workflows/create-order \
  -H "Content-Type: application/json" \
  -d '{
    "ownerUserId": "<user-id-from-above>",
    "itemName": "Latte",
    "quantity": 2,
    "price": 5.99
  }'
```

Check workflow status using the `id` from the response:

```bash
curl http://cafetiria.local/workflows/<workflowId>
```

Expected state: `COMPLETED`

---

## Verify Workflow — Compensation Path

Scale down order-service to simulate a confirmation failure:

```bash
kubectl scale deployment order-service --replicas=0
```

Start a workflow:

```bash
curl -X POST http://cafetiria.local/workflows/create-order \
  -H "Content-Type: application/json" \
  -d '{
    "ownerUserId": "<valid-user-id>",
    "itemName": "Latte",
    "quantity": 2,
    "price": 5.99
  }'
```

Check workflow status:

```bash
curl http://cafetiria.local/workflows/<workflowId>
```

Expected state: `FAILED` or `COMPENSATED`. The `lastError` field contains the failure reason.

Restore order-service:

```bash
kubectl scale deployment order-service --replicas=1
```

---

## Troubleshooting

**Pod not starting:**
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

**Services not connecting:**
```bash
kubectl get svc
kubectl get endpoints
```

**Ingress not working:**
```bash
kubectl describe ingress cafetiria-ingress
```

**Not enough memory** -> increase minikube capacity like so:
```bash
minikube stop
minikube delete
minikube start --memory= --cpus=
```

### Architecture Notes

**Correlation ID propagation:**
- Every incoming request is assigned an `X-Correlation-Id` (reused if provided, generated if absent)
- The gateway forwards it to all downstream services via `HttpServletRequestWrapper`
- Each service stores it in MDC so every log line includes it automatically
- RabbitMQ messages carry it as a message header
- The notification-service reads it back from the message header

**Resiliency:**
- All outbound `RestClient` calls use Spring Retry — 3 attempts with 300ms backoff on `ResourceAccessException`
- Returns `503 Service Unavailable` when the downstream service is unreachable after all retries
- Returns `504 Gateway Timeout` when the cause is a `SocketTimeoutException`
- HTTP connect timeout: 3s, read timeout: 5s (configured via `RestClientConfig` in each service)

**Kubernetes:**
- `order-service` runs 3 replicas with `RollingUpdate` strategy
- HPA auto-scales `order-service` between 3–6 replicas based on CPU (70% threshold)
- All services have `readinessProbe`, `livenessProbe`, and resource limits

**Structured logging:**
- All services use Logback with a custom pattern that includes `correlationId` from MDC
- Every log line looks like: `10:15:32.123 [http-nio-8088-exec-1] INFO  c.m.m.o.CreateOrderUseCase [correlationId=abc-123] - Placing order...`
- To stream logs from all order-service pods at once:
```bash
kubectl logs -l app=order-service --prefix=true
```

---

### Scaling

Scale order-service manually:
```bash
kubectl scale deployment order-service --replicas=3
```

Enable and apply HPA:
```bash
minikube addons enable metrics-server
kubectl apply -f k8s/order-service-hpa.yml
kubectl get hpa
```

---

### Rollout Steps

Build and load a new image:
```bash
docker build -t order-service:v2 ./order-service
minikube image load order-service:v2
```

Trigger rolling update:
```bash
kubectl set image deployment/order-service order-service=order-service:v2
```

Watch progress:
```bash
kubectl rollout status deployment/order-service
```

Rollback:
```bash
kubectl rollout undo deployment/order-service
```

---

### Verify Correlation ID

Send a request with a custom correlation ID:
```bash
curl -X POST http://cafetiria.local/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: my-test-id-123" \
  -d '{
    "ownerUserId": "<user-id>",
    "itemName": "Latte",
    "quantity": 2,
    "price": 5.99
  }'
```

Check it appears across all pods:
```bash
kubectl logs -l app=order-service | grep "my-test-id-123"
kubectl logs -l app=gateway | grep "my-test-id-123"
kubectl logs -l app=notification-service | grep "my-test-id-123"
```

Verify 3 pods are running:
```bash
kubectl get pods -l app=order-service
```
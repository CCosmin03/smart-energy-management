import pika
import json
import time
import uuid
import random
from datetime import datetime

DEVICE_ID = "c1c29c60-c217-4a93-947d-1d2df2641678"   # ID real din DB
INTERVAL = 10  # seconds

# Conectare la RabbitMQ din Docker (prin localhost)
connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='localhost', port=5672)
)

channel = connection.channel()

EXCHANGE = "sync.exchange"

print("=== PYTHON SIMULATOR STARTED ===")

def generate_consumption():
    hour = datetime.utcnow().hour
    if hour < 6:
        return 0.1
    if hour < 12:
        return 0.3
    if hour < 18:
        return 0.5
    return 0.4


# trimitem un TEST
test_event = {
    "eventType": "TEST",
    "payload": "\"Hello from Python\""
}

channel.basic_publish(
    exchange=EXCHANGE,
    routing_key="",
    body=json.dumps(test_event)
)

print(">>> Sent TEST event")


while True:
    base = generate_consumption()
    noise = random.random() * 0.2
    value = base + noise

    measurement = {
        "deviceId": DEVICE_ID,
        "consumption": value,
        "timestamp": int(time.time() * 1000)
    }

    sync_event = {
        "eventType": "MEASUREMENT_CREATED",
        "payload": json.dumps(measurement)
    }

    channel.basic_publish(
        exchange=EXCHANGE,
        routing_key="",
        body=json.dumps(sync_event)
    )

    print(f"[SIM] Sent measurement: {value:.4f} kWh")

    time.sleep(INTERVAL)

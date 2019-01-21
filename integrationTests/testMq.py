#!/usr/bin/env python
import pika
import threading
from time import sleep
import uuid

internal_lock = threading.Lock()

hostname = 'mq.icafe.bg'
credentials = pika.PlainCredentials('admin', 'jenh563e4f')
params = pika.ConnectionParameters(hostname, credentials=credentials)
connection = pika.BlockingConnection(params)
channel = connection.channel()

channel.exchange_declare(exchange='sales', exchange_type='topic', durable=True)
myQueue = channel.queue_declare(exclusive=True)
callback_queue = myQueue.method.queue
corr_id = str(uuid.uuid4())

channel.basic_publish(exchange='sales',
                      routing_key='sales.transaction_request',
                      properties=pika.BasicProperties(
                          reply_to=callback_queue,
                          correlation_id=corr_id
                      ),
                      body="""
{ 
  "ip": "85.85.85.85",
  "recurringId": null,
  "description": "Some description of the transaction",
  "type": "initial",
  "amount": 1.0 
}
""")


def _on_response(ch, method, props, body):
    print(body)


channel.basic_consume(_on_response, no_ack=True, queue=callback_queue)
while True:
    with internal_lock:
        connection.process_data_events()
        sleep(0.1)

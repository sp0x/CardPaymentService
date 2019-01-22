#!/usr/bin/env python
import pika
import threading
from time import sleep
import uuid
import random

internal_lock = threading.Lock()

hostname = 'mq.icafe.bg'
credentials = pika.PlainCredentials('admin', 'jenh563e4f')
params = pika.ConnectionParameters(hostname, credentials=credentials)
connection = pika.BlockingConnection(params)
channel = connection.channel()

channel.exchange_declare(exchange='sales', exchange_type='topic', durable=True)
myQueue = channel.queue_declare(exclusive=True)
callback_queue = myQueue.method.queue
recurringId = str(random.randint(1000000,9999999)) + "icafe"

def send_initial():
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
      "expirationDate": "1019",
      "description": "Some description of the transaction",
      "type": "initial",
      "amount": 1
    }
    """)


def send_secondary(transactionId):
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
      "paymentId": \"""" + transactionId + """\",
      "description": "Secondary transaction",
      "type": "secondary",
      "amount": 100
    }
    """)


def _on_response(ch, method, props, body):
    import json
    body_str = body.decode('utf8')
    body_json = json.loads(body_str)
    print(body_json)
    if body_json['type']== "transactionFinished":
        if body_json['success']==True:
            pass
            #send_secondary(body_json['transactionId'])



channel.basic_consume(_on_response, no_ack=True, queue=callback_queue)

send_initial()
#send_secondary("oCmietxn8gW1jWIWo9C4ZrOxuaw=")

while True:
    with internal_lock:
        connection.process_data_events()
        sleep(0.1)

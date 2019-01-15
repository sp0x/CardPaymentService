# Service for recurring payments using FiBank's ECOMM service.

Workflow

    * This service gets a payment request(with callback urls)
    * it issues it to the bank ECOMM
    * we get a transaction id and send it back to the caller.
    * The caller comes back after making/declining/timing out the purchase and calls us to finalize.
    * We finalize the transaction payment and log it.
         
## Exchanges:
`sales`:
- sales.transaction_request - Queue for transaction requests  
Example transaction request:
```json
{ 
  "ip": "clientIp",
  "recurringId": "...", //Transaction id that you get after an initial payment. For initial transactions this isn't required.
  "description": "Some description of the transaction",
  "type": "initial|secondary", //Initial is always placed for the first transaction.
  "amount": 1.0 // the amount of the transaction (double)
}
```
Initial transactions start a recurring payment log.  
Transaction reply:
```json
{
  "url": "The url to which to redirect the user",
  "transactionId": "the id of the transaction",
  "result": "the result of the transaction",
  "resultCode": "code"
}
```
The reply is sent back to the `replyTo` header of the message!

## Configuration:
Merchant configuration is given in the file `merchant.properties`  
Required keys are:
- bank.server.url  - That's the merchant handler's url (https://mdpay-test.fibank.bg:9443/ecomm/MerchantHandler)
- keystore.file - The location of the keystore (for Windows use double forward slashes //)
- keystore.type - The type of keystore (for java it's JKS)
- keystore.password  - The password for the keystore  

Refer to the FiBank docs for more information.

General settings are in the file `settings.properties`  
- mq.host - The host to connect to for rabbit mq
- mq.user - user
- mq.pass - pass

#### Environment 
Environment variables have higher priority than settings defined in .properties files.  
`MQ_HOST` - The RabbitMQ Host to connect to  
`MQ_PORT` - RabbitMQ Port to use  
`MQ_USER` - User for the exchange  
`MQ_PASS` - Password for the exchange  
`KEYSTORE` - The location of the keystore if you're using another one  
`KEYSTORE_PASS` - The password for the keystore

#### Security notes:
You have to enable MD5 and MD5withRSA in your `/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security`
Since fibank's merchant is outdated and uses old algos.  
Look for
 - jdk.certpath.disabledAlgorithms  
 - jdk.tls.disabledAlgorithms
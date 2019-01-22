# Service for recurring payments using FiBank's ECOMM service.

Workflow  
For initial payments
* Call the `sales.transaction_request` queue
* Send the user to the resulting `url`, with a redirection for success & failure.
* Optional: continuously listen for updates for each payment request.

For secondary payments
* Call the queue with the recurringId(or transaction id from ^ )
* The response is a direct payment response with no user interaction.

         
## Running the service
To run the service you need:
 - Maven / Docker
 - Redis  
Run the `run.sh` script to test and run the service.

## Testing
To test the connectivity with this service, look at the folder `integrationTests`
  
## Exchanges:
`sales`:
- sales.transaction_request - Queue for transaction requests  

Example transaction request:  
```json
{ 
  "ip": "clientIp",
  "paymentId": "paymentId",
  "expirationDate": "0119", 
  "description": "Some description of the transaction",   
  "type": "initial|secondary",
  "amount": 1,
  "redirectOnError": "urlToRedirectOnError",
  "redirectOnOk": "urlToRedirectOnSuccess"
}
```
Arguments:
* `paymentId` - Optional, unique ID to link with the registration  
If you don't specify it initially, use the transaction id from the first transaction for this recurring payment.  
For initial transactions this is optional.  
* `expirationDate` - MMYY The date on which the recurring transaction expires.  
Initial transactions start a recurring payment log.  
Optional for secondary transactions  
* `description` -  Description or Order number to show to the client
* `type` - Whether this is the first payment request, or a secondary.
* `amount` - The amount of the transaction (int)
* `redirectOnError` - Optional, the client is redirected to this url after the transaction fails  
* `redirectOnOk` - Optional, the client is redirected to this url after the transaction goes through with success
* `currency` - 3 Digit currency code. Default is 975 (Bulgarian Leva)
  
Transaction reply:  
```json
{
  "url": "The url to which to redirect the user",
  "transactionId": "the id of the transaction",
  "result": "ok|failed|error",
  "error": "Request error message",
  "type": "transactionCreation"
}
```
The reply is sent back to the `replyTo` header of the message!  
After filling in the payment details, you would get another reply to the same `replyTo` dest.
```json
{
  "success": false, //True or false
  "type": "transactionFinished",
  "transactionId": "idOfTheTransaction", //Transaction id
  "errorStatus": "Declined", //If failed The status of the transaction
  "error": "Error message from the payment processing platform" //If failed message from backend 
  
}
```

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
- http.host - Hostname on which the callback HTTP would listen
- http.port - Port for the callback HTTP server   
Note that by default this service connects to the redis server on `localhost`

#### Environment 
Environment variables have higher priority than settings defined in .properties files.  
`MQ_HOST` - The RabbitMQ Host to connect to  
`MQ_PORT` - RabbitMQ Port to use  
`MQ_USER` - User for the exchange  
`MQ_PASS` - Password for the exchange  
`KEYSTORE` - The location of the keystore if you're using another one  
`KEYSTORE_PASS` - The password for the keystore  
`HTTP_HOST` - Host address for the callback http server  
`HTTP_PORT` - Port for the callback http server

#### Security notes:
You have to enable MD5 and MD5withRSA in your `/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security`
Since fibank's merchant is outdated and uses old algos.  
Look for
 - jdk.certpath.disabledAlgorithms  
 - jdk.tls.disabledAlgorithms
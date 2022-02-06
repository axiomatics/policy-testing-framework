curl -i --data @canCeciliaViewRecord1.json -H "X-xacml-transaction-id:MyTransId" -H "Content-type:application/xacml+json" -X POST --user pdp-user:secret http:/127.0.0.1:8081/authorize

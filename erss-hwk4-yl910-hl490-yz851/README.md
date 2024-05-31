# erss-hwk4-yl910-hl490-yz851


## Getting started

```

order:
order_id：
account_id:
stock_sym: string
type: sell buy
requested_price:
remained_shares:
executions
status: cancle/open

executions:
execution_id (主键): 
order_id: 对应的交易请求（外键，关联到transactions表）。
executed_shares:
executed_price
execution_timestamp

accounts:
string: accountid
balance: int

account_stocks:
account_id:
stock_id:
shares:


class:
XMLParser:
String type; create query cancel transactions

Response:
combine to xml and response to client

TransactionExecutor: 
match()

CreateExecutor:
QueryExecutor:
CancelExecutor:

Server:
receive()
forward()
process():调用XMLParser


```


```
<transactions>
<order	sym="SPY" amount="100" limit="145.67"/>
</transactions>

<transactions>
<cancel transactions Id="1"/>
</transactions>

<transactions>
<query transactionsId="1"/>
</transactions>

<create>
<account id="123456" balance="1000"/>
<symbol sym="SPY">
   <account id="123456">100000</account>
</symbol>
</create>


Forward:

<canceled>

<status id="1">
<open shares="100"/>
<executed shares="200" price="125" time="1519348326"/>
</status>

```
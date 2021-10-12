
## Kafka on Macos

https://medium.com/@Ankitthakur/apache-kafka-installation-on-mac-using-homebrew-a367cdefd273

```
brew install kafka
...
```
then after several minutes..

```
To start kafka:
  brew services start kafka
Or, if you don't want/need a background service you can just run:
  /usr/local/opt/kafka/bin/kafka-server-start /usr/local/etc/kafka/server.properties

```

#### Start zookeeper

```
(base) ツ zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties

```

#### Start kafka

```
(base) ツ  kafka-server-start /usr/local/etc/kafka/server.properties

```

#### Create a topic
So the instructions on [here](https://medium.com/@Ankitthakur/apache-kafka-installation-on-mac-using-homebrew-a367cdefd273) for creating a topic were
```
$ kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```
but  that was complaining with ..

```
(base) ツ  kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
Exception in thread "main" joptsimple.UnrecognizedOptionException: zookeeper is not a recognized option
```
So I looked at my
```
(base) ツ netstat -lan|grep LIST
tcp46      0      0  *.9092                 *.*                    LISTEN     
tcp46      0      0  *.58961                *.*                    LISTEN     
tcp46      0      0  *.2181                 *.*                    LISTEN   
```
And saw other than the `2181` which I knew was `zookeeper` , there was also this `9092` , so  after looking at the options, I tried removing that bad option and tried this instead and this worked ...

```
(base) ツ kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
Created topic test.
```
So that created a topic. And oh cool you can produce command line ...
```
(base) ツ kafka-console-producer --broker-list localhost:9092 --topic test
>123
>64783219
>5234783291
>4321532
>898798
>4657

```

Consumer also
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning
123
64783219
5234783291
4321532
898798
4657
```

### Kafka and AWS lambda
Hmm so according to [aws docs](https://docs.aws.amazon.com/lambda/latest/dg/with-kafka.html) you can make kafka an event stream for a Lambda. And an event would contain a topic, partition,  along with a timestamp and a batch of messages. And I see an offset too.

```
{
   "eventSource":"aws:SelfManagedKafka",
   "bootstrapServers":"b-2.demo-cluster-1.a1bcde.c1.kafka.us-east-1.amazonaws.com:9092,b-1.demo-cluster-1.a1bcde.c1.kafka.us-east-1.amazonaws.com:9092",
   "records":{
      "mytopic-0":[
         {
            "topic":"mytopic",
            "partition":"0",
            "offset":15,
            "timestamp":1545084650987,
            "timestampType":"CREATE_TIME",
            "value":"SGVsbG8sIHRoaXMgaXMgYSB0ZXN0Lg==",
            "headers":[
               {
                  "headerKey":[
                     104,
                     101,
                     97,
                     100,
                     101,
                     114,
                     86,
                     97,
                     108,
                     117,
                     101
                  ]
               }
            ]
         }
      ]
   }
}
```

* But it is not clear whether this has been set to aut-commit ?

* Actually now I'm reading there that

 > After successful processing, your Kafka topic is committed to your Kafka cluster.

 > If your Lambda function returns an error for any of the messages in a batch, Lambda retries the whole batch of messages until processing succeeds or the messages expire.

 > The maximum amount of time that Lambda lets a function run before stopping it is 14 minutes.

* So basically this implements _"at least once"_ for you. So all you have to do is make sure your lambda fail propagates and it will be retried.
* The message expiry, that can be a long time though. So I figure if the timestamp indeed is in the event given to the lambda, then the lambda perhaps can look at that.
* Or that last line about the `14 minutes` can on the outset prevent infinite processing.

### Python ...
* Here , [kafka-python](https://towardsdatascience.com/kafka-python-explained-in-10-lines-of-code-800e3e07dad1)  ,
* that uses

```
pip install kafka-python

```
* Although there is also the Confluent one [here](https://github.com/confluentinc/confluent-kafka-python)  , per [confluent](https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/python.html)  docs.

* The former seems simpler for now ..

```python
# producer.py
from kafka import KafkaProducer
import json
producer = KafkaProducer(bootstrap_servers=['localhost:9092'],
                         value_serializer=lambda x:
                         json.dumps(x).encode('utf-8'))

#
for e in range(1000):
    data = {'number' : e}
    producer.send('numtest', value=data)
    sleep(5)                        
```

```python
from kafka import KafkaConsumer
import json

def deserializer(x):
    try:
        return json.loads(x.decode('utf-8'))
    except json.JSONDecodeError:
        return x

consumer = KafkaConsumer(
    'test',
     bootstrap_servers=['localhost:9092'],
     auto_offset_reset='earliest',  # Read from what was committed last.
     enable_auto_commit=True,
     group_id='my-group',
     value_deserializer=lambda x: x)


```

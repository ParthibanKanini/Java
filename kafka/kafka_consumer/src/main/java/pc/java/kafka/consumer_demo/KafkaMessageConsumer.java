package pc.java.kafka.consumer_demo;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"topicName"}, containerFactory = "kafkaListenerContainerFactory")
public class KafkaMessageConsumer {

    @KafkaHandler(isDefault = true)
    public void onMessage(@Payload Object message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId) {
        System.out.println("Received message (Default handler): " + message+" from partition :"+partitionId);
    }

    @KafkaHandler
    public void handleMessage(Object message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId) {
      System.out.println("The Object message : " + message.toString()+" from partition :"+partitionId);
    }

} 

/*
 Any temporary failure that can be avoided by performing retries is called a Transient failure. 
 Non-transient failures are inevitable and occur due to a bug in application logic or a deserialization exception. 

 A listener container is a container that contains a consumer or listener of Kafka messages.

 */

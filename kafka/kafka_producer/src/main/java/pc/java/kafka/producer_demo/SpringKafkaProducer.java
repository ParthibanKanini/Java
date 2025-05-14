package pc.java.kafka.producer_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Qualifier("SpringKafkaProducer")
public class SpringKafkaProducer implements KafkaProducer {

    // Message Serializations are configured in application.properties        
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendMessage(String topic, Object message) {
        System.out.println("Sending message using Spring Kafka template.");
        System.out.println("Topic:" + topic+" :: Message:"+message );
        kafkaTemplate.send(topic, message);
    }

}

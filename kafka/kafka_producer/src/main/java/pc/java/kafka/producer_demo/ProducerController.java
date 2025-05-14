package pc.java.kafka.producer_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class ProducerController {

    @Autowired
    // @Qualifier("SpringKafkaProducer") // TODO: Hardcoding the impl to be used.
    @Qualifier("ApacheKafkaProducer") 
    private KafkaProducer producer;

    @PostMapping("/send/{topic}")
    public String publishMessage(@PathVariable("topic") String topic, @RequestBody String message) {
        producer.sendMessage(topic, message);
        return "Message sent to topic: " + topic+" is "+message;
    }

}


package pc.java.kafka.producer_demo;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.common.config.SaslConfigs.*;

@Component
@Qualifier("ApacheKafkaProducer")
public class ApacheKafkaProducer implements pc.java.kafka.producer_demo.KafkaProducer {

    @Override
    public void sendMessage(String topic, Object message) {
        final Properties props = new Properties() {{
            // User-specific properties that you must set
            put(BOOTSTRAP_SERVERS_CONFIG, "<BOOTSTRAP SERVERS>");
            put(SASL_JAAS_CONFIG,         "org.apache.kafka.common.security.plain.PlainLoginModule required username='<CLUSTER API KEY>' password='<CLUSTER API SECRET>';");

            // Fixed properties
            put(KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getCanonicalName());
            put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            put(ACKS_CONFIG,                   "all");
            put(SECURITY_PROTOCOL_CONFIG,      "SASL_SSL");
            put(SASL_MECHANISM,                "PLAIN");
        }};
        try (final Producer<String, String> producer = new KafkaProducer<String,Object>(props)) {
        producer.send(
                new ProducerRecord<>(topic, null, message.toString()),
                (event, ex) -> {
                    if (ex != null)
                    ex.printStackTrace();
                    else
                    System.out.printf("Produced event to topic %s: value = %s%n", topic, message);
                });
                    }
        String[] users = {"eabara", "jsmith", "sgarcia", "jbernard", "htanaka", "awalther"};
        String[] items = {"book", "alarm clock", "t-shirts", "gift card", "batteries"};
        try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
            final Random rnd = new Random();
            final int numMessages = 10;
            // for (int i = 0; i < numMessages; i++) {
            //     String user = users[rnd.nextInt(users.length)];
            //     String item = items[rnd.nextInt(items.length)];

            //     producer.send(
            //             new ProducerRecord<>(topic, user, item),
            //             (event, ex) -> {
            //                 if (ex != null)
            //                     ex.printStackTrace();
            //                 else
            //                     System.out.printf("Produced event to topic %s: key = %-10s value = %s%n", topic, user, item);
            //             });
            // }
            System.out.printf("%s events were produced to topic %s%n", numMessages, topic);
        }
    }
}

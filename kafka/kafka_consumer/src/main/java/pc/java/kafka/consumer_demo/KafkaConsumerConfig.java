package pc.java.kafka.consumer_demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.ClassUtils;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@PropertySource(value = { "classpath:application.properties" })
public class KafkaConsumerConfig {

    @Value("${kafka.topic.name}")
    private String topicName;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers = "localhost:9092";

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        // ConcurrentKafkaListenerContainerFactory: Supports concurrent message
        // consumption by creating multiple KafkaMessageListenerContainer instances.
        // KafkaListenerContainerFactory: Basic implementation for single-threaded
        // message consumption.
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1); // TODO: Read from prop.
        // Acknowledge each record after it is processed.
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        
        //Container Error Handlers
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
            // Retry configuration: Retry failed message 3 times with a 1-second interval
            // recover after 3 failures, with no back off - e.g. send to a dead-letter topic
            System.out.println("Failed message!!!! " + record.value());
        },new FixedBackOff(1000L, 3)));
        // TODO: How to handle DLT?
        // Define exceptions that should bypass retries and optimize error handling by
        // directly sending to dead-letter topic.
        // deh.addNotRetryableExceptions(Exception.class);
        return factory;
    } 

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        // Configure consumer properties like bootstrap servers, group ID, and
        // deserializers
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ClassUtils.resolveClassName(keyDeserializer, null));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ClassUtils.resolveClassName(valueDeserializer, null));
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Listener Error Handler that handles exceptions thrown by the Kafka consumer.
     * When message cannot be processed succesfully even after retires, the message is sent to deal-letter topic.
     * @param recoverer
     * @return
     */
    @Bean
    public KafkaListenerErrorHandler eh(DeadLetterPublishingRecoverer recoverer) {
        return (msg, ex) -> {
            if (msg.getHeaders().get(KafkaHeaders.DELIVERY_ATTEMPT, Integer.class) > 9) {
                recoverer.accept(msg.getHeaders().get(KafkaHeaders.RAW_DATA, ConsumerRecord.class), ex);
                return "FAILED";
            }
            throw ex;
        };
    } 

    /**
     * Handles exceptions thrown by the Kafka consumer.
     * When message cannot be processed succesfully even after retires, the message
     * is sent to deal-letter topic. Ensures failed messages are not lost.
     * By default the failed messages sent to <original-topic-name>-dlt.
     * 
     * @param kafkaTemplate
     * @return
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // Dead-letter publishing recoverer shall send failed messages to a dead-letter
        // topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        DefaultErrorHandler deh = new DefaultErrorHandler(recoverer, backOff);
        // Acknowledge the message after it is handled
        deh.setAckAfterHandle(true);

        return deh;
    }

}

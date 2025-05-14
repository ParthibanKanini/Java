package pc.java.kafka.consumer_demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.util.ClassUtils;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@PropertySource(value={"classpath:application.properties"})
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
    public KafkaMessageListenerContainer<?, ?> kafkaMessageListenerContainer() {
        // Create container properties
        ContainerProperties containerProperties = new ContainerProperties(topicName);
        // containerProperties.setGroupId(groupId);
        // ensures that each record is acknowledged after it is processed.
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        // Create KafkaMessageListenerContainer that shall receive all messages from all topics
        // and partitions on a single thread. 
        KafkaMessageListenerContainer<?,?> container = new KafkaMessageListenerContainer<>(consumerFactory(), containerProperties);
        container.setupMessageListener(new KafkaConsumer());
        return container;
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
        
        // Retry configuration: Retry failed message 3 times with a 1-second interval
        FixedBackOff backOff = new FixedBackOff(1000L, 3);
               
        DefaultErrorHandler deh = new DefaultErrorHandler(recoverer, backOff);
        // Acknowledge the message after it is handled
        deh.setAckAfterHandle(true);
        // Define exceptions that should bypass retries and optimize error handling by directly sending to dead-letter topic. 
        //deh.addNotRetryableExceptions(Exception.class);
        return deh;
    } 

    /*
     * @Bean("custom-container")
     * public ConcurrentKafkaListenerContainerFactory<String, Stock>
     * kafkaListenerContainerFactory(){
     * ConcurrentKafkaListenerContainerFactory<String, Stock>
     * kafkaListenerContainerFactory = new
     * ConcurrentKafkaListenerContainerFactory<>();
     * kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
     * kafkaListenerContainerFactory.setConcurrency(2);
     * kafkaListenerContainerFactory.setCommonErrorHandler(defaultErrorHandler());
     * // uncomment to enable blocking retries.
     * kafkaListenerContainerFactory.getContainerProperties().setAckMode(
     * ContainerProperties.AckMode.RECORD);
     * return kafkaListenerContainerFactory;
     * }
     */


}

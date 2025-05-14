package pc.java.kafka.producer_demo;

public interface KafkaProducer {
    void sendMessage(String topic, Object message);
}

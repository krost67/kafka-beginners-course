package com.podlasenko.kafka.utils;

import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@UtilityClass
public class KafkaUtils {
    public static final String KAFKA_BOOTSTRAP_SERVER = "127.0.0.1:9092";
    public static final String KAFKA_TOPIC_NAME = "demo_java";
    public static final String KAFKA_CONSUMER_FIRST_GROUP_ID = "first-group";

    // Wikimedia variables
    public static final String WIKIMEDIA_TOPIC_NAME = "wikimedia.recentchange";
    public static final String WIKIMEDIA_URL = "https://stream.wikimedia.org/v2/stream/recentchange";
    // OpenSearch variables
    public static final String OPEN_SEARCH_URL = "http://localhost:9200";
    public static final String KAFKA_CONSUMER_OPENSEARCH_GROUP_ID = "consumer-opensearch-demo";

    /**
     * create Producer config
     */
    public static Properties getKafkaProducerProperties() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return properties;
    }

    /**
     * create Consumer config
     */
    public static Properties getKafkaConsumerProperties(String bootstrapServer, String groupId) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Value can be: none/earliest/latest
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return properties;
    }


}

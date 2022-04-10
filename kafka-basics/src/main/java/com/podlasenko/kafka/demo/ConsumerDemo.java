package com.podlasenko.kafka.demo;

import com.podlasenko.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.podlasenko.kafka.utils.KafkaUtils.KAFKA_TOPIC_NAME;

/**
 * Demo for pooling records from Kafka topic by Consumer
 */
@Slf4j
public class ConsumerDemo {

    public static void main(String[] args) {
        log.info("Consumer demo starts....");

        // Create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                KafkaUtils.getKafkaConsumerProperties());

        // Subscribe to kafka topic(s)
        consumer.subscribe(Collections.singleton(KAFKA_TOPIC_NAME));

        // poll records
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            records.forEach(entry ->
                    log.info("Key: " + entry.key() + "; Value: " + entry.value() + ";")
            );
        }
    }
}

package com.podlasenko.kafka.demo;

import com.podlasenko.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static com.podlasenko.kafka.utils.KafkaUtils.KAFKA_TOPIC_NAME;

/**
 * Demo for sending record to Kafka topic by Producer
 */
@Slf4j
public class ProducerDemo {

    public static void main(String[] args) {
        log.info("Producer demo starts....");

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(
                KafkaUtils.getKafkaProducerProperties());

        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                KAFKA_TOPIC_NAME,
                "Hello from java!");

        // send the data - asynchronous
        producer.send(producerRecord);

        // flush data - synchronous
        producer.flush();
        // flush and close
        producer.close();

        log.info("Producer demo completed.");
    }
}

package com.podlasenko.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;

public class ProducerDemoWithCallback {
    private final static Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());

    public static void main(String[] args) {
        log.info("Producer demo with callback starts....");

        // create Producer Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                "demo_java",
                "Hello from java!");

        // send the data with callback - asynchronous
        producer.send(producerRecord, (recordMetadata, e) -> {
            // executes every time a record is successfully sent or exception is thrown
            if (Objects.isNull(e)) {
                // the record was successfully sent
                log.info("Received new metadata: \n" +
                        "Topic: " + recordMetadata.topic() + "\n" +
                        "Partition: " + recordMetadata.partition() + "\n" +
                        "Offset: " + recordMetadata.offset() + "\n" +
                        "DateTime: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(recordMetadata.timestamp()),
                        TimeZone.getDefault().toZoneId()));
            } else {
                log.error("Error while processing record: " + e);
            }
        });

        // flush data - synchronous
        producer.flush();

        // flush and close
        producer.close();

        log.info("Producer demo with callback completed.");
    }
}

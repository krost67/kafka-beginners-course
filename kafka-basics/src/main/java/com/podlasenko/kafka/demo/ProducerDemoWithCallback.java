package com.podlasenko.kafka.demo;

import com.podlasenko.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;

import static com.podlasenko.kafka.utils.KafkaUtils.KAFKA_TOPIC_NAME;

/**
 * Demo for sending records to Kafka topic by Producer
 * Process callback
 */
@Slf4j
public class ProducerDemoWithCallback {

    public static void main(String[] args) {
        log.info("Producer demo with callback starts....");

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(
                KafkaUtils.getKafkaProducerProperties());

        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                KAFKA_TOPIC_NAME,
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

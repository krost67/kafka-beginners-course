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
 * Demo for sending records with keys to Kafka topic by Producer
 * Process callback
 */
@Slf4j
public class ProducerDemoKeys {

    public static void main(String[] args) {
        log.info("Producer demo with keys starts....");

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(
                KafkaUtils.getKafkaProducerProperties());

        // send the data with callback - asynchronous
        for (int i = 0; i < 10; i++) {
            String recordKey = "id_" + i;
            String recordValue = "message " + i;

            // create a producer record
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                    KAFKA_TOPIC_NAME,
                    recordKey,
                    recordValue);

            producer.send(producerRecord, (recordMetadata, e) -> {
                // executes every time a record is successfully sent or exception is thrown
                if (Objects.isNull(e)) {
                    // the record was successfully sent
                    log.info("----- Received new metadata ----- \n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Record key: " + producerRecord.key() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "DateTime: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(recordMetadata.timestamp()),
                            TimeZone.getDefault().toZoneId()));
                } else {
                    log.error("Error while processing record: " + e);
                }
            });
        }

        // flush and close
        producer.close();

        log.info("Producer demo with keys completed.");
    }
}

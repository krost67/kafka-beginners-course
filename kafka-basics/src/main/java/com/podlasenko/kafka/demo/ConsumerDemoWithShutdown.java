package com.podlasenko.kafka.demo;

import com.podlasenko.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Demo for pooling records from Kafka topic with shutdown by Consumer
 */
@Slf4j
public class ConsumerDemoWithShutdown {

    public static void main(String[] args) {
        log.info("Consumer demo with shutdown starts....");

        // Create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                KafkaUtils.getKafkaConsumerProperties());

        // get a reference to a current thread
        final Thread mainThread = Thread.currentThread();
        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detecting a shutdown. Let's exit by calling consumer.wakeup() ...");
            consumer.wakeup();

            // join main thread to allow the execution of the code in the main thread
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                log.error("Error while joining main method: ", e);
            }
        }));

        // Subscribe to kafka topic(s)
        consumer.subscribe(Collections.singleton("demo_java"));

        try (consumer) {
            // poll records
            while (true) {
                log.info("Pooling...");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                records.forEach(entry ->
                        log.info("Key: " + entry.key() + "; Value: " + entry.value() + ";")
                );
            }
        } catch (WakeupException e) {
            log.info("Wakeup exception.");
            // ignore this as this is an expected exception when closing consumer
        } catch (Exception e) {
            log.error("Error while pooling records: ", e);
        }

        log.info("Consumer is now gracefully closed.");
    }
}

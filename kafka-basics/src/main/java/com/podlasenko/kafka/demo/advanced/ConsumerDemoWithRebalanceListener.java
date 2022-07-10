package com.podlasenko.kafka.demo.advanced;

import com.podlasenko.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.podlasenko.kafka.utils.KafkaUtils.*;

/**
 * Demo for pooling records from Kafka topic by Consumer
 * with shutdown
 * we disable auto commit (otherwise we wouldn't need a rebalance listener)
 * <p>
 * on every message being successfully synchronously processed,
 * we call listener.addOffsetToTrack(record.topic(), record.partition(), record.offset());
 * which allows us to track how far we've been processing in our consumer
 * <p>
 * when we're done with a batch we call consumer.commitAsync();
 * to commit offsets without blocking our consumer loop.
 * <p>
 * on the consumer shutdown, we finally call again consumer.commitSync(listener.getCurrentOffsets());
 * to commit one last time based on how far we've read before closing the consumer.
 */
@Slf4j
public class ConsumerDemoWithRebalanceListener {
    public static void main(String[] args) {
        log.info("I am a Kafka Consumer with a Rebalance");

        // create consumer configs
        Properties kafkaConfig = KafkaUtils.getKafkaConsumerProperties(KAFKA_BOOTSTRAP_SERVER, KAFKA_CONSUMER_FIRST_GROUP_ID);
        // we disable Auto Commit of offsets
        kafkaConfig.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaConfig);
        ConsumerRebalanceListenerImpl listener = new ConsumerRebalanceListenerImpl(consumer);

        // get a reference to the current thread
        final Thread mainThread = Thread.currentThread();
        // adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
            consumer.wakeup();
            // join the main thread to allow the execution of the code in the main thread
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        try {
            // subscribe consumer to our topic(s)
            consumer.subscribe(Collections.singleton(KAFKA_TOPIC_NAME), listener);
            // poll for new data
            while (true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                    // we track the offset we have been committed in the listener
                    listener.addOffsetToTrack(record.topic(), record.partition(), record.offset());
                }
                // We commitAsync as we have processed all data, and we don't want to block until the next .poll() call
                consumer.commitAsync();
            }
        } catch (WakeupException e) {
            log.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        } catch (Exception e) {
            log.error("Unexpected exception", e);
        } finally {
            try {
                consumer.commitSync(listener.getCurrentOffsets()); // we must commit the offsets synchronously here
            } finally {
                consumer.close();
                log.info("The consumer is now gracefully closed.");
            }
        }
    }
}

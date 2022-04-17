package com.podlasenko.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.podlasenko.kafka.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.podlasenko.kafka.utils.KafkaUtils.WIKIMEDIA_TOPIC_NAME;
import static com.podlasenko.kafka.utils.KafkaUtils.WIKIMEDIA_URL;

/**
 * Demo for getting Wikimedia events and produce its into Kafka cluster
 */
@Slf4j
public class WikimediaChangesProducer {

    public static void main(String[] args) throws InterruptedException {
        Properties producerConfig = KafkaUtils.getKafkaProducerProperties();
        // set safe producer config (only for Kafka version <= 2.8 )
        producerConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfig);

        EventHandler eventHandler = new WikimediaChangeHandler(producer, WIKIMEDIA_TOPIC_NAME);
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(WIKIMEDIA_URL));
        EventSource eventSource = builder.build();

        // start the producer in another thread
        eventSource.start();

        // we will produce for 10 minutes
        TimeUnit.MINUTES.sleep(10);
    }
}

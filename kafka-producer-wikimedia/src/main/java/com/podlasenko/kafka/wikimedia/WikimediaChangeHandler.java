package com.podlasenko.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class WikimediaChangeHandler implements EventHandler {
    private final KafkaProducer<String, String> kafkaProducer;
    private final String kafkaTopic;

    public WikimediaChangeHandler(KafkaProducer<String, String> kafkaProducer,
                                  String kafkaTopic) {
        this.kafkaProducer = kafkaProducer;
        this.kafkaTopic = kafkaTopic;
    }

    @Override
    public void onOpen() {
        log.info("Open Wikimedia change handler");
    }

    @Override
    public void onClosed() {
        log.info("Close Wikimedia change handler");
        kafkaProducer.close();
    }

    @Override
    public void onMessage(String s, MessageEvent messageEvent) {
        log.info(messageEvent.getData());
        // asynchronous
        kafkaProducer.send(new ProducerRecord<>(kafkaTopic, messageEvent.getData()));
    }

    @Override
    public void onComment(String s) throws Exception {
        // no implement
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error while stream reading ", throwable);
    }
}

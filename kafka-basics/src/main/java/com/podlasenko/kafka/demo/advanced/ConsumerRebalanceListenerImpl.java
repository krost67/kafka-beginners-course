package com.podlasenko.kafka.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * we track internally in the class the offsets of how far we have consumers using currentOffsets
 *
 * in the function addOffsetToTrack we make sure to increment the offset by 1 in order to commit the position properly
 *
 * we use a synchronous consumer.commitSync call in onPartitionsRevoked
 * to block until the offsets are successfully committed
 *
 */
@Slf4j
public class ConsumerRebalanceListenerImpl implements ConsumerRebalanceListener {

    private final KafkaConsumer<String, String> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public ConsumerRebalanceListenerImpl(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    public void addOffsetToTrack(String topic, int partition, long offset){
        currentOffsets.put(
                new TopicPartition(topic, partition),
                new OffsetAndMetadata(offset + 1, null));
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        log.info("onPartitionsRevoked callback triggered");
        log.info("Committing offsets: " + currentOffsets);

        consumer.commitSync(currentOffsets);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        log.info("onPartitionsAssigned callback triggered");
    }

    // this is used when we shut down our consumer gracefully
    public Map<TopicPartition, OffsetAndMetadata> getCurrentOffsets() {
        return currentOffsets;
    }
}

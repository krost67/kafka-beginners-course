package com.podlasenko.kafka.opensearch;

import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

import static com.podlasenko.kafka.opensearch.OpenSearchClientUtil.createOpenSearchClient;
import static com.podlasenko.kafka.utils.KafkaUtils.*;

/**
 * Demo for consuming Wikimedia events from Kafka and sending into OpenSearch
 */
@Slf4j
public class OpenSearchConsumer {
    private static final String WIKIMEDIA_INDEX_NAME = "wikimedia";

    public static void main(String[] args) throws IOException {
        // create an OpenSearch Client
        RestHighLevelClient openSearchClient = createOpenSearchClient();
        // create Kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                getKafkaConsumerProperties(KAFKA_BOOTSTRAP_SERVER, KAFKA_CONSUMER_OPENSEARCH_GROUP_ID)
        );

        // Subscribe to Wikimedia kafka topic
        consumer.subscribe(Collections.singleton(WIKIMEDIA_TOPIC_NAME));

        try (openSearchClient; consumer) {
            // create the index in OpenSearch if it doesn't exist already
            createWikimediaIndex(openSearchClient);
            // consuming data
            while (true) {
                ConsumerRecords<String, String> records = consumeWikimediaData(consumer);
                // create a bulk request to improve performance
                BulkRequest bulkRequest = new BulkRequest();

                for (ConsumerRecord<String, String> record : records) {
                    // two strategies to avoid duplicates
                    // 1. set your own id
                    // String id = record.topic() + "_" + record.partition() + "_" + record.offset();
                    // 2. (better way) set id from record if exist
                    String id = extractId(record.value());
                    IndexRequest request = new IndexRequest(WIKIMEDIA_INDEX_NAME)
                            .id(id)
                            .source(record.value(), XContentType.JSON);
                    bulkRequest.add(request);
                }

                sendBulkToOpenSearch(openSearchClient, bulkRequest);
            }
        }
    }

    private static void createWikimediaIndex(RestHighLevelClient openSearchClient) throws IOException {
        if (openSearchClient.indices().exists(new GetIndexRequest(WIKIMEDIA_INDEX_NAME), RequestOptions.DEFAULT)) {
            log.info("Wikimedia index already exists");
        } else {
            openSearchClient.indices().create(new CreateIndexRequest(WIKIMEDIA_INDEX_NAME), RequestOptions.DEFAULT);
            log.info("Wikimedia index has been created");
        }
    }

    private static ConsumerRecords<String, String> consumeWikimediaData(KafkaConsumer<String, String> consumer) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));
        int recordsCount = records.count();
        log.info("Received " + recordsCount + " record(s)");

        return records;
    }

    private static String extractId(String recordValue) {
        // using gson library to parse JSON
        return JsonParser.parseString(recordValue)
                .getAsJsonObject()
                .get("meta")
                .getAsJsonObject()
                .get("id")
                .getAsString();
    }

    private static void sendBulkToOpenSearch(RestHighLevelClient openSearchClient,
                                             BulkRequest bulkRequest) {
        try {
            if (bulkRequest.numberOfActions() > 0) {
                BulkResponse response = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                log.info("Inserted " + response.getItems().length + " record(s) into OpenSearch");
            }
        } catch (Exception e) {
            //TODO fix
        }
    }
}

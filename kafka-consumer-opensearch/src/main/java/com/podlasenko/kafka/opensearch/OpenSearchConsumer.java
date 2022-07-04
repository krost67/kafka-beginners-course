package com.podlasenko.kafka.opensearch;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;

import java.io.IOException;

import static com.podlasenko.kafka.opensearch.OpenSearchClientUtil.createOpenSearchClient;

/**
 * Demo for consuming Wikimedia events from Kafka and sending into OpenSearch
 */
@Slf4j
public class OpenSearchConsumer {
    private static final String WIKIMEDIA_INDEX_NAME = "wikimedia";

    public static void main(String[] args) throws IOException {
        // create an OpenSearch Client
        RestHighLevelClient openSearchClient = createOpenSearchClient();

        try (openSearchClient) {
            // create the index in OpenSearch if it doesn't exist already
            createWikimediaIndex(openSearchClient);

        }
    }

    public static void createWikimediaIndex(RestHighLevelClient openSearchClient) throws IOException {
        if (openSearchClient.indices().exists(new GetIndexRequest(WIKIMEDIA_INDEX_NAME), RequestOptions.DEFAULT)) {
            log.info("Wikimedia index already exists");
        } else {
            openSearchClient.indices().create(new CreateIndexRequest(WIKIMEDIA_INDEX_NAME), RequestOptions.DEFAULT);
            log.info("Wikimedia index has been created");
        }
    }
}

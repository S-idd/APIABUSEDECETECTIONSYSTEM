package com.apiabusedecetionsystem;

import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KongLogProcessor {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        System.out.println(">>> Flink KongLogProcessor started.");

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "flink-kong-group");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
                "kong-logs",
                new SimpleStringSchema(),
                props
        );

        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(
                "abuse-alerts",
                new SimpleStringSchema(),
                props
        );

        System.out.println(">>> Connected to Kafka topic: kong-logs");

        ObjectMapper mapper = new ObjectMapper();

        DataStream<String> alerts = env
            .addSource(consumer)
            .map(json -> {
                try {
                    Map<String, Object> logMap = mapper.readValue(json, Map.class);
                    int status = (int) logMap.getOrDefault("status", 0);
                    int latency = (int) logMap.getOrDefault("latency", 0);
                    String ip = (String) logMap.getOrDefault("client_ip", "unknown");

                    if (status == 429) {
                        return "ABUSE DETECTED [RateLimitExceeded] from IP " + ip + ", latency=" + latency;
                    } else if (latency > 1000) {
                        return "ABUSE DETECTED [HighLatency] from IP " + ip + ", latency=" + latency;
                    } else {
                        return null; // not abusive
                    }

                } catch (Exception e) {
                    return "[ERROR] Failed to parse log: " + json;
                }
            })
            .filter(msg -> msg != null);

        alerts.print(); // Optional: for debugging
        alerts.addSink(producer); // Send to Kafka topic: abuse-alerts

        env.execute("Kong Log Abuse Detection");
    }
}

package org.autong.service.queue;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.autong.annotation.Runnable;
import org.autong.config.Settings;
import org.autong.service.AbstractClient;
import org.autong.service.queue.model.Request;
import org.autong.service.queue.model.Response;
import org.autong.util.DataUtil;

/**
 * KafkaClient class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
public class KafkaClient extends AbstractClient<KafkaClient, Request, Response> {

  /**
   * Constructor for KafkaClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   * @since 1.0.5
   */
  public KafkaClient(Settings settings, JsonObject request) {
    super(settings, DataUtil.toObject(request, Request.class));
  }

  /** {@inheritDoc} */
  @Override
  public Class<Request> getRequestType() {
    return Request.class;
  }

  /** {@inheritDoc} */
  @Override
  public Class<Response> getResponseType() {
    return Response.class;
  }

  /** {@inheritDoc} */
  @Override
  public Request mergeRequest(Request request) {
    return this.mergeRequest(request, Request.builder().build());
  }

  /** {@inheritDoc} */
  @Override
  public Request mergeRequest(JsonObject request) {
    return this.mergeRequest(DataUtil.toObject(request, Request.class));
  }

  /** {@inheritDoc} */
  @Override
  @Runnable
  public Response resolve(Request request) {
    Response response;
    switch (request.getType()) {
      case PUBLISH -> response = this.publish(request);
      case RECEIVE -> response = this.receive(request);
      default -> throw new InvalidParameterException();
    }
    return response;
  }

  // region private methods

  private Response publish(Request request) {
    Response response = Response.builder().build();
    JsonObject responseBody = new JsonObject();

    try (Producer<String, String> producer = new KafkaProducer<>(request.getProducerProperties())) {
      final ProducerRecord<String, String> producerRecord = getProducerRecord(request);
      if (!request.getHeaders().isEmpty()) {
        request
            .getHeaders()
            .forEach((key, value) -> producerRecord.headers().add(key, value.getBytes()));
      }
      producer.send(producerRecord);
      responseBody.addProperty("status", "success");
    } catch (Exception ex) {
      responseBody.addProperty("status", "failed");
      response.setException(ex.getMessage());
    }
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(responseBody));
    return response;
  }

  private static ProducerRecord<String, String> getProducerRecord(Request request) {
    ProducerRecord<String, String> producerRecord;
    if (request.getPartitionIdList() == null || request.getPartitionIdList().isEmpty()) {

      producerRecord =
          new ProducerRecord<>(request.getTopic(), request.getKey(), request.getValue());

    } else {

      producerRecord =
          new ProducerRecord<>(
              request.getTopic(),
              request.getPartitionIdList().get(0),
              request.getKey(),
              request.getValue());
    }
    return producerRecord;
  }

  private Response receive(Request request) {
    Response response = Response.builder().build();
    JsonObject responseBody = new JsonObject();

    try (KafkaConsumer<String, String> consumer =
        new KafkaConsumer<>(request.getConsumerProperties())) {
      ArrayList<String> consumerValues = new ArrayList<>();
      Map<String, String> consumerHeaders = new HashMap<>();
      consumer.subscribe(Collections.singleton(request.getTopic()));
      if (request.getPartitionIdList() == null) {
        request.setPartitionIdList(new ArrayList<>());
      }
      if (request.getPartitionIdList().isEmpty()) {

        consumer
            .partitionsFor(request.getTopic())
            .forEach(partitionInfo -> request.getPartitionIdList().add(partitionInfo.partition()));
      }

      for (int partitionId : request.getPartitionIdList()) {
        consumer.poll(Duration.ofMillis(request.getPollTime()));
        long offset = consumer.position(new TopicPartition(request.getTopic(), partitionId));
        consumer.seek(
            new TopicPartition(request.getTopic(), partitionId),
            offset - request.getRecordCounts());
        int recordCount = 0;
        int retry = 0;
        int maxRetry = 3;
        while (recordCount < request.getRecordCounts() && retry < maxRetry) {
          ConsumerRecords<String, String> consumerRecords =
              consumer.poll(Duration.ofMillis(request.getPollTime()));
          recordCount += consumerRecords.count();
          retry++;
          for (ConsumerRecord<String, String> records : consumerRecords) {

            if (StringUtils.isEmpty(request.getValue())
                || records.value().contains(request.getValue())) {
              consumerValues.add(records.value());
            }
            if (records.headers() != null && records.headers().iterator().hasNext()) {
              for (Header header : records.headers()) {
                consumerHeaders.put(
                    header.key(), new String(header.value(), StandardCharsets.UTF_8));
              }
            }
          }
        }
      }
      responseBody.addProperty("status", "success");
      responseBody.add("message", DataUtil.toJsonArray(consumerValues));
      responseBody.add("headers", DataUtil.toJsonObject(consumerHeaders));
    } catch (Exception ex) {
      responseBody.addProperty("status", "failed");
      response.setException(ex.getMessage());
    }
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(responseBody));
    return response;
  }
}

package org.autong.service.queue.model;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.Builder;
import lombok.Data;
import org.autong.enums.QueueRequestType;

/**
 * Request class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
@Data
@Builder
public class Request {

  private QueueRequestType type;

  // Tibco
  private String queueName;
  private String message;
  @Builder.Default private Map<String, String> headers = new HashMap<>();
  @Builder.Default private JsonObject params = new JsonObject();
  private String username;
  private String password;
  private String url;
  @Builder.Default private Integer connAttemptCount = 10;
  @Builder.Default private Integer connAttemptDelay = 2000;
  @Builder.Default private Integer connAttemptTimeout = 10000;

  // Kafka
  private String topic;
  private String key;
  private String value;
  private Integer recordCounts;
  @Builder.Default private List<Integer> partitionIdList = new ArrayList<>();
  @Builder.Default private Integer pollTime = 10000;
  private Properties producerProperties;
  private Properties consumerProperties;
}

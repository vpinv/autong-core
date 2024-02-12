package org.autong.service;

import com.google.gson.JsonObject;
import java.security.InvalidParameterException;
import org.apache.commons.lang3.NotImplementedException;
import org.autong.config.Settings;
import org.autong.enums.ClientType;

/**
 * ClientFactory class.
 *
 * @version 1.0.3
 * @since 1.0.3
 */
public class ClientFactory {
  private ClientFactory() {}

  /**
   * getClient.
   *
   * @param clientType a {@link org.autong.enums.ClientType} object
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   * @return a {@link org.autong.service.Client} object
   * @since 1.0.4
   */
  public static Client<? extends AbstractClient<?, ?, ?>, ?, ?> getClient(
      ClientType clientType, Settings settings, JsonObject request) {
    Client<? extends AbstractClient<?, ?, ?>, ?, ?> client;
    switch (clientType) {
      case REST, REST_RESTASSURED -> client =
          new org.autong.service.rest.RestAssuredClient(settings, request);
      case REST_UNIREST -> client = new org.autong.service.rest.UnirestClient(settings, request);
      case SOAP, SOAP_RESTASSURED -> client =
          new org.autong.service.soap.RestAssuredClient(settings, request);
      case DATABASE_ORACLE -> client =
          new org.autong.service.database.OracleClient(settings, request);
      case QUEUE_KAFKA -> client = new org.autong.service.queue.KafkaClient(settings, request);
      case QUEUE_TIBCO -> client = new org.autong.service.queue.TibcoClient(settings, request);
      case CACHE_REDIS -> client = new org.autong.service.cache.RedisClient(settings, request);
      case DATABASE_CASSANDRA,
          DATABASE_MONGODB,
          DATABASE_POSTGRES -> throw new NotImplementedException();
      default -> throw new InvalidParameterException(
          "Unsupported client type: " + clientType.name());
    }
    return client;
  }

  /**
   * getClient.
   *
   * @param clientType a {@link java.lang.String} object
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   * @return a {@link org.autong.service.Client} object
   * @since 1.0.5
   */
  public static Client<? extends AbstractClient<?, ?, ?>, ?, ?> getClient(
      String clientType, Settings settings, JsonObject request) {
    return getClient(ClientType.valueOf(clientType), settings, request);
  }
}

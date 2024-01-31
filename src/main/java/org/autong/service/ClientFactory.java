package org.autong.service;

import com.google.gson.JsonObject;
import java.security.InvalidParameterException;
import org.autong.config.Settings;
import org.autong.enums.ClientType;
import org.autong.service.rest.RestAssuredClient;

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
  @SuppressWarnings("unchecked")
  public static Client getClient(ClientType clientType, Settings settings, JsonObject request) {
    Client client;
    switch (clientType) {
      case REST -> client = new RestAssuredClient(settings, request);
      case SOAP -> client = new org.autong.service.soap.RestAssuredClient(settings, request);
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
   */
  public static Client getClient(String clientType, Settings settings, JsonObject request) {
    return getClient(ClientType.valueOf(clientType), settings, request);
  }
}

package org.autong.service;

import java.security.InvalidParameterException;
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
   * @return a {@link org.autong.service.Client} object
   */
  public static Client getClient(ClientType clientType) {
    Client client;
    switch (clientType) {
      case REST -> client = new RestAssuredClient();
      case SOAP -> client = new org.autong.service.soap.RestAssuredClient();
      default -> throw new InvalidParameterException(
          "Unsupported client type: " + clientType.name());
    }
    return client;
  }
}

package org.autong.service.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.autong.config.Settings;
import org.autong.service.Client;
import org.autong.service.ClientFactory;

/**
 * Service class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@SuppressWarnings("rawtypes")
@Getter
public class Service {

  private final Client client;
  private final Map<String, JsonObject> requestMap;

  /**
   * Constructor for Service.
   *
   * @param serviceInfo a {@link com.google.gson.JsonObject} object
   * @since 1.0.5
   */
  public Service(JsonObject serviceInfo) {
    this.client =
        ClientFactory.getClient(
            serviceInfo.getAsJsonObject().get("client").getAsString(),
            Settings.builder().build(),
            serviceInfo);
    requestMap = new HashMap<>();
    this.initializeServiceMap(serviceInfo);
  }

  /**
   * getRequest.
   *
   * @param methodName a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonObject} object
   */
  public JsonObject getRequest(String methodName) {
    return this.getRequestMap().get(methodName);
  }

  // region private methods

  private void initializeServiceMap(JsonObject serviceInfo) {
    for (JsonElement method : serviceInfo.get("methods").getAsJsonArray()) {
      requestMap.put(method.getAsJsonObject().get("name").getAsString(), method.getAsJsonObject());
    }
  }

  // endregion
}

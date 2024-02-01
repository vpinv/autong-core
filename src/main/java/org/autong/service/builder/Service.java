package org.autong.service.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.autong.annotation.Loggable;
import org.autong.config.Settings;
import org.autong.service.Client;
import org.autong.service.ClientFactory;
import org.autong.util.DataUtil;

/**
 * Service class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
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

  @Loggable
  private void initializeServiceMap(JsonObject serviceInfo) {
    for (JsonElement api : serviceInfo.get("apis").getAsJsonArray()) {
      requestMap.put(api.getAsJsonObject().get("name").getAsString(), api.getAsJsonObject());
    }
  }

  /**
   * getRequest.
   *
   * @param methodName a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.5
   */
  @Loggable
  public JsonObject getRequest(String methodName) {
    return this.getRequestMap().get(methodName);
  }

  /**
   * run.
   *
   * @param methodName a {@link java.lang.String} object
   * @param request a {@link com.google.gson.JsonObject} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.5
   */
  @Loggable
  public JsonObject run(String methodName, JsonObject request) {
    request = DataUtil.deepMerge(request, requestMap.get(methodName));
    return DataUtil.toJsonObject(
        this.getClient()
            .resolve(
                this.getClient()
                    .mergeRequest(DataUtil.toObject(request, this.getClient().getRequestType()))));
  }
}

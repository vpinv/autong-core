package org.autong.service.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import org.autong.enums.DataType;
import org.autong.service.base.Validator;
import org.autong.util.DataUtil;

/**
 * ServiceFactory class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class ServiceFactory {
  private static HashMap<String, Service> serviceMap;

  private ServiceFactory() {}

  /**
   * initialize.
   *
   * @param suite a {@link com.google.gson.JsonObject} object
   * @since 1.0.5
   */
  public static void initialize(JsonObject suite) {
    JsonArray serviceImports = suite.get("$import").getAsJsonArray();

    serviceMap = new HashMap<>();
    for (JsonElement serviceInfo : serviceImports) {
      Service service =
          new Service(
              DataUtil.read(
                  serviceInfo.getAsJsonObject().get("path").getAsString(), DataType.YAML));
      serviceMap.put(serviceInfo.getAsJsonObject().get("name").getAsString(), service);
    }
  }

  /**
   * get.
   *
   * @param name a {@link java.lang.String} object
   * @return a {@link org.autong.service.builder.Service} object
   * @since 1.0.5
   */
  public static Service get(String name) {
    return serviceMap.get(name);
  }

  /**
   * validate.
   *
   * @param validator a {@link org.autong.service.base.Validator} object
   * @since 1.0.5
   */
  public static void validate(Validator validator) {
    ServiceValidator.validate(validator);
  }
}

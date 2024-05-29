package org.autong.service.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.Map;
import org.autong.enums.DataType;
import org.autong.service.Validator;
import org.autong.util.DataUtil;
import org.testng.Assert;

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
   * @param validator a {@link org.autong.service.Validator} object
   * @since 1.0.5
   */
  public static void validate(Object validator) {
    ServiceValidator.validate((Validator) validator);
  }

  /**
   * validate.
   *
   * @param validator a {@link org.autong.service.Validator} object
   * @since 1.0.8
   */
  public static void validate(Validator validator) {
    JsonObject actual = formatResponse(validator.getActual());
    DocumentContext responseContext = JsonPath.parse(actual.toString());
    JsonObject expected = validator.getExpected();

    for (JsonElement expression : expected.getAsJsonArray("steps")) {
      Object result = responseContext.read(expression.getAsString());
      Assert.assertTrue(result instanceof Iterable iter && iter.iterator().hasNext());
    }
  }

  /**
   * setVars.
   *
   * @param response a {@link java.lang.Object} object
   * @param step a {@link com.google.gson.JsonObject} object
   * @param cache a {@link java.util.Map} object
   * @since 1.0.8
   */
  public static void setVars(Object response, JsonObject step, Map<String, JsonElement> cache) {
    JsonObject jsonResponse = formatResponse(DataUtil.toJsonObject(response));
    DocumentContext responseContext = JsonPath.parse(jsonResponse.toString());

    Map<String, String> map = DataUtil.toObject(step.get("variables").getAsJsonObject(), Map.class);
    for (Map.Entry<String, String> entry : map.entrySet()) {
      Object result = responseContext.read(entry.getValue());
      cache.put(entry.getKey(), DataUtil.toJsonElement(result));
    }
  }

  /**
   * formatResponse.
   *
   * @param response a {@link com.google.gson.JsonObject} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject formatResponse(JsonObject response) {
    if (response.has("body")) {
      String body = response.get("body").getAsString();
      if (DataUtil.getDataType(body) == DataType.JSON) {
        response.add("body", DataUtil.toJsonObject(body));
      } else if (DataUtil.getDataType(body) == DataType.XML) {
        response.add("body", DataUtil.xmlToJsonObject(body));
      }
    }

    return response;
  }
}

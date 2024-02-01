package org.autong.service.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.autong.enums.DataType;
import org.autong.service.base.Validator;
import org.autong.util.DataUtil;
import org.testng.Assert;

/**
 * ServiceValidator class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class ServiceValidator {

  private ServiceValidator() {}

  /**
   * validate.
   *
   * @param validator a {@link org.autong.service.base.Validator} object
   * @since 1.0.5
   */
  public static void validate(Validator validator) {
    JsonObject actual = validator.getActual();

    if (actual.has("body")) {
      String body = actual.get("get").getAsString();
      if (DataUtil.getDataType(body) == DataType.JSON) {
        actual.add("body", DataUtil.toJsonObject(body));
      } else if (DataUtil.getDataType(body) == DataType.XML) {
        actual.add("body", DataUtil.xmlToJsonObject(body));
      }
    }

    DocumentContext responseContext = JsonPath.parse(actual.toString());
    JsonObject expected = validator.getExpected();

    for (JsonElement step : expected.getAsJsonArray("steps")) {
      String expression = step.getAsJsonObject().get("expression").getAsString();
      Object result = responseContext.read(expression);
      Assert.assertTrue(result instanceof Iterable iter && iter.iterator().hasNext());
    }
  }
}

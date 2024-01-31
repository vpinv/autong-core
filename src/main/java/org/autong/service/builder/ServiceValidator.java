package org.autong.service.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.autong.service.base.Validator;
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
   */
  public static void validate(Validator validator) {
    JsonObject actual = validator.getActual();
    JsonObject expected = validator.getExpected();

    for (JsonElement step : expected.getAsJsonArray("steps")) {
      String expression = step.getAsJsonObject().get("expression").getAsString();
      DocumentContext responseContext = JsonPath.parse(actual.toString());
      Object result = responseContext.read(expression);
      Assert.assertTrue(result instanceof Iterable iter && iter.iterator().hasNext());
    }
  }
}

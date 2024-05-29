package org.autong.test.testng;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.autong.enums.DataType;
import org.autong.service.Client;
import org.autong.service.builder.ServiceFactory;
import org.autong.util.DataUtil;
import org.autong.util.LoggerUtil;
import org.testng.ITest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TestRunner class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
@Getter
public class TestRunner implements ITest {
  private String testcaseName;
  private String testSuitePath;
  private JsonObject testSuite;

  /**
   * setTestDataResourcePath.
   *
   * @param testSuitePath a {@link java.lang.String} object
   */
  public void setTestDataResourcePath(String testSuitePath) {
    this.testSuitePath = testSuitePath;
  }

  /** {@inheritDoc} */
  @Override
  public String getTestName() {
    return this.getTestcaseName();
  }

  /** beforeSuite. */
  @BeforeClass
  public void beforeClass() {
    testSuite = DataUtil.read(this.getTestSuitePath(), DataType.YAML);
    ServiceFactory.initialize(this.getTestSuite());
  }

  /**
   * dataProvider.
   *
   * @param method a {@link java.lang.reflect.Method} object
   * @return a {@link java.util.Iterator} object
   */
  @DataProvider(name = "dataProvider")
  public Iterator<JsonElement> dataProvider(Method method) {
    JsonArray testCases = DataUtil.getAsJsonArray(this.getTestSuite(), "testSuite");
    return Objects.requireNonNull(testCases).iterator();
  }

  /**
   * testcaseBeforeMethod.
   *
   * @param method a {@link java.lang.reflect.Method} object
   * @param testCaseObject an array of {@link java.lang.Object} objects
   */
  @BeforeMethod(alwaysRun = true)
  public void testcaseBeforeMethod(Method method, Object[] testCaseObject) {
    LoggerUtil.reconfigure();
    if (testCaseObject != null
        && testCaseObject.length != 0
        && testCaseObject[0] instanceof JsonObject testCase) {
      this.testcaseName =
          testCase.get("testcaseId").getAsString().replaceAll("\\s+", "_")
              + "__"
              + testCase.get("testcaseName").getAsString().replaceAll("\\s+", "_");
    }
  }

  /**
   * run.
   *
   * @param data a {@link com.google.gson.JsonObject} object
   */
  @Test(dataProvider = "dataProvider")
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void run(JsonObject data) {
    Map<String, JsonElement> cache = new HashMap<>();
    JsonArray steps = data.getAsJsonArray("steps");

    for (JsonElement step : steps) {

      JsonObject request =
          DataUtil.deepMerge(
              step.getAsJsonObject().getAsJsonObject("request"),
              ServiceFactory.get(step.getAsJsonObject().get("service").getAsString())
                  .getRequest(step.getAsJsonObject().get("method").getAsString()));

      Client client =
          ServiceFactory.get(step.getAsJsonObject().get("service").getAsString()).getClient();

      Object response =
          client
              .withValidator(ServiceFactory::validate)
              .withExpectedResult(step.getAsJsonObject().get("validation").getAsJsonObject())
              .resolve(client.mergeRequest(request));

      ServiceFactory.setVars(response, step.getAsJsonObject(), cache);
    }
  }
}

package org.autong;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Method;
import java.util.Iterator;
import org.autong.enums.DataType;
import org.autong.runner.RunnableFactory;
import org.autong.service.builder.ServiceFactory;
import org.autong.util.DataUtil;
import org.autong.util.LoggerUtil;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TestRunner class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class TestRunner implements ITest {

  private String testcaseName;
  JsonObject testSuite;

  /** beforeSuite. */
  @BeforeSuite
  public void beforeSuite() {
    testSuite = DataUtil.read("org.autong/loginServiceTest.yaml", DataType.YAML);
    ServiceFactory.initialize(testSuite);
  }

  /**
   * dataProvider.
   *
   * @param method a {@link java.lang.reflect.Method} object
   * @return a {@link java.util.Iterator} object
   */
  @DataProvider(name = "dataProvider")
  public Iterator<JsonElement> dataProvider(Method method) {
    JsonArray testCases = DataUtil.getAsJsonArray(testSuite, "testSuite");
    return testCases.iterator();
  }

  /** {@inheritDoc} */
  @Override
  public String getTestName() {
    return this.testcaseName;
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
   * test.
   *
   * @param data a {@link com.google.gson.JsonObject} object
   */
  @Test(dataProvider = "dataProvider")
  public void test(JsonObject data) {
    JsonArray steps = data.getAsJsonArray("steps");
    for (JsonElement step : steps) {
      RunnableFactory.get()
          .withValidator(ServiceFactory::validate)
          .withExpectedResult(step.getAsJsonObject().get("validator").getAsJsonObject())
          .run(
              step.getAsJsonObject().get("name").getAsString(),
              () ->
                  ServiceFactory.get(step.getAsJsonObject().get("service").getAsString())
                      .run(
                          step.getAsJsonObject().get("method").getAsString(),
                          step.getAsJsonObject().getAsJsonObject("request")));
    }
  }
}

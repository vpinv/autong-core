package org.autong.test.testng;

import com.github.underscore.Underscore;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.autong.config.BaseConfig;
import org.autong.enums.DataType;
import org.autong.test.model.TestData;
import org.autong.util.DataUtil;
import org.autong.util.LoggerUtil;
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

/**
 * Abstract TestCase class.
 *
 * @version 1.0.8
 * @since 1.0.8
 */
public abstract class TestCase implements ITest {
  private final InheritableThreadLocal<String> testDataResourcePath =
      new InheritableThreadLocal<>();
  private final InheritableThreadLocal<JsonObject> suiteTestData = new InheritableThreadLocal<>();
  private final InheritableThreadLocal<String> testcaseName = new InheritableThreadLocal<>();

  /**
   * Setter for the field <code>testDataResourcePath</code>.
   *
   * @param testDataResourcePath a {@link java.lang.String} object
   */
  public void setTestDataResourcePath(String testDataResourcePath) {
    this.testDataResourcePath.set(testDataResourcePath);
  }

  /**
   * Getter for the field <code>suiteTestData</code>.
   *
   * @return a {@link com.google.gson.JsonObject} object
   */
  public JsonObject getSuiteTestData() {
    if (this.suiteTestData.get() == null) {
      this.suiteTestData.set(DataUtil.read(this.testDataResourcePath.get(), DataType.YAML));
    }
    return this.suiteTestData.get();
  }

  /** {@inheritDoc} */
  @Override
  public String getTestName() {
    return this.testcaseName.get();
  }

  /** testcaseBeforeSuite. */
  @BeforeSuite(alwaysRun = true)
  public void testcaseBeforeSuite() {
    BaseConfig.configure();
  }

  /** testcaseAfterSuite. */
  @AfterSuite(alwaysRun = true)
  public void testcaseAfterSuite() {
    this.suiteTestData.remove();
    this.testDataResourcePath.remove();
  }

  /** testcaseBeforeClass. */
  @BeforeClass(alwaysRun = true)
  public void testcaseBeforeClass() {
    if (this.testDataResourcePath.get() == null) {
      throw new InvalidParameterException("testDataResourcePath must not be null");
    }
    this.suiteTestData.set(DataUtil.read(this.testDataResourcePath.get(), DataType.YAML));
  }

  /** testcaseAfterClass. */
  @AfterClass(alwaysRun = true)
  public void testcaseAfterClass() {
    this.suiteTestData.remove();
  }

  /**
   * testcaseBeforeMethod.
   *
   * @param method a {@link java.lang.reflect.Method} object
   * @param testDataObject an array of {@link java.lang.Object} objects
   */
  @BeforeMethod(alwaysRun = true)
  public void testcaseBeforeMethod(Method method, Object[] testDataObject) {
    LoggerUtil.reconfigure();

    if (testDataObject != null
        && testDataObject.length != 0
        && testDataObject[0] instanceof TestData singleTestData) {
      this.testcaseName.set(
          singleTestData.getTestcaseId().replaceAll("\\s+", "_")
              + "__"
              + singleTestData.getTestcaseName().replaceAll("\\s+", "_")
              + "__"
              + method.getName());
    }
  }

  /** testcaseAfterMethod. */
  @AfterMethod(alwaysRun = true)
  public void testcaseAfterMethod() {
    this.testcaseName.remove();
  }

  /**
   * dataProvider.
   *
   * @param method a {@link java.lang.reflect.Method} object
   * @return a {@link java.util.Iterator} object
   */
  @SuppressWarnings("DataProviderReturnType")
  @DataProvider(name = "dataProvider")
  public Iterator<TestData> dataProvider(Method method) {
    if (this.getSuiteTestData().has(method.getName())
        && this.getSuiteTestData().get(method.getName()).isJsonArray()) {
      Type testDataType = new TypeToken<List<TestData>>() {}.getType();
      List<TestData> testDataList =
          DataUtil.getGson()
              .fromJson(this.getSuiteTestData().getAsJsonArray(method.getName()), testDataType);

      String testcaseId = System.getProperty("testcaseId");
      if (testcaseId == null) {
        return testDataList.iterator();
      }

      List<TestData> filteredTestDataList =
          Underscore.filter(
              testDataList,
              singleTestData -> singleTestData.getTestcaseId().equalsIgnoreCase(testcaseId));
      return filteredTestDataList.iterator();
    }
    return Collections.emptyIterator();
  }
}

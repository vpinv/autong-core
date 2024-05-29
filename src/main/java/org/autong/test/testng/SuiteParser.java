package org.autong.test.testng;

import com.github.underscore.Optional;
import com.github.underscore.U;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.commons.io.IOUtils;
import org.autong.enums.DataType;
import org.autong.exception.CoreException;
import org.autong.util.DataUtil;
import org.testng.TestNGException;
import org.testng.internal.Yaml;
import org.testng.internal.YamlParser;
import org.testng.xml.XmlSuite;

/**
 * SuiteParser class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public class SuiteParser extends YamlParser {
  /** {@inheritDoc} */
  @Override
  public XmlSuite parse(String filePath, InputStream inputStream, boolean loadClasses) {
    try {
      JsonObject suiteObject = DataUtil.read(new File(filePath).toURI().toURL(), DataType.YAML);
      if (suiteObject.has("suites") && suiteObject.get("suites") instanceof JsonArray suiteArray) {
        String suiteName =
            System.getProperty("suite") != null
                ? System.getProperty("suite")
                : suiteArray.get(0).getAsJsonObject().get("name").getAsString();

        Optional<JsonElement> testngSuite =
            U.find(
                suiteArray,
                suite -> suite.getAsJsonObject().get("name").getAsString().equals(suiteName));
        if (testngSuite.isEmpty()) {
          throw new CoreException("Did not find suite - " + suiteName);
        }

        InputStream testngSuiteStream =
            IOUtils.toInputStream(testngSuite.get().getAsJsonObject().toString());
        return Yaml.parse(filePath, testngSuiteStream, loadClasses);
      }
      return Yaml.parse(filePath, inputStream, loadClasses);
    } catch (FileNotFoundException ex) {
      throw new TestNGException(ex);
    } catch (MalformedURLException ex) {
      throw new CoreException(ex);
    }
  }
}

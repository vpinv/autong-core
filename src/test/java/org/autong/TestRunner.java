package org.autong;

import org.testng.annotations.BeforeSuite;

/**
 * TestRunner class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class TestRunner extends org.autong.test.TestRunner {

  /** beforeSuite. */
  @BeforeSuite
  public void beforeSuite() {
    this.setTestDataResourcePath("org.autong/fsCreateOrderTest.yaml");
  }
}

package org.autong.test.model;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;

/**
 * TestData class.
 *
 * @version 1.0.8
 * @since 1.0.8
 */
@Builder
@Data
public class TestData {
  private String testcaseId;
  private String testcaseName;
  private JsonObject request;
  private JsonObject response;
}

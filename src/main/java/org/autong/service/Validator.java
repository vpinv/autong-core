package org.autong.service;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;

/**
 * Validator class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
@Data
@Builder
public class Validator {
  private JsonObject actual;
  private JsonObject expected;
}

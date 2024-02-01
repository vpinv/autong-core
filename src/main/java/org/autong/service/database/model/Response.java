package org.autong.service.database.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;

/**
 * Response class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
@Data
@Builder
public class Response {
  private Request request;
  private Result result;

  /**
   * Result class.
   *
   * @version 1.0.5
   * @since 1.0.5
   */
  @Data
  @Builder
  public static class Result {
    private JsonObject metaData;
    private JsonArray data;
  }
}

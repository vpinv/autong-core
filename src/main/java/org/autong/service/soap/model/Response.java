package org.autong.service.soap.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Response class.
 *
 * @version 1.0.3
 * @since 1.0.3
 */
@Data
@Builder
public class Response {
  private Request request;
  private Integer statusCode;
  private String statusLine;
  private String contentType;
  private Map<String, String> headers;
  private String body;
  private String sessionId;
  private Map<String, String> cookies;
}

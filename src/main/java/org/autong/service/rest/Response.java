package org.autong.service.rest;

import java.io.InputStream;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Response class.
 *
 * @version 1.0.1
 * @since 1.0.1
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
  private byte[] bytes;
  private InputStream stream;
  private String sessionId;
  private Map<String, String> cookies;
}

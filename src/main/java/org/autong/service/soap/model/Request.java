package org.autong.service.soap.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.autong.enums.HttpMethod;

/**
 * Request class.
 *
 * @version 1.0.3
 * @since 1.0.3
 */
@Data
@Builder
public class Request {
  private String baseUri;
  private String basePath;
  private HttpMethod method;
  @Builder.Default private Map<String, String> headers = new HashMap<>();
  private boolean ignoreBaseHeaders;
  private String body;
  private String responseType;
}
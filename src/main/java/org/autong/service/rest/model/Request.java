package org.autong.service.rest.model;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.autong.enums.HttpRequestType;

/**
 * Request class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
@Data
@Builder
public class Request {
  private HttpRequestType type;
  private String baseUri;
  private String basePath;
  private String accept;
  @Builder.Default private Map<String, String> headers = new HashMap<>();
  private boolean ignoreBaseHeaders;
  private JsonObject params;
  private String body;
  private JsonObject multiPart;
  private Integer timeout;
  private Boolean withCredentials;
  private Authorization authorization;
  private ResponseType responseType;
  private Integer maxContentLength;
  private Integer maxBodyLength;
  @Builder.Default private boolean verifySsl = false;

  /**
   * Authorization class.
   *
   * @version 1.0.1
   * @since 1.0.1
   */
  @Data
  @Builder
  public static class Authorization {
    private String username;
    private String password;
  }

  /**
   * ResponseType class.
   *
   * @version 1.0.1
   * @since 1.0.1
   */
  public enum ResponseType {
    TEXT,
    BYTE_ARRAY,
  }
}

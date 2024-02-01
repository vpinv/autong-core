package org.autong.service.cache.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.autong.enums.CacheRequestType;

/**
 * Request class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
@Data
@Builder
public class Request {

  // Redis
  @Builder.Default private Map<String, Integer> hostAndPort = new HashMap<>();
  @Builder.Default private String password = "";
  @Builder.Default private String userName = "";
  private Body body;
  private CacheRequestType type;

  /**
   * Request class.
   *
   * @author prakash.adak
   * @version 1.0.85
   * @since 1.0.85
   */
  @Data
  @Builder
  public static class Body {
    private String key;
    private String field;
    private String value;
    private Map<String, String> valueMap;
  }
}

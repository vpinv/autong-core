package org.autong.settings;

import lombok.Builder;
import lombok.Data;

/**
 * Settings class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
@Data
@Builder
public class Settings {
  @Builder.Default private boolean retry = true;
  @Builder.Default private int maxRetries = 2;
  @Builder.Default private int retryDelay = 1000;
  @Builder.Default private int initialDelay = 0;
  @Builder.Default private boolean throwOnError = true;
  @Builder.Default private boolean enableValidator = true;
  @Builder.Default private boolean navigateToPage = true;
  @Builder.Default private boolean enableBaseUrlNavigation = true;
  @Builder.Default private boolean enableUrlNavigation = false;
  @Builder.Default private Timeout timeout = Timeout.builder().build();

  /**
   * Timeout class.
   *
   * @version 1.0.1
   * @since 1.0.1
   */
  @Builder
  @Data
  public static class Timeout {
    @Builder.Default private int sleepTimeout = 100;
    @Builder.Default private int scriptTimeout = 60000;
    @Builder.Default private int pageLoadTimeout = 60000;
    @Builder.Default private int implicitlyWait = 5000;
    @Builder.Default private int waitTimeout = 60000;
    @Builder.Default private int waitInterval = 2000;
  }
}

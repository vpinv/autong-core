package org.autong.service;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.autong.settings.Settings;

/**
 * Service class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public class Service {
  @Getter private final Settings settings;
  @Getter private Settings updatedSettings;
  @Getter private JsonObject expectedResult;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private Consumer<Validator> validator;

  /**
   * Constructor for Service.
   *
   * @param settings a {@link org.autong.settings.Settings} object
   */
  protected Service(Settings settings) {
    this.settings = settings;
    this.reset();
  }

  /** reset. */
  public void reset() {
    this.updatedSettings = null;
    this.expectedResult = null;
    this.validator = null;
  }

  /**
   * withSettings.
   *
   * @param settings a {@link org.autong.settings.Settings} object
   * @return a {@link org.autong.service.Service} object
   */
  public Service withSettings(Settings settings) {
    this.updatedSettings = settings;
    return this;
  }

  /**
   * withValidator.
   *
   * @param validator a {@link java.util.function.Consumer} object
   * @return a {@link org.autong.service.Service} object
   */
  public Service withValidator(Consumer<Validator> validator) {
    this.validator = validator;
    return this;
  }

  /**
   * withExpectedResult.
   *
   * @param expected a {@link com.google.gson.JsonObject} object
   * @return a {@link org.autong.service.Service} object
   */
  public Service withExpectedResult(JsonObject expected) {
    this.expectedResult = expected;
    return this;
  }

  /**
   * mergeRequest.
   *
   * @param newRequest a R object
   * @param type a {@link java.lang.Class} object
   * @param <R> a R class
   * @return a R object
   */
  public <R> R mergeRequest(R newRequest, Class<R> type) {
    return null;
  }
}

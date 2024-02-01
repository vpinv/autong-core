package org.autong.service.base;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import org.autong.config.Settings;

/**
 * BaseService interface.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@SuppressWarnings("InterfaceTypeParameterName")
public interface BaseService<Type> {
  /**
   * getSettings.
   *
   * @return a {@link org.autong.config.Settings} object
   * @since 1.0.5
   */
  Settings getSettings();

  /**
   * getUpdatedSettings.
   *
   * @return a {@link org.autong.config.Settings} object
   * @since 1.0.5
   */
  Settings getUpdatedSettings();

  /**
   * getValidator.
   *
   * @return a {@link java.util.function.Consumer} object
   * @since 1.0.5
   */
  Consumer<Validator> getValidator();

  /**
   * getExpectedResult.
   *
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.5
   */
  JsonObject getExpectedResult();

  /**
   * reset.
   *
   * @since 1.0.5
   */
  void reset();

  /**
   * withSettings.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @return a Type object
   * @since 1.0.5
   */
  Type withSettings(Settings settings);

  /**
   * withExpectedResult.
   *
   * @param expected a {@link com.google.gson.JsonObject} object
   * @return a Type object
   * @since 1.0.5
   */
  Type withExpectedResult(JsonObject expected);

  /**
   * withValidator.
   *
   * @param validator a {@link java.util.function.Consumer} object
   * @return a Type object
   * @since 1.0.5
   */
  Type withValidator(Consumer<Validator> validator);
}

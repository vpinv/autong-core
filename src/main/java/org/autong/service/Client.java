package org.autong.service;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import org.autong.config.Settings;

/**
 * Client interface.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
@SuppressWarnings("InterfaceTypeParameterName")
public interface Client<Type extends AbstractClient<?, ?, ?>, Request, Response> {

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

  /**
   * getRequestType.
   *
   * @return a {@link java.lang.Class} object
   * @since 1.0.5
   */
  Class<Request> getRequestType();

  /**
   * getResponseType.
   *
   * @return a {@link java.lang.Class} object
   * @since 1.0.5
   */
  Class<Response> getResponseType();

  /**
   * mergeRequest.
   *
   * @param request a Request object
   * @return a Request object
   * @since 1.0.5
   */
  Request mergeRequest(Request request);

  /**
   * mergeRequest.
   *
   * @param request a {@link com.google.gson.JsonObject} object
   * @return a Request object
   * @since 1.0.8
   */
  Request mergeRequest(JsonObject request);

  /**
   * resolve.
   *
   * @param request a Request object
   * @return a Response object
   * @since 1.0.5
   */
  Response resolve(Request request);
}

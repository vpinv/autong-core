package org.autong.config;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.autong.annotation.Loggable;

/**
 * BaseConfig class.
 *
 * @version 1.0.8
 * @since 1.0.8
 */
public class BaseConfig {
  @Getter private final String env;
  @Getter private final Set<String> context;

  private static BaseConfig config = null;

  private BaseConfig(String env, Set<String> contextList) {
    this.env = env;
    this.context = new HashSet<>();
    this.context.add("DEFAULT");
    this.context.addAll(contextList);
  }

  /**
   * configure.
   *
   * @return a {@link org.autong.config.BaseConfig} object
   */
  public static BaseConfig configure() {
    String env =
        System.getProperty("env") != null ? System.getProperty("env").toUpperCase() : "DEFAULT";
    Set<String> contextList = new HashSet<>();
    if (System.getProperty("context") == null) {
      contextList.add("DEFAULT");
    } else {
      Arrays.stream(System.getProperty("context").split("\\s+"))
          .forEach(context -> contextList.add(context.toUpperCase()));
    }

    return configure(env, contextList);
  }

  /**
   * configure.
   *
   * @param json a {@link com.google.gson.JsonObject} object
   * @return a {@link org.autong.config.BaseConfig} object
   */
  public static BaseConfig configure(JsonObject json) {
    if (json == null) {
      json = new JsonObject();
    }

    String env = json.has("env") ? json.get("env").getAsString().toUpperCase() : "DEFAULT";
    Set<String> contextList = new HashSet<>();

    if (json.has("context")) {
      json.get("context")
          .getAsJsonArray()
          .forEach(context -> contextList.add(context.getAsString().toUpperCase()));
    } else {
      contextList.add("DEFAULT");
    }

    return configure(env, contextList);
  }

  /**
   * configure.
   *
   * @param env a {@link java.lang.String} object
   * @param context a {@link java.util.Set} object
   * @return a {@link org.autong.config.BaseConfig} object
   */
  public static BaseConfig configure(String env, Set<String> context) {
    if (config == null) {
      return reconfigure(env, context);
    }
    return config;
  }

  /**
   * reconfigure.
   *
   * @param env a {@link java.lang.String} object
   * @param context a {@link java.util.Set} object
   * @return a {@link org.autong.config.BaseConfig} object
   */
  @Loggable
  public static BaseConfig reconfigure(String env, Set<String> context) {
    config = new BaseConfig(env, context);
    return config;
  }

  // region private methods

  // endregion
}

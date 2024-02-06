package org.autong.service;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.autong.config.Settings;
import org.autong.util.DataUtil;

/**
 * Abstract AbstractClient class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@Getter
@SuppressWarnings("ClassTypeParameterName")
public abstract class AbstractClient<Type extends AbstractClient<?, ?, ?>, Request, Response>
    implements Client<Type, Request, Response> {
  private final Request baseRequest;
  private final Settings settings;
  private Settings updatedSettings;
  private JsonObject expectedResult;

  @Setter(AccessLevel.PROTECTED)
  private Consumer<Validator> validator;

  /**
   * Constructor for AbstractClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a Request object
   * @since 1.0.5
   */
  protected AbstractClient(Settings settings, Request request) {
    this.settings = settings;
    this.baseRequest = this.mergeRequest(request);
  }

  /**
   * reset.
   *
   * @since 1.0.5
   */
  public void reset() {
    this.updatedSettings = null;
    this.expectedResult = null;
    this.validator = null;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Type withSettings(Settings settings) {
    this.updatedSettings = settings;
    return (Type) this;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Type withValidator(Consumer<Validator> validator) {
    this.validator = validator;
    return (Type) this;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Type withExpectedResult(JsonObject expected) {
    this.expectedResult = expected;
    return (Type) this;
  }

  /**
   * mergeRequest.
   *
   * @param sourceRequest a Request object
   * @param targetRequest a Request object
   * @return a Request object
   */
  protected Request mergeRequest(Request sourceRequest, Request targetRequest) {
    JsonObject source = DataUtil.toJsonObject(sourceRequest);
    JsonObject target = DataUtil.toJsonObject(targetRequest);

    if (this.getBaseRequest() != null) {
      target = DataUtil.toJsonObject(this.getBaseRequest());
    }

    if (source.has("ignoreBaseHeaders") && source.get("ignoreBaseHeaders").getAsBoolean()) {
      target.remove("headers");
    }

    DataUtil.deepMerge(source, target);
    return DataUtil.getGson().fromJson(target, this.getRequestType());
  }
}

package org.autong.service;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.autong.config.Settings;
import org.autong.service.base.AbstractBaseService;
import org.autong.service.database.model.Request;
import org.autong.util.DataUtil;

/**
 * Abstract AbstractClient class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@Getter
@SuppressWarnings("ClassTypeParameterName")
public abstract class AbstractClient<Type, Request, Response> extends AbstractBaseService<Type>
    implements Client<Type, Request, Response> {
  private final Request baseRequest;

  /**
   * Constructor for AbstractClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a Request object
   * @since 1.0.5
   */
  protected AbstractClient(Settings settings, Request request) {
    super(settings);
    this.baseRequest = this.mergeRequest(request);
  }

  /** {@inheritDoc} */
  @Override
  public Request mergeRequest(Request newRequest) {
    JsonObject source = DataUtil.toJsonObject(newRequest);
    JsonObject target =
        DataUtil.toJsonObject(org.autong.service.database.model.Request.builder().build());

    if (this.getBaseRequest() != null) {
      target = DataUtil.toJsonObject(this.getBaseRequest());
    }

    if (source.has("ignoreBaseHeaders") && source.get("ignoreBaseHeaders").getAsBoolean()) {
      target.remove("headers");
    }

    JsonObject mergedRequest = DataUtil.deepMerge(source, target);
    return DataUtil.getGson().fromJson(mergedRequest, this.getRequestType());
  }
}

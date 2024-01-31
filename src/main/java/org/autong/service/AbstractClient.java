package org.autong.service;

import lombok.Getter;
import org.autong.config.Settings;
import org.autong.service.base.AbstractBaseService;

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
   */
  protected AbstractClient(Settings settings, Request request) {
    super(settings);
    this.baseRequest = this.mergeRequest(request);
  }
}

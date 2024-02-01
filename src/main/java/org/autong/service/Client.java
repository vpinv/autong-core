package org.autong.service;

import org.autong.service.base.BaseService;

/**
 * Client interface.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
@SuppressWarnings("InterfaceTypeParameterName")
public interface Client<Type, Request, Response> extends BaseService<Type> {

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
   * @param newRequest a Request object
   * @return a Request object
   * @since 1.0.5
   */
  Request mergeRequest(Request newRequest);

  /**
   * resolve.
   *
   * @param request a Request object
   * @return a Response object
   * @since 1.0.5
   */
  Response resolve(Request request);
}

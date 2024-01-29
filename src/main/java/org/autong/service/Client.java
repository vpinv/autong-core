package org.autong.service;

/**
 * Client interface.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public interface Client<I, O> {
  /**
   * resolve.
   *
   * @param request a I object
   * @return a O object
   */
  O resolve(I request);
}

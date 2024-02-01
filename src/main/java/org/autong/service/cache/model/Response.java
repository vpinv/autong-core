package org.autong.service.cache.model;

import lombok.Builder;
import lombok.Data;

/**
 * Response class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
@Data
@Builder
public class Response {
  private Request request;
  private String body;
  private String exception;
}

package org.autong.exception;

/**
 * CoreException class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public class CoreException extends RuntimeException {

  /**
   * Constructor for CoreException.
   *
   * @since 1.0.1
   */
  public CoreException() {
    super();
  }

  /**
   * Constructor for CoreException.
   *
   * @param message a {@link java.lang.String} object
   * @since 1.0.1
   */
  public CoreException(String message) {
    super(message);
  }

  /**
   * Constructor for CoreException.
   *
   * @param message a {@link java.lang.String} object
   * @param cause a {@link java.lang.Throwable} object
   * @since 1.0.1
   */
  public CoreException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor for CoreException.
   *
   * @param cause a {@link java.lang.Throwable} object
   * @since 1.0.1
   */
  public CoreException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor for CoreException.
   *
   * @param message a {@link java.lang.String} object
   * @param cause a {@link java.lang.Throwable} object
   * @param enableSuppression a boolean
   * @param writableStackTrace a boolean
   * @since 1.0.1
   */
  protected CoreException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

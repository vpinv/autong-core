package org.autong.runner;

import com.epam.reportportal.annotations.Step;
import java.util.function.Supplier;
import org.autong.config.Settings;
import org.autong.service.base.AbstractBaseService;

/**
 * Runnable class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class Runnable extends AbstractBaseService<Runnable> {
  /**
   * Constructor for Runnable.
   *
   * @param settings a {@link org.autong.config.Settings} object
   */
  public Runnable(Settings settings) {
    super(settings);
  }

  /**
   * run.
   *
   * @param message a {@link java.lang.String} object
   * @param method a {@link java.util.function.Supplier} object
   * @param <R> a R class
   * @return a R object
   */
  @Step("{message}")
  @org.autong.annotation.Runnable
  public <R> R run(String message, Supplier<R> method) {
    return method.get();
  }
}

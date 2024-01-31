package org.autong.runner;

import org.autong.config.Settings;

/**
 * RunnableFactory class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class RunnableFactory {
  private static ThreadLocal<Runnable> runner = new ThreadLocal<>();

  private RunnableFactory() {}

  /**
   * get.
   *
   * @return a {@link org.autong.runner.Runnable} object
   */
  public static Runnable get() {
    return get(Settings.builder().build(), false);
  }

  /**
   * get.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param newInstance a {@link java.lang.Boolean} object
   * @return a {@link org.autong.runner.Runnable} object
   */
  public static Runnable get(Settings settings, Boolean newInstance) {
    if (runner.get() == null || newInstance) {
      runner.remove();
      runner.set(new Runnable(settings));
    }

    return runner.get();
  }
}

package org.autong.util;

import com.google.common.io.Resources;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.autong.exception.CoreException;

/**
 * LoggerUtil class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public class LoggerUtil {
  private static Logger log = null;

  static {
    try {
      URL log4j2XmlUrl = Resources.getResource("log4j2.xml");
      System.setProperty("log4j2.configurationFile", log4j2XmlUrl.toURI().toString());
    } catch (URISyntaxException e) {
      throw new CoreException(e);
    }
  }

  private LoggerUtil() {}

  /**
   * getLogger.
   *
   * @param loggerName a {@link java.lang.String} object
   * @return a {@link org.apache.logging.log4j.Logger} object
   */
  public static Logger getLogger(String loggerName) {
    log = LogManager.getLogger(loggerName);
    return log;
  }

  /** reconfigure. */
  public static void reconfigure() {
    try {
      ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
      getLogger(LoggerUtil.class.getName());
      log.debug("Logger initialized");
      log.debug(getLogFileName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * getLogFileName.
   *
   * @return a {@link java.lang.String} object
   */
  public static String getLogFileName() {
    try {
      FileAppender appender =
          (FileAppender)
              LoggerContext.getContext().getConfiguration().getAppenders().get("FileAppender");
      return appender.getFileName();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}

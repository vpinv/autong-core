package org.autong.aspect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.autong.config.Settings;
import org.autong.exception.CoreException;
import org.autong.service.Client;
import org.autong.service.Validator;
import org.autong.util.DataUtil;
import org.autong.util.LoggerUtil;

/**
 * RunnableAspect class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@SuppressWarnings("unused")
@Aspect
public class RunnableAspect {

  private Logger log;
  private static final Gson gson =
      new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

  /**
   * beforeMethodAop.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @since 1.0.5
   */
  @Before("@annotation(org.autong.annotation.Runnable) && execution(* *(..))")
  public void beforeMethodAop(JoinPoint joinPoint) {
    StepAspect.startNestedStep(joinPoint);
    log = LoggerUtil.getLogger(LoggableAspect.getMethodSignature(joinPoint));

    if (joinPoint.getArgs().length != 0) {
      String message =
          MessageFormat.format(
              "args: {0}", LoggableAspect.formatMethodArguments(joinPoint.getArgs()));
      log.debug(message);
    }
  }

  /**
   * aroundMethodAop.
   *
   * @param proceedingJoinPoint a {@link org.aspectj.lang.ProceedingJoinPoint} object
   * @return a {@link java.lang.Object} object
   * @throws java.lang.Throwable if any.
   * @since 1.0.5
   */
  @Around("@annotation(org.autong.annotation.Runnable) && execution(* *(..))")
  public Object aroundMethodAop(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    StopWatch timer = StopWatch.create();
    timer.start();

    Settings settings = getSettings(log, proceedingJoinPoint);
    Consumer<Validator> validator = getValidator(proceedingJoinPoint);
    JsonObject expectedResult = getExpectedResult(proceedingJoinPoint);
    reset(proceedingJoinPoint);

    Object object = null;
    int retryCount = 0;
    boolean isRetry = false;
    Throwable exception;
    do {
      try {
        wait(log, settings, isRetry, retryCount);
        object = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        runValidation(settings, validator, expectedResult, object, proceedingJoinPoint);
        exception = null;
      } catch (Throwable error) {
        log.warn(ExceptionUtils.getMessage(error));
        exception = error;
        isRetry = true;
      }
      retryCount++;
    } while (settings != null
        && settings.isRetry()
        && retryCount <= settings.getMaxRetries()
        && exception != null);

    timer.stop();
    log.trace("Execution time - {}", timer);
    if (exception != null && (settings == null || settings.isThrowOnError())) {
      throw exception;
    }

    return object;
  }

  /**
   * afterMethodAop.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @param result a {@link java.lang.Object} object
   * @since 1.0.5
   */
  @AfterReturning(
      pointcut = "@annotation(org.autong.annotation.Runnable) && execution(* *(..))",
      returning = "result")
  public void afterMethodAop(JoinPoint joinPoint, Object result) {
    if (result != null) {
      String message =
          MessageFormat.format("result: {0}", LoggableAspect.formatMethodReturnType(result));
      log.debug(message);
    }
    StepAspect.finishNestedStep(joinPoint);
  }

  /**
   * afterThrowAop.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @param ex a {@link java.lang.Throwable} object
   * @since 1.0.5
   */
  @AfterThrowing(
      pointcut = "@annotation(org.autong.annotation.Runnable) && execution(* *(..))",
      throwing = "ex")
  public void afterThrowAop(JoinPoint joinPoint, Throwable ex) {
    StepAspect.failedNestedStep(joinPoint, ex);
  }

  // region private methods

  private static void runValidation(
      Settings settings,
      Consumer<Validator> validator,
      JsonObject expectedResult,
      Object result,
      ProceedingJoinPoint proceedingJoinPoint) {
    if (settings != null && settings.isEnableValidator() && validator != null) {
      JsonObject actualResult = result == null ? new JsonObject() : DataUtil.toJsonObject(result);
      Validator input = Validator.builder().actual(actualResult).expected(expectedResult).build();
      Objects.requireNonNull(validator).accept(input);
    }
  }

  private static Settings getSettings(Logger log, ProceedingJoinPoint proceedingJoinPoint) {
    if (proceedingJoinPoint.getThis() instanceof Client<?, ?, ?> service) {
      Settings settings = getUpdatedSettings(proceedingJoinPoint);
      if (settings != null) {
        String message =
            MessageFormat.format(
                "Settings overridden: {0}", LoggableAspect.formatMethodReturnType(settings));
        log.trace(message);
        return settings;
      }
      return getDefaultSettings(proceedingJoinPoint);
    }
    return null;
  }

  private static Settings getDefaultSettings(ProceedingJoinPoint proceedingJoinPoint) {
    if (proceedingJoinPoint.getThis() instanceof Client<?, ?, ?> service) {
      return service.getSettings();
    }
    return null;
  }

  private static Settings getUpdatedSettings(ProceedingJoinPoint proceedingJoinPoint) {
    if (proceedingJoinPoint.getThis() instanceof Client<?, ?, ?> service) {
      return service.getUpdatedSettings();
    }
    return null;
  }

  private static Consumer<Validator> getValidator(ProceedingJoinPoint proceedingJoinPoint) {
    if (proceedingJoinPoint.getThis() instanceof Client<?, ?, ?> service) {
      return service.getValidator();
    }
    return null;
  }

  private static JsonObject getExpectedResult(ProceedingJoinPoint proceedingJoinPoint) {
    if (proceedingJoinPoint.getThis() instanceof Client<?, ?, ?> service) {
      return service.getExpectedResult();
    }
    return null;
  }

  private static void reset(ProceedingJoinPoint proceedingJoinPoint) {
    if (proceedingJoinPoint.getThis() instanceof Client<?, ?, ?> service) {
      service.reset();
    }
  }

  private static void wait(Logger log, Settings settings, boolean isRetry, int retryCount) {
    waitIfInitialDelay(log, settings, isRetry);
    waitIfRetry(log, settings, isRetry, retryCount);
  }

  private static void waitIfRetry(Logger log, Settings settings, boolean isRetry, int retryCount) {
    if (settings != null && isRetry) {
      log.trace("Sleeping for {} ms", settings.getRetryDelay());
      try {
        TimeUnit.MILLISECONDS.sleep(settings.getRetryDelay());
      } catch (InterruptedException e) {
        throw new CoreException(e);
      }

      log.warn("Retry - {}", retryCount);
    }
  }

  private static void waitIfInitialDelay(Logger log, Settings settings, boolean isRetry) {
    if (settings != null && !isRetry && settings.getInitialDelay() > 0) {
      log.trace("Sleeping for {} ms initial delay", settings.getInitialDelay());
      try {
        TimeUnit.MILLISECONDS.sleep(settings.getInitialDelay());
      } catch (InterruptedException e) {
        throw new CoreException(e);
      }
    }
  }

  // endregion
}

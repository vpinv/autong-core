package org.autong.aspect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import groovy.json.StringEscapeUtils;
import java.text.MessageFormat;
import java.util.Arrays;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.autong.util.DataUtil;
import org.autong.util.LoggerUtil;

/**
 * LoggableAspect class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@SuppressWarnings("unused")
@Aspect
public class LoggableAspect {

  private Logger log;
  private static final Gson gson =
      new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

  /**
   * beforeMethodAop.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   */
  @Before("@annotation(org.autong.annotation.Loggable) && execution(* *(..))")
  public void beforeMethodAop(JoinPoint joinPoint) {
    StepAspect.startNestedStep(joinPoint);
    log = LoggerUtil.getLogger(getMethodSignature(joinPoint));

    if (joinPoint.getArgs().length != 0) {
      String message =
          MessageFormat.format("args: {0}", formatMethodArguments(joinPoint.getArgs()));
      log.trace(message);
    }
  }

  /**
   * afterMethodAop.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @param result a {@link java.lang.Object} object
   */
  @AfterReturning(
      pointcut = "@annotation(org.autong.annotation.Loggable) && execution(* *(..))",
      returning = "result")
  public void afterMethodAop(JoinPoint joinPoint, Object result) {

    if (result != null) {
      String message = MessageFormat.format("result: {0}", formatMethodReturnType(result));
      log.trace(message);
    }
    StepAspect.finishNestedStep(joinPoint);
  }

  /**
   * afterThrowAop.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @param ex a {@link java.lang.Throwable} object
   */
  @AfterThrowing(
      pointcut = "@annotation(org.autong.annotation.Loggable) && execution(* *(..))",
      throwing = "ex")
  public void afterThrowAop(JoinPoint joinPoint, Throwable ex) {
    log.debug(ExceptionUtils.getMessage(ex));
    StepAspect.failedNestedStep(joinPoint, ex);
  }

  /**
   * getMethodSignature.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @return a {@link java.lang.String} object
   */
  public static String getMethodSignature(JoinPoint joinPoint) {
    return joinPoint.getSignature().getDeclaringTypeName()
        + "."
        + joinPoint.getSignature().getName()
        + "()";
  }

  /**
   * formatMethodArguments.
   *
   * @param args an array of {@link java.lang.Object} objects
   * @return a {@link java.lang.String} object
   */
  public static String formatMethodArguments(Object[] args) {
    try {
      return StringEscapeUtils.unescapeJava(DataUtil.jsonToYaml(gson.toJson(args)));
    } catch (Exception ex) {
      return Arrays.toString(args);
    }
  }

  /**
   * formatMethodReturnType.
   *
   * @param result a {@link java.lang.Object} object
   * @return a {@link java.lang.String} object
   */
  public static String formatMethodReturnType(Object result) {
    try {
      return StringEscapeUtils.unescapeJava(DataUtil.jsonToYaml(gson.toJson(result)));
    } catch (Exception ex) {
      return MessageFormat.format("{0}", result);
    }
  }
}

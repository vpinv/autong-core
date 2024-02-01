package org.autong.aspect;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.annotations.Step;
import com.epam.reportportal.aspect.StepRequestUtils;
import com.epam.reportportal.service.Launch;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * StepAspect class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class StepAspect {

  private StepAspect() {}

  /**
   * startNestedStep.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @since 1.0.5
   */
  public static void startNestedStep(JoinPoint joinPoint) {
    Step step = getStep(joinPoint);
    if (step == null || step.isIgnored()) {
      return;
    }

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    StartTestItemRQ startStepRequest =
        StepRequestUtils.buildStartStepRequest(signature, step, joinPoint);
    ofNullable(Launch.currentLaunch())
        .ifPresent(l -> l.getStepReporter().startNestedStep(startStepRequest));
  }

  /**
   * finishNestedStep.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @since 1.0.5
   */
  public static void finishNestedStep(JoinPoint joinPoint) {
    Step step = getStep(joinPoint);
    if (step == null || step.isIgnored()) {
      return;
    }
    ofNullable(Launch.currentLaunch()).ifPresent(l -> l.getStepReporter().finishNestedStep());
  }

  /**
   * failedNestedStep.
   *
   * @param joinPoint a {@link org.aspectj.lang.JoinPoint} object
   * @param throwable a {@link java.lang.Throwable} object
   * @since 1.0.5
   */
  public static void failedNestedStep(JoinPoint joinPoint, final Throwable throwable) {
    Step step = getStep(joinPoint);
    if (step == null || step.isIgnored()) {
      return;
    }
    ofNullable(Launch.currentLaunch())
        .ifPresent(l -> l.getStepReporter().finishNestedStep(throwable));
  }

  private static Step getStep(JoinPoint joinPoint) {
    try {
      List<Class<?>> argClassList = new ArrayList<>();
      Arrays.stream(joinPoint.getArgs()).toList().forEach(arg -> argClassList.add(arg.getClass()));
      Class<?> instance =
          joinPoint.getThis() == null ? joinPoint.getClass() : joinPoint.getThis().getClass();

      return instance
          .getMethod(joinPoint.getSignature().getName(), argClassList.toArray(Class[]::new))
          .getAnnotation(Step.class);
    } catch (Exception ex) {
      return null;
    }
  }
}

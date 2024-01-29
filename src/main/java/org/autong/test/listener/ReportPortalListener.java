package org.autong.test.listener;

import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.BaseTestNGListener;
import com.epam.reportportal.testng.ITestNGService;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.utils.MemoizingSupplier;
import java.util.function.Supplier;
import javax.validation.constraints.NotNull;
import org.testng.ITestResult;

/**
 * ReportPortalListener class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public class ReportPortalListener extends BaseTestNGListener {
  /** Constant <code>SERVICE</code> */
  public static final Supplier<ITestNGService> SERVICE =
      new MemoizingSupplier<>(() -> new ReportPortalService(ReportPortal.builder().build()));

  /**
   * Constructor for ReportPortalListener.
   *
   * @since 1.0.1
   */
  public ReportPortalListener() {
    super(SERVICE.get());
  }

  public static class ReportPortalService extends TestNGService {

    public ReportPortalService(@NotNull ReportPortal reportPortal) {
      super(reportPortal);
    }

    @Override
    protected String createStepName(ITestResult testResult) {
      if (testResult.getTestName() != null) {
        return testResult.getTestName();
      }
      return super.createStepName(testResult);
    }
  }
}

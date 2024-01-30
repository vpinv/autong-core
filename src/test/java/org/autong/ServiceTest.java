package org.autong;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.autong.enums.ClientType;
import org.autong.enums.HttpMethod;
import org.autong.service.Service;
import org.autong.service.rest.model.Request;
import org.autong.service.rest.model.Response;
import org.autong.settings.Settings;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * ServiceTest class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class ServiceTest {
  /** test. */
  @Test
  public void test() {
    Service service = new Service(Settings.builder().build(), ClientType.REST);
    Request request =
        Request.builder()
            .baseUri("https://petstore.swagger.io")
            .basePath("/v2/pet/1")
            .method(HttpMethod.GET)
            .headers(ImmutableMap.ofEntries(Map.entry("accept", "application/json")))
            .build();
    Response response = service.resolve(request);
    Assert.assertEquals(response.getStatusCode(), 200);
  }
}

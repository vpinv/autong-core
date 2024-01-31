package org.autong;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import java.util.Map;
import org.autong.config.Settings;
import org.autong.enums.ClientType;
import org.autong.enums.HttpMethod;
import org.autong.service.ClientFactory;
import org.autong.service.rest.RestAssuredClient;
import org.autong.service.rest.model.Request;
import org.autong.service.rest.model.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * ServiceTest class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
public class ServiceTest {
  /**
   * test.
   *
   * @since 1.0.4
   */
  @Test
  public void test() {
    RestAssuredClient client =
        (RestAssuredClient)
            ClientFactory.getClient(ClientType.REST, Settings.builder().build(), new JsonObject());

    Request request =
        Request.builder()
            .baseUri("https://petstore.swagger.io")
            .basePath("/v2/pet/1")
            .method(HttpMethod.GET)
            .headers(ImmutableMap.ofEntries(Map.entry("accept", "application/json")))
            .build();
    Response response = client.resolve(request);
    Assert.assertEquals(response.getStatusCode(), 400);
  }
}

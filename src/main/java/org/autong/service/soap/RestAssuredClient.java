package org.autong.service.soap;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.specification.RequestSpecification;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import org.autong.annotation.Loggable;
import org.autong.service.Client;
import org.autong.service.soap.model.Request;
import org.autong.service.soap.model.Response;

/**
 * RestAssuredClient class.
 *
 * @version 1.0.3
 * @since 1.0.3
 */
public class RestAssuredClient implements Client<Request, Response> {

  /** {@inheritDoc} */
  @Override
  public Response resolve(Request request) {
    RequestSpecification requestSpecification = buildRequest(request);

    io.restassured.response.Response response;
    switch (request.getMethod()) {
      case GET -> response = RestAssured.given().spec(requestSpecification).get();
      case POST -> response = RestAssured.given().spec(requestSpecification).post();
      case PUT -> response = RestAssured.given().spec(requestSpecification).put();
      case PATCH -> response = RestAssured.given().spec(requestSpecification).patch();
      case DELETE -> response = RestAssured.given().spec(requestSpecification).delete();
      default -> response = new RestAssuredResponseImpl();
    }

    return buildResponse(request, response);
  }

  // region private methods

  @Loggable
  private static RequestSpecification buildRequest(Request request) {
    RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
    requestSpecBuilder.setBaseUri(request.getBaseUri());
    requestSpecBuilder.setBasePath(request.getBasePath());

    if (request.getHeaders() != null) {
      requestSpecBuilder.addHeaders(request.getHeaders());
    }

    if (request.getBody() != null) {
      switch (request.getHeaders().get("Content-Type")) {
        case "application/soap+xml; charset=utf-8" -> requestSpecBuilder.setBody(request.getBody());
        default -> throw new InvalidParameterException();
      }
    }

    return requestSpecBuilder.build();
  }

  @Loggable
  private static Response buildResponse(
      Request request, io.restassured.response.Response restassuredResponse) {
    Response response =
        Response.builder()
            .request(request)
            .statusCode(restassuredResponse.getStatusCode())
            .statusLine(restassuredResponse.getStatusLine())
            .contentType(restassuredResponse.getContentType())
            .build();

    Map<String, String> headers = new HashMap<>();
    restassuredResponse
        .getHeaders()
        .forEach(header -> headers.put(header.getName(), header.getValue()));
    response.setHeaders(headers);

    if (restassuredResponse.getBody() != null) {

      String responseBody = restassuredResponse.getBody().prettyPrint();
      response.setBody(responseBody);
    }

    if (restassuredResponse.getCookies() != null) {
      response.setCookies(restassuredResponse.getCookies());
    }

    return response;
  }

  // endregion
}

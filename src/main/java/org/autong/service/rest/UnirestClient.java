package org.autong.service.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestConfigException;
import kong.unirest.UnirestInstance;
import kong.unirest.gson.GsonObjectMapper;
import org.autong.annotation.Loggable;
import org.autong.config.Settings;
import org.autong.service.AbstractClient;
import org.autong.service.rest.model.Request;
import org.autong.service.rest.model.Response;
import org.autong.util.DataUtil;

/**
 * UnirestClient class.
 *
 * @version 1.0.3
 * @since 1.0.3
 */
public class UnirestClient extends AbstractClient<UnirestClient, Request, Response> {

  /**
   * Constructor for UnirestClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   */
  protected UnirestClient(Settings settings, JsonObject request) {
    super(settings, DataUtil.toObject(request, Request.class));
  }

  /** {@inheritDoc} */
  @Override
  public Class<Request> getRequestType() {
    return Request.class;
  }

  /** {@inheritDoc} */
  @Override
  public Class<Response> getResponseType() {
    return Response.class;
  }

  /** {@inheritDoc} */
  @Override
  public Request mergeRequest(Request newRequest) {
    JsonObject source = DataUtil.toJsonObject(newRequest);
    JsonObject target = DataUtil.toJsonObject(Request.builder().build());
    if (this.getBaseRequest() != null) {
      target = DataUtil.toJsonObject(this.getBaseRequest());
    }

    if (source.has("ignoreBaseHeaders") && source.get("ignoreBaseHeaders").getAsBoolean()) {
      target.remove("headers");
    }

    JsonObject mergedRequest = DataUtil.deepMerge(source, target);
    return DataUtil.getGson().fromJson(mergedRequest, Request.class);
  }

  /** {@inheritDoc} */
  @Override
  public Response resolve(Request request) {
    if (request.getMultiPart() != null) {
      return resolveMultiPartRequest(request);
    } else {
      return resolveNormalRequest(request);
    }
  }

  // region private loggable methods

  @Loggable
  private static UnirestInstance createInstance(Request request) {
    UnirestInstance instance = Unirest.spawnInstance();
    instance.config().defaultBaseUrl(request.getBaseUri());

    if (!request.isVerifySsl()) {
      instance.config().verifySsl(false);
    }

    if (request.getHeaders() != null) {
      request.getHeaders().forEach(instance.config()::addDefaultHeader);
    }

    return instance;
  }

  @Loggable
  private static Response buildTextResponse(
      Request request, HttpResponse<JsonNode> unirestResponse) {
    Response response =
        Response.builder()
            .request(request)
            .statusCode(unirestResponse.getStatus())
            .statusLine(unirestResponse.getStatusText())
            .contentType(unirestResponse.getHeaders().getFirst("Content-Type"))
            .build();

    Map<String, String> headers = new HashMap<>();
    unirestResponse
        .getHeaders()
        .all()
        .forEach(header -> headers.put(header.getName(), header.getValue()));
    response.setHeaders(headers);

    if (unirestResponse.getBody() != null) {
      String responseBody = unirestResponse.getBody().toPrettyString();
      response.setBody(responseBody);
    } else if (unirestResponse.getParsingError().isPresent()) {
      String responseBody = unirestResponse.getParsingError().get().getOriginalBody();
      response.setBody(responseBody);
    }

    if (unirestResponse.getCookies() != null) {
      Map<String, String> cookies = new HashMap<>();
      unirestResponse
          .getCookies()
          .forEach(cookie -> headers.put(cookie.getName(), cookie.getValue()));
      response.setCookies(cookies);
    }

    return response;
  }

  @Loggable
  private static Response buildByteArrayResponse(
      Request request, HttpResponse<byte[]> unirestResponse) {
    Response response =
        Response.builder()
            .request(request)
            .statusCode(unirestResponse.getStatus())
            .statusLine(unirestResponse.getStatusText())
            .contentType(unirestResponse.getHeaders().getFirst("Content-Type"))
            .build();

    Map<String, String> headers = new HashMap<>();
    unirestResponse
        .getHeaders()
        .all()
        .forEach(header -> headers.put(header.getName(), header.getValue()));
    response.setHeaders(headers);

    if (unirestResponse.getBody() != null) {
      String responseBody = Arrays.toString(unirestResponse.getBody());
      response.setBody(responseBody);
    } else if (unirestResponse.getParsingError().isPresent()) {
      String responseBody = unirestResponse.getParsingError().get().getOriginalBody();
      response.setBody(responseBody);
    }

    if (request.getResponseType() != null
        && (request.getResponseType().equals(Request.ResponseType.BYTE_ARRAY))) {
      response.setBytes(unirestResponse.getBody());
      response.setStream(new ByteArrayInputStream(response.getBytes()));
    }

    if (unirestResponse.getCookies() != null) {
      Map<String, String> cookies = new HashMap<>();
      unirestResponse
          .getCookies()
          .forEach(cookie -> headers.put(cookie.getName(), cookie.getValue()));
      response.setCookies(cookies);
    }

    return response;
  }

  @Loggable
  private static Map<String, Object> buildMultiPartBody(Request request) {
    Map<String, Object> map = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : request.getMultiPart().entrySet()) {
      if (entry.getValue().isJsonArray()) {
        for (JsonElement filePath : entry.getValue().getAsJsonArray()) {
          map.put(entry.getKey(), new File(filePath.getAsString()));
        }
      } else {
        map.put(entry.getKey(), entry.getValue().getAsString());
      }
    }
    return map;
  }

  // endregion

  // region private non loggable methods

  private static Response resolveMultiPartRequest(Request request) {
    Response response;
    MultipartBody multipartBody;
    UnirestInstance unirest = createInstance(request);
    request.getHeaders().remove("Content-Type");

    Map<String, Object> body = buildMultiPartBody(request);
    switch (request.getMethod()) {
      case POST -> multipartBody = unirest.post(request.getBasePath()).fields(body);
      case PUT -> multipartBody = unirest.put(request.getBasePath()).fields(body);
      case PATCH -> multipartBody = unirest.patch(request.getBasePath()).fields(body);
      default -> throw new UnirestConfigException(
          "Invalid method type " + request.getMethod().name());
    }

    if (request.getResponseType() != null
        && request.getResponseType().equals(Request.ResponseType.BYTE_ARRAY)) {
      HttpResponse<byte[]> unirestResponse = multipartBody.asBytes();
      response = buildByteArrayResponse(request, unirestResponse);
    } else {
      HttpResponse<JsonNode> unirestResponse = multipartBody.asJson();
      response = buildTextResponse(request, unirestResponse);
    }

    unirest.close();
    return response;
  }

  private static Response resolveNormalRequest(Request request) {
    Response response;
    HttpRequestWithBody requestWithBody = null;
    GetRequest getRequest = null;
    UnirestInstance unirest = createInstance(request);

    switch (request.getMethod()) {
      case GET -> getRequest = unirest.get(request.getBasePath());
      case POST -> requestWithBody = unirest.post(request.getBasePath());
      case PUT -> requestWithBody = unirest.put(request.getBasePath());
      case PATCH -> requestWithBody = unirest.patch(request.getBasePath());
      case DELETE -> requestWithBody = unirest.delete(request.getBasePath());
      default -> throw new UnirestConfigException(
          "Invalid method type " + request.getMethod().name());
    }

    if (request.getResponseType() != null
        && (request.getResponseType().equals(Request.ResponseType.BYTE_ARRAY))) {
      response = resolveByteArrayResponse(request, getRequest, requestWithBody);
    } else {
      response = resolveTextResponse(request, getRequest, requestWithBody);
    }

    unirest.close();
    return response;
  }

  private static Response resolveByteArrayResponse(
      Request request, GetRequest getRequest, HttpRequestWithBody requestWithBody) {
    Response response = Response.builder().build();
    HttpResponse<byte[]> unirestResponse = null;
    if (getRequest != null) {
      unirestResponse = getRequest.asBytes();
    } else if (requestWithBody != null) {
      if (request.getBody() == null) {
        unirestResponse = requestWithBody.asBytes();
      } else {
        unirestResponse = requestWithBody.body(request.getBody()).asBytes();
      }
    }

    if (unirestResponse != null) {
      response = buildByteArrayResponse(request, unirestResponse);
    }

    return response;
  }

  private static Response resolveTextResponse(
      Request request, GetRequest getRequest, HttpRequestWithBody requestWithBody) {
    Response response = Response.builder().build();
    HttpResponse<JsonNode> unirestResponse = null;
    if (getRequest != null) {
      unirestResponse = getRequest.asJson();
    } else if (requestWithBody != null) {
      if (request.getBody() == null) {
        unirestResponse = requestWithBody.withObjectMapper(new GsonObjectMapper()).asJson();
      } else {
        unirestResponse =
            requestWithBody
                .body(request.getBody())
                .withObjectMapper(new GsonObjectMapper())
                .asJson();
      }
    }
    if (unirestResponse != null) {
      response = buildTextResponse(request, unirestResponse);
    }

    return response;
  }

  // endregion
}

package org.autong.service.cache;

import com.google.gson.JsonObject;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.autong.config.Settings;
import org.autong.enums.CacheRequestType;
import org.autong.service.AbstractClient;
import org.autong.service.cache.model.Request;
import org.autong.service.cache.model.Response;
import org.autong.util.DataUtil;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;

/**
 * RedisClient class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
public class RedisClient extends AbstractClient<RedisClient, Request, Response> {

  /**
   * Constructor for RedisClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   */
  protected RedisClient(Settings settings, JsonObject request) {
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
  public Response resolve(Request request) {
    Response response;
    switch (request.getType()) {
      case GET, GET_ALL -> response = this.get(request);
      case SET, SET_ALL -> response = this.set(request);
      case EXIST -> response = this.contains(request);
      case DELETE -> response = this.delete(request);
      default -> throw new InvalidParameterException();
    }
    return response;
  }

  // region private methods

  private Response get(Request request) {
    Response response = Response.builder().build();
    JsonObject result = new JsonObject();
    if (isCluster(request)) {
      if (request.getType().equals(CacheRequestType.GET)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                (request.getBody().getField() != null)
                    ? this.getJedisCluster(request)
                        .hget(request.getBody().getKey(), request.getBody().getField())
                    : this.getJedisCluster(request).get(request.getBody().getKey())));

      } else if (request.getType().equals(CacheRequestType.GET_ALL)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                this.getJedisCluster(request).hgetAll(request.getBody().getKey())));
      }
    } else {
      if (request.getType().equals(CacheRequestType.GET)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                (request.getBody().getField() != null)
                    ? this.getJedis(request)
                        .hget(request.getBody().getKey(), request.getBody().getField())
                    : this.getJedis(request).get(request.getBody().getKey())));
      } else if (request.getType().equals(CacheRequestType.GET_ALL)) {
        result.add(
            "data",
            DataUtil.toJsonElement(this.getJedis(request).hgetAll(request.getBody().getKey())));
      }
    }
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(result));
    return response;
  }

  private Response contains(Request request) {
    Response response = Response.builder().build();
    JsonObject result = new JsonObject();
    result.add(
        "data",
        DataUtil.toJsonElement(
            (isCluster(request))
                ? this.getJedisCluster(request).exists(request.getBody().getKey())
                : this.getJedis(request).exists(request.getBody().getKey())));
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(result));
    return response;
  }

  /**
   * delete.
   *
   * @param request a {@link org.autong.service.cache.model.Request} object
   * @return a {@link org.autong.service.cache.model.Response} object
   */
  public Response delete(Request request) {
    Response response = Response.builder().build();
    JsonObject result = new JsonObject();
    result.add(
        "data",
        DataUtil.toJsonElement(
            (isCluster(request))
                ? this.getJedisCluster(request).del(request.getBody().getKey())
                : this.getJedis(request).del(request.getBody().getKey())));
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(result));
    return response;
  }

  private Response set(Request request) {

    Response response = Response.builder().build();

    JsonObject result = new JsonObject();
    if (isCluster(request)) {
      if (request.getType().equals(CacheRequestType.SET)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                (request.getBody().getField() != null)
                    ? this.getJedisCluster(request)
                        .hset(
                            request.getBody().getKey(),
                            request.getBody().getField(),
                            request.getBody().getValue())
                    : this.getJedisCluster(request)
                        .set(request.getBody().getKey(), request.getBody().getValue())));
      } else if (request.getType().equals(CacheRequestType.SET_ALL)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                this.getJedisCluster(request)
                    .hset(request.getBody().getKey(), request.getBody().getValueMap())));
      }
    } else {
      if (request.getType().equals(CacheRequestType.SET)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                (request.getBody().getField() != null)
                    ? this.getJedis(request)
                        .hset(
                            request.getBody().getKey(),
                            request.getBody().getField(),
                            request.getBody().getValue())
                    : this.getJedis(request)
                        .set(request.getBody().getKey(), request.getBody().getValue())));
      } else if (request.getType().equals(CacheRequestType.SET_ALL)) {
        result.add(
            "data",
            DataUtil.toJsonElement(
                this.getJedis(request)
                    .hset(request.getBody().getKey(), request.getBody().getValueMap())));
      }
    }
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(result));
    return response;
  }

  private JedisPooled getJedis(Request request) {
    JedisPooled jedis;
    JedisClientConfig config =
        DefaultJedisClientConfig.builder()
            .user(request.getUserName())
            .password(request.getPassword())
            .build();
    jedis = new JedisPooled(this.getJedisClusterNodes(request).stream().toList().get(0), config);
    return jedis;
  }

  private JedisCluster getJedisCluster(Request request) {
    JedisCluster jedisCluster;
    JedisClientConfig config =
        DefaultJedisClientConfig.builder()
            .user(request.getUserName())
            .password(request.getPassword())
            .build();
    jedisCluster = new JedisCluster(this.getJedisClusterNodes(request), config);
    return jedisCluster;
  }

  private Set<HostAndPort> getJedisClusterNodes(Request request) {
    Set<HostAndPort> jedisClusterNodes = new HashSet<>();
    Set<Map.Entry<String, Integer>> entrySet = request.getHostAndPort().entrySet();
    for (Map.Entry<String, Integer> entry : entrySet) {
      jedisClusterNodes.add(new HostAndPort(entry.getKey(), entry.getValue()));
    }
    return jedisClusterNodes;
  }

  private boolean isCluster(Request request) {
    return request.getHostAndPort().size() > 1;
  }

  // #endregion

}

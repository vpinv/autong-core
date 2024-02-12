package org.autong.service.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.security.InvalidParameterException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.autong.annotation.Runnable;
import org.autong.config.Settings;
import org.autong.exception.CoreException;
import org.autong.service.AbstractClient;
import org.autong.service.database.model.Request;
import org.autong.service.database.model.Response;
import org.autong.util.DataUtil;

/**
 * PostgresClient class.
 *
 * @version 1.0.6
 * @since 1.0.6
 */
public class PostgresClient extends AbstractClient<PostgresClient, Request, Response> {
  private Connection connection;

  /**
   * Constructor for PostgresClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   */
  public PostgresClient(Settings settings, JsonObject request) {
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
  public Request mergeRequest(Request request) {
    return this.mergeRequest(request, Request.builder().build());
  }

  /** {@inheritDoc} */
  @Override
  public Request mergeRequest(JsonObject request) {
    return this.mergeRequest(DataUtil.toObject(request, Request.class));
  }

  /** {@inheritDoc} */
  @Override
  @Runnable
  public Response resolve(Request request) {
    Response response;
    switch (request.getType()) {
      case FETCH -> response = this.fetch(request);
      case ADD -> response = this.add(request);
      case UPDATE -> response = this.update(request);
      case DELETE -> response = this.delete(request);
      default -> throw new InvalidParameterException();
    }
    return response;
  }

  // region private methods

  private Response fetch(Request request) {
    try {
      QueryRunner queryRunner = new QueryRunner();
      List<Map<String, Object>> result =
          queryRunner.query(this.getConnection(request), request.getQuery(), new MapListHandler());

      List<Map<String, String>> formattedResult = new ArrayList<>();
      result.forEach(
          map -> {
            Map<String, String> newMap = new HashMap<>();
            map.forEach((key, value) -> newMap.put(key, this.convertToString(value)));
            formattedResult.add(newMap);
          });

      JsonArray jsonArray = DataUtil.getGson().toJsonTree(formattedResult).getAsJsonArray();
      return Response.builder()
          .request(request)
          .result(Response.Result.builder().data(jsonArray).build())
          .build();
    } catch (SQLException se) {
      throw new CoreException("Couldn't query the database.", se);
    } finally {
      this.closeConnection();
    }
  }

  private Response add(Request request) {
    throw new NotImplementedException("Function not in use!");
  }

  private Response update(Request request) {
    try {
      QueryRunner queryRunner = new QueryRunner();
      int noOfUpdatedRows = queryRunner.update(this.getConnection(request), request.getQuery());
      JsonObject metaData = new JsonObject();
      metaData.addProperty("updatedRows", noOfUpdatedRows);
      return Response.builder()
          .request(request)
          .result(Response.Result.builder().metaData(metaData).build())
          .build();
    } catch (SQLException se) {
      throw new CoreException("Update query failed.", se);
    } finally {
      this.closeConnection();
    }
  }

  private Response delete(Request request) {
    try {
      QueryRunner queryRunner = new QueryRunner();
      int noOfUpdatedRows = queryRunner.update(this.getConnection(request), request.getQuery());
      JsonObject metaData = new JsonObject();
      metaData.addProperty("updatedRows", noOfUpdatedRows);
      return Response.builder()
          .request(request)
          .result(Response.Result.builder().metaData(metaData).build())
          .build();
    } catch (SQLException se) {
      throw new CoreException("Delete query failed.", se);
    } finally {
      this.closeConnection();
    }
  }

  private Connection getConnection(Request request) {
    Properties props = new Properties();
    props.setProperty("user", request.getUsername());
    props.setProperty("password", request.getPassword());
    props.setProperty("ssl", request.getSsl().toString());
    String url =
        MessageFormat.format(
            "jdbc:postgresql://{0}:{1}/{2}",
            request.getHost(), request.getPort(), request.getDbName());
    try {
      this.connection = DriverManager.getConnection(url, props);
      return this.connection;
    } catch (SQLException e) {
      throw new CoreException(e);
    }
  }

  private void closeConnection() {
    try {
      if (this.connection != null && !this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (SQLException e) {
      throw new CoreException(e);
    }
  }

  private String convertToString(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof String string) {
      return string.trim();
    }

    if (value instanceof Clob clob) {
      try {
        return clob.getSubString(1, (int) clob.length()).trim();
      } catch (SQLException ex) {
        throw new CoreException(ex);
      }
    }

    return value.toString().trim();
  }

  // endregion
}

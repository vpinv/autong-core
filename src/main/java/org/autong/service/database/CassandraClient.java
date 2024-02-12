package org.autong.service.database;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.autong.annotation.Runnable;
import org.autong.config.Settings;
import org.autong.exception.CoreException;
import org.autong.service.AbstractClient;
import org.autong.service.database.model.Request;
import org.autong.service.database.model.Response;
import org.autong.util.DataUtil;

/**
 * CassandraClient class.
 *
 * @version 1.0.6
 * @since 1.0.6
 */
public class CassandraClient extends AbstractClient<CassandraClient, Request, Response> {

  private CqlSession session;

  /**
   * Constructor for CassandraClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   */
  public CassandraClient(Settings settings, JsonObject request) {
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
      List<Row> rows =
          this.getConnection(request)
              .execute(SimpleStatement.builder(request.getQuery()).build())
              .all();
      ColumnDefinitions columnDefinitions = rows.get(0).getColumnDefinitions();
      List<Map<String, String>> result =
          rows.stream()
              .map(
                  row -> {
                    Map<String, String> colValue = new LinkedHashMap<>();
                    columnDefinitions.forEach(
                        columnDefinition ->
                            colValue.put(
                                String.valueOf(columnDefinition.getName()),
                                String.valueOf(row.getObject(columnDefinition.getName()))));
                    return colValue;
                  })
              .collect(Collectors.toList());
      JsonArray jsonArray = DataUtil.getGson().toJsonTree(result).getAsJsonArray();
      return Response.builder()
          .request(request)
          .result(Response.Result.builder().data(jsonArray).build())
          .build();
    } catch (InvalidQueryException se) {
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
      ResultSet result =
          this.getConnection(request).execute(SimpleStatement.builder(buildQuery(request)).build());
      JsonObject metaData = new JsonObject();
      metaData.addProperty("wasApplied", result.wasApplied());
      return Response.builder()
          .request(request)
          .result(Response.Result.builder().metaData(metaData).build())
          .build();
    } catch (InvalidQueryException se) {
      throw new CoreException("Update query failed.", se);
    } finally {
      this.closeConnection();
    }
  }

  private Response delete(Request request) {
    try {
      ResultSet result =
          this.getConnection(request).execute(SimpleStatement.builder(buildQuery(request)).build());
      JsonObject metaData = new JsonObject();
      metaData.addProperty("wasApplied", result.wasApplied());
      return Response.builder()
          .request(request)
          .result(Response.Result.builder().metaData(metaData).build())
          .build();
    } catch (InvalidQueryException se) {
      throw new CoreException("Delete query failed.", se);
    } finally {
      this.closeConnection();
    }
  }

  private CqlSession getConnection(Request request) {
    DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
    session =
        CqlSession.builder()
            .withConfigLoader(loader)
            .addContactPoint(
                new InetSocketAddress(request.getHost(), Integer.parseInt(request.getPort())))
            .withAuthCredentials(request.getUsername(), request.getPassword())
            .withKeyspace(request.getKeyspace())
            .withLocalDatacenter(request.getDatacenter())
            .build();
    return session;
  }

  private void closeConnection() {
    if (this.session != null && !this.session.isClosed()) {
      this.session.close();
    }
  }

  private String buildQuery(Request request) {
    return Boolean.TRUE.equals(request.getExecuteIfPresent())
        ? request.getQuery() + " IF EXISTS"
        : request.getQuery();
  }

  // endregion
}

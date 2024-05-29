package org.autong.service.database;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.autong.annotation.Runnable;
import org.autong.config.Settings;
import org.autong.exception.CoreException;
import org.autong.service.AbstractClient;
import org.autong.service.database.model.Request;
import org.autong.service.database.model.Response;
import org.autong.util.DataUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * MongoDbClient class.
 *
 * @version 1.0.6
 * @since 1.0.6
 */
public class MongoDbClient extends AbstractClient<MongoDbClient, Request, Response> {
  private MongoClient mongoClient;

  /**
   * Constructor for MongoDbClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public MongoDbClient(Settings settings, JsonObject request) {
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
      MongoCollection<Document> mongoCollection =
          this.getConnection(request)
              .getDatabase(request.getDbName())
              .getCollection(request.getCollection());

      List<String> fieldNames = new ArrayList<>();
      if (request.getParams().has("fieldNames")) {
        request
            .getParams()
            .get("fieldNames")
            .getAsJsonArray()
            .asList()
            .forEach(element -> fieldNames.add(element.getAsString()));
      }

      Bson projection = Projections.fields(Projections.include(fieldNames));

      FindIterable<Document> resultDocuments =
          mongoCollection
              .find(createFilter(request.getParams().get("queryFilter").getAsJsonObject()))
              .projection(projection);
      List<Document> result = new ArrayList<>();

      for (Document document : resultDocuments) {
        result.add(document);
      }
      JsonArray jsonArray = DataUtil.toJsonArray(result);
      for (int i = 0; i < jsonArray.size(); i++) {
        jsonArray.get(i).getAsJsonObject().addProperty("_id", result.get(i).get("_id").toString());
      }
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
      MongoCollection<Document> mongoCollection =
          this.getConnection(request)
              .getDatabase(request.getDbName())
              .getCollection(request.getCollection());

      long updatedRowCount =
          mongoCollection
              .updateOne(
                  createFilter(request.getParams().get("queryFilter").getAsJsonObject()),
                  new Document(
                      "$set",
                      createFilter(request.getParams().get("updateFilter").getAsJsonObject())))
              .getMatchedCount();

      JsonObject metaData = new JsonObject();
      metaData.addProperty("updatedRows", updatedRowCount);

      return Response.builder()
          .request(request)
          .result(Response.Result.builder().metaData(metaData).build())
          .build();
    } catch (InvalidQueryException se) {
      throw new CoreException("Couldn't query the database.", se);
    } finally {
      this.closeConnection();
    }
  }

  private Response delete(Request request) {
    try {
      MongoCollection<Document> mongoCollection =
          this.getConnection(request)
              .getDatabase(request.getDbName())
              .getCollection(request.getCollection());

      long deletedRowCount =
          mongoCollection
              .deleteOne(createFilter(request.getParams().get("queryFilter").getAsJsonObject()))
              .getDeletedCount();

      JsonObject metaData = new JsonObject();
      metaData.addProperty("deletedRows", deletedRowCount);

      return Response.builder()
          .request(request)
          .result(Response.Result.builder().metaData(metaData).build())
          .build();
    } catch (InvalidQueryException se) {
      throw new CoreException("Couldn't query the database.", se);
    } finally {
      this.closeConnection();
    }
  }

  private MongoClient getConnection(Request request) {
    String url;
    if (request.getUsername() != null) {
      url =
          MessageFormat.format(
              "mongodb://{0}:{1}@{2}:{3}",
              request.getUsername(), request.getPassword(), request.getHost(), request.getPort());
    } else {
      url = MessageFormat.format("mongodb://{0}:{1}", request.getHost(), request.getPort());
    }

    if (StringUtils.isNotEmpty(request.getAuthenticationDatabase())) {
      url = url + "/" + request.getAuthenticationDatabase();
    }

    try {
      if (request.getParams().has("queryParams")) {
        URIBuilder builder = new URIBuilder(url + "/");
        for (Map.Entry<String, JsonElement> param :
            request.getParams().get("queryParams").getAsJsonObject().asMap().entrySet()) {
          builder.addParameter(param.getKey(), param.getValue().getAsString());
        }
        url = builder.build().toString();
      }
    } catch (Exception e) {
      throw new CoreException("Couldn't parse url query params.", e);
    }

    mongoClient = new MongoClient(new MongoClientURI(url));
    return mongoClient;
  }

  private void closeConnection() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  private static Document createFilter(JsonObject filterObject) {
    Document filter = new Document();

    for (Map.Entry<String, JsonElement> entry : filterObject.entrySet()) {
      filter.append(entry.getKey(), entry.getValue().getAsString());
    }

    return filter;
  }

  // endregion
}

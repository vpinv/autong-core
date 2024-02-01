package org.autong.service.database.model;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;
import org.autong.enums.DatabaseRequestType;

/**
 * Request class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
@Data
@Builder
public class Request {
  private DatabaseRequestType type;
  private String host;
  private String port;
  private String username;
  private String password;
  private String dbName;
  private String keyspace;
  private String collection;
  private String query;
  @Builder.Default private JsonObject params = new JsonObject();
  @Builder.Default private String datacenter = "datacenter1";
  @Builder.Default private Boolean ssl = false;
  @Builder.Default private Boolean executeIfPresent = true;
  private String authenticationDatabase;
}

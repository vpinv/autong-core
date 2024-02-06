package org.autong.service.queue;

import com.google.gson.JsonObject;
import com.tibco.tibjms.TibjmsConnection;
import com.tibco.tibjms.TibjmsConnectionFactory;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.commons.collections4.MapUtils;
import org.autong.annotation.Runnable;
import org.autong.config.Settings;
import org.autong.service.AbstractClient;
import org.autong.service.queue.model.Request;
import org.autong.service.queue.model.Response;
import org.autong.util.DataUtil;
import org.springframework.jms.core.JmsTemplate;

/**
 * TibcoClient class.
 *
 * @version 1.0.5
 * @since 1.0.5
 */
public class TibcoClient extends AbstractClient<TibcoClient, Request, Response> {

  /**
   * Constructor for TibcoClient.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @param request a {@link com.google.gson.JsonObject} object
   */
  public TibcoClient(Settings settings, JsonObject request) {
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
      case PUBLISH -> response = this.publish(request);
      case RECEIVE -> response = this.receive(request);
      default -> throw new InvalidParameterException();
    }
    return response;
  }

  // region private methods

  private Response publish(Request request) {
    Response response = Response.builder().build();
    JsonObject responseBody = new JsonObject();
    try {
      JmsTemplate jmsTemplate = this.getConnectionFactory(request);
      jmsTemplate.convertAndSend(
          request.getQueueName(),
          request.getMessage(),
          messageProcessor -> {
            if (MapUtils.isNotEmpty(request.getHeaders())) {
              for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                messageProcessor.setStringProperty(entry.getKey(), entry.getValue());
              }
            }
            return messageProcessor;
          });
      responseBody.addProperty("status", "success");
    } catch (Exception ex) {
      responseBody.addProperty("status", "failed");
      response.setException(ex.getMessage());
    }
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(responseBody));
    return response;
  }

  private Response receive(Request request) {
    Response response = Response.builder().build();
    JsonObject responseBody = new JsonObject();
    try {
      TibjmsConnectionFactory out = new TibjmsConnectionFactory(request.getUrl());
      out.setUserName(request.getUsername());
      out.setUserPassword(request.getPassword());
      TibjmsConnection connection = (TibjmsConnection) out.createConnection();
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = session.createQueue(request.getQueueName());
      QueueBrowser queueBrowser =
          session.createBrowser(
              queue, "JMSTimestamp > " + (System.currentTimeMillis() - request.getPollTime()));
      Enumeration<Message> messageEnumeration =
          (Enumeration<Message>) queueBrowser.getEnumeration();

      List<String> textMessageList = new ArrayList<>();
      while (messageEnumeration.hasMoreElements()) {
        String message = ((TextMessage) messageEnumeration.nextElement()).getText();
        if (request.getParams().has("filter")) {
          String filterString = request.getParams().get("filter").getAsString();
          if (message.contains(filterString)) {
            textMessageList.add(message);
          }
        } else {
          textMessageList.add(message);
        }
      }

      queueBrowser.close();
      session.close();
      connection.close();
      responseBody.addProperty("status", "success");
      responseBody.add("message", DataUtil.toJsonArray(textMessageList));
    } catch (Exception ex) {
      responseBody.addProperty("status", "failed");
      response.setException(ex.getMessage());
    }
    response.setRequest(request);
    response.setBody(DataUtil.getGson().toJson(responseBody));
    return response;
  }

  private JmsTemplate getConnectionFactory(Request request) {
    TibjmsConnectionFactory connectionFactory = new TibjmsConnectionFactory(request.getUrl());
    connectionFactory.setUserName(request.getUsername());
    connectionFactory.setUserPassword(request.getPassword());
    connectionFactory.setConnAttemptCount(request.getConnAttemptCount());
    connectionFactory.setConnAttemptDelay(request.getConnAttemptDelay());
    connectionFactory.setConnAttemptTimeout(request.getConnAttemptTimeout());

    JmsTemplate template = new JmsTemplate();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  // endregion

}

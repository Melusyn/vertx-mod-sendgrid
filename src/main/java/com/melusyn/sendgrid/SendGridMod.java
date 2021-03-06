/*
 * Copyright 2014 Melusyn SAS
 *
 * Melusyn licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 *
 */
package com.melusyn.sendgrid;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGrid.Email;
import com.sendgrid.SendGrid.Response;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class SendGridMod extends Verticle {

  private SendGrid sendgrid;
  private static ObjectMapper jsonMapper = new ObjectMapper();

  static private final String MELUSYN_MAIL_ID = "mailuuid";

  private Logger logger = LoggerFactory.getLogger(getClass());

  private Map<String, Integer> templateSuppressionGroup = new HashMap<>();
  private String hostname = "unknown";

  @Override
  public void start() {
    super.start();
    EventBus eb = vertx.eventBus();

    String address = getOptionalStringConfig("address", "melusyn.sendgrid");
    String sendgridUsername = getMandatoryStringConfig("sendgrid_username");
    String sendgridPassword = getMandatoryStringConfig("sendgrid_password");

    JsonObject suppressionJson = container.config().getObject("suppressions", new JsonObject());
    for (String templateId : suppressionJson.getFieldNames()) {
      templateSuppressionGroup.put(templateId, suppressionJson.getInteger(templateId));
    }


    try {
      InetAddress ip = InetAddress.getLocalHost();
      hostname = ip.getHostName();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }


    sendgrid = new SendGrid(sendgridUsername, sendgridPassword);

    eb.registerHandler(address + ".send", this::sendEmail);
    eb.registerHandler(address + ".sendv2", this::sendEmailV2);
  }

  public void sendEmail(Message<JsonObject> message) {
    logger.debug("Unmarshalling SendGridMod request : " + message.body().encodePrettily());

    SendGridRequest sendGridRequest;
    try {
      sendGridRequest = jsonMapper.readValue(message.body().encode(), SendGridRequest.class);
    } catch (Exception e) {
      logger.error(e, e);
      message.fail(500, e.getMessage());
      return;
    }

    Email email = new Email();

    if (sendGridRequest.getTemplateId() != null) {
      email.addFilter("templates", "template_id", sendGridRequest.getTemplateId());
    }


    email.addTo(sendGridRequest.getTos().toArray(new String[sendGridRequest.getTos().size()]));
    email.addToName(sendGridRequest.getToNames().toArray(new String[sendGridRequest.getToNames().size()]));

    email.setFrom(sendGridRequest.getFrom());
    email.setFromName(sendGridRequest.getFromName());

    //This will replace <%subject%> tag in your template (if using a template)
    email.setSubject(sendGridRequest.getSubject());

    if (sendGridRequest.getBodyAsHtml()) {
      email.setHtml(sendGridRequest.getBody());
    }
    email.setText(sendGridRequest.getBody());

    sendGridRequest.getSubstitutions().forEach((key, value) ->
      email.addSubstitution(key, value.toArray(new String[value.size()]))
    );

    int suppressionGroupId = templateSuppressionGroup.getOrDefault(sendGridRequest.getTemplateId(), 0);
    if (suppressionGroupId != 0) {
      email.setASMGroupId(suppressionGroupId);
    }

    sendGridRequest.getContext().forEach(email::addUniqueArg);

    String uuid = UUID.randomUUID().toString();
    email.addUniqueArg(MELUSYN_MAIL_ID, uuid);

    try {
      Response response = sendgrid.send(email);

      if (response.getCode() != 200) {
        logger.debug("SendGrid failed and responded with : " + response.getCode() + " - " + response.getMessage());
        message.fail(response.getCode(), response.getMessage());
        return;
      }

      JsonObject jResponse = SendGridResponse.instance()
        .code(response.getCode())
        .message(response.getMessage())
        .status(response.getStatus())
        .toJson();

      logger.debug("SendGrid successfully responded with : " + jResponse.encode());
      message.reply(jResponse);

    } catch (Exception e) {
      logger.error(e, e);
      message.fail(500, e.getMessage());
    }
  }

  public void sendEmailV2(Message<JsonObject> message) {
    SendGridRequestV2 request;
    try {
      request = jsonMapper.readValue(message.body().encode(), SendGridRequestV2.class);
    } catch (IOException e) {
      logger.error(e);
      message.fail(400, e.getMessage());
      return;
    }

    if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
      String errorMessage = "Your request has no recipient. It must at least have one recipient";
      logger.error(errorMessage);
      message.fail(400, errorMessage);
      return;
    }

    Email email = new Email();

    if (request.getTemplateId() != null) {
      email.addFilter("templates", "template_id", request.getTemplateId());
    }

    email.setFrom(request.getFrom());
    email.setFromName(request.getFromName());
    email.setSubject(request.getSubject());

    if (request.getCcs() != null) {
      request.getCcs().forEach(recipient -> email.addCc(recipient.getEmail()));
    }

    if (request.getBccs() != null) {
      request.getBccs().forEach(recipient -> email.addBcc(recipient.getEmail()));
    }

    if (request.getReplyTo() != null) {
      email.setReplyTo(request.getReplyTo());
    }

    if (request.getSections() != null) {
      request.getSections().forEach(email::addSection);
    }

    if (request.getBodyAsHtml()) {
      email.setHtml(request.getBody());
    }
    email.setText(request.getBody());

    Map<String, List<String>> substitutions = new HashMap<>();
    request.getRecipients().forEach(recipient -> {
      email.addTo(recipient.getEmail(), recipient.getFullName());

      if (recipient.getSubstitutions() != null) {
        recipient.getSubstitutions().forEach((name, value) -> {
          List<String> substitution = substitutions.get(name);
          if (substitution == null) {
            substitution = new ArrayList<>();
            substitutions.put(name, substitution);
          }
          substitution.add(value);
        });
      }
    });

    if (request.getAttachments() != null) {
      request.getAttachments().forEach((name, attachment) -> {
        try {
          email.addAttachment(name, new ByteArrayInputStream(attachment));
        } catch (IOException ignored) {
          logger.error("Michel owes a beer to Hugo.", ignored);
        }
      });
    }

    substitutions.forEach((key, list) ->
      email.addSubstitution(key, list.toArray(new String[list.size()]))
    );

    int suppressionGroupId = templateSuppressionGroup.getOrDefault(request.getTemplateId(), 0);
    if (suppressionGroupId != 0) {
      email.setASMGroupId(suppressionGroupId);
    }

    request.getContext().forEach(email::addUniqueArg);

    String uuid = UUID.randomUUID().toString();
    email.addUniqueArg(MELUSYN_MAIL_ID, uuid);
    email.addUniqueArg("HOSTNAME", hostname);

    try {
      Response response = sendgrid.send(email);
      if (response.getCode() != 200) {
        JsonObject emailJson = new JsonObject(jsonMapper.writeValueAsString(request));
        logger.error("SendGrid failed and responded with : " + response.getCode() + " - " + response.getMessage() + " with email:" + emailJson.encodePrettily());
        message.fail(500, response.getMessage());
        logger.warn(message.body().encodePrettily());
        return;
      }

      JsonObject jResponse = SendGridResponse.instance()
        .code(response.getCode())
        .message(response.getMessage())
        .status(response.getStatus())
        .toJson();

      logger.debug("SendGrid successfully responded with : " + jResponse.encode());
      message.reply(jResponse);

    } catch (Exception e) {
      logger.error(e, e);
      message.fail(500, e.getMessage());
    }
  }

  protected String getMandatoryStringConfig(String fieldName) {
    String s = container.config().getString(fieldName);
    if (s == null) {
      throw new IllegalArgumentException(fieldName + " must be specified in config.");
    }
    return s;
  }

  protected String getOptionalStringConfig(String fieldName, String defaultValue) {
    String s = container.config().getString(fieldName);
    if (s == null) {
      return defaultValue;
    }
    return s;
  }

  @Override
  public void stop() {
    super.stop();
  }
}

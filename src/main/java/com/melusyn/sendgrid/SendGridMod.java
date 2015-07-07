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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendGridMod extends Verticle {

  private SendGrid sendgrid;
  static ObjectMapper jsonMapper = new ObjectMapper();

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void start() {
    super.start();
    EventBus eb = vertx.eventBus();

    String address = getOptionalStringConfig("address", "melusyn.sendgrid");
    String sendgridUsername = getMandatoryStringConfig("sendgrid_username");
    String sendgridPassword = getMandatoryStringConfig("sendgrid_password");

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

    //This will replace <%body%> tag in your template (if using a template)
    if (sendGridRequest.getBodyAsHtml()) {
      email.setHtml(sendGridRequest.getBody());
    } else {
      email.setText(sendGridRequest.getBody());
    }

    sendGridRequest.getSubstitutions().forEach((key, value) ->
      email.addSubstitution(key, value.toArray(new String[value.size()]))
    );

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
      request.getCcs().forEach(email::addCc);
    }

    if (request.getBccs() != null) {
      request.getBccs().forEach(email::addBcc);
    }

    if (request.getReplyTo() != null) {
      email.setReplyTo(request.getReplyTo());
    }

    //This will replace <%body%> tag in your template (if using a template)
    if (request.getBodyAsHtml()) {
      email.setHtml(request.getBody());
    } else {
      email.setText(request.getBody());
    }

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

    substitutions.forEach((key, list) ->
      email.addSubstitution(key, list.toArray(new String[list.size()]))
    );

    try {
      Response response = sendgrid.send(email);
      if (response.getCode() != 200) {
        logger.error("SendGrid failed and responded with : " + response.getCode() + " - " + response.getMessage());
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

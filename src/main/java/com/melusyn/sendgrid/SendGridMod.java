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

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGrid.Email;
import com.sendgrid.SendGrid.Response;

/*
 * @author Hugo Cordier
 */
public class SendGridMod extends BusModBase {

	private SendGrid sendgrid;
	private String address;
	static ObjectMapper jsonMapper = new ObjectMapper();
	
	@Override
	public void start() {
		super.start();
		
		address = getOptionalStringConfig("address", "melusyn.sendgrid");
		String sendgridUsername = getMandatoryStringConfig("sendgrid_username");
		String sendgridPassword = getMandatoryStringConfig("sendgrid_password");
		sendgrid = new SendGrid(sendgridUsername, sendgridPassword);
		
		eb.registerHandler(address+".send", (Message<JsonObject> message) -> {
			
			logger.debug("Unmarshalling SendGridMod request : "+message.body().encodePrettily());
			
			SendGridRequest sendGridRequest;
			try {
				sendGridRequest = jsonMapper.readValue(message.body().encode(), SendGridRequest.class);
			} catch (Exception e) {
				logger.error(e, e);
				message.fail(500, e.getMessage());
				return;
			}
		
			Email email = new Email();

			if (sendGridRequest.getTemplateId() != null ) {
				email.addFilter("templates", "template_id", sendGridRequest.getTemplateId());
			}
			email.addTo(sendGridRequest.getTos().toArray(new String[0]));
			email.addToName(sendGridRequest.getToNames().toArray(new String[0]));
			
			email.setFrom(sendGridRequest.getFrom());
			email.setFromName(sendGridRequest.getFromName());
			
			//This will replace <%subject%> tag in your template (if using a template)
			email.setSubject(sendGridRequest.getSubject());
			
			//This will replace <%body%> tag in your template (if using a template)
			email.setText(sendGridRequest.getBody());
			
			sendGridRequest.getSubstitutions().forEach((key, value) -> {
				email.addSubstitution(key, value.toArray(new String[0]));
			});
			
			try {
				Response response = sendgrid.send(email);
				
				if (response.getCode() != 200) {
					logger.debug("SendGrid failed and responded with : "+response.getCode()+" - "+response.getMessage());
					message.fail(response.getCode(), response.getMessage());
					return;
				}
				
				JsonObject jResponse = SendGridResponse.instance()
					.code(response.getCode())
					.message(response.getMessage())
					.status(response.getStatus())
					.toJson();
				
				logger.debug("SendGrid successfully responded with : "+jResponse.encode());
				message.reply(jResponse);
				
			} catch (Exception e) {
				logger.error(e, e);
				message.fail(500, e.getMessage());
			}
		});
	}
	
	@Override
	public void stop() {
		super.stop();
	}
}

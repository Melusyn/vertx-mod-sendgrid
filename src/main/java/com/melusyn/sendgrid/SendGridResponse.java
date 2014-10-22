package com.melusyn.sendgrid;

import org.vertx.java.core.json.JsonObject;

public class SendGridResponse {
	private int code;
	private String message;
	private boolean status;
	
	static SendGridResponse instance() {
		return new SendGridResponse();
	}
	
	public SendGridResponse code(int code) {
		this.code = code;
		return this;
	}
	
	public SendGridResponse message(String message) {
		this.message = message;
		return this;
	}
	
	public SendGridResponse status(boolean status) {
		this.status = status;
		return this;
	}
	
	public JsonObject toJson() {
		return new JsonObject()
			.putNumber("code", code)
			.putString("message", message)
			.putBoolean("status", status);
	}
}

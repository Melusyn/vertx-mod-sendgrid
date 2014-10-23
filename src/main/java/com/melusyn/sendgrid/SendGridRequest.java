package com.melusyn.sendgrid;

import java.util.List;
import java.util.Map;

public class SendGridRequest {
	
	private String templateId;
	private String from;
	private String fromName;
	private List<String> tos;
	private List<String> toNames;
	private String subject;
	private String body;
	private Map<String, List<String>> substitutions;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<String> getTos() {
		return tos;
	}

	public void setTos(List<String> tos) {
		this.tos = tos;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public List<String> getToNames() {
		return toNames;
	}

	public void setToNames(List<String> toNames) {
		this.toNames = toNames;
	}

	public Map<String, List<String>> getSubstitutions() {
		return substitutions;
	}

	public void setSubstitutions(Map<String, List<String>> substitutions) {
		this.substitutions = substitutions;
	}
	
}

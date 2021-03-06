package com.melusyn.sendgrid;

import java.util.HashMap;
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
  private Boolean bodyAsHtml;
	private Map<String, List<String>> substitutions;

	private Map<String, String> context = new HashMap<>();

  public Boolean getBodyAsHtml() {
    return bodyAsHtml;
  }

  public void setBodyAsHtml(Boolean bodyAsHtml) {
    this.bodyAsHtml = bodyAsHtml;
  }

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

	public Map<String, String> getContext() {
		return context;
	}

	public void setContext(Map<String, String> context) {
		this.context = context;
	}

	public <E extends Enum> void addContext(E contextKey, String value) {
		context.put(contextKey.toString(), value);
	}

	public void addContext(String contextKey, String value) {
		context.put(contextKey, value);
	}
}

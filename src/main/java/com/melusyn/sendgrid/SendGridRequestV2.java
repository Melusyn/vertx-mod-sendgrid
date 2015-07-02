package com.melusyn.sendgrid;

import java.util.List;
import java.util.Map;

public class SendGridRequestV2 {

  private String templateId;

  private String from;
  private String fromName;

  private String subject;
  private String body;
  private Boolean bodyAsHtml;

  private List<Recipient> recipients;

  public static class Recipient {
    protected String fullName;
    protected String email;

    protected Map<String, String> substitutions;

    public String getFullName() {
      return fullName;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public Map<String, String> getSubstitutions() {
      return substitutions;
    }

    public void setSubstitutions(Map<String, String> substitutions) {
      this.substitutions = substitutions;
    }
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Boolean getBodyAsHtml() {
    return bodyAsHtml;
  }

  public void setBodyAsHtml(Boolean bodyAsHtml) {
    this.bodyAsHtml = bodyAsHtml;
  }

  public List<Recipient> getRecipients() {
    return recipients;
  }

  public void setRecipients(List<Recipient> recipients) {
    this.recipients = recipients;
  }
}

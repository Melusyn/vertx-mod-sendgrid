package com.melusyn.sendgrid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SendGridRequestV2 {

  private String templateId;

  private String from;
  private String fromName;

  private Set<Recipient> ccs;
  private Set<Recipient> bccs;
  private String replyTo;

  private String subject;
  private String body;
  private boolean bodyAsHtml;

  private List<Recipient> recipients;
  private Map<String, String> sections;
  private Map<String, byte[]> attachments;

  private Map<String, String> context = new HashMap<>();

  public Map<String, byte[]> getAttachments() {
    return attachments;
  }

  public void setAttachments(Map<String, byte[]> attachments) {
    this.attachments = attachments;
  }

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

  public boolean getBodyAsHtml() {
    return bodyAsHtml;
  }

  public void setBodyAsHtml(boolean bodyAsHtml) {
    this.bodyAsHtml = bodyAsHtml;
  }

  public List<Recipient> getRecipients() {
    return recipients;
  }

  public void setRecipients(List<Recipient> recipients) {
    this.recipients = recipients;
  }

  public Set<Recipient> getCcs() {
    return ccs;
  }

  public void setCcs(Set<Recipient> ccs) {
    this.ccs = ccs;
  }

  public Set<Recipient> getBccs() {
    return bccs;
  }

  public void setBccs(Set<Recipient> bccs) {
    this.bccs = bccs;
  }

  public String getReplyTo() {
    return replyTo;
  }

  public void setReplyTos(String replyTo) {
    this.replyTo = replyTo;
  }

  public Map<String, String> getSections() {
    return sections;
  }

  public void setSections(Map<String, String> sections) {
    this.sections = sections;
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

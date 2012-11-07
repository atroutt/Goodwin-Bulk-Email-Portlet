package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class Message implements Serializable {
	private static final long serialVersionUID = 5015690733359120993L;

	private List<String> recipients;
	private Boolean sendAsDean = new Boolean(false);
	private String copyTo;
	private String replyTo;
	private String subject;
	private String body;
	private Boolean includeLogo = new Boolean(false);
	private String logo = "logo1";
	private List<MultipartFile> attachments = new ArrayList<MultipartFile>();

	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
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

	public List<MultipartFile> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<MultipartFile> attachments) {
		this.attachments = attachments;
	}

	public String getCopyTo() {
		return copyTo;
	}

	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public void setSendAsDean(Boolean sendAsDean) {
		this.sendAsDean = sendAsDean;
	}

	public Boolean getSendAsDean() {
		return sendAsDean;
	}

	public void setIncludeLogo(Boolean includeLogo) {
		this.includeLogo = includeLogo;
	}

	public Boolean getIncludeLogo() {
		return includeLogo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getLogo() {
		return logo;
	}
}

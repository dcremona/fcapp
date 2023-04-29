package common.mail;

import java.io.Serializable;

public class CLParser implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String from = null;
	private String to = null;
	private String cc = null;
	private String bcc = null;
	private String subject = null;
	private String message = null;
	private String att = null;

	public CLParser(String[] args) {
		from = args[0];
		to = args[1];
		subject = args[2];
		message = args[3];
		if (args.length > 4) {
			cc = args[4];
		}
		if (args.length > 5) {
			bcc = args[5];
		}
		if (args.length > 6) {
			att = args[6];
		}
	}

	public String getAtt() {
		return att;
	}

	public void setAtt(String att) {
		this.att = att;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

}

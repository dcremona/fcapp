package common.mail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;

public class MailClient{

	private static final Logger log = LoggerFactory.getLogger(MailClient.class);

	private JavaMailSenderImpl javaMailSender;

	public MailClient(JavaMailSenderImpl jMS) {
		javaMailSender = jMS;
	}

	public void sendMail(String from, String[] to, String[] cc, String[] bcc,
			String subject, String messageBody, String typeMessage,
			String priority, String[] attachments)
			throws IOException, MessagingException, AddressException {

		log.info("****************************************");
		log.info("INIZIO ESECUZIONE sendMail              ");
		log.info("****************************************");

		log.info(" from: " + from);
		log.info(" subject: " + subject);
		log.info(" message: " + messageBody);

		if (attachments != null) {

			MimeMessage msg = javaMailSender.createMimeMessage();
			msg.setFrom(from);

			MimeMessageHelper helper = new MimeMessageHelper(msg,true);
			helper.setFrom(from, "notifiche-fclt");
			helper.setTo(to);
			if (cc != null) {
				helper.setCc(cc);
			}
			if (bcc != null) {
				helper.setBcc(bcc);
			}

			helper.setSubject(subject);
			if (typeMessage.equals("text/html")) {
				helper.setText(messageBody, true);
			} else {
				msg.setText(messageBody);
			}

			for (int i = 0; i <= attachments.length - 1; i++) {
				File file = new File(attachments[i]);
				helper.addAttachment(file.getName(), file);
			}

			javaMailSender.send(msg);

		} else {

			if (typeMessage.equals("text/html")) {

				MimeMessage msg = javaMailSender.createMimeMessage();
				msg.setFrom(from);
				MimeMessageHelper helper = new MimeMessageHelper(msg,true);
				helper.setFrom(from, "notifiche-fclt");
				helper.setTo(to);
				if (cc != null) {
					helper.setCc(cc);
				}
				if (bcc != null) {
					helper.setBcc(bcc);
				}
				helper.setSubject(subject);
				helper.setText(messageBody, true);
				javaMailSender.send(msg);

			} else {

				SimpleMailMessage msg = new SimpleMailMessage();
				msg.setFrom(from);
				msg.setTo(to);
				if (cc != null) {
					msg.setCc(cc);
				}
				if (bcc != null) {
					msg.setBcc(bcc);
				}
				msg.setSubject(subject);
				msg.setText(messageBody);
				javaMailSender.send(msg);
			}
		}

		log.info("****************************************");
		log.info("FINE ESECUZIONE sendMail              ");
		log.info("****************************************");
	}

	public void sendMail2(String from, String[] to, String[] cc, String[] bcc,
			String subject, String messageBody, String typeMessage,
			String priority, Map<String, InputStream> images)
			throws IOException, MessagingException, AddressException {

		log.info("****************************************");
		log.info("INIZIO ESECUZIONE sendMail              ");
		log.info("****************************************");

		log.info(" from: " + from);
		log.info(" subject: " + subject);
		log.info(" message: " + messageBody);

		if (images != null) {

			MimeMessage msg = javaMailSender.createMimeMessage();
			msg.setFrom(from);

			MimeMessageHelper helper = new MimeMessageHelper(msg,true);
			helper.setFrom(from, "notifiche-fclt");
			helper.setTo(to);
			if (cc != null) {
				helper.setCc(cc);
			}
			if (bcc != null) {
				helper.setBcc(bcc);
			}

			helper.setSubject(subject);
			if (typeMessage.equals("text/html")) {
				helper.setText(messageBody, true);
			} else {
				msg.setText(messageBody);
			}

			Iterator<?> it = images.entrySet().iterator();
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry) it.next();

				InputStream inputStream = (InputStream) pairs.getValue();
				File somethingFile = File.createTempFile("test", ".png");
				try {
					FileUtils.copyInputStreamToFile(inputStream, somethingFile);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				} finally {
					IOUtils.closeQuietly(inputStream);
				}
				helper.addInline((String) pairs.getKey(), somethingFile);
			}

			javaMailSender.send(msg);

		} else {

			if (typeMessage.equals("text/html")) {

				MimeMessage msg = javaMailSender.createMimeMessage();
				msg.setFrom(from);
				MimeMessageHelper helper = new MimeMessageHelper(msg,true);
				helper.setFrom(from, "notifiche-fclt");
				helper.setTo(to);
				if (cc != null) {
					helper.setCc(cc);
				}
				if (bcc != null) {
					helper.setBcc(bcc);
				}
				helper.setSubject(subject);
				helper.setText(messageBody, true);
				javaMailSender.send(msg);

			} else {

				SimpleMailMessage msg = new SimpleMailMessage();
				msg.setFrom(from);
				msg.setTo(to);
				if (cc != null) {
					msg.setCc(cc);
				}
				if (bcc != null) {
					msg.setBcc(bcc);
				}
				msg.setSubject(subject);
				msg.setText(messageBody);
				javaMailSender.send(msg);

			}
		}

		log.info("****************************************");
		log.info("FINE ESECUZIONE sendMail              ");
		log.info("****************************************");

	}

}

// public class MailClient implements ConnectionListener,TransportListener{
//
// private static final Logger log = LoggerFactory.getLogger(MailClient.class);
//
// private static Properties mailProps = null;
//
// boolean debug = true;
//
// String mailServer = null;
// String user = null;
// String password = null;
// String mailer = this.getClass().getName();
//
// String ambiente = null;
// String mitt = null;
//
// /**
// * @return Returns the mailServer.
// */
// public String getMailServer() {
// return mailServer;
// }
//
// /**
// * @param mailServer
// * The mailServer to set.
// */
// public void setMailServer(String mailServer) {
// this.mailServer = mailServer;
// }
//
// /**
// * @return Returns the password.
// */
// public String getPassword() {
// return password;
// }
//
// /**
// * @param password
// * The password to set.
// */
// public void setPassword(String password) {
// this.password = password;
// }
//
// /**
// * @return Returns the user.
// */
// public String getUser() {
// return user;
// }
//
// /**
// * @param user
// * The user to set.
// */
// public void setUser(String user) {
// this.user = user;
// }
//
// public MailClient(Properties p) {
//
// try {
//
// mailProps = p;
//
// mailServer = mailProps.getProperty("mail.smtp.host");
// user = mailProps.getProperty("mail.user");
// password = mailProps.getProperty("mail.password");
//
// ambiente = mailProps.getProperty("ambiente");
// mitt = mailProps.getProperty("from");
//
// log.info(" ambiente: " + ambiente);
// log.info(" mailServer: " + mailServer);
// log.info(" user: " + user);
// log.info(" password: " + password);
// log.info(" from: " + mitt);
//
// } catch (Exception ex) {
// log.error("Error while reading properties ", ex);
// }
//
// }
//
// public void sendMail(String from, String[] to, String[] cc, String[] bcc,
// String subject, String messageBody, String typeMessage,
// String priority, String[] attachments) throws IOException,
// MessagingException, AddressException, NamingException {
//
// log.info("****************************************");
// log.info("INIZIO ESECUZIONE sendMail ");
// log.info("****************************************");
//
// if (from == null) {
// from = mitt;
// }
// log.info(" from: " + from);
// log.info(" subject: " + subject);
// log.info(" message: " + messageBody);
//
// Session session = buildSession();
//
// MimeMessage message = new MimeMessage(session);
// message.setFrom(new InternetAddress(from));
//
// InternetAddress[] addressTo = new InternetAddress[to.length];
// for (int t = 0; t < to.length; t++) {
// addressTo[t] = new InternetAddress(to[t]);
// }
// message.addRecipients(Message.RecipientType.TO, addressTo);
//
// InternetAddress[] addressCC = null;
// if (cc != null && cc.length != 0) {
// addressCC = new InternetAddress[cc.length];
// for (int c = 0; c < cc.length; c++) {
// addressCC[c] = new InternetAddress(cc[c]);
// }
// }
// message.setRecipients(Message.RecipientType.CC, addressCC);
//
// InternetAddress[] addressBcc = null;
// if (bcc != null && bcc.length != 0) {
// addressBcc = new InternetAddress[bcc.length];
// for (int bc = 0; bc < bcc.length; bc++) {
// addressBcc[bc] = new InternetAddress(bcc[bc]);
// }
// }
// message.setRecipients(Message.RecipientType.BCC, addressBcc);
//
// message.setSubject(subject);
//
// Calendar cal = Calendar.getInstance();
// Date date = cal.getTime();
// message.setSentDate(date);
//
// if (attachments != null) {
//
// MimeMultipart mp = new MimeMultipart();
//
// MimeBodyPart text = new MimeBodyPart();
// text.setDisposition(Part.INLINE);
//
// if (typeMessage.equals("text/html")) {
// text.setDataHandler(new DataHandler(new HTMLDataSource(messageBody)));
// } else {
// text.setContent(MimeUtility.encodeText(messageBody), typeMessage);
// }
// mp.addBodyPart(text);
//
// addAtachments(attachments, mp);
//
// message.setContent(mp);
//
// } else {
//
// if (typeMessage.equals("text/html")) {
// message.setDataHandler(new DataHandler(new HTMLDataSource(messageBody)));
// } else {
// message.setText(messageBody);
// }
// }
//
// message.setHeader("X-Mailer", mailer);
// if (!priority.equals("3")) {
// message.setHeader("X-Priority", priority);
// }
// message.saveChanges(); // implicit with send()
//
// Transport transport = session.getTransport("smtp");
// transport.addConnectionListener(this);
// transport.addTransportListener(this);
// if (ambiente.equals("SVILUPPO")) {
// transport.connect(mailServer, user, password);
// } else if (ambiente.equals("PRODUZIONE")) {
// transport.connect(user, password);
// }
// transport.sendMessage(message, message.getAllRecipients());
// transport.close();
//
// log.info("****************************************");
// log.info("FINE ESECUZIONE sendMail ");
// log.info("****************************************");
//
// }
//
// protected void addAtachments(String[] attachments, Multipart multipart)
// throws MessagingException, AddressException {
// for (int i = 0; i <= attachments.length - 1; i++) {
// MimeBodyPart file_part = new MimeBodyPart();
// File file = new File(attachments[i]);
// FileDataSource fds = new FileDataSource(file);
// DataHandler dh = new DataHandler(fds);
// file_part.setFileName(file.getName());
// file_part.setDisposition(Part.ATTACHMENT);
// file_part.setDescription("Attached file: " + file.getName());
// file_part.setDataHandler(dh);
// multipart.addBodyPart(file_part);
// }
// }
//
// /*
// * Inner class to act as a JAF datasource to send HTML e-mail content
// */
// class HTMLDataSource implements DataSource{
// private String html;
//
// public HTMLDataSource(String htmlString) {
// html = htmlString;
// }
//
// // Return html string in an InputStream.
// // A new stream must be returned each time.
// public InputStream getInputStream() throws IOException {
// if (html == null)
// throw new IOException("Null HTML");
// return new ByteArrayInputStream(html.getBytes());
// }
//
// public OutputStream getOutputStream() throws IOException {
// throw new IOException("This DataHandler cannot write HTML");
// }
//
// public String getContentType() {
// return "text/html";
// }
//
// public String getName() {
// return "JAF text/html dataSource to send e-mail only";
// }
// }
//
// public void sendMail2(String from, String[] to, String[] cc, String[] bcc,
// String subject, String messageBody, String typeMessage,
// String priority, Map<String, InputStream> images) throws IOException,
// MessagingException, AddressException, NamingException {
//
// log.info("****************************************");
// log.info("INIZIO ESECUZIONE sendMail ");
// log.info("****************************************");
//
// Session session = buildSession();
//
// SMTPMessage message = new SMTPMessage(session);
//
// message.setFrom(new InternetAddress(from));
//
// InternetAddress[] addressTo = new InternetAddress[to.length];
// for (int t = 0; t < to.length; t++) {
// addressTo[t] = new InternetAddress(to[t]);
// }
// message.addRecipients(Message.RecipientType.TO, addressTo);
//
// InternetAddress[] addressCC = null;
// if (cc != null && cc.length != 0) {
// addressCC = new InternetAddress[cc.length];
// for (int c = 0; c < cc.length; c++) {
// addressCC[c] = new InternetAddress(cc[c]);
// }
// }
// message.setRecipients(Message.RecipientType.CC, addressCC);
//
// InternetAddress[] addressBcc = null;
// if (bcc != null && bcc.length != 0) {
// addressBcc = new InternetAddress[bcc.length];
// for (int bc = 0; bc < bcc.length; bc++) {
// addressBcc[bc] = new InternetAddress(bcc[bc]);
// }
// }
// message.setRecipients(Message.RecipientType.BCC, addressBcc);
//
// message.setSubject(subject);
//
// Calendar cal = Calendar.getInstance();
// message.setSentDate(cal.getTime());
//
// MimeMultipart content = new MimeMultipart("related");
// // HTML part
// MimeBodyPart textPart = new MimeBodyPart();
// log.debug(messageBody);
// textPart.setText(messageBody, "US-ASCII", "html");
// content.addBodyPart(textPart);
//
// Iterator<?> it = images.entrySet().iterator();
// while (it.hasNext()) {
// @SuppressWarnings("rawtypes")
// Map.Entry pairs = (Map.Entry) it.next();
// // log.debug(pairs.getKey() + " = " + pairs.getValue());
// MimeBodyPart imagePart = new MimeBodyPart();
//
// InputStream inputStream = ( InputStream) pairs.getValue();
// File somethingFile = File.createTempFile("test", ".png");
// try {
// FileUtils.copyInputStreamToFile(inputStream, somethingFile);
// } catch (Exception e) {
// e.printStackTrace();
// log.error(e.getMessage());
// } finally {
// IOUtils.closeQuietly(inputStream);
// }
// imagePart.attachFile(somethingFile);
//
// imagePart.setContentID("<" + (String) pairs.getKey() + ">");
// imagePart.setDisposition(MimeBodyPart.INLINE);
// content.addBodyPart(imagePart);
// }
// message.setContent(content);
//
// Transport.send(message);
//
// log.info("****************************************");
// log.info("FINE ESECUZIONE sendMail ");
// log.info("****************************************");
// }
//
// public Session buildSession() {
//
// mailProps.put("mail.host", mailProps.getProperty("mail.smtp.host"));
//
// final PasswordAuthentication usernamePassword = new
// PasswordAuthentication(mailProps.getProperty("mail.user"),mailProps.getProperty("mail.password"));
// Authenticator auth = new Authenticator(){
// protected PasswordAuthentication getPasswordAuthentication() {
// return usernamePassword;
// }
// };
//
// Session session = Session.getInstance(mailProps, auth);
// session.setDebug(Boolean.parseBoolean(mailProps.getProperty("mail.debug")));
//
// return session;
// }
//
// // implement ConnectionListener interface
// public void opened(ConnectionEvent e) {
// // log.debug(">>> ConnectionListener.opened()");
// }
//
// public void disconnected(ConnectionEvent e) {
// // log.debug(">>> ConnectionListener.disconnected()");
// }
//
// public void closed(ConnectionEvent e) {
// // log.debug(">>> ConnectionListener.closed()");
// }
//
// // implement TransportListener interface
// public void messageDelivered(TransportEvent e) {
// log.info(">>> TransportListener.messageDelivered().");
// log.info(" Valid Addresses:");
// Address[] valid = e.getValidSentAddresses();
// if (valid != null) {
// for (int i = 0; i < valid.length; i++)
// log.info(" " + valid[i]);
// }
// }
//
// public void messageNotDelivered(TransportEvent e) {
// log.info(">>> TransportListener.messageNotDelivered().");
// log.info(" Invalid Addresses:");
// Address[] invalid = e.getInvalidAddresses();
// if (invalid != null) {
// for (int i = 0; i < invalid.length; i++)
// log.info(" " + invalid[i]);
// }
// }
//
// public void messagePartiallyDelivered(TransportEvent e) {
// // SMTPTransport doesn't partially deliver msgs
// log.info(">>> SMTPTransport doesn't partially deliver msgs");
// log.info(" partially Addresses:");
// Address[] invalid = e.getInvalidAddresses();
// if (invalid != null) {
// for (int i = 0; i < invalid.length; i++)
// log.info(" " + invalid[i]);
// }
// }
//
// }
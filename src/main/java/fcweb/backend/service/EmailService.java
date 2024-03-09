package fcweb.backend.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService{

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private JavaMailSender primarySender;
	private JavaMailSender secondarySender;

	public EmailService(
			@Qualifier("primarySender") JavaMailSender primarySender,
			@Qualifier("secondarySender") JavaMailSender secondarySender) {
		this.primarySender = primarySender;
		this.secondarySender = secondarySender;
	}

	public void sendPrimaryEmail(String from, String to, String subject,
			String text) throws Exception {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);

		primarySender.send(message);

	}

	public void sendSecondaryEmail(String from, String to, String subject,
			String text) throws Exception {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);

		secondarySender.send(message);

	}

	public void sendMail(boolean bPrimary, String from, String[] to,
			String[] cc, String[] bcc, String subject, String messageBody,
			String typeMessage, String priority, String[] attachments)
			throws Exception {

		log.info("****************************************");
		log.info("INIZIO ESECUZIONE sendMail              ");
		log.info("****************************************");

		log.info(" bPrimary: " + bPrimary);
		log.info(" from: " + from);
		log.info(" subject: " + subject);
		log.info(" message: " + messageBody);

		if (attachments != null) {
			MimeMessage msg = null;
			if (bPrimary) {
				msg = primarySender.createMimeMessage();
			} else {
				msg = secondarySender.createMimeMessage();
			}
			msg.setFrom(from);

			MimeMessageHelper helper = new MimeMessageHelper(msg,true);
			if (!bPrimary) {
				helper.setFrom(from, "notifiche-fclt");	
			}
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

			if (bPrimary) {
				primarySender.send(msg);
			} else {
				secondarySender.send(msg);
			}

		} else {

			if (typeMessage.equals("text/html")) {

				MimeMessage msg = null;
				if (bPrimary) {
					msg = primarySender.createMimeMessage();
				} else {
					msg = secondarySender.createMimeMessage();
				}
				msg.setFrom(from);
				MimeMessageHelper helper = new MimeMessageHelper(msg,true);
				if (!bPrimary) {
					helper.setFrom(from, "notifiche-fclt");	
				}
				helper.setTo(to);
				if (cc != null) {
					helper.setCc(cc);
				}
				if (bcc != null) {
					helper.setBcc(bcc);
				}
				helper.setSubject(subject);
				helper.setText(messageBody, true);

				if (bPrimary) {
					primarySender.send(msg);
				} else {
					secondarySender.send(msg);
				}

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

				if (bPrimary) {
					primarySender.send(msg);
				} else {
					secondarySender.send(msg);
				}
			}
		}

		log.info("****************************************");
		log.info("FINE ESECUZIONE sendMail                ");
		log.info("****************************************");
	}

	public void sendMail2(boolean bPrimary, String from, String[] to,
			String[] cc, String[] bcc, String subject, String messageBody,
			String typeMessage, String priority,
			Map<String, InputStream> images)
			throws Exception {

		log.info("****************************************");
		log.info("INIZIO ESECUZIONE sendMail2              ");
		log.info("****************************************");

		log.info(" bPrimary: " + bPrimary);
		log.info(" from: " + from);
		log.info(" subject: " + subject);
		log.info(" message: " + messageBody);

		if (images != null) {

			MimeMessage msg = null;
			if (bPrimary) {
				msg = primarySender.createMimeMessage();
			} else {
				msg = secondarySender.createMimeMessage();
			}

			msg.setFrom(from);

			MimeMessageHelper helper = new MimeMessageHelper(msg,true);
			if (!bPrimary) {
				helper.setFrom(from, "notifiche-fclt");	
			}
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

			if (bPrimary) {
				primarySender.send(msg);
			} else {
				secondarySender.send(msg);
			}

		} else {

			if (typeMessage.equals("text/html")) {

				MimeMessage msg = null;
				if (bPrimary) {
					msg = primarySender.createMimeMessage();
				} else {
					msg = secondarySender.createMimeMessage();
				}

				msg.setFrom(from);
				MimeMessageHelper helper = new MimeMessageHelper(msg,true);
				if (!bPrimary) {
					helper.setFrom(from, "notifiche-fclt");	
				}
				helper.setTo(to);
				if (cc != null) {
					helper.setCc(cc);
				}
				if (bcc != null) {
					helper.setBcc(bcc);
				}
				helper.setSubject(subject);
				helper.setText(messageBody, true);

				if (bPrimary) {
					primarySender.send(msg);
				} else {
					secondarySender.send(msg);
				}

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

				if (bPrimary) {
					primarySender.send(msg);
				} else {
					secondarySender.send(msg);
				}

			}
		}

		log.info("****************************************");
		log.info("FINE ESECUZIONE sendMail2               ");
		log.info("****************************************");

	}

}

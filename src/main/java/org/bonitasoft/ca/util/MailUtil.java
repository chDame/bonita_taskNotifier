package org.bonitasoft.ca.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.ca.model.MailVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sun.mail.smtp.SMTPTransport;

@Service
public class MailUtil {

	private static Logger logger = LoggerFactory.getLogger(MailUtil.class);

	static class AsyncMailer extends Thread {
		private String recipient;
		private MailType mailType;
		private MailVariables mailVar;
		public AsyncMailer(String recipient, MailType mailType, MailVariables mailVar) {
			this.recipient = recipient;
			this.mailType = mailType;
			this.mailVar = mailVar;
		}
		public void run() {
			if (IdemPotenceUtil.isSent(mailVar.getTaskId(), recipient, mailType)) {
				return;
			}
			IdemPotenceUtil.setSended(mailVar.getTaskId(), recipient, mailType);
			try {
				Properties prop = System.getProperties();
				prop.put("mail.transport.protocol", "smtp");
				prop.put("mail.smtp.auth", PropertiesUtil.getProperty("smtp.auth"));
				prop.put("mail.smtp.host", PropertiesUtil.getProperty("smtp.server"));

				String smtpUsername = PropertiesUtil.getProperty("smtp.username");
				String smtpPassword = PropertiesUtil.getProperty("smtp.password");

				Authenticator authenticator = null;
				if (StringUtils.isNotBlank(smtpUsername) && StringUtils.isNotBlank(smtpPassword)) {
					authenticator = new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(smtpUsername, smtpPassword);
						}
					};
				}
				Session session = Session.getInstance(prop, authenticator);
				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(PropertiesUtil.getProperty("mail.from")));

				msg.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipient, false));

				// TEXT email
				//msg.setText(EMAIL_TEXT);

				// HTML email
				Map<String, String> replacements = new HashMap<>();
				replacements.put("firstname", mailVar.getFirstname());
				replacements.put("lastname", mailVar.getLastname());
				replacements.put("taskLink", mailVar.getTaskLink());
				replacements.put("taskName", mailVar.getTaskName());
				replacements.put("taskId", mailVar.getTaskId().toString());
				replacements.put("processName", mailVar.getProcessName());
				replacements.put("caseId", mailVar.getCaseId());

				msg.setSubject(MailContentBuilder.buildSubject(mailType.getSubject(), replacements));

				MailContentBuilder.buildContent(msg, mailType.getTemplateName(), replacements);


				SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");

				// connect
				transport.connect();

				// send
				transport.sendMessage(msg, msg.getAllRecipients());

				System.out.println("Response: " + transport.getLastServerResponse());

				transport.close();

			} catch (MessagingException e) {
				logger.error(e.getMessage());
			}
		}
	}


	public static void sendTaskMail(String recipient, MailType mailType, MailVariables mailVariables) {
		AsyncMailer mailer = new AsyncMailer(recipient, mailType, mailVariables);
		mailer.start();
	}
}

package org.bonitasoft.ca.eventhandler;

import static org.junit.Assert.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.bonitasoft.ca.model.MailVariables;
import org.bonitasoft.ca.util.MailType;
import org.bonitasoft.ca.util.MailUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;


@RunWith(JUnit4.class)
public class MailUtilTest {

	@Test
	public void sendNewTaskMail() throws MessagingException {
		GreenMail greenMail = new GreenMail(new ServerSetup(25, "localhost", ServerSetup.PROTOCOL_SMTP));
		greenMail.setUser("no-reply@test.com", "no-reply@test.com", "pwd");
		greenMail.start();
		MailVariables mailVar = new MailVariables();
		mailVar.setFirstname("Mélanie");
		mailVar.setLastname("Zetaufrais");
		mailVar.setTaskLink("http://www.google.com");
		mailVar.setTaskName("My cool task");
		mailVar.setTaskId(12345L);
		mailVar.setProcessName("Pool");
		mailVar.setCaseId("test 12345");
		MailUtil.sendTaskMail("christophe.dame@bonitasoft.com", MailType.TASK_ASSIGNED, mailVar);
		//wait one second because the mail is sended in a separate thread
		assertTrue(greenMail.waitForIncomingEmail(1000, 1));
		assertEquals(1, greenMail.getReceivedMessages().length);
		MimeMessage message = greenMail.getReceivedMessages()[0];
		assertEquals(message.getSubject(), "[Pool] New Task My cool task (12345) has been assigned to you");
		greenMail.stop();
	}
	
}

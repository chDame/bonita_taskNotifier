package org.bonitasoft.ca.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

@Service
public class MailContentBuilder {

    private static VelocityEngine velocityEngine = null;

	public static synchronized VelocityEngine getVelocityEngine() {
		if (velocityEngine!=null) {
			return velocityEngine;
		}
    	Properties velocityProperties = new Properties();
//		velocityProperties.put("velocimacro.permissions.allow.inline", "true");
//		velocityProperties.put("velocimacro.permissions.allow.inline.to.replace.global", "true");
//		velocityProperties.put("velocimacro.permissions.allow.inline.local.scope", "true");
		velocityProperties.put("input.encoding", "UTF-8");
		velocityProperties.put("output.encoding", "UTF-8");
		velocityProperties.put("file.resource.loader.path", PropertiesUtil.getProperty("mail.template.path"));
//		velocityProperties.put("resource.loader", "webapp, class");
//		velocityProperties.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
//		velocityProperties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader ");
//		velocityProperties.put("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
//		velocityProperties.put("webapp.resource.loader.path", "/WEB-INF/velocity/");
//		velocityProperties.put("webapp.resource.loader.cache", "true");
//		velocityProperties.put("webapp.resource.loader.cache", "false");
		//velocityEngine.setVelocityProperties(velocityProperties);
    	velocityEngine = new VelocityEngine(velocityProperties);
    	return velocityEngine;
	}

    public static void buildContent(Message message, String templateName, Map<String, String> replacements) throws MessagingException {
    	message.setDataHandler(new DataHandler(new HTMLDataSource(newUserTask(templateName, replacements))));
    }
    
    private static String newUserTask(String templateName, Map<String, String>  replacements) {
    	Template template = getVelocityEngine().getTemplate(templateName);
    	 
		VelocityContext velocityContext = new VelocityContext();
		for(Map.Entry<String, String> keyValue : replacements.entrySet()) {
			if (keyValue.getValue()!=null) {
				velocityContext.put(keyValue.getKey(), keyValue.getValue());
			}
		}
 
		StringWriter stringWriter = new StringWriter();
 
		template.merge(velocityContext, stringWriter);
		return stringWriter.toString();
    }
    
    static class HTMLDataSource implements DataSource {

        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (html == null) throw new IOException("html message is null!");
            return new ByteArrayInputStream(html.getBytes());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        @Override
        public String getContentType() {
            return "text/html";
        }

        @Override
        public String getName() {
            return "HTMLDataSource";
        }
    }

	public static String buildSubject(String subject, Map<String, String> replacements) {
		for(Map.Entry<String, String> keyValue : replacements.entrySet()) {
			if (keyValue.getValue()!=null) {
				subject = subject.replace("${"+keyValue.getKey()+"}", keyValue.getValue());
			}
		}
		return subject;
	}
}
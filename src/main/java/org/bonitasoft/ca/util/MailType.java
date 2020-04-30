package org.bonitasoft.ca.util;

public enum MailType {
	TASK_ASSIGNABLE("assignableTask"),
	TASK_ASSIGNED("assignedTask");
	
	private String code;
	
	MailType(String code) {
		this.code = code;
	}
	
	public String getSubject() {
		return PropertiesUtil.getProperty("mail."+code+".subject");
	}
	
	public String getTemplateName() {
		return PropertiesUtil.getProperty("mail."+code+".templateName");
	}
}

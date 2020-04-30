# Task Notifier

This repository contains a project that should be compiled as a jar and used by Bonita for task notifications


## How to use this jar

1. Get it from the repo.
1. Build it with Gradle : gradlew.bat fatJar
1. Put the jar in the libs folder server/webapps/bonita/WEB-INF/lib
1. Get the setup files by executing a setup/setup pull
1. Modify the bonita-tenant-sp-custom.xml file
1. Commit the file by executing a setup/setup push
1. Properties can be modified in the server/conf/TaskNotifier.properties
1. Restart the server and check that it works as expected.

NB : If you modify the properties, you should restart the server as they are loaded on start.
NB : To run it locally with Bonita Studio, it's the same process. But you need to change the file setup/database.properties
## database.properties (local only)
``` properties
	 h2.database.dir=../../MyProject/h2_database
```

## bonita-tenant-sp-custom.xml
``` xml
	<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
	      xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
	
	    <bean id="taskAssignableNotifier" class="org.bonitasoft.ca.eventhandler.TaskAssignableNotifier">
	    	<constructor-arg name="tenantId" value="${tenantId}" /> 
	    </bean>
	    <bean id="taskAssignedNotifier" class="org.bonitasoft.ca.eventhandler.TaskAssignedNotifier">
	    	<constructor-arg name="tenantId" value="${tenantId}" /> 
	    </bean>
	
	    <bean id="eventHandlers" class="org.springframework.beans.factory.config.MapFactoryBean">
	        <property name="targetMapClass">
	            <value>java.util.HashMap</value>
	        </property>
	        <property name="sourceMap">
	            <map>
	                <entry key="ACTIVITYINSTANCE_STATE_UPDATED" value-ref="taskAssignableNotifier"/>
	                <entry key="HUMAN_TASK_INSTANCE_ASSIGNEE_UPDATED" value-ref="taskAssignedNotifier"/>
	            </map>
	        </property>
	    </bean>
	
	</beans>
```

## TaskNotifier.properties
``` properties
	#usefull to build the taskLink
	bonita.url=http://localhost:8080/bonita/
	#smtp properties
	smtp.auth=true
	smtp.server=mail.***.fr
	#only if smtp.auth true
	smtp.username=no-reply@***.fr
	smtp.password=***
	#if we don't want the mails to be sent to certains recipients (only the * wildcard at the end or the beginning (one, none or both) is accepted
	smtp.recipient.filters=*@acme.com, *Kelly*, paul.dubois@domain.fr
	#should send mail when task is manually assigned
	mail.manuallyAssigned=true
	#where to find the mail templates
	mail.template.path=C:/Users/christophe.dame/workspace/task-notifier/src/test/resources
	#new task assignable
	mail.assignableTask.subject=[${processName}] New Task ${taskName} (${taskId}) is available
	mail.assignableTask.templateName=assignableTaskTemplate.vm
	#new task assigned
	mail.assignedTask.subject=[${processName}] New Task ${taskName} (${taskId}) has been assigned to you
	mail.assignedTask.templateName=assignedTaskTemplate.vm
	
	#who is the sender (from field)
	mail.from=no-reply@***.fr
```

## EmailTemplateExemple.vm
``` html
<p>Hello <b>${firstname} ${lastname}</b></p>
 
<p>The task <b>${taskName}</b> of ${processName} is pending your action : <a href="${taskLink}"/>Click here</a></p>
 
<p>Case number: ${caseId}</p>

<p>Regards</p>
```

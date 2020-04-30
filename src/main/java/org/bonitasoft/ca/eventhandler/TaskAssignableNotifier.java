package org.bonitasoft.ca.eventhandler;

import javax.mail.MessagingException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAssignableNotifier extends AbstractTaskNotifier {

	private static Logger logger = LoggerFactory.getLogger(TaskAssignableNotifier.class);

	private static final long serialVersionUID = 5892801961711156471L;

	private final static String IDENTIFIER = "TaskAssignableNotifier";
	
	private long tenantId;
	

	public TaskAssignableNotifier(long tenantId) throws SHandlerExecutionException {
		this.tenantId = tenantId;
		logger.info("TaskAssignableNotifier started for tenant {}", tenantId);
	}

	@Override
	public void execute(SEvent event) throws SHandlerExecutionException {
		logger.info("TaskAssignableNotifier: executing event {}", event.getType());
		try {
			SHumanTaskInstance sHumanTaskInstance = (SHumanTaskInstance) event.getObject( );
			if (sHumanTaskInstance.getAssigneeId()>0) {
				//if there is an auto assignation (single actor filter), we have a new task event with an assigneeId.
				//we then should send the assignedTask (and not assignable)
				assignedTaskNotification(sHumanTaskInstance);
			} else {
				assignableTaskNotification(sHumanTaskInstance);
			}
		} catch (SBonitaException e) {
			logger.error("TaskAssignableNotifier: Error in Event Handler " + e);
		}

	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public long getTenantId() {
		return tenantId;
	}
}

package org.bonitasoft.ca.eventhandler;

import org.bonitasoft.ca.util.PropertiesUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAssignedNotifier extends AbstractTaskNotifier {

	private static Logger logger = LoggerFactory.getLogger(TaskAssignedNotifier.class);

	private static final long serialVersionUID = 5892801961711156471L;

	private final static String IDENTIFIER = "TaskAssignableNotifier";
	
	private long tenantId;

	public TaskAssignedNotifier(long tenantId) throws SHandlerExecutionException {
		this.tenantId = tenantId;
		logger.info("TaskAssignedNotifier started for tenant {}", tenantId);
	}
	
	@Override
	public boolean isInterested(SEvent event) {
		if (Boolean.valueOf(PropertiesUtil.getProperty("mail.manuallyAssigned"))) {
			return super.isInterested(event);
		}
		return false;
	}

	@Override
	public void execute(SEvent event) throws SHandlerExecutionException {
		logger.info("TaskAssignedNotifier: executing event {}", event.getType());
		try {
			SHumanTaskInstance sHumanTaskInstance = (SHumanTaskInstance) event.getObject( );
			assignedTaskNotification(sHumanTaskInstance);
		} catch (SBonitaException e) {
			logger.error("TaskAssignedNotifier: Error in Event Handler " + e);
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

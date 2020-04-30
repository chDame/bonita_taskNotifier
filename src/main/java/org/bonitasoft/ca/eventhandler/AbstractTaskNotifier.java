package org.bonitasoft.ca.eventhandler;

import java.util.List;

import org.bonitasoft.ca.model.MailVariables;
import org.bonitasoft.ca.model.RecipientFilter;
import org.bonitasoft.ca.util.MailType;
import org.bonitasoft.ca.util.MailUtil;
import org.bonitasoft.ca.util.PropertiesUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchUserDescriptor;
import org.bonitasoft.engine.search.identity.SearchUsersWhoCanExecutePendingHumanTaskDeploymentInfo;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.slf4j.Logger;

@SuppressWarnings("serial")
public abstract class AbstractTaskNotifier implements SHandler<SEvent> {

	private static String instanceUrl = null;
	
	private TenantServiceAccessor tenantAccessor; 
	
	private RecipientFilter[] filters = null;
	
	protected void assignedTaskNotification(SHumanTaskInstance sHumanTaskInstance) throws SBonitaException {
		getLogger().info("assignedTaskNotification: taskId {}", sHumanTaskInstance.getId());
		Long assigneeId = sHumanTaskInstance.getAssigneeId();
		if (assigneeId>0) {
			Long humanTaskInstanceId = sHumanTaskInstance.getId(); 
			Long caseId = sHumanTaskInstance.getParentProcessInstanceId();
			String processName = getTenantAccessor().getProcessInstanceService().getProcessInstance(caseId).getName();
			IdentityService identityService = getTenantAccessor().getIdentityService();
	
			SContactInfo contactInfo = identityService.getUserContactInfo(assigneeId, false);
	
			SUser user = identityService.getUser(assigneeId);
			MailVariables mailVar = new MailVariables();
			mailVar.setFirstname(user.getFirstName());
			mailVar.setLastname(user.getLastName());
			mailVar.setTaskLink(getTaskLink(humanTaskInstanceId));
			mailVar.setTaskName(sHumanTaskInstance.getDisplayName());
			mailVar.setTaskId(humanTaskInstanceId);
			mailVar.setCaseId(caseId.toString());
			mailVar.setProcessName(processName);
	
			MailUtil.sendTaskMail(contactInfo.getEmail(), MailType.TASK_ASSIGNED, mailVar);
		}
	}
	
	protected void assignableTaskNotification(SHumanTaskInstance sHumanTaskInstance) throws SBonitaException {
		getLogger().info("assignableTaskNotification: taskId {}", sHumanTaskInstance.getId());

		Long humanTaskInstanceId = sHumanTaskInstance.getId(); 
		Long caseId = sHumanTaskInstance.getParentProcessInstanceId();
		String processName = getTenantAccessor().getProcessInstanceService().getProcessInstance(caseId).getName();

		ActivityInstanceService activityInstanceService = getTenantAccessor().getActivityInstanceService();
		IdentityService identityService = getTenantAccessor().getIdentityService();
		SearchEntitiesDescriptor searchEntitiesDescriptor = getTenantAccessor().getSearchEntitiesDescriptor();
		SearchUserDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchUserDescriptor();
		
		SearchOptionsBuilder searchOptionBuilder = new SearchOptionsBuilder(0,10);
		// Build a searcher that returns a humanTask candidates
		SearchUsersWhoCanExecutePendingHumanTaskDeploymentInfo searcher = new SearchUsersWhoCanExecutePendingHumanTaskDeploymentInfo(humanTaskInstanceId, activityInstanceService, searchDescriptor, searchOptionBuilder.done());
		searcher.execute();
		SearchResult<User> users = searcher.getResult();
		List<User> userList = users.getResult();
		for(User user : userList){
			SContactInfo contact = identityService.getUserContactInfo(user.getId(), false);
			if (!shouldBeFiltered(contact.getEmail())) {
				MailVariables mailVar = new MailVariables();
				mailVar.setTaskLink(getTaskLink(humanTaskInstanceId));
				mailVar.setTaskName(sHumanTaskInstance.getDisplayName());
				mailVar.setTaskId(humanTaskInstanceId);
				mailVar.setFirstname(user.getFirstName());
				mailVar.setLastname(user.getLastName());
				mailVar.setCaseId(caseId.toString());
				mailVar.setProcessName(processName);
				MailUtil.sendTaskMail(contact.getEmail(), MailType.TASK_ASSIGNABLE, mailVar);
			}
		}
	}

	@Override
	public boolean isInterested(SEvent event) {

		// Get the object associated with the event
		Object eventObject = event.getObject();
		// Check if the event is related to a task 
		if (eventObject instanceof SFlowNodeInstance) {
			SFlowNodeInstance flowNodeInstance = (SFlowNodeInstance) eventObject;

			// Verify that the state of the task is ready. (4) 
			return (flowNodeInstance.getType().equals(SFlowNodeType.USER_TASK) && flowNodeInstance.getStateId()==4);
		}
		return false;
	}
	
	public String getInstanceUrl() {
		if (instanceUrl!=null) {
			return instanceUrl;
		}
		instanceUrl = PropertiesUtil.getProperty("bonita.url");
		return instanceUrl;
	}
	
	private synchronized RecipientFilter[] getFilters() {
		if (filters==null) {
			String [] stringFilters = PropertiesUtil.getProperty("smtp.recipient.filters").split(",");
			filters = new RecipientFilter[stringFilters.length];
			int i=0;
			for(String filter : stringFilters) {
				filters[i++] = new RecipientFilter(filter);
			}
		}
		return filters;
	}
	
	public boolean shouldBeFiltered(String email) {
		for(RecipientFilter filter : getFilters()) {
			if (filter.matches(email)) {
				return true;
			}
		}
		return false;
	}
	
	public String getTaskLink(Long humanTaskInstanceId) {
		return getInstanceUrl() + "/portal/form/taskInstance/"+humanTaskInstanceId;
	}
	
	private TenantServiceAccessor getTenantServiceAccessor() throws SHandlerExecutionException {
		try {
			ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
			return serviceAccessorFactory.createTenantServiceAccessor(getTenantId());
		} catch (Exception e) {
			throw new SHandlerExecutionException(e.getMessage(), null);
		}
	} 
	
	public TenantServiceAccessor getTenantAccessor() throws SHandlerExecutionException {
		if (tenantAccessor!=null) {
			return tenantAccessor;
		}
		tenantAccessor = getTenantServiceAccessor();
		return tenantAccessor;
	}
	
	public abstract Logger getLogger();
	public abstract long getTenantId();

}

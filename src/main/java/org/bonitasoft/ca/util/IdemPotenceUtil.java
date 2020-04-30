package org.bonitasoft.ca.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class IdemPotenceUtil {

	static Cache<Long, Map<String, List<MailType>>> normalizedObjectCache = CacheBuilder.newBuilder()
			.maximumSize(400)
			.expireAfterAccess(2, TimeUnit.HOURS)
			.build();
	
	public static synchronized void setSended(Long taskId, String mail, MailType mailType) {
		Map<String, List<MailType>> taskMailType = normalizedObjectCache.getIfPresent(taskId);
		if (taskMailType==null) {
			taskMailType = new HashMap<>();
			normalizedObjectCache.put(taskId, taskMailType);
		}
		if (taskMailType.get(mail)==null) {
			taskMailType.put(mail, new ArrayList<MailType>());
		}
		taskMailType.get(mail).add(mailType);
	}
	
	public static synchronized boolean isSent(Long taskId, String mail, MailType mailType) {
		Map<String, List<MailType>> taskMailType = normalizedObjectCache.getIfPresent(taskId);
		boolean result = false;
		if (taskMailType!=null && taskMailType.get(mail)!=null) {
			result = taskMailType.get(mail).contains(mailType);
		}
		
		setSended(taskId, mail, mailType);
		return result;
	}
	
}

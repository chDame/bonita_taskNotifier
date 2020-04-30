package org.bonitasoft.ca.model;

public class RecipientFilter {

	private String filter;
	private boolean startsWithStar = false;
	private boolean endsWithStar = false;
	
	public RecipientFilter(String filter) {
		this.filter = filter.trim();
		if (this.filter.startsWith("*")) {
			startsWithStar = true;
		}
		if (this.filter.endsWith("*")) {
			endsWithStar = true;
		}
		this.filter = this.filter.replace("*", "");
	}
	
	public boolean matches(String email) {
		if (startsWithStar && endsWithStar) {
			return email.contains(filter);
		}
		if (startsWithStar) {
			return email.endsWith(filter);
		}
		if (endsWithStar) {
			return email.startsWith(filter);
		}
		return email.equals(filter);
	}
	
}

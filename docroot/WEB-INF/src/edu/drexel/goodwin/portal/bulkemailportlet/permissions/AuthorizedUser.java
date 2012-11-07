package edu.drexel.goodwin.portal.bulkemailportlet.permissions;

public class AuthorizedUser {

	private String screenName;
	private String fullName;

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public String toString() {
		return fullName + " (" + screenName + ")";
	}
}

/**
 * 
 */
package edu.drexel.goodwin.portal.bulkemailportlet.preferences;

/**
 * @author skk54
 * 
 */
public class UserPermission {
	private String userName;
	private Boolean isAuthorized;

	public UserPermission(String userName, Boolean isAuthorized) {
		super();
		this.userName = userName;
		this.isAuthorized = isAuthorized;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setIsAuthorized(Boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	public Boolean getIsAuthorized() {
		return isAuthorized;
	}

}

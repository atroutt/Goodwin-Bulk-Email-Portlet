package edu.drexel.goodwin.portal.bulkemailportlet.preferences;

import java.util.HashMap;
import java.util.Map;

public class PermissionsMap {

	private Map<Long, Map<String, UserPermission>> map = new HashMap<Long, Map<String, UserPermission>>();

	public Map<Long, Map<String, UserPermission>> getMap() {
		return map;
	}

	public void setMap(Map<Long, Map<String, UserPermission>> map) {
		this.map = map;
	}

}

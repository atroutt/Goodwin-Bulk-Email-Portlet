package edu.drexel.goodwin.portal.bulkemailportlet.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.context.PortletConfigAware;

import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

import edu.drexel.goodwin.portal.bulkemailportlet.addresses.BulkEmailAddressDao;
import edu.drexel.goodwin.portal.bulkemailportlet.addresses.SavedEmailAddress;

/**
 * This controller handles updating preferences for the bulk email portlet.
 * 
 * @author Audrey Troutt
 * 
 */
@Controller
@RequestMapping("EDIT")
public class BulkEmailPreferencesController implements PortletConfigAware {

	private final Logger logger = Logger.getLogger(this.getClass());
	private PermissionsMap permissionsMap;
	private PortletConfig portletConfig;

	@Autowired
	private BulkEmailAddressDao bulkEmailAddressDao;

	@ModelAttribute(value = "emailAddress")
	public SavedEmailAddress getModel() {
		return new SavedEmailAddress();
	}

	public void setPermissionsMap(PermissionsMap permissionsMap) {
		this.permissionsMap = permissionsMap;
	}

	@Override
	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
	}

	public PortletConfig getPortletConfig() {
		return this.portletConfig;
	}

	@RenderMapping
	public String view(ModelMap modelMap) {
		modelMap.addAttribute("emailAddresses", setDeanAddressAtTheEnd(bulkEmailAddressDao.getAll()));

		try {
			final long companyId = CompanyThreadLocal.getCompanyId();
			String roleName = getPortletConfig().getInitParameter("roleName");
			Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
			long roleId = role.getRoleId();
			List<User> people = UserLocalServiceUtil.getRoleUsers(roleId);
			modelMap.addAttribute("people", people);
			modelMap.addAttribute("permissions", getPermissionsMap());
			String sendAsDean = getPortletConfig().getInitParameter("sendAsDean");
			modelMap.put("sendAsDeanLabel", sendAsDean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "preferencesForm";
	}

	@ModelAttribute(value = "permissions")
	public PermissionsMap getPermissionsMap() {

		try {
			final long companyId = CompanyThreadLocal.getCompanyId();
			String roleName = getPortletConfig().getInitParameter("roleName");
			Role role = RoleLocalServiceUtil.getRole(companyId, roleName);

			long roleId = role.getRoleId();
			List<User> people = UserLocalServiceUtil.getRoleUsers(roleId);
			permissionsMap = new PermissionsMap();
			List<SavedEmailAddress> allAddresses = setDeanAddressAtTheEnd(bulkEmailAddressDao.getAll());

			for (SavedEmailAddress address : allAddresses) {
				Map<String, UserPermission> mapUserPermissions = new HashMap<String, UserPermission>();
				for (User user : people) {
					List<SavedEmailAddress> addressesForUser = bulkEmailAddressDao.getAddressesForUser(user.getScreenName());
					Boolean isAuthorized = Boolean.FALSE;
					if (!addressesForUser.isEmpty()) {
						isAuthorized = addressesForUser.contains(address);
					}
					UserPermission userPermission = new UserPermission(user.getScreenName(), isAuthorized);
					mapUserPermissions.put(user.getScreenName(), userPermission);
				}
				permissionsMap.getMap().put(address.getId(), mapUserPermissions);
			}
		} catch (Exception e) {
			logger.error("Unexpected exception ocurred while retrievin address: Cause : " + e.getCause() + ". Message : " + e.getMessage());
		}
		return permissionsMap;
	}

	@ActionMapping(value = "addAddress")
	public void addAddress(SavedEmailAddress emailAddress, ActionRequest request, ActionResponse response) throws PortletException, IOException {
		try {
			bulkEmailAddressDao.addEmailAddress(emailAddress);
		} catch (Exception e) {
			logger.error("Unexpected exception ocurred while adding email address: Cause : " + e.getCause() + ". Message : " + e.getMessage());
		}
	}

	@ActionMapping(params = "myaction=removeAddress")
	public void removeAddress(@RequestParam long addressId, @RequestParam String addresslabel, @RequestParam String addressEmail, ActionRequest request, ActionResponse response) throws PortletException, IOException {
		logger.debug("received remove request");
		try {
			SavedEmailAddress emailAddress = new SavedEmailAddress();
			emailAddress.setId(addressId);
			emailAddress.setAddress(addressEmail);
			emailAddress.setLabel(addresslabel);
			bulkEmailAddressDao.removeEmailAddress(emailAddress);
		} catch (Exception e) {
			logger.error("Unexpected exception ocurred while removing email address: Cause : " + e.getCause() + ". Message : " + e.getMessage());
		}
	}

	@ActionMapping(value = "savePermissions")
	public void savePermissions(@ModelAttribute(value = "permissions") PermissionsMap permissions, ActionRequest request, ActionResponse response) throws PortletException, IOException {
		logger.debug("received update request");
		try {
			logger.debug("permissions.getMap().size(): " + permissions.getMap().size());
			Iterator<Long> itAddresses = permissionsMap.getMap().keySet().iterator();
			while (itAddresses.hasNext()) {
				List<String> screenNames = new ArrayList<String>();

				Long addressId = itAddresses.next();
				Map<String, UserPermission> userPermissionsMap = permissions.getMap().get(addressId);
				Iterator<String> itUserPermissions = userPermissionsMap.keySet().iterator();
				while (itUserPermissions.hasNext()) {
					String screenName = itUserPermissions.next();
					UserPermission userPermission = (UserPermission) userPermissionsMap.get(screenName);
					if (userPermission.getIsAuthorized()) {
						screenNames.add(screenName);
					}
				}
				bulkEmailAddressDao.upadteUsersForAddress(addressId, screenNames);
			}
		} catch (Exception e) {
			logger.error("Unexpected exception occurred while updating addresses for user : Cause : " + e.getCause() + ". Message : " + e.getMessage());
		}
	}

	public List<SavedEmailAddress> setDeanAddressAtTheEnd(List<SavedEmailAddress> addresses) {
		String sendAsDean = getPortletConfig().getInitParameter("sendAsDean");
		SavedEmailAddress deanAddress = bulkEmailAddressDao.getEmailBylabel(sendAsDean);
		if (addresses.contains(deanAddress)) {
			// Remove Dean's address and add at the end. This makes is appear at the end of the list.
			addresses.remove(deanAddress);
			addresses.add(deanAddress);
		}
		return addresses;
	}

}

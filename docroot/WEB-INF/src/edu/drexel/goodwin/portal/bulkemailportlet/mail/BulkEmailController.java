package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.context.PortletConfigAware;

import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

import edu.drexel.goodwin.portal.bulkemailportlet.addresses.BulkEmailAddressDao;
import edu.drexel.goodwin.portal.bulkemailportlet.addresses.SavedEmailAddress;

/**
 * This controller handles sending emails to the specified addresses from the currently logged in portal user. This includes attachments.
 * 
 * By design the sender is BCC on the message because the application does not otherwise keep a record of the emails that are sent.
 * 
 * @author Audrey Troutt
 * 
 */
@Controller
@RequestMapping("VIEW")
public class BulkEmailController implements PortletConfigAware {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	private BulkEmailAddressDao bulkEmailAddressDao;

	@Autowired
	private MessageValidator validator;

	@Autowired
	private EmailMessageSender messageSender;

	@Autowired
	private UserUtil userUtil;

	private PortletConfig portletConfig;

	@Override
	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
	}

	public PortletConfig getPortletConfig() {
		return this.portletConfig;
	}

	@RenderMapping
	public String view(ModelMap modelMap, PortletRequest request) {
		try {
			if (!modelMap.containsAttribute("message")) {
				modelMap.addAttribute("message", prepareNewMessage(request, modelMap));
			}
			
			User user = UserLocalServiceUtil.getUserById(Long.valueOf(request.getRemoteUser()));
			
			// check if the logged in user has privilege to send as Dean.
			List<SavedEmailAddress> authorizedAddressesForCurrentUser = bulkEmailAddressDao.getAddressesForUser(user.getScreenName());
			
			modelMap.put("sendAsDean", new Boolean(canUserSendAsDean(authorizedAddressesForCurrentUser)));
			
			if (userIsAdmin(user)) {
				modelMap.put("possibleToAddresses", getAllPossibleToAddresses());
			} else {
				modelMap.put("possibleToAddresses", authorizedAddressesForCurrentUser);
			}
			
		} catch (Exception e) {
			modelMap.put("errormsg", "There was an error retrieving values from database. Check database connection.");
			logger.error("Unexpected exception ocurred while retrieving values from database.", e);
		}
		return "composeEmail";
	}

	private boolean canUserSendAsDean(List<SavedEmailAddress> authorizedAddressesForCurrentUser) {
		String sendAsDeanLabel = getPortletConfig().getInitParameter("sendAsDean");
		SavedEmailAddress sendAsDeanEmailAddress = bulkEmailAddressDao.getEmailBylabel(sendAsDeanLabel);
		// remove returns true if something was removed. If it was in their list we want it gone either way.
		return authorizedAddressesForCurrentUser.remove(sendAsDeanEmailAddress);
	}

	private boolean userIsAdmin(User user) throws PortalException, SystemException {
		String adminRoleName = getPortletConfig().getInitParameter("adminRoleName");
		Role adminRole = RoleLocalServiceUtil.getRole(CompanyThreadLocal.getCompanyId(), adminRoleName);
		return user.getRoles().contains(adminRole);
	}

	private Message prepareNewMessage(PortletRequest request, ModelMap modelMap) {
		final Message message = new Message();
		try {
			message.setBody("<br/><br/>" + userUtil.getCurrentUserSignature(request));
		} catch (Exception e) {
			modelMap.put("errormsg", "There was an error connecting to the database. Please refresh the page.");
		}
		return message;
	}

	@ActionMapping(value = "sendEmail")
	public void sendEmail(Message message, BindingResult result, ModelMap modelMap, PortletRequest request, ActionRequest actionRequest) {
		boolean isPreview = false;

		if (request.getParameter("submitType").equals("send")) {
			isPreview = false;
			validator.validate(message, result);
		} else if (request.getParameter("submitType").equals("preview")) {
			isPreview = true;
		}

		if (result.hasErrors()) {
			modelMap.addAllAttributes(result.getAllErrors());
			modelMap.put("errormsg", "There was an error. Please check the messages below.");
			modelMap.put("possibleToAddresses", getAllPossibleToAddresses());
			return;
		}
		
		try {
			messageSender.sendEmail(message, request, isPreview);
			modelMap.put("msg", isPreview ? "Preview Email Sent" : "Email Sent");
			modelMap.addAttribute("message", prepareNewMessage(request, modelMap));
		} catch (Exception e) {
			modelMap.put("errormsg", "There was an error sending your email. Please try again.");
			logger.error("Unexpected exception ocurred while sending an email.", e);
		}
	}

	public List<SavedEmailAddress> getAllPossibleToAddresses() {
		String sendAsDean = getPortletConfig().getInitParameter("sendAsDean");
		SavedEmailAddress sendAsDeanAddress = bulkEmailAddressDao.getEmailBylabel(sendAsDean);
		List<SavedEmailAddress> addresses = bulkEmailAddressDao.getAll();
		addresses.remove(sendAsDeanAddress);
		return addresses;
	}
}

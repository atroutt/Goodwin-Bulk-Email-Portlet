package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

@Service
public class PortalUserUtil implements UserUtil {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public InternetAddress getCurrentUserEmailAddress(PortletRequest request) throws PortalException, SystemException, AddressException, UnsupportedEncodingException {
		User user = PortalUtil.getUser(request);
		String emailAddress = user.getEmailAddress();
		String fullname = user.getFullName();
		InternetAddress fromAddress = new InternetAddress(emailAddress, fullname);
		return fromAddress;
	}

	/**
	 * Gets the user's signature if it exists, otherwise will return an empty string. Does not return null Strings.
	 */
	@Override
	public String getCurrentUserSignature(PortletRequest request) {
		String signature = "";
		try {
			User user = PortalUtil.getUser(request);
			signature = (String) user.getExpandoBridge().getAttribute("signature");
		} catch (Exception e) {
			logger.error("Error trying to get user signature from currently logged in portal user.", e);
		}
		if (signature == null) {
			return "";
		}
		return signature;
	}
}

package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.PortletRequest;

import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;

public interface UserUtil {

	public abstract InternetAddress getCurrentUserEmailAddress(PortletRequest request) throws PortalException, SystemException, AddressException, UnsupportedEncodingException;

	/**
	 * Gets the user's signature if it exists, otherwise will return an empty string. Does not return null Strings.
	 */
	public abstract String getCurrentUserSignature(PortletRequest request);

}
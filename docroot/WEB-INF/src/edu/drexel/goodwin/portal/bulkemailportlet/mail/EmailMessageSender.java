package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.portlet.context.PortletConfigAware;

import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.mail.MailEngineException;

import edu.drexel.goodwin.portal.bulkemailportlet.addresses.BulkEmailAddressDao;
import edu.drexel.goodwin.portal.bulkemailportlet.addresses.Person;

@Service
public class EmailMessageSender implements PortletConfigAware {

	private static final boolean USE_HTML_FORMAT = true;

	private PortletConfig portletConfig;

	@Autowired
	private UserUtil userUtil;

	@Autowired
	private EmailAttachmentManager attachmentManager;

	@Autowired
	private BulkEmailAddressDao bulkEmailAddressDao;

	@Override
	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
	}

	public PortletConfig getPortletConfig() {
		return this.portletConfig;
	}

	/**
	 * Used for tests
	 * @param bulkEmailAddressDao
	 */
	protected void setBulkEmailAddressDao(BulkEmailAddressDao bulkEmailAddressDao) {
		this.bulkEmailAddressDao = bulkEmailAddressDao;
	}
	
	public void sendEmail(Message message, PortletRequest request, boolean isPreview) throws AddressException, MailEngineException, PortalException, SystemException, IOException, Exception {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();

		final MimeMessageHelper preparedMessage = prepareMessageForSending(sender, message, request, isPreview);

		sender.send(preparedMessage.getMimeMessage());
	}

	private MimeMessageHelper prepareMessageForSending(JavaMailSenderImpl sender, Message message, PortletRequest request, Boolean isPreview) throws PortalException, SystemException, AddressException, IOException, Exception {
		MimeMessageHelper helper = new MimeMessageHelper(sender.createMimeMessage(), true);

		InternetAddress currentUserEmailAddress = userUtil.getCurrentUserEmailAddress(request);
		
		setFromAddress(message, currentUserEmailAddress, helper);
		setReplyToAddresses(message, helper);
		setRecipients(message, currentUserEmailAddress, isPreview, helper);
		setSubject(message, helper);
		setMessageBody(message, request, helper);
		addAttachments(message, request, helper);

		return helper;
	}

	protected void setFromAddress(Message message, InternetAddress currentUserEmailAddress, MimeMessageHelper helper) throws UnsupportedEncodingException, PortalException, SystemException, AddressException, MessagingException {
		InternetAddress fromAddress;
		if (message.getSendAsDean()) {
			Person theDean = bulkEmailAddressDao.getPerson("dean");
			String deanName = theDean.getName();
			String deanAddress = theDean.getAddress();
			fromAddress = new InternetAddress(deanAddress, deanName);
		} else {
			fromAddress = currentUserEmailAddress;
		}
		helper.setFrom(fromAddress);
	}

	private void setReplyToAddresses(Message message, MimeMessageHelper helper) throws AddressException, MessagingException {
		InternetAddress[] replyToAddresses;
		String replyToString = message.getReplyTo();
		if (StringUtils.isBlank(replyToString)) {
			replyToAddresses = new InternetAddress[] {};
		} else {
			replyToAddresses = listToArray(convertToInternetAddresses(getListOfAddressesFromSemicolonSeparatedList(replyToString)));
		}
		helper.getMimeMessage().setReplyTo(replyToAddresses);
	}

	protected List<String> getListOfAddressesFromSemicolonSeparatedList(String semicolonSeparatedAddresses) {
		if (StringUtils.isBlank(semicolonSeparatedAddresses)) {
			return new ArrayList<String>();
		}
		return Arrays.asList(semicolonSeparatedAddresses.split(";"));
	}

	private InternetAddress[] listToArray(List<InternetAddress> recipientsList) {
		return recipientsList.toArray(new InternetAddress[recipientsList.size()]);
	}

	private List<InternetAddress> convertToInternetAddresses(List<String> recipientsEmailAddresses) throws AddressException {
		List<InternetAddress> toAddresses = new ArrayList<InternetAddress>();
	
		for (int i = 0; i < recipientsEmailAddresses.size(); i++) {
			toAddresses.add(convertToInternetAddress(recipientsEmailAddresses.get(i).trim()));
		}
	
		return toAddresses;
	}

	private InternetAddress convertToInternetAddress(final String stringAddress) throws AddressException {
		return new InternetAddress(stringAddress);
	}

	protected void setRecipients(Message message, InternetAddress currentUserEmailAddress, Boolean isPreview, MimeMessageHelper helper) throws MessagingException, PortalException, SystemException, AddressException, UnsupportedEncodingException {
		if (isPreview) {
			helper.setBcc(currentUserEmailAddress);
		} else {
			List<InternetAddress> recipientsList = convertToInternetAddresses(message.getRecipients());
			// add sender to recipients list
			recipientsList.add(currentUserEmailAddress);
			// add copyTo addresses to recipient list
			recipientsList.addAll(convertToInternetAddresses(getListOfAddressesFromSemicolonSeparatedList(message.getCopyTo())));
			// add the reply to people
			recipientsList.addAll(convertToInternetAddresses(getListOfAddressesFromSemicolonSeparatedList(message.getReplyTo())));
			// All recipients are BCCed by default in order to prevent reply-all. See HD-3543
			helper.setBcc(listToArray(recipientsList));
		}
	}

	private void setSubject(Message message, MimeMessageHelper helper) throws MessagingException {
		helper.setSubject(message.getSubject());
	}

	private void setMessageBody(Message message, PortletRequest request, MimeMessageHelper helper) throws MessagingException {
		if (message.getIncludeLogo()) {
			String content = "<html><body><img src='cid:logo'><br/><br/>" + message.getBody() + "</body></html>";
			helper.setText(content, USE_HTML_FORMAT);
			helper.addInline("logo", getLogoFromFileSystem(message, request));
		} else {
			helper.setText(message.getBody(), USE_HTML_FORMAT);
		}
	}

	private FileSystemResource getLogoFromFileSystem(Message message, PortletRequest request) {
		ServletContext servletContext = PortalUtil.getHttpServletRequest(request).getSession().getServletContext();
		String logoName = message.getLogo();
		String filepath = servletContext.getRealPath("images/" + logoName + ".png");
		FileSystemResource resource = new FileSystemResource(new File(filepath));
		return resource;
	}

	private void addAttachments(Message message, PortletRequest request, MimeMessageHelper helper) throws IOException {
		attachmentManager.addAllUploadedFilesAsAttachments(message, request, helper);
	}
}

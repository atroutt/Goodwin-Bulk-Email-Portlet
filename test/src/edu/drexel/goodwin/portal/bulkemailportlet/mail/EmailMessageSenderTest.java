package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import edu.drexel.goodwin.portal.bulkemailportlet.addresses.BulkEmailAddressDao;
import edu.drexel.goodwin.portal.bulkemailportlet.addresses.Person;

public class EmailMessageSenderTest {

	private BulkEmailAddressDao bulkEmailAddressDao;
	private EmailMessageSender emailMessageSender;

	@Before
	public void setUp() throws Exception {
		emailMessageSender = new EmailMessageSender();
		bulkEmailAddressDao = createMock(BulkEmailAddressDao.class);
		emailMessageSender.setBulkEmailAddressDao(bulkEmailAddressDao);
	}
	
	@Test
	public void fromAddressShouldNotBeTheDeanIfUserDidNotChoseToSendAsDean() throws Exception {
		MimeMessageHelper helper = new MimeMessageHelper(new JavaMailSenderImpl().createMimeMessage());
		Message message = new Message();
		InternetAddress currentUserEmailAddress = new InternetAddress("testSender@example.com");
		
		message.setSendAsDean(false);
		emailMessageSender.setFromAddress(message, currentUserEmailAddress, helper);
		assertEquals("testSender@example.com", helper.getMimeMessage().getFrom()[0].toString());
	}

	@Test
	public void fromAddressShouldBeTheDeanIfUserChoseToSendAsDean() throws Exception {
		MimeMessageHelper helper = new MimeMessageHelper(new JavaMailSenderImpl().createMimeMessage());
		Message message = new Message();
		InternetAddress currentUserEmailAddress = new InternetAddress("testSender@example.com");

		message.setSendAsDean(false);
		emailMessageSender.setFromAddress(message, currentUserEmailAddress, helper);
		assertEquals("testSender@example.com", helper.getMimeMessage().getFrom()[0].toString());

		Person theDean = new Person();
		theDean.setName("Dean Test");
		theDean.setAddress("theDean@example.com");
		expect(bulkEmailAddressDao.getPerson("dean")).andReturn(theDean).atLeastOnce();
		
		replay(bulkEmailAddressDao);
		
		message.setSendAsDean(true);
		emailMessageSender.setFromAddress(message, currentUserEmailAddress, helper);
		assertEquals("Dean Test <theDean@example.com>", helper.getMimeMessage().getFrom()[0].toString());
		
		verify(bulkEmailAddressDao);
	}

	@Test
	public void recipientsShouldBeAddedToMessageForNonPreviewEmails() throws Exception {
		MimeMessageHelper helper = new MimeMessageHelper(new JavaMailSenderImpl().createMimeMessage());
		Message message = new Message();
		InternetAddress fromAddress = new InternetAddress("testSender@example.com");

		message.setRecipients(Arrays.asList("audrey@mathforum.org", "tester@test.com", "thirdperson@test.com"));

		boolean isPreview = false;
		emailMessageSender.setRecipients(message, fromAddress, isPreview, helper);
		assertEquals(4, helper.getMimeMessage().getAllRecipients().length);

		message.setCopyTo("copyperson@example.org;otherperson@example.org");

		emailMessageSender.setRecipients(message, fromAddress, isPreview, helper);
		assertEquals(6, helper.getMimeMessage().getAllRecipients().length);

		message.setCopyTo("<copyperson@example.org>; Tammy Tester <otherperson@example.org>");

		emailMessageSender.setRecipients(message, fromAddress, isPreview, helper);
		assertEquals(6, helper.getMimeMessage().getAllRecipients().length);
		assertEquals("Tammy Tester <otherperson@example.org>", helper.getMimeMessage().getAllRecipients()[5].toString());
	}

	@Test
	public void recipientsShouldNotBeAddedToMessageForPreviewEmails() throws Exception {
		MimeMessageHelper helper = new MimeMessageHelper(new JavaMailSenderImpl().createMimeMessage());
		Message message = new Message();
		InternetAddress fromAddress = new InternetAddress("testSender@example.com");

		message.setRecipients(Arrays.asList("audrey@mathforum.org", "tester@test.com", "thirdperson@test.com"));

		boolean isPreview = true;
		emailMessageSender.setRecipients(message, fromAddress, isPreview, helper);
		assertEquals(1, helper.getMimeMessage().getAllRecipients().length);

		message.setCopyTo("copyperson@example.org;otherperson@example.org");

		emailMessageSender.setRecipients(message, fromAddress, isPreview, helper);
		assertEquals(1, helper.getMimeMessage().getAllRecipients().length);

		message.setCopyTo("<copyperson@example.org>; Tammy Tester <otherperson@example.org>");

		emailMessageSender.setRecipients(message, fromAddress, isPreview, helper);
		assertEquals(1, helper.getMimeMessage().getAllRecipients().length);
		assertEquals("testSender@example.com", helper.getMimeMessage().getAllRecipients()[0].toString());
	}

	@Test
	public void testGetListOfAddressesFromSemicolonSeparatedList() {
		List<String> result = emailMessageSender.getListOfAddressesFromSemicolonSeparatedList("audrey@mathforum.org;audrey+test12@mathforum.org; test@example.com;");
		assertListParsedCorrectly(result, "audrey@mathforum.org", "audrey+test12@mathforum.org", " test@example.com");

		result = emailMessageSender.getListOfAddressesFromSemicolonSeparatedList("audrey@mathforum.org");
		assertListParsedCorrectly(result, "audrey@mathforum.org");

		result = emailMessageSender.getListOfAddressesFromSemicolonSeparatedList("audrey@mathforum.org, test@example.org");
		assertListParsedCorrectly(result, "audrey@mathforum.org, test@example.org");

		result = emailMessageSender.getListOfAddressesFromSemicolonSeparatedList("Annie Fetter <annie@mathforum.org>");
		assertListParsedCorrectly(result, "Annie Fetter <annie@mathforum.org>");

	}

	private void assertListParsedCorrectly(List<String> result, String... expectedStrings) {
		List<String> expected = Arrays.asList(expectedStrings);
		assertEquals(expected, result);
	}

}

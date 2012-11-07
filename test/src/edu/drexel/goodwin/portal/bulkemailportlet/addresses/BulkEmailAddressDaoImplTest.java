package edu.drexel.goodwin.portal.bulkemailportlet.addresses;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.drexel.goodwin.portal.bulkemailportlet.permissions.AuthorizedUser;
import edu.drexel.goodwin.portal.bulkemailportlet.permissions.AuthorizedUserDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:test-applicationContext.xml" })
public class BulkEmailAddressDaoImplTest {

	@Autowired
	private BulkEmailAddressDao bulkEmailAddressDao;

	@Autowired
	private AuthorizedUserDao authorizedUserDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Before
	public void setUp() {
		jdbcTemplate.execute("CREATE TABLE addresses ( label VARCHAR(256), address VARCHAR(256), id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY);");
		jdbcTemplate.execute("create table authorized_users(full_name varchar(128) not null, screen_name varchar(128) not null primary key);");
		jdbcTemplate.execute("create table authorized_user_addresses(address_id int not null, authorized_user_screen_name varchar(128) not null);");
	}

	@After
	public void tearDown() {
		jdbcTemplate.execute("DROP TABLE addresses;");
		jdbcTemplate.execute("DROP TABLE authorized_users;");
		jdbcTemplate.execute("DROP TABLE authorized_user_addresses;");
	}

	@Test
	public void testGetAll() {
		List<SavedEmailAddress> addresses = bulkEmailAddressDao.getAll();
		assertTrue(addresses.size() == 0);

		bulkEmailAddressDao.addEmailAddress(makeMeAnAddress());
		bulkEmailAddressDao.addEmailAddress(makeMeAnAddress());
		bulkEmailAddressDao.addEmailAddress(makeMeAnAddress());

		addresses = bulkEmailAddressDao.getAll();
		assertTrue(addresses.size() == 3);
	}

	@Test
	public void testAddEmailAddress() {
		SavedEmailAddress emailAddress = makeMeAnAddress();

		List<SavedEmailAddress> addresses = bulkEmailAddressDao.getAll();
		assertFalse(addresses.contains(emailAddress));

		bulkEmailAddressDao.addEmailAddress(emailAddress);

		addresses = bulkEmailAddressDao.getAll();
		assertTrue(addresses.contains(emailAddress));
	}

	private SavedEmailAddress makeMeAnAddress() {
		double r = Math.random() * 100;
		SavedEmailAddress emailAddress = new SavedEmailAddress();
		emailAddress.setAddress("test" + r + "@example.com");
		emailAddress.setLabel("label" + r);
		return emailAddress;
	}

	@Test
	public void testRemoveEmailAddress() {
		final SavedEmailAddress address = makeMeAnAddress();

		bulkEmailAddressDao.addEmailAddress(address);
		List<SavedEmailAddress> addresses = bulkEmailAddressDao.getAll();
		assertTrue(addresses.contains(address));

		bulkEmailAddressDao.removeEmailAddress(address);
		addresses = bulkEmailAddressDao.getAll();
		assertFalse(addresses.contains(address));
	}

	@Test
	public void testGetAndSaveAddressesForUser() {
		AuthorizedUser user = new AuthorizedUser();
		final String screenName = "user123";
		user.setScreenName(screenName);
		user.setFullName("Full Name");
		authorizedUserDao.add(user);

		List<SavedEmailAddress> addressesForUser = bulkEmailAddressDao.getAddressesForUser(screenName);
		assertEquals(0, addressesForUser.size());

		final SavedEmailAddress address1 = makeMeAnAddress();
		bulkEmailAddressDao.addEmailAddress(address1);
		final SavedEmailAddress address2 = makeMeAnAddress();
		bulkEmailAddressDao.addEmailAddress(address2);
		
		List<SavedEmailAddress> addresses = Arrays.asList(address1, address2);

		bulkEmailAddressDao.updateAddressesForUser(screenName, addresses);
		
		addressesForUser = bulkEmailAddressDao.getAddressesForUser(screenName);
		assertEquals(2, addressesForUser.size());
	}

}
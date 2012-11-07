package edu.drexel.goodwin.portal.bulkemailportlet.addresses;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BulkEmailAddressDaoImpl implements BulkEmailAddressDao{

	private final SavedEmailAddressRowMapper savedEmailAddressRowMapper = new SavedEmailAddressRowMapper();
	
	private final PersonRowMapper personRowMapper = new PersonRowMapper();
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<SavedEmailAddress> getAll() {
		List<SavedEmailAddress> addresses = jdbcTemplate.query("Select * from addresses order by label", savedEmailAddressRowMapper);
		return addresses;
	}

	@Override
	public void addEmailAddress(SavedEmailAddress address) {
		jdbcTemplate.update("insert into addresses (address, label) values(?, ?)", address.getAddress(), address.getLabel());
	}

	@Override
	public void removeEmailAddress(SavedEmailAddress address) {
		jdbcTemplate.update("delete from authorized_user_addresses where address_id = ?", address.getId());
		jdbcTemplate.update("delete from addresses where id = ? and address = ? and label = ?", address.getId(), address.getAddress(), address.getLabel());

	}

	@Override
	public List<SavedEmailAddress> getAddressesForUser(String screenName) {
		return jdbcTemplate.query("select * from addresses a inner join authorized_user_addresses aua on aua.address_id = a.id where aua.authorized_user_screen_name = ?", new Object[] { screenName }, savedEmailAddressRowMapper);
	}

	@Override
	public void updateAddressesForUser(String screenName, List<SavedEmailAddress> addresses) {
		jdbcTemplate.update("delete from authorized_user_addresses where authorized_user_screen_name = ?", screenName);
		for (SavedEmailAddress savedEmailAddress : addresses) {
			jdbcTemplate.update("insert into authorized_user_addresses (authorized_user_screen_name, address_id) values (?, ?)", screenName, savedEmailAddress.getId());
		}
	}
	
	@Override
	public void upadteUsersForAddress(Long addressId, List<String> screenNames){
		jdbcTemplate.update("delete from authorized_user_addresses where address_id = ?", addressId);
		for(String screenName : screenNames){
			jdbcTemplate.update("insert into authorized_user_addresses (authorized_user_screen_name,address_id) values (?,?)", screenName,addressId);
		}
				
	}

	@Override
	public SavedEmailAddress getEmailBylabel(String label) {
		return jdbcTemplate.queryForObject("Select * from addresses a where a.label = ?", new Object[] {label}, savedEmailAddressRowMapper);
	}
	
	@Override
	public Person getPerson(String role) {
		return jdbcTemplate.queryForObject("Select * from people a where a.role = ?", new Object[] {role}, personRowMapper);
	}
	
	private class SavedEmailAddressRowMapper implements RowMapper<SavedEmailAddress> {
		@Override
		public SavedEmailAddress mapRow(ResultSet row, int rowNumber) throws SQLException {
			SavedEmailAddress emailAddress = new SavedEmailAddress();
			emailAddress.setId(row.getLong("id"));
			emailAddress.setAddress(row.getString("address"));
			emailAddress.setLabel(row.getString("label"));
			return emailAddress;
		}
	}
	
	private class PersonRowMapper implements RowMapper<Person> {
		@Override
		public Person mapRow(ResultSet row, int rowNumber) throws SQLException {
			Person person = new Person();
			person.setId(row.getLong("id"));
			person.setRole(row.getString("role"));
			person.setName(row.getString("name"));
			person.setAddress(row.getString("address"));		
			return person;
		}
	}
	
}

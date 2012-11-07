package edu.drexel.goodwin.portal.bulkemailportlet.addresses;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface BulkEmailAddressDao {

	List<SavedEmailAddress> getAll();
	
	Person getPerson(String name); 
	
	List<SavedEmailAddress> getAddressesForUser(String screenName); 
	
	void addEmailAddress(SavedEmailAddress emailAddress);

	void removeEmailAddress(SavedEmailAddress emailAddress);

	void updateAddressesForUser(String screenName, List<SavedEmailAddress> addresses);
	
	void upadteUsersForAddress(Long addressId, List<String> screenNames);
	
	SavedEmailAddress getEmailBylabel(String label);
	
}

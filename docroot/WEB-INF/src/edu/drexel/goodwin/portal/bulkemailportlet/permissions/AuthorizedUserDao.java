package edu.drexel.goodwin.portal.bulkemailportlet.permissions;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizedUserDao {

	List<AuthorizedUser> getAll();

	void add(AuthorizedUser user);

}

package edu.drexel.goodwin.portal.bulkemailportlet.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuthorizedUserDaoImpl implements AuthorizedUserDao {

	private final AuthorizedUserRowMapper authorizedUserRowMapper = new AuthorizedUserRowMapper();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<AuthorizedUser> getAll() {
		return jdbcTemplate.query("Select * from authorized_users order by screen_name", authorizedUserRowMapper);
	}

	@Override
	public void add(AuthorizedUser user) {
		jdbcTemplate.update("insert into authorized_users (full_name, screen_name) values (?,?)  ", user.getFullName(), user.getScreenName());

	}

	private class AuthorizedUserRowMapper implements RowMapper<AuthorizedUser> {
		@Override
		public AuthorizedUser mapRow(ResultSet row, int rowNumber) throws SQLException {
			AuthorizedUser user = new AuthorizedUser();
			user.setFullName(row.getString("full_name"));
			user.setScreenName(row.getString("screen_name"));
			return user;
		}
	}

}

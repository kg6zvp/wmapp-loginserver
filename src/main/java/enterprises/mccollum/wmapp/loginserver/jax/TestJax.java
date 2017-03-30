package enterprises.mccollum.wmapp.loginserver.jax;

import java.util.UUID;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.TestUser;
import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.loginserver.TokenUtils;

@Local
@Stateless
public class TestJax {
	@Inject
	UserGroupBean groupBean;
	
	@Inject
	DomainUserBean userBean;
	
	@Inject
	UserTokenBean tokenBean;
	
	@Inject
	TokenUtils tokenUtils;
	
	/**
	 * Provide a response for the test user's getToken function
	 * @param obj
	 * @return
	 */
	public Response getToken(JsonObject obj) {
		String username = obj.getString("username");
		if(username.contains("@"))
			username = username.split("@")[0];
		String password = obj.getString("password");

		String deviceName = null;
		if(obj.containsKey("devicename") && !obj.isNull("devicename")){
			deviceName = obj.getString("devicename");
		}else{
			deviceName = UUID.randomUUID().toString(); //generate random string from UUID
		}
		
		if(!username.equals(TestUser.USERNAME)
			|| !password.equals(TestUser.PASSWORD))
			return Response.status(Status.UNAUTHORIZED).build();
		
		DomainUser u = getTestDomainUser(); //TODO: Test/fix the testdomainuser stuff
		
		UserToken token = new UserToken();
		token.setDeviceName(deviceName);
		//remove expired tokens from database or something
		token.setBlacklisted(false);
		token.setExpirationDate(tokenUtils.getNewExpirationDate());
		token.setStudentID(u.getStudentId());
		token.setUsername(u.getUsername());
		token.setEmployeeType(u.getEmployeeType());
		
		token = tokenBean.persist(token); //actually put it in the database and get the ID for the token
		//do magical encryption stuff here probably
		return Response.ok(token).build();
	}

	/**
	 * Return a DomainUser object populated with the data from the test user
	 * @return
	 */
	private DomainUser getTestDomainUser() {
		if(userBean.containsKey(TestUser.STUDENT_ID))
			return userBean.get(TestUser.STUDENT_ID);
		DomainUser u = new DomainUser();
		u.setDepartment(TestUser.DEPARTMENT);
		u.setEmail(TestUser.EMAIL);
		u.setEmployeeType(TestUser.EMPLOYEE_TYPE);
		u.setFirstName(TestUser.FIRST_NAME);
		u.setFullName(TestUser.FULL_NAME);
		u.setLastName(TestUser.LAST_NAME);
		u.setPhone(TestUser.PHONE);
		u.setStudentId(TestUser.STUDENT_ID);
		u.setUsername(TestUser.USERNAME);

		userBean.persist(u);
		u.addToGroup(getTestGroup());
		groupBean.save(getTestGroup());
		userBean.save(u);
		return u;
	}

	private UserGroup getTestGroup() {
		if(groupBean.findGroup(TestUser.GROUP_NAME) != null)
			return groupBean.findGroup(TestUser.GROUP_NAME);
		UserGroup g = new UserGroup();
		g.setName(TestUser.GROUP_NAME);
		groupBean.persist(g);
		return g;
	}

}

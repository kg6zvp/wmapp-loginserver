/**
 * 
 */
package enterprises.mccollum.wmapp.loginserver.jax;

import java.util.Base64;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.unboundid.ldap.sdk.LDAPException;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.loginserver.LdapCapture;

/**
 * @author smccollum
 *
 */
@RequestScoped
@Path("token")
@Produces({ "application/json", "application/xml" })
@Consumes({ "application/json", "application/xml" })
public class TokenResources {
	@Inject
	DomainUserBean userBean;

	@Inject
	UserGroupBean groupBean;
	
	@Inject
	UserTokenBean tokenBean;
	
	@Inject
	LdapCapture ldapManager;
	
	//getToken
	@POST
	@Path("getToken")
	public Response getToken(JsonObject obj){
	//public Response getToken(@FormParam("username")String username, @FormParam("password")String password, @FormParam("devicename")String deviceName){
		String username = obj.getString("username");
		String password = obj.getString("password");
		String deviceName = obj.getString("devicename");
		System.out.println("username: "+username);
		DomainUser u = null;
		u = ldapManager.login(username, password);
		if(u == null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("").build(); //return useful error to client for debugging porpoises
		if(u == null)
			return Response.status(Status.FORBIDDEN).build();
		UserToken token = new UserToken();
		token.setDeviceName(deviceName);
		//remove expired tokens from database or something
		token.setBlacklisted(false);
		token.setExpirationDate(System.currentTimeMillis());
		token.setGroups(u.getGroups());
		token.setStudentID(u.getStudentId());
		token.setUsername(u.getUsername());
		
		token = tokenBean.persist(token); //actually put it in the database and get the ID for the token
		//do magical encryption stuff here probably
		return Response.ok(token).build();
	}
	
	//renewToken
	@POST
	@Path("renewToken")
	public Response renewToken(){
		return Response.ok().build();
	}
	
	//tokenValid
	@GET
	@Path("tokenValid")
	public Response isValidToken(){
		return Response.ok().build();
	}
	
	//invalidateToken
	@POST
	@Path("invalidateToken")
	public Response invalidateToken(){
		return Response.ok().build();
	}
	
	//listTokens
	@GET
	@Path("listTokens")
	public Response listTokens(){
		return Response.ok().build();
	}
	
	//subscribeToInvalidation
	@POST
	@Path("subscribeToInvalidation")
	public Response subscribeToInvalidation(){
		return Response.ok().build();
	}
	
	private Object fixRecursion(Object o){
		if(o instanceof UserToken){
			UserToken u = (UserToken)o;
			for(UserGroup g : u.getGroups())
				g.setUsers(null);
			return u;
		}else if(o instanceof UserGroup){
			UserGroup g = (UserGroup)o;
			for(DomainUser u : g.getUsers()){ //list users, but don't list groups and users for every user
				u.setGroups(null);
			}
			return g;
		}
		return null;
	}
	
	private String signToken(UserToken token){
		Base64.Encoder encoder = Base64.getEncoder();
		return "";
	}
}

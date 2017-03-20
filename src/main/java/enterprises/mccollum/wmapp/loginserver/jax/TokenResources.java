/**
 * 
 */
package enterprises.mccollum.wmapp.loginserver.jax;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.unboundid.ldap.sdk.LDAPBindException;
import com.unboundid.ldap.sdk.LDAPException;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.InvalidationSubscription;
import enterprises.mccollum.wmapp.authobjects.InvalidationSubscriptionBean;
import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.loginserver.CryptoSingleton;
import enterprises.mccollum.wmapp.loginserver.LdapCapture;
import enterprises.mccollum.wmapp.loginserver.TokenUtils;

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
	
	@Inject
	TokenUtils tokenUtils;
	
	@Inject
	CryptoSingleton cs;
	
	@Inject
	InvalidationSubscriptionBean invalidations;
	
	//getToken
	@POST
	@Path("getToken")
	public Response getToken(JsonObject obj){
	//public Response getToken(@FormParam("username")String username, @FormParam("password")String password, @FormParam("devicename")String deviceName){
		String username = obj.getString("username");
		String password = obj.getString("password");
		String deviceName = null;
		if(obj.containsKey("devicename") && !obj.isNull("devicename")){
			deviceName = obj.getString("devicename");
		}else{
			deviceName = UUID.randomUUID().toString(); //generate random string from UUID
		}
		System.out.println("username: "+username);
		DomainUser u = null;
		try {
			u = ldapManager.login(username, password);
		} catch (LDAPBindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.UNAUTHORIZED).entity(mkErrorEntity("Incorrect username or password")).build();
		} catch(LDAPException e){
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(mkErrorEntity("LDAP error")).build(); //return useful error to client for debugging porpoises
		}
		UserToken token = new UserToken();
		token.setDeviceName(deviceName);
		//remove expired tokens from database or something
		token.setBlacklisted(false);
		token.setExpirationDate(getNewExpirationDate());
		token.setStudentID(u.getStudentId());
		token.setUsername(u.getUsername());
		token.setEmployeeType(u.getEmployeeType());
		
		token = tokenBean.persist(token); //actually put it in the database and get the ID for the token
		//do magical encryption stuff here probably
		return Response.ok(token).build();
	}
	
	private JsonObject mkErrorEntity(String msg){
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("error", msg);
		return job.build();
	}
	
	//renewToken
	@POST
	@Path("renewToken")
	public Response renewToken(UserToken givenToken, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		//check if token is still valid
		try {
			if(!validateToken(givenToken, signatureB64)){
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} catch (NoSuchAlgorithmException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("500: NoSuchAlgorithm").build();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (SignatureException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		//TODO: Add announcement
		//create new token
		UserToken token = givenToken;
		//TODO: would be nice to check LDAP database for new stuff instead of reusing data
		token.setExpirationDate(getNewExpirationDate()); //TODO: Add Jared's code
		token = tokenBean.save(token);
		System.out.println("Renewing: "+givenToken.getUsername());
		return Response.ok(token).build();
	}
	
	private boolean validateToken(UserToken givenToken, String signatureB64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		if(signatureB64 == null){ //if there's no signature, just leave
			System.out.println("no signature");
			return false;
		}
		byte[] givenTokenBytes = tokenUtils.getTokenString(givenToken).getBytes(StandardCharsets.UTF_8);
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(cs.getPublicKey());
		sig.update(givenTokenBytes);
		if(!sig.verify(Base64.getDecoder().decode(signatureB64))){ //if key wasn't signed by us
			System.out.println("key wasn't signed by us");
			return false;
		}
		//load old token from database
		List<UserToken> matches = tokenBean.getMatching(givenToken);
		if(matches.size() != 1){ //if their token isn't real or is corrupted or is just somehow wrong to allow it to match with others
			//TODO: Notify admin that a signed token has been found which is invalid!
			System.out.println("Matches: "+matches.size());
			return false;
		}
		UserToken oldToken = matches.get(0);
		//check expiration date
		if(oldToken.getExpirationDate() < System.currentTimeMillis()){ //if the expiration is before now, they're not authorized
			System.out.println("Rejected for expiration date");
			return false;
		}
		return true;
	}
	
	/**
	 * Return an expiration date valid for the token currently being generated
	 * @return
	 */
	private Long getNewExpirationDate(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return cal.getTimeInMillis();
	}

	//tokenValid
	@GET
	@Path("tokenValid")
	public Response isValidToken(UserToken givenToken, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		return Response.ok().build();
	}
	
	//invalidateToken
	@POST
	@Path("invalidateToken")
	public Response invalidateToken(UserToken givenToken, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		return Response.ok().build();
	}
	
	//listTokens
	@GET
	@Path("listTokens")
	public Response listTokens(@HeaderParam(UserToken.TOKEN_HEADER)String tokenString, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		Gson gson = new Gson();
		UserToken token = gson.fromJson(tokenString, UserToken.class);
		try {
			if(!validateToken(token, signatureB64)){
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		UserToken key = new UserToken();
		key.setStudentID(token.getStudentID());
		DomainUser u = userBean.get(token.getStudentID());
		List<UserGroup> groups = new LinkedList<>();
		for(UserGroup g : u.getGroups()){
			groups.add(g);
		}
		List<UserToken> outstandingTokens = new LinkedList<>();
		for(UserToken t : tokenBean.getMatching(key)){
			if(!t.getBlacklisted() && t.getExpirationDate() > System.currentTimeMillis()){ //if it's not blacklisted and it's not expired
				outstandingTokens.add(t);
			}
		}
		return Response.ok().build();
	}
	
	//subscribeToInvalidation
	@POST
	@Path("subscribeToInvalidation")
	public Response subscribeToInvalidation(UserToken givenToken, @HeaderParam("invalidationEndpoint")String url, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		InvalidationSubscription invalidSub = new InvalidationSubscription();
		invalidSub.setUrl(url);
		invalidSub.setTokenId(givenToken.getTokenId());
		invalidations.persist(invalidSub);
		return Response.ok().build();
	}
	
	/**
	 * Designed to fix infinite node recursion
	 * @param o
	 * @return
	 */
	private Object fixRecursion(Object o){
		if(o instanceof UserGroup){
			UserGroup g = (UserGroup)o;
			for(DomainUser u : g.getUsers()){ //list users, but don't list groups and users for every user
				u.setGroups(null);
			}
			return g;
		}
		return null;
	}
}

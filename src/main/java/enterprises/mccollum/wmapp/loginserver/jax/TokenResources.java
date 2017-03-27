/**
 * 
 */
package enterprises.mccollum.wmapp.loginserver.jax;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.unboundid.ldap.sdk.LDAPBindException;
import com.unboundid.ldap.sdk.LDAPException;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.InvalidationSubscription;
import enterprises.mccollum.wmapp.authobjects.InvalidationSubscriptionBean;
import enterprises.mccollum.wmapp.authobjects.TestUser;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.loginserver.LdapCapture;
import enterprises.mccollum.wmapp.loginserver.TokenUtils;
import enterprises.mccollum.wmapp.loginserver.ValidationUtils;

/**
 * @author smccollum
 *
 */
@RequestScoped
@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
	InvalidationSubscriptionBean invalidations;
	
	@Inject
	ValidationUtils validationUtils;
	
	@Inject
	APIUtils apiUtils;
	
	@Inject
	TokenUtils tokenUtils;

	@Inject
	TestJax testJax;
	
	//getToken
	/**
	 * @api {post} https://auth.wmapp.mccollum.enterprises/api/token/getToken Get Token 
	 * @apiName PostGetToken
	 * @apiGroup Token
	 * @apiDescription This call will retrieve a User Token to use for all microservices.
	 * @apiParam {String} Token The User Token used to authenticate.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 * @apiError (Response Error) {401} UNAUTHORIZED The username or password was incorrect.
	 * @apiError (Response Error) {500} INTERNAL_SERVER_ERROR There was an error with the LDAP query.
	 * @apiSuccess (Success Response Header) {String} TokenSignature	The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 * @apiSuccess {long} tokenId	The UUID of the token generated
	 * @apiSuccess {long} studentId	The student ID of the user.
	 * @apiSuccess {String} username	The username of the user.
	 * @apiSuccess {String} devicename	The name of the device being used.
	 * @apiSuccess {long} expirationDate	The expiration date of the token in milliseconds EPOCH time.
	 * @apiSuccess {boolean} blacklisted	The status of the token. Will be true if invalidateToken has been called on this token.
	 * @apiSuccess {String} employeeType	The type of the user. Can be student, facstaff, alum, or community.
	 * @apiExample Success Response Header Example
	 * TokenSignature: NU2edZPpt1RjkvJjNM2t1l/fP0p8in+6mqk7Nh6Govxo6EZaei4B16iHMLDY0PwB/FvAvZwQEuT25l6CQSLTC4sC8KBWdIDGTV/k698ZEOqoytibRU05AKrGmcSZsdfqdhZAS9cp1apGTQXrijP/0BicpjIM+sVB71sN/mMecsVSG1qHJxpiothNgcuJCG0uBgMwLKpuhhZ67s6kDbr7pyH49bal4ooBfbmS50PcaN5IhFaD7YtOb1FRD6dK0DgYwcjOulfQ4I3HXgnQ1i9IWXjQbFKSFNlpg414yW9tA7xgcL3bvIiRSpruW6J2LaOKQNv9qQO5wXbcQ3BrWXPc7jbljrH8296kBfhzPmAtH2xDg4uzI/JRby7NS5ftDGOouP6ptBp/Do4pMQviPDX46dcYzD5c=
	 * @apiExample Success Response Body Example
	 * {
  "tokenId": 5,
  "studentID": 851,
  "username": "spidey",
  "deviceName": "iphone7",
  "employeeType": "student",
  "expirationDate": 1492934837502,
  "blacklisted": false
}
	 *
	 */
	@POST
	@Path("getToken")
	public Response getToken(JsonObject obj){
	//public Response getToken(@FormParam("username")String username, @FormParam("password")String password, @FormParam("devicename")String deviceName){
		String username = obj.getString("username");
		if(username.equals(TestUser.USERNAME))
			return testJax.getToken(obj);
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
			return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("Incorrect username or password")).build();
		} catch(LDAPException e){
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity("LDAP error")).build(); //return useful error to client for debugging porpoises
		}
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

	//renewToken
	/**
	 * @api {post} https://auth.wmapp.mccollum.enterprises/api/token/renewToken Renew Token
	 * @apiName PostRenewToken
	 * @apiGroup Token
	 * @apiDescription This call allows a user to update a User Token with a new expiration date. 
	 * @apiParam {String} Token The User Token used to authenticate.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 * @apiError (Response Error) {500} INTERNAL_SERVER_ERROR Can be No Such Algorithm, Invalid Signature, or Invalid Public Key.
	 * @apiError (Response Error) {401} UNAUTHORIZED The username or password was incorrect.
	 * @apiSuccess {long} tokenId	The UUID of the token generated
	 * @apiSuccess {long} studentId	The student ID of the user.
	 * @apiSuccess {String} username	The username of the user.
	 * @apiSuccess {String} devicename	The name of the device being used.
	 * @apiSuccess {long} expirationDate	The expiration date of the token in milliseconds EPOCH time.
	 * @apiSuccess {boolean} blacklisted	The status of the token. Will be true if invalidateToken has been called on this token.
	 * @apiSuccess {String} employeeType	The type of the user. Can be student, facstaff, alum, or community.
	 * @apiExample Success Response Header Example
	 * TokenSignature: NU2edZPpt1RjkvJjNM2t1l/fP0p8in+6mqk7Nh6Govxo6EZaei4B16iHMLDY0PwB/FvAvZwQEuT25l6CQSLTC4sC8KBWdIDGTV/k698ZEOqoytibRU05AKrGmcSZsdfqdhZAS9cp1apGTQXrijP/0BicpjIM+sVB71sN/mMecsVSG1qHJxpiothNgcuJCG0uBgMwLKpuhhZ67s6kDbr7pyH49bal4ooBfbmS50PcaN5IhFaD7YtOb1FRD6dK0DgYwcjOulfQ4I3HXgnQ1i9IWXjQbFKSFNlpg414yW9tA7xgcL3bvIiRSpruW6J2LaOKQNv9qQO5wXbcQ3BrWXPc7jbljrH8296kBfhzPmAtH2xDg4uzI/JRby7NS5ftDGOouP6ptBp/Do4pMQviPDX46dcYzD5c=
	 * @apiExample Success Response Body Example
	 * {
  "tokenId": 5,
  "studentID": 851,
  "username": "spidey",
  "deviceName": "iphone7",
  "employeeType": "student",
  "expirationDate": 1492934837502, // Will return with updated Expiration Date
  "blacklisted": false
}
	 */
	@POST
	@Path("renewToken")
	public Response renewToken(UserToken givenToken, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		//check if token is still valid
		try {
			if(!validationUtils.validateToken(givenToken, signatureB64)){
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} catch (NoSuchAlgorithmException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity("500: NoSuchAlgorithm")).build();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build();
		} catch (SignatureException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build();
		}
		//TODO: Add announcement
		//create new token
		UserToken token = givenToken;
		//TODO: would be nice to check LDAP database for new stuff instead of reusing data
		token.setExpirationDate(tokenUtils.getNewExpirationDate());
		token = tokenBean.save(token);
		System.out.println("Renewing: "+givenToken.getUsername());
		return Response.ok(token).build();
	}

	//tokenValid
	/**
	 * @api {get} https://auth.wmapp.mccollum.enterprises/api/token/tokenValid Check Valid Token
	 * @apiName GetTokenValid
	 * @apiGroup Token
	 * @apiDescription This call is for checking if a User Token is valid. 
	 * @apiParam {String} Token The token that needs to be checked.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature that needs to be verified.
	 * 
	 * 
	 * @param tokenString The User Token given that you are trying to check the validity of.
	 * @param signatureB64 The SHA256 RSA signature of the User Token that you are trying to verify.
	 * @return Not implemented yet.
	 */
	@GET
	@Path("tokenValid")
	public Response isValidToken(@HeaderParam(UserToken.TOKEN_HEADER)String tokenString, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		UserToken authToken = new Gson().fromJson(tokenString, UserToken.class);
		try{
			if(!validationUtils.validateToken(authToken, signatureB64))
				return Response.status(Status.UNAUTHORIZED).build();
		}catch(Exception e){
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build();
		}
		
		//If we made it here, the token's okay and we should let them know
		return Response.status(Status.OK).build();
	}
	
	//invalidateToken
	/**
	 * @api {post} https://auth.wmapp.mccollum.enterprises/api/token/invalidateToken Invalidate Token
	 * @apiName PostInvalidateToken
	 * @apiGroup Token
	 * @apiDescription This call allows a user to invalidate a token on another device that they are signed in on. 
	 * @apiParam {String} Token The User Token used to authenticate.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 * 
	 * @param givenToken The token to be invalidated
	 * @param tokenString The token being used to authenticate this request. This can be the same as givenToken
	 * @param signatureB64 The signature of the token being used to authenticate this request
	 */
	@POST
	@Path("invalidateToken")
	public Response invalidateToken(UserToken givenToken, @HeaderParam(UserToken.TOKEN_HEADER)String tokenString, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		UserToken authToken = new Gson().fromJson(tokenString, UserToken.class);
		try {
			if(!validationUtils.validateToken(authToken, signatureB64))
				return Response.status(Status.UNAUTHORIZED).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build();
		}

		if(!authToken.getUsername().equals(givenToken.getUsername())) //If someone's trying to invalidate anther user's token
			return Response.status(Status.FORBIDDEN).entity(apiUtils.mkErrorEntity("You are not allowed to invalidate someone else's token. An administrator will be notified")).build();

		//If we made it here, the token's alright
		UserToken token = tokenBean.getMatching(givenToken).get(0);
		token.setBlacklisted(true);
		tokenBean.save(token);
		return Response.status(Status.OK).entity(token).build();
	}
	
	//listTokens
	/**
	 * @api {get} https://auth.wmapp.mccollum.enterprises/api/token/listTokens Get Token List
	 * @apiName GetTokenList
	 * @apiGroup Token
	 * @apiDescription This call is for retrieving all the tokens associated with the user account. 
	 * @apiParam {String} Token The User Token used to retrieve all tokens associated with the user.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 * @apiError (Response Error) {500} INTERNAL_SERVER_ERROR Can be No Such Algorithm, Invalid Signature, or Invalid Public Key.
	 * @apiError (Response Error) {401} UNAUTHORIZED The username or password was incorrect.
	 * @apiExample  Success Response
	 * [
  {
    "tokenId": 4,
    "studentID": 935,
    "username": "erichtofen",
    "deviceName": "box",
    "employeeType": "student",
    "expirationDate": 1492934768266,
    "blacklisted": false
  },
  {
    "tokenId": 5,
    "studentID": 935,
    "username": "erichtofen",
    "deviceName": "mk2",
    "employeeType": "student",
    "expirationDate": 1492934837502,
    "blacklisted": false
  }
]
	 *
	 */
	@GET
	@Path("listTokens")
	public Response listTokens(@HeaderParam(UserToken.TOKEN_HEADER)String tokenString, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		Gson gson = new Gson();
		UserToken token = gson.fromJson(tokenString, UserToken.class);
		try {
			if(!validationUtils.validateToken(token, signatureB64)){
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		UserToken key = new UserToken();
		key.setStudentID(token.getStudentID());
		List<UserToken> outstandingTokens = new LinkedList<>();
		for(UserToken t : tokenBean.getMatching(key)){
			if(!t.getBlacklisted() && t.getExpirationDate() > System.currentTimeMillis()){ //if it's not blacklisted and it's not expired
				outstandingTokens.add(t);
			}
		}
		return Response.ok(outstandingTokens).build();
	}
	
	//subscribeToInvalidation
	/**
	 * @api {post} https://auth.wmapp.mccollum.enterprises/api/token/subscribeToInvalidation Subscribe To Token Invalidation
	 * @apiName PostInvalidTokenSubscription
	 * @apiGroup Token
	 * @apiDescription This call allows a microservice to subscribe to updates from the central server for token invalidations. 
	 * @apiParam {String} Token The User Token used to authenticate.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 */
	@POST
	@Path("subscribeToInvalidation")
	public Response subscribeToInvalidation(InvalidationSubscription invalidationSubscription, @HeaderParam(UserToken.TOKEN_HEADER)String givenToken, @HeaderParam(UserToken.SIGNATURE_HEADER)String signatureB64){
		//TODO: Add authentication and authorization checks.
		UserToken authToken = new Gson().fromJson(givenToken, UserToken.class);
		try{
			if(!validationUtils.validateToken(authToken, signatureB64))
				return Response.status(Status.UNAUTHORIZED).build();
		}catch(Exception e){
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build();
		}
			
		invalidationSubscription.setId(null); //just in case someone tries something funny
		invalidationSubscription.setTokenId(authToken.getTokenId()); //set explicitly
		invalidations.persist(invalidationSubscription); //throw it in the database
		
		return Response.status(Status.OK).build();
	}
	
}

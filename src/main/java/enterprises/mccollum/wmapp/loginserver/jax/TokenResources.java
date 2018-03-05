/**
 * 
 */
package enterprises.mccollum.wmapp.loginserver.jax;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unboundid.ldap.sdk.LDAPBindException;

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
import enterprises.mccollum.wmapp.ssauthclient.APIUtils;
import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

/**
 * @author smccollum
 *
 */
@RequestScoped
@Path("token")
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
	APIUtils apiUtils;
	
	@Inject
	TokenUtils tokenUtils;

	@Inject
	TestJax testJax;
	
	//getToken
	/**
	 * @api {post} api/token/getToken Get Token 
	 * @apiName PostGetToken
	 * @apiGroup Token
	 * @apiDescription This call will retrieve a User Token to use for all microservices
	 *
	 * @apiParam (credentials) {String} username Username of the user to login
	 * @apiParam (credentials) {String} [devicename] The name of the device being logged in from. This is used to help the user identify which devices they're logged in on. If unspecified, a random UUID will be generated for this value
	 * @apiParam (credentials) {String} password The password of the user account to be logged into
	 *
	 * @apiError (Response Error) {401} UNAUTHORIZED The username or password was incorrect.
	 * @apiError (Response Error) {500} INTERNAL_SERVER_ERROR There was an error with the LDAP query.
	 *
	 * @apiSuccess (Success Response Header) {String} TokenSignature	The base64 encoded signature of the user's token (<a href="http://lmgtfy.com?iie=1&q=what+is+base64+encoding" target="_blank">What is base64 encoding</a>)
	 * @apiSuccess {long} tokenId	The UUID of the token generated
	 * @apiSuccess {long} studentId	The student ID of the user.
	 * @apiSuccess {String} username	The username of the user.
	 * @apiSuccess {String} devicename	The name of the device being used.
	 * @apiSuccess {long} expirationDate	The expiration date/time of the token in milliseconds using EPOCH time (<a href="http://lmgtfy.com/?iie=1&q=what+is+epoch+time" target="_blank">What is epoch time</a>)
	 * @apiSuccess {boolean} blacklisted	The token's revocation status (will be true if token has been invalidated or revoked)
	 * @apiSuccess {String} employeeType	The type of the user (most commonly: student, facstaff, alum, or community)
	 *
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
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getToken(JsonObject obj){
	//public Response getToken(@FormParam("username")String username, @FormParam("password")String password, @FormParam("devicename")String deviceName){
		String username = obj.getString("username");
		if(username.contains("@"))
			username = username.split("@")[0];
		username = username.toLowerCase(); //fix users inputting caseful strings
		if(username.equals(TestUser.USERNAME))
			return testJax.getToken(obj);
		String password = obj.getString("password");
		String deviceName = null;
		if(obj.containsKey("devicename") && !obj.isNull("devicename")){
			deviceName = obj.getString("devicename");
		}else{
			deviceName = UUID.randomUUID().toString(); //generate random string from UUID
		}
		DomainUser u = null;
		try {
			u = ldapManager.login(username, password);
		} catch (LDAPBindException e) {
			e.printStackTrace();
			System.out.printf("Login failure: %s\n", username);
			return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("Incorrect username or password")).build();
		} catch(Exception e){
			e.printStackTrace();
			System.out.printf("Error accessing LDAP: %s\n", username);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity("LDAP error"+e.getMessage())).build(); //return useful error to client for debugging porpoises
		}
		System.out.printf("Login Success: %s\n", username);
		return Response.ok(tokenUtils.createToken(u, deviceName, false)).build();
	}
	
	/**
	 * Issues a new token, but is currently not implemented
	 * @return
	 */
	@GET
	@Path("getToken")
	public Response issueToken(@Context SecurityContext seCtx){
		Principal tp = seCtx.getUserPrincipal();
		if(tp == null || !(tp instanceof WMPrincipal))
				return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		WMPrincipal principal = (WMPrincipal) tp;
		System.out.println("GET getToken: "+principal.getName());
		return Response.status(Status.NOT_IMPLEMENTED).build();
	}

	//renewToken
	/**
	 * @api {get} api/token/renewToken Renew Token
	 * @apiName PostRenewToken
	 * @apiGroup Token
	 * @apiDescription This call allows a user to update a User Token with a new expiration date. 
	 * @apiHeader {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 * @apiHeader {json} Token The User Token used to authenticate.
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
	@GET
	@Path("renewToken")
	public Response renewToken(@Context SecurityContext seCtx){
		Principal principal = seCtx.getUserPrincipal();
		if(!(principal instanceof WMPrincipal)){
			return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("Not authenticated")).build();
		}
		UserToken token = tokenBean.getByTokenId(((WMPrincipal)principal).getToken().getTokenId());
		token.setExpirationDate(tokenUtils.getNewExpirationDate());
		token = tokenBean.save(token);
		System.out.println("Renewing: "+token.getUsername()+", ID="+token.getId()+", tID="+token.getTokenId());
		return Response.ok(token).build();
	}//*/
	
	//tokenValid
	/**
	 * @api {get} api/token/tokenValid Check Valid Token
	 * @apiName GetTokenValid
	 * @apiGroup Token
	 * @apiDescription This call is for checking if a User Token is valid. 
	 * @apiHeader {String} TokenSignature The base64 encoded signature of the token
	 * @apiHeader {json} Token The token that needs to be checked.
	 * 
	 * 
	 * @param tokenString The User Token given that you are trying to check the validity of.
	 * @param signatureB64 The SHA256 RSA signature of the User Token that you are trying to verify.
	 * @return Not implemented yet.
	 */
	@GET
	@Path("tokenValid")
	public Response isValidToken(@Context SecurityContext seCtx){
		Principal tp = seCtx.getUserPrincipal();
		if(tp == null || !(tp instanceof WMPrincipal))
				return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		//If we made it here, the token's okay and we should let them know
		return Response.status(Status.OK).build();
	}
	
	//invalidateToken
	/**
	 * @api {delete} api/token/invalidateToken/{tokenId} Invalidate Token
	 * @apiName InvalidateToken
	 * @apiGroup Token
	 * @apiParam tokenID {long} The ID of the token to be deleted
	 * @apiDescription This call allows a user to invalidate a token on another device that they are signed in on. 
	 * @apiHeader Token The User Token used to authenticate this request
	 * @apiHeader TokenSignature The base64 encoded signature of the token used to authenticate
	 * 
	 * @param tokenId The ID of the token to be invalidated
	 * @param tokenString The token being used to authenticate this request. This can be the same as givenToken
	 * @param signatureB64 The signature of the token being used to authenticate this request
	 */
	@DELETE
	@Path("invalidateToken/{tokenId}")
	public Response invalidateToken(@PathParam("tokenId")Long tokenId, @Context SecurityContext seCtx){
		Principal tp = seCtx.getUserPrincipal();
		if(tp == null || !(tp instanceof WMPrincipal))
				return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		WMPrincipal principal = (WMPrincipal) tp;
		
		UserToken specifiedToken = tokenBean.getByTokenId(tokenId); //token specified for invalidation
		if(specifiedToken == null) //if the token specified for invalidation doesn't exist
			return Response.status(Status.GONE).entity(apiUtils.mkErrorEntity("The specified token cannot be invalidated because it does not exist")).build();
		
		if(!principal.getToken().getUsername().equals(specifiedToken.getUsername())){ //If someone's trying to invalidate anther user's token
			return Response.status(Status.FORBIDDEN).entity(apiUtils.mkErrorEntity(String.format("You are not allowed to invalidate someone else's token. An administrator will be notified %s != %s", principal.getToken().getUsername(), specifiedToken.getUsername()))).build();
			//TODO: notify admin
		}

		//If we made it here, the token's alright
		specifiedToken.setBlacklisted(true);
		tokenBean.save(specifiedToken);
		return Response.status(Status.OK).entity(specifiedToken).build();
	}
	
	//listTokens
	/**
	 * @api {get} api/token/listTokens Get Token List
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response listTokens(@Context SecurityContext seCtx){
		Principal tp = seCtx.getUserPrincipal();
		if(tp == null || !(tp instanceof WMPrincipal))
				return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		WMPrincipal principal = (WMPrincipal) tp;
		
		UserToken key = new UserToken();
		key.setStudentID(principal.getToken().getStudentID());
		List<UserToken> outstandingTokens = new LinkedList<>();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		for(UserToken t : tokenBean.getMatching(key)){
			if(!t.getBlacklisted() && t.getExpirationDate() > (System.currentTimeMillis()/1000)){ //if it's not blacklisted and it's not expired
				outstandingTokens.add(t);
			}
		}
		String tokenList = gson.toJson(outstandingTokens); 
		return Response.ok(tokenList).build();
	}
	
	//subscribeToInvalidation
	/**
	 * @api {post} api/token/subscribeToInvalidation Subscribe To Token Invalidation
	 * @apiName PostInvalidTokenSubscription
	 * @apiGroup Token
	 * @apiDescription This call allows a microservice to subscribe to updates from the central server for token invalidations. 
	 * @apiParam {String} Token The User Token used to authenticate.
	 * @apiParam {String} TokenSignature The base64 encoded SHA256 RSA signature of the token that needs to be verified.
	 */
	@POST
	@Path("subscribeToInvalidation")
	public Response subscribeToInvalidation(InvalidationSubscription invalidationSubscription, @Context SecurityContext seCtx){
		Principal tp = seCtx.getUserPrincipal();
		if(tp == null || !(tp instanceof WMPrincipal))
				return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		WMPrincipal principal = (WMPrincipal) tp;
		
		invalidationSubscription.setId(null); //just in case someone tries something funny
		invalidationSubscription.setTokenId(principal.getToken().getTokenId()); //set explicitly
		invalidations.persist(invalidationSubscription); //throw it in the database
		
		return Response.status(Status.OK).build();
	}
	
}

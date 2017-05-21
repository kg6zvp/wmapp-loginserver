package enterprises.mccollum.wmapp.loginserver.jax;

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

import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.loginserver.ValidationUtils;
import enterprises.mccollum.wmapp.loginserver.logic.UserLogicBean;
import enterprises.mccollum.wmapp.ssauthclient.APIUtils;

/**
 * 
 * @author smccollum
 *
 */
@Path("user")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UsersResources {
	@Inject
	DomainUserBean usersBean;
	
	@Inject
	UserGroupBean groupBean;
	
	@Inject
	ValidationUtils valUtils;
	
	@Inject
	APIUtils apiUtils;
	
	@Inject
	UserLogicBean userLogic;
	
	
	
	@GET
	public Response getUserInfo(@HeaderParam(UserToken.TOKEN_HEADER)String tokenString, @HeaderParam(UserToken.SIGNATURE_HEADER) String sigBase64){
		try{
			if(!valUtils.validateToken(tokenString, sigBase64))
				return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		}catch(Exception e){
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build();
		}
		return Response.status(Status.NOT_IMPLEMENTED).build();
	}
	
	@POST
	public Response updateUserInfo(JsonObject credentials){
		if(!credentials.containsKey("username"))
			return Response.status(Status.BAD_REQUEST).entity(apiUtils.mkErrorEntity("Missing username attribute from json object in request body")).build();
		if(!credentials.containsKey("password"))
			return Response.status(Status.BAD_REQUEST).entity(apiUtils.mkErrorEntity("Missing password attribute from json object in request body")).build();
		if(userLogic.updateUserDB(credentials.getString("username"), credentials.getString("password")))
			return Response.status(Status.OK).build();
		return Response.status(Status.SERVICE_UNAVAILABLE).build();
	}
}

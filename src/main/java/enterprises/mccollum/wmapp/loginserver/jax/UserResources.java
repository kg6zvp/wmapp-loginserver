package enterprises.mccollum.wmapp.loginserver.jax;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.loginserver.ValidationUtils;
import enterprises.mccollum.wmapp.loginserver.logic.UserLogicBean;
import enterprises.mccollum.wmapp.ssauthclient.APIUtils;
import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

/**
 * 
 * @author smccollum
 *
 */
@Path("user")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class UserResources {
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
	public Response getUserInfo(@Context SecurityContext seCtx){
		if(!(seCtx.getUserPrincipal() instanceof WMPrincipal))
			return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("You are not authenticated")).build();
		WMPrincipal principal = (WMPrincipal) seCtx.getUserPrincipal();
		DomainUser u = usersBean.get(principal.getToken().getStudentID());
		u.setGroups(null); //avoid infinite recusion ;-)
		return Response.ok(u).build();
	}
	
	@POST
	public void updateUserInfo(JsonObject credentials, @Suspended final AsyncResponse asyncResponse){
		if(!credentials.containsKey("username")){
			asyncResponse.resume( Response.status(Status.BAD_REQUEST).entity(apiUtils.mkErrorEntity("Missing username attribute from json object in request body")).build());
			return;
		}
		if(!credentials.containsKey("password")){
			asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(apiUtils.mkErrorEntity("Missing password attribute from json object in request body")).build());
			return;
		}
		int processed = 0;
		asyncResponse.resume(Response.status(Status.OK).entity(apiUtils.mkSingleResponseObject("result", String.format("Update successfully started"))).build());
		try {
			processed = userLogic.updateUserDB(credentials.getString("username"), credentials.getString("password"));
		} catch (Exception e) {
			e.printStackTrace();
			asyncResponse.resume(Response.status(Status.INTERNAL_SERVER_ERROR).entity(apiUtils.mkErrorEntity(e.getMessage())).build());
		}
		asyncResponse.resume(Response.status(Status.OK).entity(apiUtils.mkSingleResponseObject("result", String.format("%d entries processed", processed))).build());
	}
}

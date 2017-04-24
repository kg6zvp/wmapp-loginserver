package enterprises.mccollum.wmapp.loginserver.jax;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.loginserver.ValidationUtils;
import enterprises.mccollum.wmapp.ssauthclient.APIUtils;

/**
 * 
 * @author smccollum
 *
 */
@Path("users")
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
}

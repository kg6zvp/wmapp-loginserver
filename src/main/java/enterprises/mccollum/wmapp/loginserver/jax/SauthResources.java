package enterprises.mccollum.wmapp.loginserver.jax;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import enterprises.mccollum.sauth.TempKeyBean;
import enterprises.mccollum.sauth.TempUserInfoKeyEntity;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.ssauthclient.APIUtils;

@Path("sauth")
@Produces(MediaType.APPLICATION_JSON)
public class SauthResources {
	@Inject
	TempKeyBean tempKeyBean;
	
	@Inject
	APIUtils apiUtils;
	
	@GET
	@Path("/{key}")
	public Response getUserToken(@PathParam("key") String key){
		TempUserInfoKeyEntity tokenInfo = tempKeyBean.get(key);
		if(tokenInfo == null){
			tempKeyBean.expireOld();
			return Response.status(Status.NOT_FOUND).entity(apiUtils.mkErrorEntity("Key not found")).build();
		}
		if(tokenInfo.getExpirationDate() < System.currentTimeMillis()){
			tempKeyBean.remove(tokenInfo);
			return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("Key expired")).build();
		}
		UserToken token = tokenInfo.getToken();
		tempKeyBean.remove(tokenInfo);
		tempKeyBean.expireOld();
		return Response.ok(token).build();
	}
	
	@GET
	public Response getTokenWithCode(@QueryParam(OAuthEndpoint.PARAM_CODE) String code){
		return getUserToken(code);
	}
}

/**
 * 
 */
package enterprises.mccollum.wmapp.loginserver.jax;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author smccollum
 *
 */
@RequestScoped
@Path("token")
@Produces({ "application/json", "application/xml" })
@Consumes({ "application/json", "application/xml" })
public class TokenResources {
	
	//getToken
	@POST
	@Path("getToken")
	public Response getToken(@FormParam("username")String username, @FormParam("password")String password, @FormParam("devicename")String deviceName){
		return Response.ok().build();
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
}

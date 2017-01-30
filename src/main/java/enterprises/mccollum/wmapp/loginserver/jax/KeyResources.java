package enterprises.mccollum.wmapp.loginserver.jax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("key")
public class KeyResources {
	@GET
	@Path("getPubKey")
	public Response getPublicKey(){
		return Response.ok().build();
	}
}

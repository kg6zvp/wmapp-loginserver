package enterprises.mccollum.wmapp.loginserver.jax;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.unboundid.util.Base64;

import enterprises.mccollum.wmapp.loginserver.CryptoSingleton;

@Path("key")
public class KeyResources {
	@Inject
	CryptoSingleton cs;
	
	@GET
	@Path("getPubKey")
	public Response getPublicKey(){
		return Response.ok(Base64.encode(cs.getPublicKey().getEncoded())).build();
	}
}

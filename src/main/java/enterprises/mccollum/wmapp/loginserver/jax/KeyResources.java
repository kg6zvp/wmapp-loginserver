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
	
	private final static String BEGIN_PUBKEY_PADDING = "-----BEGIN PUBLIC KEY-----";
	private final static String END_PUBKEY_PADDING = "-----END PUBLIC KEY-----";
	
	@GET
	@Path("getPubKey")
	public Response getPublicKey(){
		return Response.ok(getPemString(Base64.encode(cs.getPublicKey().getEncoded()))).build();
	}
	
	private String getPemString(String b64String){
		return BEGIN_PUBKEY_PADDING+'\n'+
				addReturns(b64String)+'\n'+
				END_PUBKEY_PADDING+'\n';
	}

	private String addReturns(String b64String) {
		StringBuilder sb = new StringBuilder(b64String.length()+20);
		int end = 0;
		int i = 0;
		for(i = 0; i < b64String.length(); i += 64){
			end = i+64;
			System.out.printf("begin: %d\nend: %d\n", i, end);
			if(end < b64String.length()){
				sb.append(b64String.substring(i, end)+"\n");
			}
		}
		sb.append(b64String.substring(i-64));
		System.out.println("Built string: "+sb.toString());
		return sb.toString();
		//return b64String.replaceAll("(.{64})", "\n");
	}
}

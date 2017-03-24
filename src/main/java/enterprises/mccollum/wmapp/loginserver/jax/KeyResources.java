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
	
	/**
	 * @api {get} https://auth.wmapp.mccollum.enterprises/api/key/getPubKey Request Public Key
	 * @apiName GetPubKey
	 * @apiGroup Key 
	 * @apiDescription This method is for requesting the public key which is used to verify the signature on a signed user token.
	 * @apiExample {String} Success Response
	 * 				-----BEGIN PUBLIC KEY-----
	 * 				MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvTdcdxIfo7iO0viB5TUl 
	 *				uEBPw4BaAtKPgOonxPfuSfOHP1SkBau/2NhJLKSc/P8gH4TrAnbuW3a14VeyQtBo
	 *				nZxYaRetD8wS2JRRfswYJve8jtWUcE7I0a5JjBqCCgdNFXQcubh33ilj+WPvUX7X
	 *				MXkNyQZ+4IOKP8iTo3OD5TzLJT17A5n5flvPQfXbLrzzBCqGxTgG3kLpx6Ya6YO7
	 *				kxPDZbRFgstV2sobBIhsA5bdw92r/UA0SZlojinnsxcj2XRwutBD1iZG/nyK8Rd3
	 *				PfIys9KhqQs1nddz5Zy3ZcaPNnEQaYZzVO5nKPBGuEprTOLHS4soclWz/Rbd6PDr
	 *				43Sax0i3mYiNXxOsFUjBo8RfVknkahwqDeXxxNTm86cu/kUXoOm2nTTFzHgnO7Qm
	 *				QMWzadT9BD3F34HDUYACXJnWDHRCnyJpQ48G9vR9clPSd37ygq0MPpkyI+yJ4mlL
	 *				xNjRtsm1hKCm4rHtoBl7D3GNP1REm+CUvKcMJaCgrQ+W2Cz0PYSGipaiRNw3YrCz
	 *				QZHAiEkyyAvvox647cRd/RPjHAtaPwuuB+ifAuCrmTqyNWLMoRBuJwwPiNiMEX+y
	 *				tNdHLNt/WKGTl55mDC7fqp9unq8PNdkOCKMLw3uJyIDg3C7J/kY11XL6URCRtGAa
	 *				UpRIx64HdyFNybeS3mMi5bkCAwEAAQ==
	 *				-----END PUBLIC KEY-----
	 * 	
	 *
	 */
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

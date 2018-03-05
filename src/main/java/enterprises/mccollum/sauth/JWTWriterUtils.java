package enterprises.mccollum.sauth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import enterprises.mccollum.ssauthclient.JWTReaderUtils;
import enterprises.mccollum.wmapp.loginserver.CryptoSingleton;

@Local
@Stateless
public class JWTWriterUtils {
	@Inject
	CryptoSingleton cs;
	
	final String headerB64;
	
	final Gson gsonWithExclusions;
	final Gson gson;
	
	public JWTWriterUtils(){
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("alg", "RS256");
		job.add("typ", "JWT");
		headerB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(job.build().toString().getBytes(JWTReaderUtils.DECODE_CHARSET));
		gsonWithExclusions = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		gson = new Gson();
	}
	
	/**
	 * Create a JWT from the given token
	 * @param token: The token to be turned into a Jwt
	 * @param useExclusions: Whether or not to convert the token to Json using Gson that excludes fields without the expose annotation
	 * @return Token as a string
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public String getJwt(Object token, boolean useExclusions) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		String tokenString = (useExclusions ? gsonWithExclusions.toJson(token) : gson.toJson(token));
		String tokenB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenString.getBytes(JWTReaderUtils.DECODE_CHARSET));
		
		String headerPayload = String.format("%s.%s", headerB64, tokenB64); //combine header and payload
		
		byte[] signatureBin = sign(headerPayload.getBytes(JWTReaderUtils.DECODE_CHARSET));
		String signatureB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBin);
		
		return String.format("%s.%s", headerPayload, signatureB64);
	}
	
	/**
	 * Create a JWT from the given token
	 * 
	 * Note: This function excludes Json fields not marked with Gson's @Expose annotation
	 * @param token: The token to be turned into a JWT
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public String getJwt(Object token) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		return getJwt(token, true);
	}
	
	private byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature signer = Signature.getInstance("SHA256withRSA");
		signer.initSign(cs.getPrivateKey()); //should be the private key here
		signer.update(data); //prepare for signature by sending it the data we're going to use
		return signer.sign();
	}
	
	//private void sampleWriteJwt(){
		/*String header = Base64.encodeBase64URLSafeString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.encodeBase64URLSafeString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String content = String.format("%s.%s", header, payload);

        byte[] signatureBytes = algorithm.sign(content.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.encodeBase64URLSafeString((signatureBytes));

        return String.format("%s.%s", content, signature);//*/
	//}
}

package enterprises.mccollum.wmapp.loginserver;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;

/**
 * 
 * @author smccollum
 *
 */
@Local
@Stateless
public class ValidationUtils {
	@Inject
	TokenUtils tokenUtils;
	
	@Inject
	CryptoSingleton cs;

	@Inject
	UserTokenBean tokenBean;
	
	public boolean validateToken(String tokenString, String signatureB64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Gson gson = new Gson();
		return validateToken(gson.fromJson(tokenString, UserToken.class), signatureB64);
	}
	
	public boolean validateToken(UserToken givenToken, String signatureB64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		if(signatureB64 == null){ //if there's no signature, just leave
			System.out.println("no signature");
			return false;
		}
		byte[] givenTokenBytes = tokenUtils.getTokenString(givenToken).getBytes(StandardCharsets.UTF_8);
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(cs.getPublicKey());
		sig.update(givenTokenBytes);
		if(!sig.verify(Base64.getDecoder().decode(signatureB64))){ //if key wasn't signed by us
			System.out.println("key wasn't signed by us");
			return false;
		}
		//load old token from database
		List<UserToken> matches = tokenBean.getMatching(givenToken);
		if(matches.size() != 1){ //if their token isn't real or is corrupted or is just somehow wrong to allow it to match with others
			//TODO: Notify admin that a signed token has been found which is invalid!
			System.out.println("Matches: "+matches.size());
			return false;
		}
		UserToken oldToken = matches.get(0);
		if(!jsonMatches(oldToken, givenToken))
			return false;
		//check expiration date
		if(oldToken.getExpirationDate() < System.currentTimeMillis()){ //if the expiration is before now, they're not authorized
			System.out.println("Rejected for expiration date");
			return false;
		}
		return true;
	}
	
	/**
	 * Check if the json generated from both class instances is a perfect match
	 * 
	 * @param oldToken
	 * @param givenToken
	 * @return
	 */
	private boolean jsonMatches(UserToken oldToken, UserToken givenToken) {
		Gson gson = new Gson();
		return (gson.toJson(oldToken).equals(gson.toJson(givenToken)));
	}
}

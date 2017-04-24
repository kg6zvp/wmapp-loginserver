package enterprises.mccollum.wmapp.loginserver;

import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.google.gson.Gson;

import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.ssauthclient.SSAuthClient;

/**
 * 
 * @author smccollum
 *
 */
@Singleton
@Startup
public class TokenUtils {
	Gson gson;
	
	@Inject
	SSAuthClient ssAuthClient;
	
	@PostConstruct
	public void init(){
		gson = new Gson();
	}

	public String getTokenArray(List<UserToken> tokens){
		return ssAuthClient.getGsonWithExclusions().toJson(tokens);
	}
	
	public String getTokenString(UserToken givenToken) {
		return ssAuthClient.getGsonWithExclusions().toJson(givenToken);
	}

	/**
	 * Return an expiration date valid for the token currently being generated
	 * @return
	 */
	public Long getNewExpirationDate(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return cal.getTimeInMillis();
	}
	
	public Gson getGson(){
		return gson;
	}

	public UserToken getToken(String tokenString) {
		return getGson().fromJson(tokenString, UserToken.class);
	}
}

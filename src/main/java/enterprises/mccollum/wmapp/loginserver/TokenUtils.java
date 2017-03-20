package enterprises.mccollum.wmapp.loginserver;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.google.gson.Gson;

import enterprises.mccollum.wmapp.authobjects.UserToken;

@Singleton
@Startup
public class TokenUtils {
	Gson gson;
	
	@PostConstruct
	public void init(){
		gson = new Gson();
	}

	public String getTokenArray(List<UserToken> tokens){
		return gson.toJson(tokens);
	}
	
	public String getTokenString(UserToken givenToken) {
		return gson.toJson(givenToken);
	}
}

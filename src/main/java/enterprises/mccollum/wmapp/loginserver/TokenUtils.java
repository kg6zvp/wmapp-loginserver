package enterprises.mccollum.wmapp.loginserver;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserToken;

@Singleton
@Startup
public class TokenUtils {

	@PostConstruct
	public void init(){}

	public JsonObjectBuilder getGroupObject(UserGroup group){
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("id", group.getId());
		job.add("name", group.getName());
		job.add("ldapName", group.getLdapName());
		return job;
	}

	public String getJsonString(JsonObjectBuilder job){
		return job.build().toString();
	}

	public JsonObjectBuilder getTokenObject(UserToken userToken){
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("tokenId", userToken.getTokenId());
		job.add("studentID", userToken.getStudentID());
		job.add("username", userToken.getUsername());
		job.add("deviceName", userToken.getDeviceName());
		job.add("expirationDate", userToken.getExpirationDate());
		job.add("blacklisted", userToken.getBlacklisted());
		job.add("employeeType", userToken.getEmployeeType());
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(UserGroup g : userToken.getGroups()){
			jab.add(getGroupObject(g));
		}
		job.add("groups", jab);
		return job;
	}
}

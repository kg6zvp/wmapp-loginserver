package enterprises.mccollum.sauth;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import enterprises.mccollum.wmapp.authobjects.UserToken;

@Entity
@XmlRootElement
public class TempUserInfoKeyEntity {
	@Id
	String key;
	Long expirationDate;
	
	@OneToOne
	UserToken token;
	
	public TempUserInfoKeyEntity(){}

	public void generateKey(){
		setKey(token.getUsername()+UUID.randomUUID().toString());
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Long getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Long expirationDate) {
		this.expirationDate = expirationDate;
	}
	public UserToken getToken() {
		return token;
	}
	public void setToken(UserToken token) {
		this.token = token;
	}
}

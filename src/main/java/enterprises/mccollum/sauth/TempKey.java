package enterprises.mccollum.sauth;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import enterprises.mccollum.wmapp.authobjects.UserToken;

@Entity
@XmlRootElement
public class TempKey {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	
	String keyStr;
	Long expirationDate;
	
	@OneToOne
	UserToken token;
	
	public TempKey(){}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void generateKey(){
		setKey(token.getUsername()+UUID.randomUUID().toString());
	}
	public String getKey() {
		return keyStr;
	}
	public void setKey(String key) {
		this.keyStr = key;
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

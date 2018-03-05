package enterprises.mccollum.wmapp.loginserver;

import java.util.Calendar;
import java.util.UUID;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.ssauthclient.SSAuthClient;

/**
 * 
 * @author smccollum
 *
 */
@Local
@Stateless
public class TokenUtils {
	@Inject
	SSAuthClient ssAuthClient;

	@Inject
	UserTokenBean tokenBean;
	
	final long timeDiff;
	final long timeDiffWeb;
	
	public TokenUtils(){
		timeDiff = genTimeDiff();
		timeDiffWeb = genTimeDiffWeb();
	}
	
	long genTimeDiff(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return ((cal.getTimeInMillis()-System.currentTimeMillis())/1000);
	}
	
	long genTimeDiffWeb(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 8);
		return ((cal.getTimeInMillis()-System.currentTimeMillis())/1000);
	}

	/**
	 * Create a token for the given DomainUser with the given deviceName
	 * @param u
	 * @param deviceName
	 * @param isWebToken
	 * @return
	 */
	public UserToken createToken(DomainUser u, String deviceName, boolean isWebToken){
		UserToken token = new UserToken();
		token.setDeviceName( (deviceName != null ? deviceName : UUID.randomUUID().toString()) );
		token.setBlacklisted(false); //not blacklisted by default
		token.setIssueDate( (System.currentTimeMillis()/1000) ); //Set the issue date
		token.setExpirationDate( (isWebToken ? getNewExpirationDateWeb() : getNewExpirationDate()) );
		token.setStudentID(u.getStudentId());
		token.setUsername(u.getUsername());
		token.setEmployeeType(u.getEmployeeType());
		
		token = tokenBean.persist(token); //actually put it in the database and get the ID for the token
		token.setTokenId(token.getId()); //set the tokenID to the id from the database
		token = tokenBean.save(token); //Save the changes to the token
		return token;
	}
	
	/**
	 * Return an expiration date valid for the token currently being generated
	 * 
	 * Uses Unix time (like System.currentTimeMillis()/1000)
	 * @return
	 */
	public long getNewExpirationDate(){
		/*Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return cal.getTimeInMillis();//*/
		return ( (System.currentTimeMillis()/1000) + timeDiff);
	}
	/**
	 * Return an expiration date for a web token
	 * 
	 * Uses Unix time (like System.currentTimeMillis()/1000)
	 * @return
	 */
	public long getNewExpirationDateWeb(){
		return ( (System.currentTimeMillis()/1000) + timeDiffWeb);
	}
}

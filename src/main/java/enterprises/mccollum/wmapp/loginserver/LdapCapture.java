package enterprises.mccollum.wmapp.loginserver;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPBindException;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;

/**
 * Various methods for interacting with the LDAP server utilizing LDAP UnboundID LDAP SDK Standard Edition
 * 
 * @author James Solum, Sam McCollum
 * 
 * About UnboundID: https://www.ldap.com/unboundid-ldap-sdk-for-java
 * Using UnboundID: https://docs.ldap.com/ldap-sdk/docs/getting-started/connections.html
 * 
 * TODO: Implement a connection pool, 
 */
@Singleton
@Startup
public class LdapCapture {
	public static String server = "pdc.westmont.edu"; // Westmont LDAP Server IP address
	public static int port = 389;
	public static String userBaseDN = "CN=Users,DC=campus,DC=westmont,DC=edu";
	public static String groupBaseDN = "CN=Groups,DC=campus,DC=westmont,DC=edu";

	@Inject
	DomainUserBean dUsers;

	@Inject
	UserGroupBean uGroups;

	@Inject
	UserTokenBean tokens;
	
	private UserGroup readGroupFromEntry(SearchResultEntry entry) throws LDAPSearchException{
		UserGroup g = new UserGroup();
		//create instance of UserGroup from appropriate LDAP entry
		if(entry == null) //not sure what sometimes happens here
			return null;
		if(!entry.hasAttribute("distinguishedName")){
			Logger.getLogger("LDAPCapture").log(Level.WARNING, String.format("Couldn't get group info for group %s", entry.toLDIFString()));
			return null;
		}
		g.setLdapName(entry.getAttributeValue("distinguishedName"));
		g.setName(entry.getAttributeValue("cn"));
		return g;
	}

	/**
	 * Create an LDAP group from an Entry and persist it to the database
	 * @param entry
	 * @return
	 * @throws LDAPSearchException
	 */
	@SuppressWarnings("unused")
	private UserGroup groupFromEntry(SearchResultEntry entry) throws LDAPSearchException{
		return uGroups.persist(readGroupFromEntry(entry));
	}

	/**
	 * Read a user from {@link SearchResultEntry} without side-effects (doesn't write to database)
	 * @param conn
	 * @param userEntry
	 * @return
	 * @throws LDAPSearchException
	 */
	private DomainUser readUserFromEntry(LDAPConnection conn, SearchResultEntry userEntry) throws LDAPSearchException{
		DomainUser u = new DomainUser();
		Long studentID = userEntry.getAttributeValueAsLong("uidNumber"); // Use datatelID if this breaks
		if(studentID == null)
			studentID = userEntry.getAttributeValueAsLong("datatelID");
		u.setStudentId(studentID);
		u.setFirstName(userEntry.getAttributeValue("givenName"));
		u.setLastName(userEntry.getAttributeValue("sn")); 
		u.setFullName(userEntry.getAttributeValue("displayName"));
		u.setUsername(userEntry.getAttributeValue("cn"));
		u.setPhone(userEntry.getAttributeValue("telephoneNumber"));
		u.setPhoneExtension(userEntry.getAttributeValue("phoneExtension"));
		u.setEmail(userEntry.getAttributeValue("mail"));
		u.setEmployeeType(userEntry.getAttributeValue("employeeType"));
		return u;
	}
	
	//TODO: re-write so that database key is not assumed to come from AD DC
	@Lock(LockType.READ)
	public DomainUser userFromEntry(LDAPConnection conn, SearchResultEntry userEntry) throws LDAPSearchException, LDAPException, RuntimeException{
		//check if all groups listed by user exist and if not, create them, then add user to them
		DomainUser u = readUserFromEntry(conn, userEntry);
		if(u.getStudentId() == null){
			Logger.getLogger("LDAPCapture").log(Level.WARNING, String.format("Couldn't find user id for user %s", u.getUsername()));
			return null;
		}
		DomainUser tempUser = dUsers.get(u.getStudentId());
		if(tempUser == null){ //if user isn't in the database...
			u = dUsers.persist(u); //save/add them to it
		}else{
			u = dUsers.save(tempUser);
		}
		/*if(userEntry.hasAttribute("memberOf")){
			for(String groupName : userEntry.getAttributeValues("memberOf")){
				String gCN = getCNFromMemberOf(groupName);
				SearchResultEntry groupEntry = getGroupAttributes(conn, gCN);
				UserGroup g = readGroupFromEntry(groupEntry);
				if(g != null){
					UserGroup tempGroup = uGroups.findGroup(g.getName());
					if(tempGroup != null){ //if the group is already in the database, get it
						g = tempGroup;
					}else{ //if the group doesn't exist, create it
						g = uGroups.persist(g);
					}
					g.addUser(u);
					uGroups.save(g); //save group entry after adding user to group
					u = dUsers.save(u); //save user entry after adding group to user's groups list
				}
			}
		}//*/
		return u; //add users to database and return the database-connected object instance
	}

	/**
	 * Parses the groupName string returned by the memberOf attribute into the real common name (CN)
	 * 
	 * WARNING: Addition of comma to group names in the LDAP database may break **everything**
	 * @param groupName
	 * @return
	 */
	private String getCNFromMemberOf(String groupName) {
		return groupName.split(",")[0].split("=")[1];
	}

	// Logs in and creates basic connection
	@Lock(LockType.READ)
	public DomainUser login(String username, String password) throws LDAPException, LDAPBindException {
		// TODO: allow multiple logins gracefully for the same user on different devices
		LDAPConnection conn = new LDAPConnection();
		conn.connect(server, port); 
		@SuppressWarnings("unused") //we're about to try to assign it. Chill
		BindResult bound = null;
		//try{
			bound = conn.bind(String.format("cn=%s,%s",username,userBaseDN), password);
		/*}catch(LDAPException e){
			conn.close();
			throw e;
		}catch(Exception e){
			conn.close();
			throw e;
		}//*/
		SearchResultEntry userEntry = getUserAttributes(conn, username);
		return userFromEntry(conn, userEntry);
	}
	
	public SearchResultEntry getUserAttributes(LDAPConnection conn, String username) throws LDAPSearchException{
		return getLdapEntry(conn, userBaseDN, "cn", username);
		/*SearchResult searchResult = conn.search(userBaseDN,
				SearchScope.SUB, String.format("cn=%s,", username));
		List<SearchResultEntry> resultsList = searchResult.getSearchEntries();
		for(SearchResultEntry e : resultsList){
			if(e.hasAttribute("cn"))
				if(e.getAttribute("cn").getValue().equals(username))
					return e;
		}
		return null;*/
	}
	
	public SearchResultEntry getGroupAttributes(LDAPConnection conn, String groupName) throws LDAPSearchException{
		return getLdapEntry(conn, groupBaseDN, "cn", groupName);
	}
	
	public SearchResultEntry getLdapEntry(LDAPConnection conn, String baseDN, String type, String value) throws LDAPSearchException{
		SearchResult searchResult = conn.search(baseDN, SearchScope.SUB, String.format("%s=%s", type, value));
		List<SearchResultEntry> resultsList = searchResult.getSearchEntries();
		for(SearchResultEntry e : resultsList){
			if(e.hasAttribute(type))
				if(e.getAttribute(type).getValue().equals(value))
					return e;
		}
		System.out.println("No results!");
		return null;
	}
}
	
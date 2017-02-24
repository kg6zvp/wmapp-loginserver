package enterprises.mccollum.wmapp.loginserver;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.Debug;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;

/**
 * Various methods for interacting with the LDAP server utilizing LDAP UnboundID LDAP SDK Standard Edition
 * 
 * @author James Solum
 * 
 * About UnboundID: https://www.ldap.com/unboundid-ldap-sdk-for-java
 * Using UnboundID: https://docs.ldap.com/ldap-sdk/docs/getting-started/connections.html
 * 
 * Things to do: Implement a connection pool, 
 */
@Singleton
@Startup
public class LdapCapture {
	static String server = "pdc.westmont.edu"; // Westmont LDAP Server IP address
	static int port = 389;
	static String userBaseDN = "cn=Users,dc=campus,dc=westmont,dc=edu";
	static String groupBaseDN = "cn=Groups,dc=campus,dc=westmont,dc=edu";
	private LDAPConnection connection;

	@Inject
	DomainUserBean dUsers;

	@Inject
	UserGroupBean uGroups;

	@Inject
	UserTokenBean tokens;
	
	@PostConstruct
	public void init(){
		Debug.setEnabled(true);
		Debug.getLogger().setLevel(Level.FINEST);
		Debug.setIncludeStackTrace(true);
		connection = new LDAPConnection();
		try{
			connection.connect(server, port); 
		}catch(Exception e){
			System.out.println("You broke everything!!!");
		}
	}

	private UserGroup readGroupFromEntry(SearchResultEntry entry) throws LDAPSearchException{
		UserGroup g = new UserGroup();
		//create instance of UserGroup from appropriate LDAP entry
		g.setId(entry.getAttributeValueAsLong("gidNumber"));
		g.setLdapName(entry.getAttributeValue("dn"));
		g.setName(entry.getAttributeValue("cn"));
		return g;
	}

	/**
	 * Create an LDAP group from an Entry and persist it to the database
	 * @param entry
	 * @return
	 * @throws LDAPSearchException
	 */
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
		u.setStudentId(userEntry.getAttributeValueAsLong("uidNumber")); // Use datatelID if this breaks
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
	
	private DomainUser userFromEntry(LDAPConnection conn, SearchResultEntry userEntry) throws LDAPSearchException{
		//check if all groups listed by user exist and if not, create them, then add user to them
		DomainUser u = dUsers.persist(readUserFromEntry(conn, userEntry));
		for(String groupName : userEntry.getAttributeValues("memberOf")){
			System.out.println("discovered group: "+groupName); //debug thing
			SearchResultEntry groupEntry = getGroupAttributes(conn, groupName);
			UserGroup g = readGroupFromEntry(groupEntry);
			UserGroup tempGroup = uGroups.get(g.getId()); // If group does not exist in database then tempGroup will be NULL
			if(tempGroup != null){ //if the group is already in the database, get it
				g = tempGroup;
			}else{ //if the group doesn't exist, create it
				g = uGroups.persist(g);
			}
			g.addUser(u);
			dUsers.save(u); //save user entry after adding group to user's groups list
			uGroups.save(g); //save group entry after adding user to group
		}
		
		return u; //add users to database and return the database-connected object instance
	}

	// Logs in and creates basic connection
	public DomainUser login(String username, String password) throws LDAPException{
		LDAPConnection conn = new LDAPConnection();
		conn.connect(server, port); 
		System.out.printf("User: %s\nPassword: ****\n", username); //, password);
		BindResult bound = conn.bind(String.format("cn=%s,%s",username,userBaseDN), password);
		SearchResultEntry userEntry = getUserAttributes(connection, username);
		return userFromEntry(connection, userEntry);
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
		SearchResult searchResult = conn.search(baseDN,
				SearchScope.SUB, String.format("%s=%s,", type, value));
		List<SearchResultEntry> resultsList = searchResult.getSearchEntries();
		for(SearchResultEntry e : resultsList){
			if(e.hasAttribute(type))
				if(e.getAttribute(type).getValue().equals(value))
					return e;
		}
		return null;
	}
	
	// Closes the Connection
	public void closeConnection(){
		connection.close();
	}
	
}
	
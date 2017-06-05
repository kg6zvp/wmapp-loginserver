package enterprises.mccollum.wmapp.loginserver.logic;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.authobjects.UserGroupBean;
import enterprises.mccollum.wmapp.loginserver.LdapCapture;

/**
 * Perform business logic on users in the database
 * @author smccollum
 */
@Local
@Stateless
public class UserLogicBean {
	public static final String USERS_FILTER = "(&(objectCategory=person)(objectClass=user))"; //"(&(objectCategory=person)(objectClass=user)(!userAccountControl:1.2.840.113556.1.4.803:=2))";
	public static final String STUDENT_FILTER = "employeeType=student";
	public static final String FACSTAFF_FILTER = "employeeType=facstaff";
	
	@Inject
	DomainUserBean dUsers;
	
	@Inject
	UserGroupBean uGroups;
	
	@Inject
	LdapCapture ldapCapture;
	
	public int updateUserDB(String username, String password) throws Exception{
		LDAPConnection conn = new LDAPConnection();
		conn.connect(LdapCapture.server, LdapCapture.port);
		BindResult bound = conn.bind(String.format("cn=%s,%s", username, LdapCapture.userBaseDN), password);
		Filter peopleFilter = Filter.create("objectCategory=person");
		Filter userClassFilter = Filter.create("objectClass=user");
		Filter ldapThing = Filter.createNOTFilter(Filter.create("userAccountControl:1.2.840.113556.1.4.803:=2"));
		Filter relevantFilter = Filter.createORFilter(Filter.create(STUDENT_FILTER), Filter.create(FACSTAFF_FILTER));
		Filter allUsersFilter = Filter.createANDFilter(peopleFilter, userClassFilter, relevantFilter, ldapThing);
		int results = 0;
		for(SearchResultEntry entry : conn.search(LdapCapture.userBaseDN, SearchScope.SUB, allUsersFilter).getSearchEntries()){
			if(entry.hasAttribute("cn")){ //if it's a valid entry, look at it and cn seems as good an attribute to check for as any
				System.out.println(String.format("Processing %s", entry.getAttributeValue("cn")));
				ldapCapture.userFromEntry(conn, entry);
				++results;
			}else{
				Logger.getLogger("UserLogicBean").log(Level.INFO, "Non-user entry found, whaddya know?");
			}
		}
		return results;
	}
}

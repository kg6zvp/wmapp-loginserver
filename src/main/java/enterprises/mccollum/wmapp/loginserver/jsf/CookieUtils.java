package enterprises.mccollum.wmapp.loginserver.jsf;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Deals with the creation and deletion of cookies
 * 
 * @author smccollum
 */
public class CookieUtils {
	public static void setCookie(HttpServletResponse res, String cookieName, String value, Long expirationDate){
		int expiry = (int) ((expirationDate - System.currentTimeMillis())/1000);
		Cookie cookie = new Cookie(cookieName, value);
		cookie.setPath("/");
		cookie.setVersion(1);
		cookie.setMaxAge(expiry);
		res.addCookie(cookie);
	}
	
	public static void deleteCookie(HttpServletResponse res, String cookieName){
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		res.addCookie(cookie);
	}
}

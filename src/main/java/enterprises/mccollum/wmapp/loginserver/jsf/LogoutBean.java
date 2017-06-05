package enterprises.mccollum.wmapp.loginserver.jsf;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.ssauthclient.SSAuthClient;//*/
import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

@Named
@ConversationScoped
@Stateful(passivationCapable=true)
public class LogoutBean {
	@Inject
	UserTokenBean tokenBean;
	
	public LogoutBean(){
		System.out.println("Created LogoutBean");
	}
	
	public String getLoggedOut(){
		return "";
	}
	
	public void setLoggedOut(String loggedOut){
		logout();
	}
	
	public void logout(){
		Logger.getLogger("LogoutController").log(Level.INFO, "Logging out...");
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		long tokenId = ((WMPrincipal)session.getAttribute(SSAuthClient.PRINCIPAL_SESSION_ATTRIBUTE)).getToken().getId();
		if(session != null){
			session.removeAttribute(SSAuthClient.PRINCIPAL_SESSION_ATTRIBUTE); //remove from session
			session.invalidate();
		}
		CookieUtils.deleteCookie((HttpServletResponse) ec.getResponse(), UserToken.TOKEN_HEADER);
		
		/*
		 * Blacklist token in Database so that it cannot be reused
		 */
		UserToken token = tokenBean.getByTokenId(tokenId);
		token.setBlacklisted(true);
		tokenBean.save(token);
		
		try {
			//ec.redirect("/login");
			ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
		} catch (IOException e) {
			e.printStackTrace();
		}//*/
	}
}

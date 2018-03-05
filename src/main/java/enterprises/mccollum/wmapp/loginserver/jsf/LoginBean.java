package enterprises.mccollum.wmapp.loginserver.jsf;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import enterprises.mccollum.CookieUtils;
import enterprises.mccollum.jee.urlutils.UrlContextUtils;
import enterprises.mccollum.jee.urlutils.UrlStateUtils;
import enterprises.mccollum.sauth.JWTWriterUtils;
import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.loginserver.LdapCapture;
import enterprises.mccollum.wmapp.loginserver.TokenUtils;
import enterprises.mccollum.wmapp.ssauthclient.SSAuthClient;
import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

@Named
@ConversationScoped
@Stateful(passivationCapable=true)
public class LoginBean {
	/**
	 * Base64-encoded URL to redirect to after logging in, passing through all parameters originally given
	 */
	public static final String LOGIN_AFTER_PARAM="redirect_uri";
	
	public static final String LOGIN_KEY_CODE_PARAM="code";
	
	@Inject
	UserTokenBean tokenBean;
	
	@Inject
	LdapCapture ldapManager;
	
	@Inject
	JWTWriterUtils jwtUtils;
	
	@Inject
	TokenUtils tokenUtils;
	
	@Inject
	UrlStateUtils urlStateUtils;
	
	@Inject
	UrlContextUtils urlCtx;
	
	String username;
	
	String password;
	
	String message;
	
	String redirectUrl;
	
	WMPrincipal principal;
	
	public LoginBean(){
		logf(Level.INFO, "created");
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMessage(){
		return message;
	}
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setupOrDoRedirect(){
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest)ec.getRequest();
		Principal p = (Principal) req.getUserPrincipal();
		if(p == null)
			p = (Principal) ((HttpSession)ec.getSession(true)).getAttribute(SSAuthClient.PRINCIPAL_SESSION_ATTRIBUTE);
		Map<String, String> params = ec.getRequestParameterMap();
		if(params != null){
			if(params.containsKey(LOGIN_AFTER_PARAM)){
				redirectUrl = urlStateUtils.decodeUrlStateToRequestUrl(params.get(LOGIN_AFTER_PARAM));
			}else{
				redirectUrl = urlCtx.getApplicationBaseUrl(req)+"/";
				//redirectUrl = ec.getApplicationContextPath()+"/";
			}
		}else{
			redirectUrl = urlCtx.getApplicationBaseUrl(req)+"/";
			//redirectUrl = ec.getApplicationContextPath()+"/";
		}
		logf(Level.INFO, "Redirect url after login: %s", redirectUrl);
		if(p != null && p instanceof WMPrincipal){
			try{
				ec.redirect(redirectUrl);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public void submit(){
		login(username, password);
	}
		
	public void login(String username, String password){
		DomainUser u = null;
		try{
			u = ldapManager.login(username, password);
			logf(Level.INFO, "Login succeeded for %s", getUsername());
		}catch(Exception e){
			e.printStackTrace();
			logf(Level.INFO, "Login failed for %s", getUsername());
			setMessage("Login failed");
			return;
		}
		UserToken token = tokenUtils.createToken(u, null, false); //uses regular token (30 days)
		String tokenString;
		try {
			tokenString = jwtUtils.getJwt(token);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
			return;
		}
		
		setMessage("Login succeeded!");
		
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(true);
		session.setAttribute(SSAuthClient.PRINCIPAL_SESSION_ATTRIBUTE, new WMPrincipal(token, tokenString)); //add to session
		
		CookieUtils.setCookie((HttpServletResponse) ec.getResponse(), UserToken.TOKEN_HEADER, tokenString, token.getJavaExpirationDate() );

		setupOrDoRedirect();
	}
	
	private void logf(Level lvl, String fmt, Object...args){
		Logger.getLogger(LoginBean.class.getSimpleName()).log(lvl, String.format(fmt, args));
	}
}
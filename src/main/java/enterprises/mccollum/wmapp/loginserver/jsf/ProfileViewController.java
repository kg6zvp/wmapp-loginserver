package enterprises.mccollum.wmapp.loginserver.jsf;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import enterprises.mccollum.wmapp.authobjects.DomainUser;
import enterprises.mccollum.wmapp.authobjects.DomainUserBean;
import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

/**
 * Backing bean for the user profile
 * @author smccollum
 */
@Named
@ConversationScoped
@Stateful(passivationCapable=true)
public class ProfileViewController {
	
	public ProfileViewController(){}
	
	@Inject
	DomainUserBean userBean;

	WMPrincipal wmp = null;
	ExternalContext ec;
	
	DomainUser user = null;
	
	@PostConstruct
	@Transactional
	public void init(){
		ec = FacesContext.getCurrentInstance().getExternalContext();
		Principal p = ec.getUserPrincipal();
		System.out.println("Obtained principal: "+(p != null ? p.getName() : "null"));
		if(p != null && p instanceof WMPrincipal){
			wmp = (WMPrincipal)p;
			user = userBean.get(wmp.getToken().getStudentID());
		}
	}
	
	public void checkLogin(){
		try {
			if(wmp == null)
				ec.redirect(getBaseUrl()+"login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DomainUser getUser(){
		return user;
	}
	
	private String getBaseUrl(){
		HttpServletRequest request = (HttpServletRequest) ec.getRequest();
		String url = request.getRequestURL().toString();
		return url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
	}
}

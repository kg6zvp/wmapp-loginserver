package enterprises.mccollum.wmapp.loginserver.jsf;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

@Stateless
public class SecuritySetup {
	SessionContext session;

	@Produces
	public WMPrincipal getPrincipal(){
		ExternalContext eCtx = FacesContext.getCurrentInstance().getExternalContext();
		if(eCtx.getUserPrincipal() != null){
			if(eCtx.getUserPrincipal() instanceof WMPrincipal){
				logf("Principal found: %s", eCtx.getUserPrincipal().getName());
				return (WMPrincipal) eCtx.getUserPrincipal();
			}
		}
		logf("No principal or cookies");
		return null;
	}
	
	void logf(String fmt, Object...params){
		Logger.getLogger("SecuritySetup").log(Level.INFO, String.format(fmt, params));
	}
}

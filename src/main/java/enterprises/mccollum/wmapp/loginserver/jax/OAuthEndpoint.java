package enterprises.mccollum.wmapp.loginserver.jax;

import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import enterprises.mccollum.sauth.TempKeyBean;
import enterprises.mccollum.jee.urlutils.UrlContextUtils;
import enterprises.mccollum.jee.urlutils.UrlStateUtils;
import enterprises.mccollum.sauth.TempKey;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.authobjects.UserTokenBean;
import enterprises.mccollum.wmapp.ssauthclient.APIUtils;
import enterprises.mccollum.wmapp.ssauthclient.WMPrincipal;

@Path("oauth")
public class OAuthEndpoint {
	public static final String PARAM_RESPONSE_TYPE="response_type",
								PARAM_CLIENT_ID="client_id",
								PARAM_REDIRECT_URI="redirect_uri",
								PARAM_SCOPE = "scope",
								PARAM_ENCODED_URI="encoded_uri";

	/**
	 * Response types for the authorization endpoint
	 */
	public static final String RESPONSE_TYPE_CODE="code",
								RESPONSE_TYPE_TOKEN="token";
	
	/**
	 * Scopes for the authorization endpoint
	 * 
	 * SCOPE_WEB: This scope is used to indicate that the code should be passed as a query parameter suitable for web
	 * SCOPE_MOBILE: This scope is used to indicate that the code should be passed after a '#' mark, usually in a custom URL schema
	 */
	public static final String SCOPE_WEB = "web",
								SCOPE_MOBILE="mobile";
	
	@Inject
	TempKeyBean authCodeStore;
	
	@Inject
	APIUtils apiUtils;
	
	@Context
	SecurityContext seCtx;
	
	@Inject
	UrlStateUtils urlStateUtils;
	
	@Context
	UriInfo uriInfo;
	
	@Inject
	HttpServletRequest req;
	
	@Inject
	UrlContextUtils urlCtxUtils;
	
	@Inject
	UserTokenBean tokenBean;
	
	/**
	 * OAuth authorization endpoint
	 * @param responseType: The response type (code and token currently implemented), defaults to code
	 * @param clientId: The client ID
	 * @param redirectUri: The Uri to redirect to when finished
	 * @param scope: 
	 * @return
	 * @throws URISyntaxException if the URI is malformed
	 */
	@GET
	@Path("authorize")
	public Response authorizationEndpoint(
			@DefaultValue(RESPONSE_TYPE_CODE) @QueryParam(PARAM_RESPONSE_TYPE) String responseType,
			@QueryParam(PARAM_CLIENT_ID) String clientId,
			@QueryParam(PARAM_REDIRECT_URI) String redirectUri,
			@DefaultValue("mobile") @QueryParam(PARAM_SCOPE) List<String> scope,
			@DefaultValue("false") @QueryParam(PARAM_ENCODED_URI)Boolean isEncodedUri){
		if(responseType == null || redirectUri == null){
			return Response.status(Status.BAD_REQUEST).entity("Bad request! Missing redirect_uri or response_type parameters").build();
		}
		if(responseType.equals(RESPONSE_TYPE_CODE)){
			return authorizeCodeFlow(clientId, redirectUri, scope, isEncodedUri);
		}else if(responseType.equals(RESPONSE_TYPE_TOKEN)){
			return authorizeTokenFlow(clientId, redirectUri, scope, isEncodedUri);
		}
		return Response.status(Status.BAD_REQUEST).build();
	}
	
	/**
	 * HELPER: Do a temporary redirect
	 * @param url
	 * @return
	 */
	ResponseBuilder doRedirect(String url){
		return Response.status(Status.TEMPORARY_REDIRECT).header("Location", url);
	}
	
	/**
	 * HELPER: Do a redirect to the login page
	 * 
	 * @param req
	 * @return
	 */
	ResponseBuilder doLoginRedirect(HttpServletRequest req){
		StringBuilder sb = new StringBuilder(urlCtxUtils.getApplicationBaseUrl(req));
		sb.append("/login?redirect_uri="+urlStateUtils.encodeRequestUrlToParam(req));
		return doRedirect(sb.toString());
	}
	
	/**
	 * OAuth Implicit Flow
	 * 
	 * @param clientId: the client ID
	 * @param redirectUri: The Uri to redirect to when finished
	 * @param scope: The Oauth Scopes for this application (defaults to mobile if none given)
	 * @return Response: a redirect if the user is not logged in, or to approve the app
	 * @throws URISyntaxException if the URI is malformed
	 */
	Response authorizeTokenFlow(String clientId, String redirectUri, List<String> scope, Boolean isEncodedUri){
		Principal tp = seCtx.getUserPrincipal();
		/**
		 * If they're not logged in, get them to log in
		 */
		if(tp == null || !(tp instanceof WMPrincipal)){
			return doLoginRedirect(req).entity("Please sign in").build();
		}
		WMPrincipal principal = (WMPrincipal) tp;
		boolean isWeb = false; //web or mobile
		check_web: for(String scopeArg: scope){
			if(scopeArg.equals(SCOPE_WEB)){
				isWeb = true;
				break check_web; //for clarity and efficiency
			}
		}
		StringBuilder fBuilder = null;
		if(isEncodedUri)
			redirectUri = urlStateUtils.decodeUrlStateToRequestUrl(redirectUri);
		/**
		 * If it's web
		 */
		if(isWeb){
			/*
			 * Prepare the URL for the token param
			 * 
			 * If there's a question mark, append an extra param
			 */
			fBuilder = prepareUriForParam(redirectUri);
		}else{ //if it's mobile, append a '#'
			fBuilder = new StringBuilder(redirectUri);
			fBuilder.append("#");
		}
		fBuilder.append(RESPONSE_TYPE_TOKEN+"=");
		fBuilder.append(principal.getTokenString());
		return doRedirect(fBuilder.toString()).build();
	}

	StringBuilder prepareUriForParam(String uri){
		StringBuilder fBuilder = new StringBuilder(uri);
		if(uri.contains("?")){
			if(!uri.endsWith("&"))
				fBuilder.append("&");
		}else{
			fBuilder.append("?");
		}
		return fBuilder;
	}
	
	/**
	 * Do the authorization code flow
	 * 
	 * @param clientId
	 * @param redirectUri
	 * @param scope
	 * @return
	 */
	Response authorizeCodeFlow(String clientId, String redirectUri, List<String> scope, Boolean isEncodedUri) {
		/*Principal tp = seCtx.getUserPrincipal();
		/**
		 * If they're not logged in, get them to log in
		 */
		/*if(tp == null || !(tp instanceof WMPrincipal)){
			return doLoginRedirect(req).entity("Please sign in").build();
		}//*/
		
		WMPrincipal principal = getPrincipal();
		if(principal == null){ //if they're not logged in, log them in
			return doLoginRedirect(req).build();
		}
		/*
		 * Build URL and create code
		 */
		String tempKey = createTempKey(principal);
		if(isEncodedUri)
			redirectUri = urlStateUtils.decodeUrlStateToRequestUrl(redirectUri);
		StringBuilder sb = prepareUriForParam(redirectUri);
		sb.append(String.format("%s=%s", RESPONSE_TYPE_CODE, tempKey));
		return doRedirect(sb.toString()).build();
	}
	
	String createTempKey(WMPrincipal wmp) {
		TempKey tempKey = new TempKey();
		tempKey.setToken(tokenBean.getByTokenId(wmp.getToken().getTokenId()));
		tempKey.setExpirationDate(System.currentTimeMillis()+600000); //allow up to 10 minutes for retrieving the key
		tempKey.generateKey();
		authCodeStore.persist(tempKey);
		return tempKey.getKey();
	}//*/

	/**
	 * Params for the token endpoint
	 */
	public static final String PARAM_GRANT_TYPE="grant_type",
								PARAM_CODE="code";
	/**
	 * Grant Types for the token endpoint
	 */
	public static final String GRANT_TYPE_CODE="authorization_code";
	
	@GET
	@Path("token")
	public Response tokenEndpoint(
			@QueryParam(PARAM_GRANT_TYPE) String grantType,
			@QueryParam(PARAM_CODE) String code){
		if(grantType.equals(GRANT_TYPE_CODE))
			return tokenAuthorizationCodeFlow(code);
		return Response.status(Status.BAD_REQUEST).build(); //not sure what we're supposed to do
	}

	Response tokenAuthorizationCodeFlow(String code) {
		if(code == null || code.length() < 1){
			return Response.status(Status.BAD_REQUEST).entity(apiUtils.mkErrorEntity("Missing parameter: code")).build();
		}
		TempKey tokenInfo = authCodeStore.getByKey(code);
		if(tokenInfo == null){
			authCodeStore.expireOld();
			return Response.status(Status.NOT_FOUND).entity(apiUtils.mkErrorEntity("Token not found")).build();
		}
		if(tokenInfo.getExpirationDate() < System.currentTimeMillis()){
			authCodeStore.remove(tokenInfo);
			return Response.status(Status.UNAUTHORIZED).entity(apiUtils.mkErrorEntity("Token expired")).build();
		}
		UserToken token = tokenInfo.getToken();
		authCodeStore.remove(tokenInfo);
		authCodeStore.expireOld();
		return Response.ok(token).build();
	}
	
	WMPrincipal getPrincipal(){
		if(seCtx.getUserPrincipal() != null && seCtx.getUserPrincipal() instanceof WMPrincipal)
			return (WMPrincipal) seCtx.getUserPrincipal();
		return null;
	}
	
	void logf(Level lvl, String fmt, Object...args){
		Logger.getLogger(OAuthEndpoint.class.getSimpleName()).log(lvl, String.format(fmt, args));
	}
}

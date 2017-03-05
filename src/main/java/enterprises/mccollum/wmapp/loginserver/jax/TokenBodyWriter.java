package enterprises.mccollum.wmapp.loginserver.jax;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.MarshallerProperties;

import enterprises.mccollum.wmapp.authobjects.UserGroup;
import enterprises.mccollum.wmapp.authobjects.UserToken;
import enterprises.mccollum.wmapp.loginserver.CryptoSingleton;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class TokenBodyWriter implements MessageBodyWriter<UserToken> {
	@Inject
	CryptoSingleton cs;

	@Override
	public long getSize(UserToken arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		//Doesn't matter for some weird reason
		return 0;
	}

	@Override
	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return (arg0 == UserToken.class);
	}

	@Override
	public void writeTo(UserToken t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
		try {
			//sign token
			JsonObjectBuilder job = getTokenObject(t);
			String objString = getJsonString(job);
			System.out.println("token: "+objString);
			final byte[] unsigned = objString.getBytes("UTF-8");
			Signature signer = Signature.getInstance("SHA256withRSA");
			signer.initSign(cs.getPrivateKey()); //should be the private key here, maybe retrieve from singleton?
			signer.update(unsigned); //prepare for signature
			byte[] signature = signer.sign();
			String signatureString = Base64.getEncoder().encodeToString(signature);
			//add signature to the headers
			httpHeaders.add(UserToken.SIGNATURE_HEADER, signatureString);
			entityStream.write(objString.getBytes("UTF-8"));
		/*} catch (JAXBException e) { 
			e.printStackTrace();
			System.out.println("Something went wrong with the conversion of the object to Json. Why?? I have no idea!:w");//*/
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.out.println("This shouldn't have ever happened as Java is guaranteed to have the SHA256withRSA algorithm in all implementations of the Signature class");
		} catch (SignatureException e) {
			e.printStackTrace();
			System.out.println("I have no idea what's going on here. It seems pretty straightforward to me to run a standardized signature algorithm on binary data. :P");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			System.out.println("Come on, man! All you had to do was provide a valid public/private key. Try again.");
		}
	}
	
	private JsonObjectBuilder getTokenObject(UserToken token){
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("tokenId", token.getTokenId());
		job.add("studentID", token.getStudentID());
		job.add("username", token.getUsername());
		job.add("deviceName", token.getDeviceName());
		job.add("expirationDate", token.getExpirationDate());
		job.add("blacklisted", token.getBlacklisted());
		job.add("employeeType", token.getEmployeeType());
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(UserGroup g : token.getGroups()){
			jab.add(getGroupObject(g));
		}
		job.add("groups", jab);
		return job;
	}
	
	private JsonObjectBuilder getGroupObject(UserGroup group){
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("id", group.getId());
		job.add("name", group.getName());
		job.add("ldapName", group.getLdapName());
		return job;
	}
	
	/**
	 * Convert token object to JSON String and return it. No side-effects
	 * 
	 * @param token
	 * @return
	 * @throws JAXBException
	 */
	private String getJsonString(UserToken token) throws JAXBException{
		JAXBContext jaxCon = JAXBContext.newInstance(token.getClass());
		StringWriter w = new StringWriter();
		Marshaller m = jaxCon.createMarshaller();
		m.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
		m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
		m.marshal(token, w);
		return w.toString();//*/
	}
	
	private String getJsonString(JsonObjectBuilder job){
		return job.build().toString();
	}
}

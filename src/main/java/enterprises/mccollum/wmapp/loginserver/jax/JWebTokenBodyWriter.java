package enterprises.mccollum.wmapp.loginserver.jax;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import enterprises.mccollum.sauth.JWTWriterUtils;
import enterprises.mccollum.wmapp.authobjects.UserToken;

@Provider
@Produces(MediaType.WILDCARD)
public class JWebTokenBodyWriter implements MessageBodyWriter<UserToken> {
	@Inject
	JWTWriterUtils jwtWriterUtils;
	
	final Gson gson;
	
	public JWebTokenBodyWriter() {
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	}
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return (type.equals(UserToken.class) || genericType.equals(UserToken.class));
		//return !(mediaType.equals(MediaType.APPLICATION_JSON)); //don't do it if it's asking for Json
	}

	@Override
	public long getSize(UserToken t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return 0; //don't worry that it's 0
	}

	@Override
	public void writeTo(UserToken t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		if(mediaType.equals(MediaType.APPLICATION_JSON_TYPE)){
			entityStream.write(gson.toJson(t).getBytes(StandardCharsets.UTF_8));
		}else{
			try {
				entityStream.write(jwtWriterUtils.getJwt(t).getBytes(StandardCharsets.UTF_8));
				return;
			} catch (InvalidKeyException e) {
				logef("Come on, man! All you had to do was provide a valid public/private key. Try again.");
			} catch (NoSuchAlgorithmException e) {
				logef("NoSuchAlgorithmException: This shouldn't have ever happened as Java is guaranteed to have the SHA256withRSA algorithm in all implementations of the Signature class");
			} catch (SignatureException e) {
				logef("I have no idea what's going on here. It seems pretty straightforward to me to run a standardized signature algorithm on binary data. :P");
			}
		}
	}

	private void logef(String format, Object...args) {
		logf(Level.SEVERE, format, args);
	}
	private void logf(Level lvl, String format, Object...args) {
		Logger.getLogger("JWebTokenBodyWriter").log(lvl, String.format(format, args));
	}

}

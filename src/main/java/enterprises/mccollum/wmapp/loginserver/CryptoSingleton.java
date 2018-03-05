package enterprises.mccollum.wmapp.loginserver;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * 
 * @author smccollum
 *
 */
@Singleton
@Startup
public class CryptoSingleton {
	/**
	 * This key will contain the public and private keys used to sign content for auth
	 */
	KeyPair serviceKey;
	
	public static final String KEYSTORE_PATH = System.getenv("WMKS_FILE");
	//public static final String KEYSTORE_PATH = "/home/smccollum/wmks.jks";
	private static final char[] KEYSTORE_PASS = "password".toCharArray();
	
	public static final String KEY_ALIAS = "WMAUTH";
	private static final char[] KEY_PASS = "password".toCharArray();
	
	@PostConstruct
	public void init(){
		try {
			logf("onInit()");
			String ksp = KEYSTORE_PATH; //System.getenv("WMKS_FILE");
			if(ksp == null || ksp.length() < 1){ //if the key store couldn't be loaded, create a key
				logf("isNull? %b", ksp == null);
				if(ksp != null)
					logf("length? %d", ksp.length());
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				kpg.initialize(4096);
				logf("Generating keypair");
				serviceKey = kpg.generateKeyPair();
			}else{ //if the keystore could be loaded, load it
				logf("Loading keypair from file");
				serviceKey = loadKeyPair();
			}
		} catch (Exception e) {
			logf(Level.SEVERE, "Honestly, I'm just sick of this at this point. I've been coding for hours trying to eliminate errors and this is what happens. I don't know what went wrong. Try checking line 44 of the file CryptoSingleton.java to see what's up here.");
			e.printStackTrace();
		}
	}
	
	@Lock(LockType.READ)
	public KeyPair getKeyPair(){
		return serviceKey;
	}
	
	@Lock(LockType.READ)
	public PrivateKey getPrivateKey(){
		return serviceKey.getPrivate();
	}
	
	@Lock(LockType.READ)
	public PublicKey getPublicKey(){
		return serviceKey.getPublic();
	}
	
	private KeyPair loadKeyPair() throws Exception{
		KeyStore ks = readKeyStore(KEYSTORE_PATH);
		PrivateKey privateKey = (PrivateKey)ks.getKey(KEY_ALIAS, KEY_PASS); //get private key
		Certificate cer = ks.getCertificate(KEY_ALIAS); //get public key, part I
		PublicKey publicKey = cer.getPublicKey();
		return new KeyPair(publicKey, privateKey);
	}
	
	private KeyStore readKeyStore(String ksPath) throws Exception{
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(ksPath), KEYSTORE_PASS);
		return ks;
	}
	
	private void logf(String fmt, Object...params){
		logf(Level.INFO, fmt, params);
	}
	private void logf(Level lvl, String fmt, Object...params){
		Logger.getLogger("CryptoSingleton").log(lvl, String.format(fmt, params));
	}
}

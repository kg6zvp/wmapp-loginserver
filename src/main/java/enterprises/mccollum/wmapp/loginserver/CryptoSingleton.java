package enterprises.mccollum.wmapp.loginserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class CryptoSingleton {
	/**
	 * This key will contain the public and private keys used to sign content for auth
	 */
	KeyPair serviceKey;
	
	public static final String KEYSTORE_PATH = "/home/smccollum/wmks.jks";
	private static final char[] KEYSTORE_PASS = "password".toCharArray();
	
	public static final String KEY_ALIAS = "WMAUTH";
	private static final char[] KEY_PASS = "password".toCharArray();
	
	@PostConstruct
	public void init() throws Exception{
		serviceKey = loadKeyPair();
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
}

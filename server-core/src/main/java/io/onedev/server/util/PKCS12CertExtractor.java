package io.onedev.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class PKCS12CertExtractor {
	
	private final File file;
	
	private final String password;
	
	public PKCS12CertExtractor(File file, String password) {
		this.file = file;
		this.password = password;
	}
	
	private String getCertContent(Certificate cert) {
	    StringWriter stringWriter = new StringWriter();
	    try (PemWriter pemWriter = new PemWriter(stringWriter)) {
	    	pemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
	    	pemWriter.flush();
	    } catch (CertificateEncodingException|IOException e) {
	    	throw new RuntimeException(e);
		}
	    return stringWriter.toString().trim();
	}
	
	public Map<String, String> extact() {
		try (InputStream is = new FileInputStream(file)) {
			Map<String, String> certs = new HashMap<>();
			KeyStore keystore = KeyStore.getInstance("pkcs12");
			keystore.load(is, password.toCharArray());
			Enumeration<String> aliases = keystore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				String siteCertContent = getCertContent(keystore.getCertificate(alias));
				String safeAlias = alias.replaceAll("[^a-zA-Z0-9\\.\\_]", "-");
				certs.put("keystore-site-cert-" + safeAlias + ".pem", siteCertContent);
				
			    Certificate chain[] = keystore.getCertificateChain(alias);
			    if (chain != null) {
			    	for (int i=0; i<chain.length; i++) {
			    		String caCertContent = getCertContent(chain[i]);
					    if (!caCertContent.equals(siteCertContent))
					    	certs.put("keystore-ca-cert-" + safeAlias + "-" + i + ".pem", caCertContent);
			    	}
			    }
			}
			return certs;
		} catch (IOException|KeyStoreException|NoSuchAlgorithmException|CertificateException e) {
			throw new RuntimeException(e);
		} 
	}

}

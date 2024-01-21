package io.onedev.server.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

public class Digest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String SHA256 = "SHA-256";

	public static final String SHA512 = "SHA-512";

	public static final String SHA1 = "SHA-1";

	public static final String MD5 = "MD5";
	
	private final String algorithm;
	
	private final String hash;
	
	public Digest(String algorithm, String hash) {
		this.algorithm = algorithm;
		this.hash = hash;
	}
	
	public String getAlgorithm() {
		return algorithm;
	}

	public String getHash() {
		return hash;
	}

	public static Digest sha256Of(InputStream data) {
		return new Digest(SHA256, computeHash(SHA256, data));
	}

	public static Digest sha256Of(byte[] data) {
		return sha256Of(new ByteArrayInputStream(data));
	}

	public static Digest sha512Of(InputStream data) {
		return new Digest(SHA512, computeHash(SHA512, data));
	}

	public static Digest sha512Of(byte[] data) {
		return sha512Of(new ByteArrayInputStream(data));
	}

	public static Digest sha1Of(InputStream data) {
		return new Digest(SHA1, computeHash(SHA1, data));
	}

	public static Digest sha1Of(byte[] data) {
		return sha1Of(new ByteArrayInputStream(data));
	}

	public static Digest md5Of(InputStream data) {
		return new Digest(MD5, computeHash(MD5, data));
	}

	public static Digest md5Of(byte[] data) {
		return md5Of(new ByteArrayInputStream(data));
	}
	
	public boolean matches(InputStream data) {
		return computeHash(algorithm, data).equals(hash);
	}	
	
	public boolean matches(byte[] data) {
		return matches(new ByteArrayInputStream(data));
	}
	
	public static String computeHash(String algorithm, InputStream data) {
		try (var dis = new DigestInputStream(data, MessageDigest.getInstance(algorithm))) {
			var buffer = new byte[BUFFER_SIZE];
			while (dis.read(buffer) != -1);
			return encodeHexString(dis.getMessageDigest().digest());
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
}

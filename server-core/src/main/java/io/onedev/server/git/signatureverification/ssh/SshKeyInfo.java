package io.onedev.server.git.signatureverification.ssh;

import java.io.Serializable;

public class SshKeyInfo implements Serializable {
	
	private final String type;
	
	private final String fingerprint;
	
	public SshKeyInfo(String type, String fingerprint) {
		this.type = type;
		this.fingerprint = fingerprint;
	}

	public String getType() {
		return type;
	}

	public String getFingerprint() {
		return fingerprint;
	}
}

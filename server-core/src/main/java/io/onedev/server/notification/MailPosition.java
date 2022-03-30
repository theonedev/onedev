package io.onedev.server.notification;

import java.io.Serializable;

public class MailPosition implements Serializable {

	private static final long serialVersionUID = 1L;

	private volatile long uidValidity = -1;
	
	private volatile long uid = -1;

	public long getUidValidity() {
		return uidValidity;
	}

	public void setUidValidity(long uidValidity) {
		this.uidValidity = uidValidity;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}
	
}

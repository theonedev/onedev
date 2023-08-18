package io.onedev.server.ee.subscription;

import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

public class Subscription implements Serializable {

	private static final long serialVersionUID = 1L;

	private String licensee;
	
	private int userDays;	
	
	private boolean trial;
	
	public String getLicensee() {
		return licensee;
	}

	public void setLicensee(String licensee) {
		this.licensee = licensee;
	}

	public int getUserDays() {
		return userDays;
	}

	public void setUserDays(int userDays) {
		this.userDays = userDays;
	}

	public boolean isTrial() {
		return trial;
	}

	public void setTrial(boolean trial) {
		this.trial = trial;
	}
	
	@Nullable
	public Date getExpirationDate(int userCount) {
		if (userDays > 0) {
			if (trial) 
				return new DateTime().plusDays(userDays).toDate();
			else 
				return new DateTime().plusDays(userDays / userCount).toDate();
		} else {
			return null;
		}
	}
	
	public boolean isExpired() {
		return userDays == 0;
	}
	
}

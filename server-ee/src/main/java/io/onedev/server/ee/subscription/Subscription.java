package io.onedev.server.ee.subscription;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

public class Subscription implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String licensee;
	
	private String licenseGroup;
	
	private int userDays;
	
	private boolean trial;
	
	public String getLicensee() {
		return licensee;
	}


	public void setLicensee(String licensee) {
		this.licensee = licensee;
	}

	public String getLicenseGroup() {
		return licenseGroup;
	}

	public void setLicenseGroup(String licenseGroup) {
		this.licenseGroup = licenseGroup;
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
		if (getUserDays() > 0) {
			if (trial) 
				return new DateTime().plusDays(getUserDays()).toDate();
			else 
				return new DateTime().plusDays(getUserDays() / userCount).toDate();
		} else {
			return null;
		}
	}
	
	public boolean isExpired() {
		return getUserDays() == 0;
	}

	public int countUsers() {
		var userManager = OneDev.getInstance(UserManager.class);
		var groupManager = OneDev.getInstance(GroupManager.class);

		int userCount;
		if (getLicenseGroup() != null) {
			var licenseGroup = groupManager.find(getLicenseGroup());
			if (licenseGroup == null)
				userCount = userManager.countNonGuests();
			else
				userCount = licenseGroup.getMemberships().size();
		} else {
			userCount = userManager.cloneCache().size();
		}
		if (userCount == 0)
			userCount = 1;
		return userCount;
	}
	
}

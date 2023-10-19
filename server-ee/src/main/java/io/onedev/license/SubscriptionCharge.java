package io.onedev.license;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.Min;

@Editable(order=100, name="Subscription Charge")
public class SubscriptionCharge extends LicensePayload {

    private static final long serialVersionUID = 1L;

	private String licenseGroup;

	private int userMonths;

	@Editable(order=50)
	public String getLicenseGroup() {
		return licenseGroup;
	}

	public void setLicenseGroup(String licenseGroup) {
		this.licenseGroup = licenseGroup;
	}

	@Editable(name="User Months", order=100)
    @Min(1)
	public int getUserMonths() {
		return userMonths;
	}

	public void setUserMonths(int userMonths) {
		this.userMonths = userMonths;
	}

}

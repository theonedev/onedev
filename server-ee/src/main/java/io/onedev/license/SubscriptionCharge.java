package io.onedev.license;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.Min;

@Editable(order=100, name="Subscription Charge")
public class SubscriptionCharge extends LicensePayload {

    private static final long serialVersionUID = 1L;

	private int userMonths;

	@Editable(name="User Months", order=100)
    @Min(1)
	public int getUserMonths() {
		return userMonths;
	}

	public void setUserMonths(int userMonths) {
		this.userMonths = userMonths;
	}

}

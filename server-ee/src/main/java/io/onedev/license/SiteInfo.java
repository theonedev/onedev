package io.onedev.license;

import io.onedev.server.annotation.Editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class SiteInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productVersion;

    private int userCount;

    private int agentCount;

	private String licenseGroup;
	
    private int remainingUserMonths;

    private List<ServerInfo> servers = new ArrayList<>();

    private String subscriptionKeys;

    @Editable(order=50)
    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    @Editable(order=100)
    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

	@Editable(order=200)
    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

	@Editable(order=250)
	public String getLicenseGroup() {
		return licenseGroup;
	}

	public void setLicenseGroup(String licenseGroup) {
		this.licenseGroup = licenseGroup;
	}

	@Editable(order=300)
    public int getRemainingUserMonths() {
        return remainingUserMonths;
    }

    public void setRemainingUserMonths(int remainingUserMonths) {
        this.remainingUserMonths = remainingUserMonths;
    }

    @Editable(order=400, name="Cluster Members")
    public List<ServerInfo> getServers() {
        return servers;
    }

    public void setServers(List<ServerInfo> servers) {
        this.servers = servers;
    }

    @Editable(order=500, name="Subscription Key UUIDs")
    public String getSubscriptionKeys() {
        return subscriptionKeys;
    }

    public void setSubscriptionKeys(String subscriptionKeys) {
        this.subscriptionKeys = subscriptionKeys;
    }

}

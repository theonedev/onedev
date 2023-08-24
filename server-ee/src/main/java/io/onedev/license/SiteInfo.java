package io.onedev.license;

import io.onedev.server.annotation.Editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class SiteInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productVersion;

    private int totalUsers;
	
	private int totalDevelopers;

    private int totalAgents;

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
    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

	@Editable(order=150)
	public int getTotalDevelopers() {
		return totalDevelopers;
	}

	public void setTotalDevelopers(int totalDevelopers) {
		this.totalDevelopers = totalDevelopers;
	}

	@Editable(order=200)
    public int getTotalAgents() {
        return totalAgents;
    }

    public void setTotalAgents(int totalAgents) {
        this.totalAgents = totalAgents;
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

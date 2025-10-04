package io.onedev.server.model;

import static io.onedev.server.model.Agent.PROP_IP_ADDRESS;
import static io.onedev.server.model.Agent.PROP_NAME;
import static io.onedev.server.model.Agent.PROP_OS_ARCH;
import static io.onedev.server.model.Agent.PROP_OS_NAME;
import static io.onedev.server.model.Agent.PROP_OS_VERSION;
import static io.onedev.server.model.Agent.PROP_PAUSED;
import static io.onedev.server.model.AgentLastUsedDate.PROP_VALUE;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.agent.AgentData;
import io.onedev.k8shelper.OsInfo;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.AgentService;
import io.onedev.server.search.entity.SortField;

@Entity
@Table(indexes={
		@Index(columnList="o_token_id"), @Index(columnList=PROP_IP_ADDRESS),
		@Index(columnList=PROP_PAUSED), @Index(columnList=PROP_NAME), 
		@Index(columnList=PROP_OS_NAME), @Index(columnList=PROP_OS_VERSION), 
		@Index(columnList=PROP_OS_ARCH), @Index(columnList="o_lastUsedDate_id")}) 
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Agent extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public static final String NAME_NAME = "Name";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_PAUSED = "paused";
	
	public static final String PROP_BUILDS = "builds";
	
	public static final String NAME_IP_ADDRESS = "Ip Address";
	
	public static final String PROP_IP_ADDRESS = "ipAddress";
	
	public static final String NAME_OS_NAME = "Os";
	
	public static final String PROP_OS_NAME = "osName";
	
	public static final String NAME_OS_VERSION = "Os Version";
	
	public static final String PROP_OS_VERSION = "osVersion";
	
	public static final String NAME_OS_ARCH = "Os Arch";
	
	public static final String PROP_OS_ARCH = "osArch";

	public static final String NAME_LAST_USED_DATE = "Last Used Date";

	public static final String PROP_LAST_USED_DATE = "lastUsedDate";
	
	public static final String PROP_TOKEN = "token";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_NAME, NAME_IP_ADDRESS, NAME_OS_NAME, NAME_OS_VERSION, NAME_OS_ARCH);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_NAME, NAME_IP_ADDRESS, NAME_OS_NAME, NAME_OS_VERSION, NAME_OS_ARCH);

	public static final Map<String, SortField<Agent>> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_NAME, new SortField<>(PROP_NAME));
		SORT_FIELDS.put(NAME_IP_ADDRESS, new SortField<>(PROP_IP_ADDRESS));
		SORT_FIELDS.put(NAME_OS_NAME, new SortField<>(PROP_OS_NAME));
		SORT_FIELDS.put(NAME_OS_VERSION, new SortField<>(PROP_OS_VERSION));
		SORT_FIELDS.put(NAME_OS_ARCH, new SortField<>(PROP_OS_ARCH));
		SORT_FIELDS.put(NAME_LAST_USED_DATE, new SortField<>(PROP_LAST_USED_DATE + "." + PROP_VALUE, DESCENDING));
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable = false, unique = true)
	private AgentToken token;
	
	@OneToMany(mappedBy="agent", cascade=CascadeType.REMOVE)
	private Collection<AgentAttribute> attributes = new ArrayList<>();
	
	@OneToMany(mappedBy="agent")
	private Collection<Build> builds = new ArrayList<>();
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String ipAddress;
	
	@Column(nullable=false)
	private String osName;
	
	@Column(nullable=false)
	private String osVersion;
	
	@Column(nullable=false)
	private String osArch;
	
	private int cpuCount;
	
	private boolean paused;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(unique=true, nullable=false)
	private AgentLastUsedDate lastUsedDate;
	
	private transient Boolean online;

	public AgentToken getToken() {
		return token;
	}

	public void setToken(AgentToken token) {
		this.token = token;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getOsArch() {
		return osArch;
	}

	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public int getCpuCount() {
		return cpuCount;
	}

	public void setCpuCount(int cpuCount) {
		this.cpuCount = cpuCount;
	}

	public Collection<AgentAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Collection<AgentAttribute> attributes) {
		this.attributes = attributes;
	}

	public AgentLastUsedDate getLastUsedDate() {
		return lastUsedDate;
	}

	public void setLastUsedDate(AgentLastUsedDate lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}

	public Map<String, String> getAttributeMap() {
		Map<String, String> attributeMap = new HashMap<>();
		for (AgentAttribute attribute: getAttributes())
			attributeMap.put(attribute.getName(), attribute.getValue());
		return attributeMap;
	}
	
	@JsonProperty
	public boolean isOnline() {
		if (online == null) {
			var agentService = OneDev.getInstance(AgentService.class);
			var clusterService = OneDev.getInstance(ClusterService.class);
			var server = agentService.getAgentServer(getId());
			online = server != null && clusterService.getServer(server, false) != null
					&& clusterService.getOnlineServers().contains(server);
		}
		return online;
	}
	
	public AgentData getAgentData() {
		return new AgentData(getToken().getValue(), new OsInfo(osName, osVersion, osArch),  
				name, ipAddress, cpuCount, getAttributeMap());
	}
	
}

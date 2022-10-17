package io.onedev.server.cluster;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class ProjectServer implements Serializable {

	private static final long serialVersionUID = 1L;

	private final UUID primary;
	
	private final List<UUID> backups;
	
	public ProjectServer(UUID primary, List<UUID> backups) {
		this.primary = primary;
		this.backups = backups;
	}

	public UUID getPrimary() {
		return primary;
	}

	public List<UUID> getBackups() {
		return backups;
	}
	
}

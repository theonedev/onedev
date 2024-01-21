package io.onedev.server.replica;

import io.onedev.commons.utils.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.io.Serializable;

import static io.onedev.server.replica.ProjectReplica.Type.PRIMARY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;

public class ProjectReplica implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final String TYPE_FILE = "type";

	public static enum Type {PRIMARY, BACKUP, REDUNDANT};

	private Type type;

	private long version;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public void loadType(File projectDir) {
		var typeFile = new File(projectDir, TYPE_FILE);
		if (typeFile.exists()) {
			try {
				type = Type.valueOf(readFileToString(typeFile, UTF_8).toUpperCase().trim());
			} catch (Exception e) {
				throw new RuntimeException("Error reading replica type from file: " + typeFile, e);
			}
		} else {
			type = PRIMARY;
		}
	}
	
	public void saveType(File projectDir) {
		FileUtils.writeFile(new File(projectDir, TYPE_FILE), type.name());
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ProjectReplica))
			return false;
		if (this == other)
			return true;
		ProjectReplica otherReplica = (ProjectReplica) other;
		return new EqualsBuilder()
				.append(type, otherReplica.type)
				.append(version, otherReplica.version)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(type).append(version).toHashCode();
	}
	
}

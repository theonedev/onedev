package io.onedev.server.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.util.IssueUtils;

@Entity
@Table(
		indexes={@Index(columnList="g_configuration_id"), @Index(columnList="commit"), @Index(columnList="name")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_configuration_id", "commit"})}
)
public class Build extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String FQN_SEPARATOR = ":";

	public static final String STATUS = "status";
	
	public enum Status {
		SUCCESS("Successful"), 
		FAILURE("Failed"), 
		ERROR("In error"), 
		RUNNING("Running");
		
		private final String description;
		
		Status(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

	};
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Configuration configuration;
	
	private String ref;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String commit;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false)
	private String url;
	
	private transient Collection<Long> fixedIssueNumbers;
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getFQN() {
		return getConfiguration().getName() + FQN_SEPARATOR + getName();
	}
	
	public Collection<Long> getFixedIssueNumbers(@Nullable Build prevBuild) {
		if (fixedIssueNumbers == null) {
			fixedIssueNumbers = new HashSet<>();

			ObjectId prevCommit;
			if (prevBuild == null)
				prevBuild = OneDev.getInstance(BuildManager.class).findPrevious(this);
			if (prevBuild != null) 
				prevCommit = ObjectId.fromString(prevBuild.getCommit());
			else if (getConfiguration().getBaseCommit() != null)
				prevCommit = ObjectId.fromString(getConfiguration().getBaseCommit());
			else
				prevCommit = null;
			
			if (prevCommit != null) {
				Project project = getConfiguration().getProject();
				
				Repository repository = project.getRepository();
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getCommit())));
					revWalk.markUninteresting(revWalk.parseCommit(prevCommit));
	
					RevCommit commit;
					while ((commit = revWalk.next()) != null) 
						fixedIssueNumbers.addAll(IssueUtils.parseFixedIssues(commit.getFullMessage()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return fixedIssueNumbers;
	}
	
}

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

import com.google.common.base.Optional;

import io.onedev.server.OneDev;
import io.onedev.server.cache.BuildInfoManager;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.facade.BuildFacade;

@Entity
@Table(
		indexes={@Index(columnList="o_configuration_id"), @Index(columnList="commitHash"), @Index(columnList="version")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_configuration_id", "commitHash"})}
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
	
	@Column(nullable=false)
	private String version;
	
	@Column(nullable=false)
	private String commitHash;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false)
	private String url;
	
	private transient Optional<Collection<Long>> fixedIssueNumbers;
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
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
		return getConfiguration().getName() + FQN_SEPARATOR + getVersion();
	}
	
	/**
	 * Get fixed issue numbers
	 * 
	 * @return
	 * 			<tt>null</tt> if fixed issue numbers information is not available yet 
	 */
	@Nullable
	public Collection<Long> getFixedIssueNumbers() {
		if (fixedIssueNumbers == null) {
			Project project = getConfiguration().getProject();
			Collection<ObjectId> prevCommits = OneDev.getInstance(BuildInfoManager.class).getPrevCommits(project, getId());
			if (prevCommits != null) {
				fixedIssueNumbers = Optional.of(new HashSet<>());
				if (!prevCommits.isEmpty()) {
					Repository repository = project.getRepository();
					try (RevWalk revWalk = new RevWalk(repository)) {
						revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getCommitHash())));
						for (ObjectId prevCommit: prevCommits)
							revWalk.markUninteresting(revWalk.parseCommit(prevCommit));

						RevCommit commit;
						while ((commit = revWalk.next()) != null) 
							fixedIssueNumbers.get().addAll(IssueUtils.parseFixedIssues(project, commit.getFullMessage()));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				fixedIssueNumbers = Optional.absent();
			}
		}
		return fixedIssueNumbers.orNull();
	}
	
	public BuildFacade getFacade() {
		return new BuildFacade(getId(), getConfiguration().getId(), getCommitHash());
	}
}

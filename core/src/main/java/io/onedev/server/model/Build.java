package io.onedev.server.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.util.IssueUtils;

@Entity
@Table(
		indexes={@Index(columnList="g_configuration_id"), @Index(columnList="commit"), @Index(columnList="uuid")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_configuration_id", "commit"})}
)
public class Build extends AbstractEntity {

	private static final long serialVersionUID = 1L;

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
	
	private long number;
	
	// used for number search in markdown editor
	@Column(nullable=false)
	private String numberStr;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Configuration configuration;
	
	@Column(nullable=false)
	private String commit;
	
	@Column(nullable=false)
	private String commitShortMessage;
	
	@Column(nullable=false)
	private String noSpaceCommitShortMessage;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false)
	private String description;
	
	@Column(nullable=false)
	private String url;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();

	private transient Collection<Issue> fixedIssues;
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
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

	public String getCommitShortMessage() {
		return commitShortMessage;
	}

	public void setCommitShortMessage(String commitShortMessage) {
		this.commitShortMessage = commitShortMessage;
		noSpaceCommitShortMessage = StringUtils.deleteWhitespace(commitShortMessage);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
		numberStr = String.valueOf(number);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public Collection<Issue> getFixedIssues() {
		if (fixedIssues == null) {
			fixedIssues = new HashSet<>();
			
			ObjectId prevCommit;
			Build prevBuild = OneDev.getInstance(BuildManager.class).findPrevious(this);
			if (prevBuild != null) 
				prevCommit = ObjectId.fromString(prevBuild.getCommit());
			else if (getConfiguration().getBaseCommit() != null)
				prevCommit = ObjectId.fromString(getConfiguration().getBaseCommit());
			else
				prevCommit = null;
			
			if (prevCommit != null) {
				Project project = getConfiguration().getProject();
				
				IssueManager issueManager = OneDev.getInstance(IssueManager.class);
				Repository repository = project.getRepository();
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getCommit())));
					revWalk.markUninteresting(revWalk.parseCommit(prevCommit));

					RevCommit commit;
					while ((commit = revWalk.next()) != null) {
						for (Long issueNumber: IssueUtils.parseFixedIssues(commit.getFullMessage())) {
							Issue issue = issueManager.find(project, issueNumber);
							if (issue != null)
								fixedIssues.add(issue);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return fixedIssues;
	}
	
}

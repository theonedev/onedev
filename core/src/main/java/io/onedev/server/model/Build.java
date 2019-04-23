package io.onedev.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Optional;

import io.onedev.server.OneDev;
import io.onedev.server.cache.BuildInfoManager;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.jackson.DefaultView;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_user_id"), @Index(columnList="commitHash"), 
				@Index(columnList="number"), @Index(columnList="jobName"), @Index(columnList="numberStr"), 
				@Index(columnList="status"), @Index(columnList="submitDate"), @Index(columnList="pendingDate"),
				@Index(columnList="runningDate"), @Index(columnList="finishDate")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "number"})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Build extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String STATUS = "status";
	
	private static final int MAX_STATUS_MESSAGE = 1024;
	
	public enum Status {
		WAITING, QUEUEING, RUNNING, SUCCESSFUL, FAILED, IN_ERROR, CANCELLED
	};
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private String jobName;
	
	private long number;
	
	// used for number search in markdown editor
	@Column(nullable=false)
	@JsonView(DefaultView.class)
	private String numberStr;
	
	@Column(nullable=false)
	private String commitHash;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date submitDate;
	
	private Date pendingDate;
	
	private Date runningDate;

	private Date finishDate;
	
	@Column(length=MAX_STATUS_MESSAGE)
	private String statusMessage;

	@OneToMany(mappedBy="build", cascade=CascadeType.REMOVE)
	private Collection<BuildParam> params = new ArrayList<>();
	
	@OneToMany(mappedBy="dependent", cascade=CascadeType.REMOVE)
	private Collection<BuildDependence> dependencies = new ArrayList<>();
	
	@OneToMany(mappedBy="dependency", cascade=CascadeType.REMOVE)
	private Collection<BuildDependence> dependents= new ArrayList<>();
	
	private transient Map<String, String> paramMap;
	
	private transient Optional<Collection<Long>> fixedIssueNumbers;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Nullable
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
		numberStr = String.valueOf(number);
	}

	public String getNumberStr() {
		return numberStr;
	}

	public void setNumberStr(String numberStr) {
		this.numberStr = numberStr;
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
		setStatus(status, null);
	}
	
	public void setStatus(Status status, @Nullable String statusMessage) {
		this.status = status;
		setStatusMessage(statusMessage);
	}
	
	public boolean isFinished() {
		return status == Status.IN_ERROR || status == Status.FAILED 
				|| status == Status.CANCELLED || status == Status.SUCCESSFUL;
	}
	
	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public Date getPendingDate() {
		return pendingDate;
	}

	public void setPendingDate(Date pendingDate) {
		this.pendingDate = pendingDate;
	}

	public Date getRunningDate() {
		return runningDate;
	}

	public void setRunningDate(Date runningDate) {
		this.runningDate = runningDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public Collection<BuildParam> getParams() {
		return params;
	}

	public void setParams(Collection<BuildParam> params) {
		this.params = params;
	}
	
	public Collection<BuildDependence> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Collection<BuildDependence> dependencies) {
		this.dependencies = dependencies;
	}

	public Collection<BuildDependence> getDependents() {
		return dependents;
	}

	public void setDependents(Collection<BuildDependence> dependents) {
		this.dependents = dependents;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		if (statusMessage != null)
			statusMessage = StringUtils.abbreviate(statusMessage, MAX_STATUS_MESSAGE);
		this.statusMessage = statusMessage;
	}

	public Map<String, String> getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap<>();
			for (BuildParam param: getParams())
				paramMap.put(param.getName(), param.getValue());
		}
		return paramMap;
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
		return new BuildFacade(getId(), getProject().getId(), getCommitHash());
	}
	
	public static String getLogWebSocketObservable(Long buildId) {
		return "build-log:" + buildId;
	}
	
}

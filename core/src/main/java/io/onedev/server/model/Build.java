package io.onedev.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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

import com.google.common.base.Optional;

import io.onedev.server.OneDev;
import io.onedev.server.cache.BuildInfoManager;
import io.onedev.server.util.Input;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.facade.BuildFacade;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="o_submitter_id"), @Index(columnList="o_canceller_id"),
				@Index(columnList="submitterName"), @Index(columnList="cancellerName"), @Index(columnList="commitHash"), 
				@Index(columnList="number"), @Index(columnList="jobName"), @Index(columnList="status"), 
				@Index(columnList="submitDate"), @Index(columnList="queueingDate"), @Index(columnList="runningDate"), 
				@Index(columnList="finishDate"), @Index(columnList="version")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "number"})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Build extends AbstractEntity implements Referenceable {

	private static final long serialVersionUID = 1L;
	
	public static final String STATUS = "status";
	
	private static final int MAX_STATUS_MESSAGE = 1024;
	
	public enum Status {
		IN_ERROR, FAILED, CANCELLED, TIMED_OUT, WAITING, QUEUEING, RUNNING, SUCCESSFUL;
		
		public String getDisplayName() {
			return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
		}
		
	};
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User submitter;
	
	private String submitterName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User canceller;
	
	private String cancellerName;
	
	@Column(nullable=false)
	private String jobName;
	
	private String version;
	
	private long number;
	
	@Column(nullable=false)
	private String commitHash;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date submitDate;
	
	private Date queueingDate;
	
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
	
	private transient Map<String, List<String>> paramMap;
	
	private transient Optional<Collection<Long>> fixedIssueNumbers;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Nullable
	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	@Nullable
	public String getSubmitterName() {
		return submitterName;
	}

	public void setSubmitterName(String submitterName) {
		this.submitterName = submitterName;
	}

	@Nullable
	public String getCancellerName() {
		return cancellerName;
	}

	public void setCancellerName(String cancellerName) {
		this.cancellerName = cancellerName;
	}

	@Nullable
	public User getCanceller() {
		return canceller;
	}

	public void setCanceller(User canceller) {
		this.canceller = canceller;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
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
				|| status == Status.CANCELLED || status == Status.SUCCESSFUL
				|| status == Status.TIMED_OUT;
	}
	
	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public Date getQueueingDate() {
		return queueingDate;
	}

	public void setQueueingDate(Date queueingDate) {
		this.queueingDate = queueingDate;
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

	public Map<String, List<String>> getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap<>();
			for (BuildParam param: getParams()) {
				List<String> values = paramMap.get(param.getName());
				if (values == null) {
					values = new ArrayList<>();
					paramMap.put(param.getName(), values);
				}
				values.add(param.getValue());
			}
			for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) { 
				List<String> values = entry.getValue();
				Collections.sort(values);
				entry.setValue(values);
			}
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
			} else {
				fixedIssueNumbers = Optional.absent();
			}
		}
		return fixedIssueNumbers.orNull();
	}
	
	public Map<String, Input> getParamInputs() {
		Map<String, Input> inputs = new LinkedHashMap<>();
		List<BuildParam> params = new ArrayList<>(getParams());
		Collections.sort(params, new Comparator<BuildParam>() {

			@Override
			public int compare(BuildParam o1, BuildParam o2) {
				if (o1.getName().equals(o2.getName())) {
					if (o1.getValue() != null) {
						if (o2.getValue() != null) 
							return o1.getValue().compareTo(o2.getValue());
						else
							return -1;
					} else {
						if (o2.getValue() != null) 
							return 1;
						else
							return 0;
					}
				} else {
					return o1.getName().compareTo(o2.getName());
				}
			}
			
		});
		for (BuildParam param: getParams()) {
			Input input = inputs.get(param.getName());
			if (input == null) {
				input = new Input(param.getName(), param.getType(), new ArrayList<>());
				inputs.put(param.getName(), input);
			}
			if (param.getValue() != null)
				input.getValues().add(param.getValue());
		}
		return inputs;
	}
	
	public BuildFacade getFacade() {
		return new BuildFacade(getId(), getProject().getId(), getCommitHash());
	}
	
	public static String getLogWebSocketObservable(Long buildId) {
		return "build-log:" + buildId;
	}
	
	public static String getWebSocketObservable(Long buildId) {
		return Build.class.getName() + ":" + buildId;
	}
	
}

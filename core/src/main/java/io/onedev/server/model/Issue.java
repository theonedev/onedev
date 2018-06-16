package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.Referenceable;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={
				@Index(columnList="g_project_id"), @Index(columnList="state"), 
				@Index(columnList="title"), @Index(columnList="noSpaceTitle"),  
				@Index(columnList="number"), @Index(columnList="numberStr"), 
				@Index(columnList="submitDate"), @Index(columnList="g_submitter_id"),
				@Index(columnList="numOfVotes"), @Index(columnList="numOfComments"),
				@Index(columnList="g_milestone_id"), @Index(columnList="LAST_ACT_DATE")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Issue extends AbstractEntity implements Referenceable {

	private static final long serialVersionUID = 1L;
	
	public static final Map<String, String> BUILTIN_FIELDS = new LinkedHashMap<>();
	
	public static final String NUMBER = "Number";
	
	public static final String STATE = "State";
	
	public static final String TITLE = "Title";
	
	public static final String DESCRIPTION = "Description";
	
	public static final String SUBMITTER = "Submitter";
	
	public static final String SUBMIT_DATE = "Submit Date";
	
	public static final String VOTES = "Votes";
	
	public static final String COMMENTS = "Comments";
	
	public static final String UPDATE_DATE = "Update Date";
	
	public static final String MILESTONE = "Milestone";
	
	static {
		BUILTIN_FIELDS.put(NUMBER, "number");
		BUILTIN_FIELDS.put(STATE, "state");
		BUILTIN_FIELDS.put(TITLE, "title");
		BUILTIN_FIELDS.put(DESCRIPTION, "description");
		BUILTIN_FIELDS.put(SUBMITTER, "submitter");
		BUILTIN_FIELDS.put(SUBMIT_DATE, "submitDate");
		BUILTIN_FIELDS.put(UPDATE_DATE, "lastActivity.date");
		BUILTIN_FIELDS.put(VOTES, "numOfVotes");
		BUILTIN_FIELDS.put(COMMENTS, "numOfComments");
		BUILTIN_FIELDS.put(MILESTONE, "milestone");
	}
	
	@Version
	private long version;
	
	@Column(nullable=false)
	private String state;
	
	@Column(nullable=false)
	private String title;
	
	@Lob
	@Column(length=65535)
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Milestone milestone;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User submitter;
	
	private String submitterName;
	
	@Column(nullable=false)
	private long submitDate;
	
	private int numOfVotes;
	
	private int numOfComments;
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();

	private long number;
	
	// used for number search in markdown editor
	@Column(nullable=false)
	private String numberStr;
	
	// used for title search in markdown editor
	@Column(nullable=false)
	private String noSpaceTitle;
	
	@Embedded
	private LastActivity lastActivity;
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueFieldUnary> fieldUnaries = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueComment> comments = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueChange> changes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueVote> votes = new ArrayList<>();
	
	@OneToMany(mappedBy="issue", cascade=CascadeType.REMOVE)
	private Collection<IssueWatch> watches = new ArrayList<>();
	
	public long getVersion() {
		return version;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		noSpaceTitle = StringUtils.deleteWhitespace(title);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	@Override
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

	public Date getSubmitDate() {
		return new Date(submitDate);
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate.getTime();
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public Collection<IssueComment> getComments() {
		return comments;
	}

	public void setComments(Collection<IssueComment> comments) {
		this.comments = comments;
	}

	public Collection<IssueChange> getChanges() {
		return changes;
	}

	public void setChanges(Collection<IssueChange> changes) {
		this.changes = changes;
	}

	public Collection<IssueVote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<IssueVote> votes) {
		this.votes = votes;
	}

	public Collection<IssueWatch> getWatches() {
		return watches;
	}

	public void setWatches(Collection<IssueWatch> watches) {
		this.watches = watches;
	}

	public int getNumOfVotes() {
		return numOfVotes;
	}

	public void setNumOfVotes(int numOfVotes) {
		this.numOfVotes = numOfVotes;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(int numOfComments) {
		this.numOfComments = numOfComments;
	}

	public Collection<IssueFieldUnary> getFieldUnaries() {
		return fieldUnaries;
	}

	public void setFieldUnaries(Collection<IssueFieldUnary> fieldUnaries) {
		this.fieldUnaries = fieldUnaries;
	}
	
	public LastActivity getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(LastActivity lastActivity) {
		this.lastActivity = lastActivity;
	}

	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitManager.class).getIssueVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public Map<String, IssueField> getEffectiveFields() {
		Map<String, IssueField> effectiveFields = new LinkedHashMap<>();

		Collection<String> fieldNames = getProject().getIssueWorkflow().getApplicableFields(state);
		
		Map<String, List<IssueFieldUnary>> unaryMap = new HashMap<>(); 
		for (IssueFieldUnary unary: getFieldUnaries()) {
			if (fieldNames.contains(unary.getName())) {
				List<IssueFieldUnary> fieldsOfName = unaryMap.get(unary.getName());
				if (fieldsOfName == null) {
					fieldsOfName = new ArrayList<>();
					unaryMap.put(unary.getName(), fieldsOfName);
				}
				fieldsOfName.add(unary);
			}
		}
		
		for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFieldSpecs()) {
			String fieldName = fieldSpec.getName();
			List<IssueFieldUnary> unaries = unaryMap.get(fieldName);
			if (unaries != null) {
				String type = unaries.iterator().next().getType();
				List<String> values = new ArrayList<>();
				for (IssueFieldUnary unary: unaries) {
					if (unary.getValue() != null)
						values.add(unary.getValue());
				}
				Collections.sort(values);
				if (!fieldSpec.isAllowMultiple() && values.size() > 1) 
					values = Lists.newArrayList(values.iterator().next());
				effectiveFields.put(fieldName, new IssueField(fieldName, type, values));
			}
		}
		return effectiveFields;
	}
	
	public static String getWebSocketObservable(Long issueId) {
		return Issue.class.getName() + ":" + issueId;
	}
	
	@Nullable
	public IssueWatch getWatch(User user) {
		for (IssueWatch watch: getWatches()) {
			if (user.equals(watch.getUser())) 
				return watch;
		}
		return null;
	}

	@Nullable
	public String getMilestoneName() {
		return getMilestone()!=null? getMilestone().getName():null;
	}
	
}

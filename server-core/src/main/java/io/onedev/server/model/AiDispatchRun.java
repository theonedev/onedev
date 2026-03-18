package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.server.ai.dispatch.AiDispatchAgent;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="state")})
public class AiDispatchRun extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_REQUEST = "request";

	public static final String PROP_COMMENT = "comment";

	public static final String PROP_AGENT = "agent";

	public static final String PROP_STATE = "state";

	private static final int MAX_FLAGS_LEN = 255;

	private static final int MAX_PROMPT_LEN = 65535;

	private static final int MAX_WORKTREE_PATH_LEN = 512;

	private static final int MAX_LOG_LEN = 1048576;

	private static final int MAX_COMMIT_SHAS_LEN = 4096;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	private PullRequestComment comment;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User triggeredBy;

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private AiDispatchAgent agent;

	@Column(length=MAX_FLAGS_LEN)
	private String flags;

	@Column(nullable=false, length=MAX_PROMPT_LEN)
	private String prompt;

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private State state = State.QUEUED;

	@Column(length=MAX_WORKTREE_PATH_LEN)
	private String worktreePath;

	@Column(length=MAX_LOG_LEN)
	private String log;

	@Column(length=MAX_COMMIT_SHAS_LEN)
	private String commitShas;

	@Column(nullable=false)
	private Date createdAt = new Date();

	private Date startedAt;

	private Date completedAt;

	private Integer exitCode;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	@Nullable
	public PullRequestComment getComment() {
		return comment;
	}

	public void setComment(PullRequestComment comment) {
		this.comment = comment;
	}

	public User getTriggeredBy() {
		return triggeredBy;
	}

	public void setTriggeredBy(User triggeredBy) {
		this.triggeredBy = triggeredBy;
	}

	public AiDispatchAgent getAgent() {
		return agent;
	}

	public void setAgent(AiDispatchAgent agent) {
		this.agent = agent;
	}

	@Nullable
	public String getFlags() {
		return flags;
	}

	public void setFlags(@Nullable String flags) {
		this.flags = StringUtils.abbreviate(flags, MAX_FLAGS_LEN);
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = StringUtils.abbreviate(prompt, MAX_PROMPT_LEN);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Nullable
	public String getWorktreePath() {
		return worktreePath;
	}

	public void setWorktreePath(@Nullable String worktreePath) {
		this.worktreePath = StringUtils.abbreviate(worktreePath, MAX_WORKTREE_PATH_LEN);
	}

	@Nullable
	public String getLog() {
		return log;
	}

	public void setLog(@Nullable String log) {
		if (log != null && log.length() > MAX_LOG_LEN)
			log = log.substring(log.length()-MAX_LOG_LEN);
		this.log = log;
	}

	public void appendLog(String message) {
		var current = log;
		if (current == null)
			current = "";
		current += message;
		setLog(current);
	}

	public List<String> getCommitShaList() {
		if (commitShas == null || commitShas.isBlank())
			return List.of();
		return List.of(commitShas.split(","));
	}

	public void setCommitShaList(Collection<String> commitShas) {
		var joined = String.join(",", commitShas);
		this.commitShas = StringUtils.abbreviate(joined, MAX_COMMIT_SHAS_LEN);
	}

	@Nullable
	public String getCommitShas() {
		return commitShas;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Nullable
	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(@Nullable Date startedAt) {
		this.startedAt = startedAt;
	}

	@Nullable
	public Date getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(@Nullable Date completedAt) {
		this.completedAt = completedAt;
	}

	@Nullable
	public Integer getExitCode() {
		return exitCode;
	}

	public void setExitCode(@Nullable Integer exitCode) {
		this.exitCode = exitCode;
	}

	public boolean hasFlag(String flag) {
		for (var each: getFlagList()) {
			if (each.equals(flag))
				return true;
		}
		return false;
	}

	public List<String> getFlagList() {
		if (flags == null || flags.isBlank())
			return List.of();
		var parsed = new ArrayList<String>();
		for (var each: flags.split(" ")) {
			if (!each.isBlank())
				parsed.add(each);
		}
		return parsed;
	}

	public boolean isReviewOnly() {
		return hasFlag("--no-commit");
	}

	public boolean isExtendedThinking() {
		return hasFlag("--think");
	}

	@Nullable
	public String getModelName() {
		var flags = getFlagList();
		for (int i = 0; i < flags.size(); i++) {
			var each = flags.get(i);
			if (each.startsWith("--model="))
				return StringUtils.substringAfter(each, "=");
			if (each.equals("--model") && i + 1 < flags.size())
				return flags.get(i + 1);
		}
		return null;
	}

	public boolean isActive() {
		return state == State.QUEUED || state == State.RUNNING;
	}

	public String getAnchor() {
		return "ai-run-" + getId();
	}

	public static String getChangeObservable(Long requestId) {
		return AiDispatchRun.class.getName() + ":" + requestId;
	}

	public static String getSessionsChangeObservable() {
		return AiDispatchRun.class.getName() + ":all";
	}

	public enum State {
		QUEUED,
		RUNNING,
		COMPLETED,
		FAILED,
		CANCELLED
	}

}

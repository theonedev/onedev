package io.onedev.server.model;

import static io.onedev.server.model.CodeComment.PROP_CREATE_DATE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.Mark;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageSupport;
import io.onedev.server.util.CollectionUtils;

@Entity
@Table(indexes={
		@Index(columnList="o_project_id"), @Index(columnList="o_user_id"),
		@Index(columnList="o_pullRequest_id"),
		@Index(columnList=Mark.PROP_COMMIT_HASH), @Index(columnList=Mark.PROP_PATH), 
		@Index(columnList=PROP_CREATE_DATE), @Index(columnList=LastUpdate.COLUMN_DATE)})
public class CodeComment extends AbstractEntity implements AttachmentStorageSupport {
	
	private static final long serialVersionUID = 1L;
	
	public static final int MAX_CONTENT_LEN = 14000;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_COMPARE_CONTEXT = "compareContext";
	
	public static final String NAME_CONTENT = "Content";
	
	public static final String PROP_CONTENT = "content";
	
	public static final String NAME_REPLY = "Reply";
	
	public static final String NAME_PATH = "Path";
	
	public static final String PROP_MARK = "mark";
	
	public static final String NAME_REPLY_COUNT = "Reply Count";
	
	public static final String PROP_REPLY_COUNT = "replyCount";
	
	public static final String NAME_CREATE_DATE = "Create Date";
	
	public static final String PROP_CREATE_DATE = "createDate";
	
	public static final String NAME_UPDATE_DATE = "Update Date";
	
	public static final String PROP_LAST_UPDATE = "lastUpdate";
	
	public static final String NAME_RESOLVED = "Status";
	
	public static final String PROP_RESOLVED = "resolved";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_REPLIES = "replies";
	
	public static final String PROP_CHANGES = "changes";
	
	public static final String PROP_UUID = "uuid";

	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_CONTENT, NAME_REPLY, NAME_PATH, NAME_CREATE_DATE, NAME_UPDATE_DATE, NAME_REPLY_COUNT);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_RESOLVED, PROP_RESOLVED, 
			NAME_CREATE_DATE, PROP_CREATE_DATE,
			NAME_UPDATE_DATE, PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE,
			NAME_REPLY_COUNT, PROP_REPLY_COUNT);
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false, length=MAX_CONTENT_LEN)
	private String content;
	
	@Column(nullable=false)
	private Date createDate = new Date();

	@Embedded
	private LastUpdate lastUpdate;
	
	private int replyCount;

	@Embedded
	private Mark mark;
	
	@Embedded
	private CompareContext compareContext;
	
	private boolean resolved;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentReply> replies = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentStatusChange> changes = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<PendingSuggestionApply> pendingSuggestionApplies = new ArrayList<>();
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private transient Collection<User> participants;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public User getUser() {
		return user;
	}

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = StringUtils.abbreviate(content, MAX_CONTENT_LEN);
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}

	public Mark getMark() {
		return mark;
	}

	public void setMark(Mark mark) {
		this.mark = mark;
	}

	public Collection<CodeCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<CodeCommentReply> replies) {
		this.replies = replies;
	}

	public Collection<CodeCommentStatusChange> getChanges() {
		return changes;
	}

	public void setChanges(Collection<CodeCommentStatusChange> changes) {
		this.changes = changes;
	}

	public Collection<PendingSuggestionApply> getPendingSuggestionApplies() {
		return pendingSuggestionApplies;
	}

	public void setPendingSuggestionApplies(Collection<PendingSuggestionApply> pendingSuggestionApplies) {
		this.pendingSuggestionApplies = pendingSuggestionApplies;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}
	
	public CompareContext getCompareContext() {
		return compareContext;
	}

	public void setCompareContext(CompareContext compareContext) {
		this.compareContext = compareContext;
	}

	public LastUpdate getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LastUpdate lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(UserInfoManager.class).getCodeCommentVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public boolean isValid() {
		try {
			return project.getRepository().getObjectDatabase().has(ObjectId.fromString(mark.getCommitHash()))
					&& (compareContext.getOldCommitHash().equals(ObjectId.zeroId().name()) 
							|| project.getRepository().getObjectDatabase().has(ObjectId.fromString(compareContext.getOldCommitHash())))
					&& (compareContext.getNewCommitHash().equals(ObjectId.zeroId().name())
							|| project.getRepository().getObjectDatabase().has(ObjectId.fromString(compareContext.getNewCommitHash())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getWebSocketObservable(Long commentId) {
		return CodeComment.class.getName() + ":" + commentId;
	}
	
	@Override
	public Project getAttachmentProject() {
		return project;
	}
	
	@Override
	public String getAttachmentGroup() {
		return uuid;
	}

	public Collection<User> getParticipants() {
		if (participants == null) {
			participants = new LinkedHashSet<>();
			if (getUser() != null)
				participants.add(getUser());
			for (CodeCommentReply reply: getReplies()) {
				if (reply.getUser() != null)
					participants.add(reply.getUser());
			}
		}
		return participants;
	}
	
}

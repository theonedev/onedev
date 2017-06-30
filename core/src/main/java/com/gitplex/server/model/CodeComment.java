package com.gitplex.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.hibernate.annotations.DynamicUpdate;

import com.gitplex.server.GitPlex;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentEvent;
import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.manager.VisitManager;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.model.support.MarkPos;
import com.gitplex.server.model.support.LastEvent;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.diff.DiffUtils;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.util.editable.EditableUtils;
import com.google.common.base.Preconditions;

/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@Entity
@Table(indexes={
		@Index(columnList="g_request_id"), @Index(columnList="g_user_id"),
		@Index(columnList="commit"), @Index(columnList="path")})
@DynamicUpdate 
public class CodeComment extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Version
	private long version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private User user;
	
	private String userName;

	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();

	@Embedded
	private LastEvent lastEvent;

	@Embedded
	private MarkPos markPos;
	
	private boolean resolved;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentReply> replies = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentStatusChange> statusChanges= new ArrayList<>();
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private transient List<CodeCommentActivity> activities;
	
	private transient Boolean codeChanged;
	
	public PullRequest getRequest() {
		return request;
	}
	
	public void setRequest(PullRequest request) {
		this.request = request;
	}
	
	@Nullable
	public User getUser() {
		return user;
	}

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getVersion() {
		return version;
	}

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public MarkPos getCommentPos() {
		return markPos;
	}

	public void setCommentPos(MarkPos markPos) {
		this.markPos = markPos;
	}

	public Collection<CodeCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<CodeCommentReply> replies) {
		this.replies = replies;
	}

	public Collection<CodeCommentStatusChange> getStatusChanges() {
		return statusChanges;
	}

	public void setStatusChanges(Collection<CodeCommentStatusChange> statusChanges) {
		this.statusChanges = statusChanges;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public LastEvent getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(LastEvent lastEvent) {
		this.lastEvent = lastEvent;
	}
	
	public void setLastEvent(PullRequestCodeCommentEvent event) {
		LastEvent lastEvent = new LastEvent();
		lastEvent.setDate(event.getDate());
		lastEvent.setType(EditableUtils.getName(event.getClass()));
		lastEvent.setUser(event.getUser());
		setLastEvent(lastEvent);
	}

	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = GitPlex.getInstance(VisitManager.class).getVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}

	public List<CodeCommentActivity> getActivities() {
		if (activities == null) {
			activities= new ArrayList<>();
			activities.addAll(getReplies());
			activities.addAll(getStatusChanges());
			activities.sort((o1, o2)->o1.getDate().compareTo(o2.getDate()));
		}
		return activities;
	}

	public boolean isCodeChanged() {
		if (codeChanged == null) {
			if (request.getHeadCommitHash().equals(markPos.getCommit())) {
				codeChanged = false;
			} else {
				Project project = request.getTargetProject();
				try (RevWalk revWalk = new RevWalk(project.getRepository())) {
					TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), markPos.getPath(), 
							request.getHeadCommit().getTree());
					if (treeWalk != null) {
						ObjectId blobId = treeWalk.getObjectId(0);
						if (treeWalk.getRawMode(0) == FileMode.REGULAR_FILE.getBits()) {
							BlobIdent blobIdent = new BlobIdent(request.getHeadCommitHash(), markPos.getPath(), 
									treeWalk.getRawMode(0));
							Blob newBlob = new Blob(blobIdent, blobId, treeWalk.getObjectReader()); 
							Blob oldBlob = project.getBlob(new BlobIdent(markPos.getCommit(), 
									markPos.getPath(), FileMode.REGULAR_FILE.getBits()));
							Preconditions.checkState(oldBlob != null && oldBlob.getText() != null);
							
							List<String> oldLines = new ArrayList<>();
							for (String line: oldBlob.getText().getLines())
								oldLines.add(WhitespaceOption.DEFAULT.process(line));
							
							List<String> newLines = new ArrayList<>();
							for (String line: newBlob.getText().getLines())
								newLines.add(WhitespaceOption.DEFAULT.process(line));
							
							Map<Integer, Integer> lineMapping = DiffUtils.mapLines(oldLines, newLines);
							int oldBeginLine = markPos.getRange().getBeginLine();
							int oldEndLine = markPos.getRange().getEndLine();
							Integer newBeginLine = lineMapping.get(oldBeginLine);
							if (newBeginLine != null) {
								for (int oldLine=oldBeginLine; oldLine<=oldEndLine; oldLine++) {
									Integer newLine = lineMapping.get(oldLine);
									if (newLine == null || newLine.intValue() != oldLine-oldBeginLine+newBeginLine) {
										codeChanged = true;
										break;
									}
								}
								if (codeChanged == null)
									codeChanged = false;
							} else {
								codeChanged = true;
							}
						} else  {
							codeChanged = true;
						}
					} else {
						codeChanged = true;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return codeChanged;
	}
}

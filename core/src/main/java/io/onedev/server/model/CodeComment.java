package io.onedev.server.model;

import java.io.IOException;
import java.io.Serializable;
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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.hibernate.annotations.DynamicUpdate;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.LastEvent;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.model.support.TextRange;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.editable.EditableUtils;

/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@Entity
@Table(indexes={
		@Index(columnList="g_project_id"), @Index(columnList="g_user_id"),
		@Index(columnList="commit"), @Index(columnList="path")})
@DynamicUpdate 
public class CodeComment extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Version
	private long version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
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
	
	@Embedded
	private CompareContext compareContext;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentReply> replies = new ArrayList<>();
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<CodeCommentRelation> relations = new ArrayList<>();
	
	@Column(nullable=false)
	private String uuid = UUID.randomUUID().toString();
	
	private transient Boolean contextChanged;
	
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

	public MarkPos getMarkPos() {
		return markPos;
	}

	public void setMarkPos(MarkPos markPos) {
		this.markPos = markPos;
	}

	public Collection<CodeCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<CodeCommentReply> replies) {
		this.replies = replies;
	}

	public Collection<CodeCommentRelation> getRelations() {
		return relations;
	}

	public void setRelations(Collection<CodeCommentRelation> relations) {
		this.relations = relations;
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

	public LastEvent getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(LastEvent lastEvent) {
		this.lastEvent = lastEvent;
	}
	
	public void setLastEvent(CodeCommentEvent event) {
		LastEvent lastEvent = new LastEvent();
		lastEvent.setDate(event.getDate());
		lastEvent.setType(EditableUtils.getDisplayName(event.getClass()));
		lastEvent.setUser(event.getUser());
		setLastEvent(lastEvent);
	}

	public boolean isVisitedAfter(Date date) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			Date visitDate = OneDev.getInstance(VisitManager.class).getCodeCommentVisitDate(user, this);
			return visitDate != null && visitDate.getTime()>date.getTime();
		} else {
			return true;
		}
	}
	
	public ComparingInfo getComparingInfo() {
		return new ComparingInfo(markPos.getCommit(), compareContext);
	}
	
	public boolean isValid() {
		return project.getRepository().hasObject(ObjectId.fromString(markPos.getCommit()));
	}
	
	@Nullable
	public TextRange mapRange(BlobIdent blobIdent) {
		RevCommit commit = project.getRevCommit(blobIdent.revision);
		if (commit.name().equals(getMarkPos().getCommit())) {
			return getMarkPos().getRange();
		} else {
			List<String> newLines = GitUtils.readLines(getProject().getRepository(), 
					commit, blobIdent.path, WhitespaceOption.DEFAULT);
			List<String> oldLines = GitUtils.readLines(getProject().getRepository(), 
					project.getRevCommit(getMarkPos().getCommit()), getMarkPos().getPath(), 
					WhitespaceOption.DEFAULT);
			return DiffUtils.mapRange(DiffUtils.mapLines(oldLines, newLines), getMarkPos().getRange());
		}
	}
	
	public boolean isContextChanged(PullRequest request) {
		if (contextChanged == null) {
			if (request.getHeadCommitHash().equals(markPos.getCommit())) {
				contextChanged = false;
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
										contextChanged = true;
										break;
									}
								}
								if (contextChanged == null)
									contextChanged = false;
							} else {
								contextChanged = true;
							}
						} else  {
							contextChanged = true;
						}
					} else {
						contextChanged = true;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return contextChanged;
	}
	
	public static class ComparingInfo implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String commit;
		
		private final CompareContext compareContext;
		
		public ComparingInfo(String commit, CompareContext compareContext) {
			this.commit = commit;
			this.compareContext = compareContext;
		}
		
		public String getCommit() {
			return commit;
		}

		public CompareContext getCompareContext() {
			return compareContext;
		}
		
	}
	
}

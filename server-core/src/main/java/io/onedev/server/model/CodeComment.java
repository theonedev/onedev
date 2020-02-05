package io.onedev.server.model;

import java.io.IOException;
import java.io.Serializable;
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

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.storage.AttachmentStorageSupport;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.SecurityUtils;
import static io.onedev.server.model.CodeComment.*;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;

@Entity
@Table(indexes={
		@Index(columnList="o_project_id"), @Index(columnList="o_user_id"),
		@Index(columnList=MarkPos.PROP_COMMIT), @Index(columnList=MarkPos.PROP_PATH), 
		@Index(columnList=PROP_CREATE_DATE), @Index(columnList=PROP_UPDATE_DATE)})
public class CodeComment extends AbstractEntity implements AttachmentStorageSupport {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String FIELD_CONTENT = "Content";
	
	public static final String PROP_CONTENT = "content";
	
	public static final String FIELD_REPLY = "Reply";
	
	public static final String FIELD_PATH = "Path";
	
	public static final String PROP_MARK_POS = "markPos";
	
	public static final String FIELD_REPLY_COUNT = "Reply Count";
	
	public static final String PROP_REPLY_COUNT = "replyCount";
	
	public static final String FIELD_CREATE_DATE = "Create Date";
	
	public static final String PROP_CREATE_DATE = "createDate";
	
	public static final String FIELD_UPDATE_DATE = "Update Date";
	
	public static final String PROP_UPDATE_DATE = "updateDate";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_RELATIONS = "relations";
	
	public static final String PROP_REPLIES = "replies";

	public static final String PROP_ID = "id";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_CONTENT, FIELD_REPLY, FIELD_PATH, FIELD_CREATE_DATE, FIELD_UPDATE_DATE, FIELD_REPLY_COUNT);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			FIELD_CREATE_DATE, PROP_CREATE_DATE,
			FIELD_UPDATE_DATE, PROP_UPDATE_DATE,
			FIELD_REPLY_COUNT, PROP_REPLY_COUNT);
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private User user;
	
	private String userName;

	@Column(nullable=false, length=16384)
	private String content;
	
	@Column(nullable=false)
	private Date createDate = new Date();

	@Column(nullable=false)
	private Date updateDate = new Date();
	
	private int replyCount;

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
	
	private transient Collection<User> participants;
	
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

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
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
	
	public ComparingInfo getComparingInfo() {
		return new ComparingInfo(markPos.getCommit(), compareContext);
	}
	
	public boolean isValid() {
		return project.getRepository().hasObject(ObjectId.fromString(markPos.getCommit()));
	}
	
	@Nullable
	public PlanarRange mapRange(BlobIdent blobIdent) {
		RevCommit commit = project.getRevCommit(blobIdent.revision, true);
		if (commit.name().equals(getMarkPos().getCommit())) {
			return getMarkPos().getRange();
		} else {
			List<String> newLines = GitUtils.readLines(getProject().getRepository(), 
					commit, blobIdent.path, WhitespaceOption.DEFAULT);
			List<String> oldLines = GitUtils.readLines(getProject().getRepository(), 
					project.getRevCommit(getMarkPos().getCommit(), true), getMarkPos().getPath(), 
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
									markPos.getPath(), FileMode.REGULAR_FILE.getBits()), true);
							Preconditions.checkState(oldBlob != null && oldBlob.getText() != null);
							
							List<String> oldLines = new ArrayList<>();
							for (String line: oldBlob.getText().getLines())
								oldLines.add(WhitespaceOption.DEFAULT.process(line));
							
							List<String> newLines = new ArrayList<>();
							for (String line: newBlob.getText().getLines())
								newLines.add(WhitespaceOption.DEFAULT.process(line));
							
							Map<Integer, Integer> lineMapping = DiffUtils.mapLines(oldLines, newLines);
							int oldBeginLine = markPos.getRange().getFromRow();
							int oldEndLine = markPos.getRange().getToRow();
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
	
	public static String getWebSocketObservable(Long commentId) {
		return CodeComment.class.getName() + ":" + commentId;
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

	@Override
	public String getAttachmentStorageUUID() {
		return uuid;
	}

	@Override
	public Project getAttachmentProject() {
		return getProject();
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

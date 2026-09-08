package io.onedev.server.web.component.markdown;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.onedev.server.util.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.Workspace;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.upload.FileUpload;
import io.onedev.server.web.util.WikiLinkResolver;
import io.onedev.server.workspace.WorkspaceService;

/** Markdown file editor shared by repository and wiki pages. */
public class BlobMarkdownEditor extends FormComponentPanel<byte[]> {

	@Inject
	private MarkdownService markdownService;

	private final BlobRenderContext context;

	private MarkdownEditor input;

	public BlobMarkdownEditor(String id, BlobRenderContext context, byte[] initialContent) {
		this(id, Model.of(initialContent), context);
	}

	public BlobMarkdownEditor(String id, IModel<byte[]> model, @Nullable BlobRenderContext context) {
		super(id, model);
		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		add(input = new MarkdownEditor("input", Model.of(new String(getModelObject(), charset)), false, context) {
			@Override
			protected String renderMarkdown(String markdown) {
				return BlobMarkdownEditor.this.renderMarkdown(markdown);
			}

			@Override
			protected String getAutosaveKey() {
				return BlobMarkdownEditor.this.getAutosaveKey();
			}

			@Override
			protected boolean shouldTrimInput() {
				return false;
			}

			@Override
			protected UserMentionSupport getUserMentionSupport() {
				return BlobMarkdownEditor.this.getUserMentionSupport();
			}

			@Override
			protected AtWhoReferenceSupport getReferenceSupport() {
				return BlobMarkdownEditor.this.getReferenceSupport();
			}

			@Override
			public BlobSelectionSupport getBlobSelectionSupport() {
				return BlobMarkdownEditor.this.getRepositoryFileSelectionSupport();
			}

			@Override
			public BlobUploadSupport getBlobUploadSupport() {
				return BlobMarkdownEditor.this.getRepositoryUploadSupport();
			}
		});

		if (context != null) {
			input.add(AttributeAppender.append("class", "no-autosize d-flex flex-grow-1 flex-column autofit"));
			if (!isAutofocus())
				input.add(AttributeAppender.append("class", "no-autofocus"));
		}
		input.setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null) {
			/*
			 * Textarea always uses CRLF as line ending, and below we change back to original EOL
			 */
			String initialContent = input.getModelObject();
			if (initialContent == null || !initialContent.contains("\r\n"))
				content = Strings.CS.replace(content, "\r\n", "\n");
			setConvertedInput(content.getBytes(StandardCharsets.UTF_8));
		} else {
			setConvertedInput(new byte[0]);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (isAutofocus()) {
			String script = String.format("$('#%s textarea').focus();", input.getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	protected Project getProject() {
		return context.getProject();
	}

	protected String renderMarkdown(String markdown) {
		var rendered = markdownService.process(markdownService.render(markdown), getProject(), context, null, false);
		return WikiLinkResolver.resolveWikiLinks(rendered, getRenderContext());		
	}

	@Nullable
	protected BlobRenderContext getRenderContext() {
		return context;
	}

	@Nullable
	protected String getAutosaveKey() {
		return context != null ? context.getEditorAutosaveKey() : null;
	}

	protected UserMentionSupport getUserMentionSupport() {
		return new UserMentionSupport() {

			@Override
			public List<User> findUsers(String query, int count) {
				UserCache cache = OneDev.getInstance(UserService.class).cloneCache();
				List<User> users = new ArrayList<>(cache.getUsers());
				users.sort(cache.comparingDisplayName(Sets.newHashSet()));

				users = new Similarities<User>(users) {

					@Override
					public double getSimilarScore(User object) {
						return cache.getSimilarScore(object, query);
					}

				};

				if (users.size() > count)
					return users.subList(0, count);
				else
					return users;
			}

		};
	}

	protected AtWhoReferenceSupport getReferenceSupport() {
		return new AtWhoReferenceSupport() {

			@Override
			public Project getCurrentProject() {
				return getProject();
			}

			@Override
			public List<PullRequest> queryPullRequests(Project project, String query, int count) {
				var subject = SecurityUtils.getSubject();
				if (SecurityUtils.canReadCode(subject, project)) {
					var requestQuery = new PullRequestQuery(new io.onedev.server.search.entity.pullrequest.FuzzyCriteria(query));
					return OneDev.getInstance(PullRequestService.class).query(subject, project, requestQuery, false, 0, count);
				} else {
					return new ArrayList<>();
				}
			}

			@Override
			public List<Issue> queryIssues(Project project, String query, int count) {
				var subject = SecurityUtils.getSubject();
				if (SecurityUtils.canAccessProject(subject, project)) {
					var projectScope = new ProjectScope(project, false, false);
					var issueQuery = new IssueQuery(new io.onedev.server.search.entity.issue.FuzzyCriteria(query));
					return OneDev.getInstance(IssueService.class).query(subject, projectScope, issueQuery, false, 0, count);
				} else {
					return new ArrayList<>();
				}
			}

			@Override
			public List<Build> queryBuilds(Project project, String query, int count) {
				var subject = SecurityUtils.getSubject();
				return OneDev.getInstance(BuildService.class).query(subject, project, query, count);
			}

			@Override
			public List<Workspace> queryWorkspaces(Project project, String query, int count) {
				return OneDev.getInstance(WorkspaceService.class).query(SecurityUtils.getSubject(), project, query, count);
			}

		};
	}

	protected boolean isAutofocus() {
		return context != null && context.getMode() == Mode.EDIT;
	}

	@Nullable
	public BlobSelectionSupport getRepositoryFileSelectionSupport() {
		if (context == null)
			return null;
		return new BlobSelectionSupport() {
			@Override
			public Project getProject() {
				return context.getProject();
			}

			@Override
			public BlobIdent getBlobIdent() {
				return context.getBlobIdent();
			}

			@Override
			public void onSelect(AjaxRequestTarget target, String path, boolean image, String text) {
				var blob = new BlobIdent(context.getBlobIdent().revision, path, FileMode.REGULAR_FILE.getBits());
				String referenceUrl = urlFor(ProjectBlobPage.class,
						ProjectBlobPage.paramsOf(getProject(), blob)).toString();
				String relativeUrl = PathUtils.relativize(context.getDirectoryUrl(), referenceUrl);
				insertUrl(target, image, relativeUrl, text != null ? text : blob.getName(), null);
			}
		};
	}

	@Nullable
	public BlobUploadSupport getRepositoryUploadSupport() {
		if (context == null)
			return null;
		return new BlobUploadSupport() {
			@Override
			public Project getProject() {
				return context.getProject();
			}

			@Override
			public BlobIdent getBlobIdent() {
				return context.getBlobIdent();
			}

			@Override
			public String getDefaultDirectory() {
				return context.getDirectory();
			}

			@Override
			public String upload(FileUpload upload, String directory, String commitMessage) {
				context.onCommitted(null, context.uploadFiles(upload, directory, commitMessage));
				String fileName = FilenameUtils.sanitizeFileName(FileUpload.getFileName(upload.getItems().iterator().next()));
				String uploadedPath = directory != null ? directory + "/" + fileName : fileName;
				String relativePath = PathUtils.relativize(context.getDirectory(), uploadedPath);
				return java.util.Arrays.stream(relativePath.split("/"))
						.map(UrlUtils::encodePath).collect(java.util.stream.Collectors.joining("/"));
			}
		};
	}

	public void insertText(AjaxRequestTarget target, String text) {
		input.insertText(target, text);
	}

	public void insertUrl(AjaxRequestTarget target, boolean image, String url, String text, @Nullable String replaceMessage) {
		input.insertUrl(target, image, url, text, replaceMessage);
	}
}

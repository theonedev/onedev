package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.component.markdown.AtWhoReferenceSupport;
import io.onedev.server.web.component.markdown.MarkdownEditor;
import io.onedev.server.web.component.markdown.UserMentionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

@SuppressWarnings("serial")
abstract class MarkdownBlobEditor extends FormComponentPanel<byte[]> {

	private final BlobRenderContext context;
	
	private MarkdownEditor input;
	
	public MarkdownBlobEditor(String id, BlobRenderContext context, byte[] initialContent) {
		super(id, Model.of(initialContent));

		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		add(input = new MarkdownEditor("input", Model.of(new String(getModelObject(), charset)), 
				false, context) {

			@Override
			protected String getAutosaveKey() {
				return MarkdownBlobEditor.this.getAutosaveKey();
			}

			@Override
			protected String renderMarkdown(String markdown) {
				MarkdownManager manager = OneDev.getInstance(MarkdownManager.class);
				return manager.process(manager.render(markdown), context.getProject(), context);
			}

			@Override
			protected UserMentionSupport getUserMentionSupport() {
				return new UserMentionSupport() {

					@Override
					public List<User> findUsers(String query, int count) {
						List<User> mentionables = OneDev.getInstance(UserManager.class).queryAndSort(Sets.newHashSet());
						List<User> filtered = MatchScoreUtils.filterAndSort(mentionables, new MatchScoreProvider<User>() {

							@Override
							public double getMatchScore(User object) {
								return object.getMatchScore(query) 
										* (mentionables.size() - mentionables.indexOf(object)) 
										/ mentionables.size();
							}
							
						});
						
						if (filtered.size() > count)
							return filtered.subList(0, count);
						else
							return filtered;
					}
					
				};
			}
			
			@Override
			protected final AtWhoReferenceSupport getReferenceSupport() {
				return new AtWhoReferenceSupport() {

					@Override
					public List<PullRequest> findPullRequests(@Nullable Project project, String query, int count) {
						if (project == null)
							project = context.getProject();
						return OneDev.getInstance(PullRequestManager.class).query(project, query, count);
					}

					@Override
					public List<Issue> findIssues(@Nullable Project project, String query, int count) {
						if (project == null) 
							project = context.getProject();
						if (SecurityUtils.canAccess(project))
							return OneDev.getInstance(IssueManager.class).query(project, query, count);
						else
							return new ArrayList<>();
					}

					@Override
					public List<Build> findBuilds(@Nullable Project project, String query, int count) {
						if (project == null)
							project = context.getProject();
						return OneDev.getInstance(BuildManager.class).query(project, query, count);
					}
					
				};
			}

		});
		
		if (context.getMode() != Mode.EDIT)
			input.add(AttributeAppender.append("class", "no-autofocus"));
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
				content = StringUtils.replace(content, "\r\n", "\n");
			setConvertedInput(content.getBytes(StandardCharsets.UTF_8));
		} else {
			setConvertedInput(new byte[0]);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (context.getMode() == Mode.EDIT) {
			String script = String.format("$('#%s textarea').focus();", input.getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	protected abstract String getAutosaveKey();
	
}

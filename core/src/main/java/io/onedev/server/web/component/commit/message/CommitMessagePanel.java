package io.onedev.server.web.component.commit.message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.CommitMessageTransformer;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.utils.Highlighter;
import io.onedev.utils.Transformer;

@SuppressWarnings("serial")
public class CommitMessagePanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<RevCommit> commitModel;
	
	private final IModel<List<Pattern>> highlightPatternsModel;
	
	public CommitMessagePanel(String id, IModel<Project> projectModel, 
			IModel<RevCommit> commitModel, IModel<List<Pattern>> highlightPatternsModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.commitModel = commitModel;
		this.highlightPatternsModel = highlightPatternsModel;
	}

	public CommitMessagePanel(String id, IModel<Project> projectModel, IModel<RevCommit> commitModel) {
		this(id, projectModel, commitModel, new LoadableDetachableModel<List<Pattern>>() {

			@Override
			protected List<Pattern> load() {
				return new ArrayList<>();
			}
			
		});
	}
	
	private String highlight(String text, @Nullable String commitUrl) {
		return Highlighter.highlightPatterns(text, highlightPatternsModel.getObject(), new Transformer<String>() {

			@Override
			public String transform(String text) {
				String transformed = new CommitMessageTransformer(getProject(), commitUrl).transform(text);
				return "<span class='highlight'>" + transformed + "</span>";
			}
			
		}, new Transformer<String>() {

			@Override
			public String transform(String text) {
				return new CommitMessageTransformer(getProject(), commitUrl).transform(text);
			}
			
		});
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	private RevCommit getCommit() {
		return commitModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PageParameters params = CommitDetailPage.paramsOf(getProject(), getCommit().name()); 
		String commitUrl = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
		
		add(new Label("summary", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return highlight(getCommit().getShortMessage(), commitUrl);
			}
			
		}).setEscapeModelStrings(false));

		add(new Label("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return highlight(GitUtils.getDetailMessage(getCommit()), null);
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitUtils.getDetailMessage(getCommit()) != null);
			}
		}.setEscapeModelStrings(false));
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("toggle") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitUtils.getDetailMessage(getCommit()) != null);
			}
		};
		add(detailedToggle);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitMessageResourceReference()));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		commitModel.detach();
		highlightPatternsModel.detach();
		
		super.onDetach();
	}
	
}

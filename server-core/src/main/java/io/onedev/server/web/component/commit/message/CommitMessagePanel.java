package io.onedev.server.web.component.commit.message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
import io.onedev.server.util.Highlighter;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class CommitMessagePanel extends Panel {

	private final IModel<RevCommit> commitModel;
	
	private final IModel<List<Pattern>> highlightPatternsModel;
	
	public CommitMessagePanel(String id, IModel<RevCommit> commitModel, 
			IModel<List<Pattern>> highlightPatternsModel) {
		super(id);
		
		this.commitModel = commitModel;
		this.highlightPatternsModel = highlightPatternsModel;
	}

	public CommitMessagePanel(String id, IModel<RevCommit> commitModel) {
		this(id, commitModel, new LoadableDetachableModel<List<Pattern>>() {

			@Override
			protected List<Pattern> load() {
				return new ArrayList<>();
			}
			
		});
	}
	
	private String highlight(String text, @Nullable String commitUrl) {
		ReferenceTransformer transformer = new ReferenceTransformer(getProject(), commitUrl);
		
		return Highlighter.highlightPatterns(text, highlightPatternsModel.getObject(), new Function<String, String>() {

			@Override
			public String apply(String text) {
				return "<span class='highlight'>" + transformer.apply(text) + "</span>";
			}
			
		}, new Function<String, String>() {

			@Override
			public String apply(String text) {
				return transformer.apply(text);
			}
			
		});
	}
	
	protected abstract Project getProject();
	
	private RevCommit getCommit() {
		return commitModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PageParameters params = CommitDetailPage.paramsOf(getProject(), getCommit().name()); 
		String commitUrl = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
		
		add(new Label("summary", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
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
		commitModel.detach();
		highlightPatternsModel.detach();
		
		super.onDetach();
	}
	
}

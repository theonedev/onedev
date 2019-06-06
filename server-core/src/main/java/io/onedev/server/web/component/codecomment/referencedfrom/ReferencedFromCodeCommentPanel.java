package io.onedev.server.web.component.codecomment.referencedfrom;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class ReferencedFromCodeCommentPanel extends GenericPanel<CodeComment> {

	public ReferencedFromCodeCommentPanel(String id, Long commentId) {
		super(id, new LoadableDetachableModel<CodeComment>() {

			@Override
			protected CodeComment load() {
				return OneDev.getInstance(CodeCommentManager.class).load(commentId);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		CodeComment comment = getModelObject();
		PageParameters params = ProjectBlobPage.paramsOf(comment);
		Link<Void> link = new BookmarkablePageLink<Void>("link", ProjectBlobPage.class, params);
		link.add(new Label("label", comment.getMarkPos().getPath()));
		
		add(link);
	}

}

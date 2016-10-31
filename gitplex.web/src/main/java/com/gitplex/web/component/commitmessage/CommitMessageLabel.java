package com.gitplex.web.component.commitmessage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.unbescape.html.HtmlEscape;

import com.gitplex.core.GitPlex;
import com.gitplex.core.commitmessagetransform.CommitMessageTransformer;
import com.gitplex.core.entity.Depot;

@SuppressWarnings("serial")
public class CommitMessageLabel extends Label {

	public CommitMessageLabel(String id, IModel<Depot> depotModel, IModel<String> messageModel) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String message = HtmlEscape.escapeHtml5(messageModel.getObject());
				for (CommitMessageTransformer transformer: GitPlex.getExtensions(CommitMessageTransformer.class)) {
					message = transformer.transform(depotModel.getObject(), message);
				}
				return message;
			}

			@Override
			protected void onDetach() {
				depotModel.detach();
				messageModel.detach();
				super.onDetach();
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}

}

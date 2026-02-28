package io.onedev.server.web.component.workspace.status;

import java.util.Collection;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import io.onedev.server.model.Workspace.Status;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.svg.SpriteImage;

public class WorkspaceStatusIcon extends SpriteImage {

	private static final long serialVersionUID = 1L;

	private final IModel<Status> model;

	public WorkspaceStatusIcon(String id, IModel<Status> model) {
		super(id, new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getIconHref(model.getObject());
			}

		});
		this.model = model;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getIconClass(model.getObject());
			}

		}));

		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return getChangeObservables();
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		model.detach();
		super.onDetach();
	}

	protected Collection<String> getChangeObservables() {
		return Sets.newHashSet();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WorkspaceStatusCssResourceReference()));
	}

	public static String getIconHref(@Nullable Status status) {
		if (status == null)
			return "dot";
		switch (status) {
			case PENDING:
				return "target";
			case ACTIVE:
				return "tick-circle-o";
			case ERROR:
				return "times-circle-o";
			default:
				throw new RuntimeException("Unexpected workspace status: " + status);
		}
	}

	public static String getIconClass(@Nullable Status status) {
		if (status != null)
			return "icon flex-shrink-0 workspace-status-" + status.name().toLowerCase();
		else
			return "icon flex-shrink-0 workspace-status-none";
	}

}

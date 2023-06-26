package io.onedev.server.web.component.build.status;

import com.google.common.collect.Sets;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings("serial")
public class BuildStatusIcon extends SpriteImage {

	private final IModel<Status> model;
	
	public BuildStatusIcon(String id, IModel<Status> model) {
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
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
	public static String getIconHref(@Nullable Status status) {
		if (status != null) {
			switch (status ) {
				case SUCCESSFUL:
					return "tick-circle-o";
				case FAILED:
					return "times-circle-o";
				case CANCELLED:
					return "cancel";
				case PENDING:
					return "target";
				case RUNNING:
					return "spin";
				case TIMED_OUT:
					return "timeout";
				case WAITING:
					return "clock";
				default:
					throw new RuntimeException("Unexpected build status: " + status);
			}
		} else {
			return "dot";
		}
	}
	
	public static String getIconClass(@Nullable Status status) {
		if (status == Build.Status.RUNNING)
			return "icon flex-shrink-0 build-status-running spin";
		else if (status != null)
			return "icon flex-shrink-0 build-status-" + status.name().toLowerCase();
		else
			return "icon flex-shrink-0 build-status-none";
	}
	
}

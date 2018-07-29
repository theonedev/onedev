package io.onedev.server.web.component.entitynav;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.http.WebSession;

import io.onedev.server.entityquery.EntityQuery;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.utils.ReflectionUtils;
import io.onedev.utils.WordUtils;

@SuppressWarnings("serial")
public abstract class EntityNavPanel<T extends AbstractEntity> extends Panel {

	private final String entityName;
	
	public EntityNavPanel(String id) {
		super(id);
		
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(EntityNavPanel.class, getClass());
		entityName = WordUtils.uncamel(typeArguments.get(0).getSimpleName()).toLowerCase();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getPosition() != null);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("next") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(getPosition().getOffset()>0);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (getPosition().getOffset() <= 0)
					tag.put("disabled", "disabled");
				tag.put("title", "Next " + entityName);
			}

			@Override
			public void onClick() {
				EntityQuery<T> query = parse(getPosition().getQuery());
				int count = getPosition().getCount();
				int offset = getPosition().getOffset() - 1;
				List<T> entities = query(query, offset, 1);
				if (!entities.isEmpty()) {
					if (!query.matches(getEntity()))
						count--;
					QueryPosition prevPosition = new QueryPosition(getPosition().getQuery(), count, offset);
					navTo(entities.get(0), prevPosition);
				} else {
					WebSession.get().warn("No more " + entityName + "s");
				}
			}
			
		});
		add(new Link<Void>("prev") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(getPosition().getOffset()<getPosition().getCount()-1);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (getPosition().getOffset() >= getPosition().getCount()-1)
					tag.put("disabled", "disabled");
				tag.put("title", "Previous " + entityName);
			}

			@Override
			public void onClick() {
				EntityQuery<T> query = parse(getPosition().getQuery());
				int offset = getPosition().getOffset();
				int count = getPosition().getCount();
				if (query.matches(getEntity())) 
					offset++;
				else
					count--;
				
				List<T> entities = query(query, offset, 1);
				if (!entities.isEmpty()) {
					QueryPosition nextPosition = new QueryPosition(getPosition().getQuery(), count, offset);
					navTo(entities.get(0), nextPosition);
				} else {
					WebSession.get().warn("No more " + entityName + "s");
				}
			}
			
		});
		
		add(new Label("current", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return entityName + " " + (getPosition().getCount()-getPosition().getOffset()) + " of " + getPosition().getCount();				
			}
			
		}));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new EntityNavCssResourceReference()));
	}

	protected abstract EntityQuery<T> parse(String queryString);
	
	protected abstract T getEntity();
	
	@Nullable
	protected abstract QueryPosition getPosition();
	
	protected abstract void navTo(T entity, QueryPosition position);
	
	protected abstract List<T> query(EntityQuery<T> query, int offset, int count);
	
}

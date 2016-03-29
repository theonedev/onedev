package com.pmease.gitplex.web.component.entityselector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.ReflectionUtils;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.gitplex.core.GitPlex;

@SuppressWarnings("serial")
public abstract class EntitySelector<T extends AbstractEntity> extends Panel {

	private final IModel<Collection<T>> entitiesModel;
	
	private final Class<T> entityClass;
	
	private final Long currentEntityId;

	private ListView<T> entitiesView;
	
	@SuppressWarnings("unchecked")
	public EntitySelector(String id, IModel<Collection<T>> entitiesModel, Long currentEntityId) {
		super(id);
		
		this.entitiesModel = entitiesModel;
		this.currentEntityId = currentEntityId;
		
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(EntitySelector.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			entityClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of entity selector implementation must "
					+ "be EntitySelector and must realize the type argument <T>");
		}
		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer entitiesContainer = new WebMarkupContainer("entities") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!entitiesView.getModelObject().isEmpty());
			}
			
		};
		entitiesContainer.setOutputMarkupPlaceholderTag(true);
		add(entitiesContainer);
		
		Label noEntitiesLabel = new Label("noEntities", getNotFoundMessage()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(entitiesView.getModelObject().isEmpty());
			}
			
		};
		noEntitiesLabel.setOutputMarkupPlaceholderTag(true);
		add(noEntitiesLabel);
		
		TextField<String> searchField = new TextField<String>("search", Model.of(""));
		add(searchField);
		searchField.add(new FormComponentInputBehavior() {
			
			@Override
			protected void onInput(AjaxRequestTarget target) {
				target.add(entitiesContainer);
				target.add(noEntitiesLabel);
			}
			
		});
		searchField.add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				Long id = params.getParameterValue("id").toLong();
				onSelect(target, GitPlex.getInstance(Dao.class).load(entityClass, id));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				String script = String.format("gitplex.entitySelector.init('%s', %s)", 
						searchField.getMarkupId(true), 
						getCallbackFunction(CallbackParameter.explicit("id")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		entitiesContainer.add(entitiesView = new ListView<T>("entities", 
				new LoadableDetachableModel<List<T>>() {

			@Override
			protected List<T> load() {
				List<T> entities = new ArrayList<>();
				for (T entity: entitiesModel.getObject()) {
					if (matches(entity, searchField.getInput())) {
						entities.add(entity);
					}
				}
				Collections.sort(entities);
				
				return entities;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<T> item) {
				T entity = item.getModelObject();
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, item.getModelObject());
					}
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("href", getUrl(item.getModelObject()));
					}
					
				};
				if (entity.getId().equals(currentEntityId)) 
					link.add(AttributeAppender.append("class", " current"));
				link.add(renderEntity("entity", item.getModel()));
				item.add(link);
				
				if (item.getIndex() == 0)
					item.add(AttributeAppender.append("class", "active"));
				item.add(AttributeAppender.append("data-id", entity.getId()));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		entitiesModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(EntitySelector.class, "entity-selector.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(EntitySelector.class, "entity-selector.css")));
	}
	
	protected abstract String getUrl(T entity);
	
	protected abstract void onSelect(AjaxRequestTarget target, T entity);
	
	protected abstract String getNotFoundMessage();
	
	protected abstract Component renderEntity(String componentId, IModel<T> entityModel);
	
	protected abstract boolean matches(T entity, @Nullable String searchTerm);
}

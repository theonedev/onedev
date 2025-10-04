package io.onedev.server.web.page.admin.buildsetting.agent;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.service.AgentService;
import io.onedev.server.model.Agent;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.agent.AgentQueryLexer;
import io.onedev.server.search.entity.agent.EverUsedSinceCriteria;
import io.onedev.server.search.entity.agent.HasRunningBuildsCriteria;
import io.onedev.server.search.entity.agent.NotUsedSinceCriteria;
import io.onedev.server.search.entity.agent.OfflineCriteria;
import io.onedev.server.search.entity.agent.OnlineCriteria;
import io.onedev.server.search.entity.agent.OsArchCriteria;
import io.onedev.server.search.entity.agent.OsCriteria;
import io.onedev.server.search.entity.agent.PausedCriteria;
import io.onedev.server.search.entity.agent.StatusCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.filteredit.FilterEditPanel;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;

class AgentFilterPanel extends FilterEditPanel<Agent> {
	
	public AgentFilterPanel(String id, IModel<EntityQuery<Agent>> queryModel) {
		super(id, queryModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var statusChoices = new LinkedHashMap<String, String>();
		statusChoices.put("Online", _T("Online"));
		statusChoices.put("Offline", _T("Offline"));

        var statusChoice = new StringSingleChoice("status", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), StatusCriteria.class, null);
				if (criterias.size() == 0)
					return null;
				else
					return criterias.get(0) instanceof OnlineCriteria ? "Online" : "Offline";
			}

			@Override
			public void setObject(String object) {					
				Criteria<Agent> rootCriteria;
				if (object == null)
					rootCriteria = null;
				else if (object.equals("Online"))
					rootCriteria = new OnlineCriteria();
				else
					rootCriteria = new OfflineCriteria();
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), StatusCriteria.class, rootCriteria, null));
				getModel().setObject(query);
			}

		}, Model.ofList(new ArrayList<>(statusChoices.keySet())), Model.ofMap(statusChoices), false);

		statusChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(statusChoice);

        var osChoice = new StringMultiChoice("os", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), OsCriteria.class, null);
				return criterias.stream().map(it -> it.getValue()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {					
				var rootCriteria = Criteria.orCriterias(object.stream().map(it -> new OsCriteria(it, AgentQueryLexer.Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), OsCriteria.class, rootCriteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var osNames = new ArrayList<>(getAgentService().getOsNames());
				Collections.sort(osNames);
				return osNames;
			}
			
		}, false);
		osChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(osChoice);		

        var osArchChoice = new StringMultiChoice("osArch", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), OsArchCriteria.class, null);
				return criterias.stream().map(it -> it.getValue()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {					
				var rootCriteria = Criteria.orCriterias(object.stream().map(it -> new OsArchCriteria(it, AgentQueryLexer.Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), OsArchCriteria.class, rootCriteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var osArchs = new ArrayList<>(getAgentService().getOsArchs());
				Collections.sort(osArchs);
				return osArchs;
			}
		}, false);
		osArchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(osArchChoice);			

		var pausedCheck = new CheckBox("paused", new IModel<Boolean>() {

			@Override
			public Boolean getObject() {
				return !getMatchingCriterias(getModelObject().getCriteria(), PausedCriteria.class, null).isEmpty();
			}

			@Override
			public void setObject(Boolean object) {
				var criteria = object? new PausedCriteria() : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), PausedCriteria.class, criteria, null));
				getModel().setObject(query);
			}

			@Override
			public void detach() {
			}

		});

		pausedCheck.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(pausedCheck);

		var hasRunningBuildsCheck = new CheckBox("hasRunningBuilds", new IModel<Boolean>() {

			@Override
			public Boolean getObject() {
				return !getMatchingCriterias(getModelObject().getCriteria(), HasRunningBuildsCriteria.class, null).isEmpty();
			}

			@Override
			public void setObject(Boolean object) {
				var criteria = object? new HasRunningBuildsCriteria() : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), HasRunningBuildsCriteria.class, criteria, null));
				getModel().setObject(query);
			}

			@Override
			public void detach() {
			}

		});

		hasRunningBuildsCheck.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(hasRunningBuildsCheck);	
		
		var everUsedSincePicker = new DatePicker("everUsedSince", new IModel<Date>() {

			@Override
			public void detach() {
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), EverUsedSinceCriteria.class, null);
				if (criterias.isEmpty())
					return null;
				else
					return criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object!=null? new EverUsedSinceCriteria(DateUtils.formatDate(object)) : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), EverUsedSinceCriteria.class, criteria, null));
				getModel().setObject(query);
			}

		}, false);
		everUsedSincePicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(everUsedSincePicker);

		var notUsedSincePicker = new DatePicker("notUsedSince", new IModel<Date>() {

			@Override
			public void detach() {
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), NotUsedSinceCriteria.class, null);
				if (criterias.isEmpty())
					return null;
				else
					return criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object!=null? new NotUsedSinceCriteria(DateUtils.formatDate(object)) : null;
				var query = getModelObject();	
				query.setCriteria(setMatchingCriteria(query.getCriteria(), NotUsedSinceCriteria.class, criteria, null));
				getModel().setObject(query);
			}

		}, false);
		notUsedSincePicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(notUsedSincePicker);			
	}
	
	private static AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}

}

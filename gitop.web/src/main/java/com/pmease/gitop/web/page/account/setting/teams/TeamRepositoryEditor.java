package com.pmease.gitop.web.page.account.setting.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.web.component.choice.RepositoryMultiChoice;
import com.pmease.gitop.web.component.link.RepositoryHomeLink;
import com.pmease.gitop.web.model.RepositoryModel;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class TeamRepositoryEditor extends Panel {

	public TeamRepositoryEditor(String id, final IModel<Team> model) {
		super(id, model);
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newRepositoriesForm());
		
		WebMarkupContainer repositoriesDiv = new WebMarkupContainer("repositories");
		repositoriesDiv.setOutputMarkupId(true);
		add(repositoriesDiv);
		
		final IModel<List<Authorization>> model = 
				new LoadableDetachableModel<List<Authorization>>() {

					@Override
					protected List<Authorization> load() {
						Dao dao = Gitop.getInstance(Dao.class);
						return dao.query(EntityCriteria.of(Authorization.class).add(Restrictions.eq("team", getTeam())));
					}
		};
		
		repositoriesDiv.add(new Label("total", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return model.getObject().size();
			}
			
		}));
		
		ListView<Authorization> repositoriesView = new ListView<Authorization>("repo", model) {

			@Override
			protected void populateItem(ListItem<Authorization> item) {
				Authorization a = item.getModelObject();
				item.add(new RepositoryHomeLink("link", new RepositoryModel(a.getRepository())));
				final Long id = a.getId();
				item.add(new AjaxLink<Void>("removelink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Dao dao = Gitop.getInstance(Dao.class);
						Authorization authorization = dao.get(Authorization.class, id);
						dao.remove(authorization);
						// TODO: add notification
						//
//						Messenger.warn("Repository [" + authorization.getRepository() 
//								+ "] is removed from team <b>[" 
//								+ authorization.getTeam().getName() + "]</b>").run(target);
						onRepositoriesChanged(target);
					}
				});
			}
		};
		
		repositoriesDiv.add(repositoriesView);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Component newRepositoriesForm() {
		Form<?> form = new Form<Void>("reposForm");
		form.add(new NotificationPanel("feedback", form));
		final IModel<Collection<Repository>> reposModel = new WildcardListModel(new ArrayList<Repository>());
		form.add(new RepositoryMultiChoice("repochoice", reposModel, new RepositoryChoiceProvider()));
		
		form.add(new AjaxButton("submit", form) {
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Collection<Repository> repositories = reposModel.getObject();
				Dao dao = Gitop.getInstance(Dao.class);
				for (Repository each : repositories) {
					Authorization authorization = new Authorization();
					authorization.setTeam(getTeam());
					authorization.setRepository(each);
					dao.persist(authorization);
				}
				
				reposModel.setObject(new ArrayList<Repository>());
				target.add(form);
				onRepositoriesChanged(target);
			}
		});
		
		return form;
	}

	class RepositoryChoiceProvider extends ChoiceProvider<Repository> {
		
		@Override
		public void query(String term, int page, Response<Repository> response) {
			Dao dao = Gitop.getInstance(Dao.class);
			int first = page * 25;
			List<Repository> repositories = dao.query(EntityCriteria.of(Repository.class)
							.add(Restrictions.eq("owner", getTeam().getOwner()))
							.add(Restrictions.like("name", term, MatchMode.START).ignoreCase())
							.addOrder(Order.asc("name")), first, 25);
			
			if (repositories.isEmpty()) {
				response.addAll(repositories);
				return;
			}
			
			List<Authorization> authorizations = dao.query(EntityCriteria.of(Authorization.class)
					.add(Restrictions.eq("team", getTeam())));
			
			List<Repository> result = Lists.newArrayList();
			for (Repository each : repositories) {
				if (!in(each, authorizations)) {
					result.add(each);
				}
			}
			
			response.addAll(result);
		}

		private boolean in(Repository repository, List<Authorization> authorizations) {
			for (Authorization each : authorizations) {
				if (Objects.equal(repository, each.getRepository())) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public void toJson(Repository choice, JSONWriter writer) throws JSONException {
			writer.key("id").value(choice.getId())
				  .key("owner").value(choice.getOwner().getName())
				  .key("name").value(choice.getName());
		}

		@Override
		public Collection<Repository> toChoices(Collection<String> ids) {
			List<Repository> list = Lists.newArrayList();
			Dao dao = Gitop.getInstance(Dao.class);
			for (String each : ids) {
				Long id = Long.valueOf(each);
				list.add(dao.load(Repository.class, id));
			}
			
			return list;
		}
	}
	
	private void onRepositoriesChanged(AjaxRequestTarget target) {
		target.add(get("repositories"));
	}
	
	private Team getTeam() {
		return (Team) getDefaultModelObject();
	}
}

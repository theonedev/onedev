package io.onedev.server.web.page.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.MembershipManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.link.ProjectLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.choice.AbstractProjectChoiceProvider;
import io.onedev.server.web.component.project.choice.ProjectChoiceResourceReference;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.ProjectAware;
import io.onedev.utils.StringUtils;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

@SuppressWarnings("serial")
public class ParticipatedProjectsPage extends UserPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private String searchInput;
	
	private DataTable<Project, Void> projectsTable;
	
	private SelectionColumn<Project, Void> selectionColumn;
	
	public ParticipatedProjectsPage(PageParameters params) {
		super(params);
	}
	
	private List<Project> getJoinedProjects() {
		Set<Project> projectSet = new HashSet<>();
		for (Membership membership: getUser().getMemberships()) {
			projectSet.add(membership.getTeam().getProject());
		}
		List<Project> projectList = new ArrayList<>(projectSet);
		Collections.sort(projectList, new Comparator<Project>() {

			@Override
			public int compare(Project o1, Project o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		return projectList;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterProjects", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(projectsTable);
			}
			
		});
		
		add(new SelectToAddChoice<ProjectFacade>("addNew", new AbstractProjectChoiceProvider() {

			@Override
			public void query(String term, int page, Response<ProjectFacade> response) {
				List<ProjectFacade> notJoinedProjects = new ArrayList<>();
				
				Collection<Long> joinedProjectIds = getJoinedProjects().stream().map(it->it.getId()).collect(Collectors.toSet());
				for (ProjectFacade project: OneDev.getInstance(CacheManager.class).getProjects().values()) {
					if (!joinedProjectIds.contains(project.getId()))
						notJoinedProjects.add(project);
				}
				Collections.sort(notJoinedProjects);
				Collections.reverse(notJoinedProjects);
				
				MatchScoreProvider<ProjectFacade> matchScoreProvider = new MatchScoreProvider<ProjectFacade>() {

					@Override
					public double getMatchScore(ProjectFacade object) {
						return MatchScoreUtils.getMatchScore(object.getName(), term);
					}
					
				};

				notJoinedProjects = MatchScoreUtils.filterAndSort(notJoinedProjects, matchScoreProvider);
				
				new ResponseFiller<ProjectFacade>(response).fill(notJoinedProjects, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add project...");
				getSettings().setFormatResult("onedev.server.projectChoiceFormatter.formatResult");
				getSettings().setFormatSelection("onedev.server.projectChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("onedev.server.projectChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, ProjectFacade selection) {
				new TeamsEditModal(target) {
					
					@Override
					public Project getProject() {
						return OneDev.getInstance(ProjectManager.class).load(selection.getId());
					}

					@Override
					protected void onSaved(AjaxRequestTarget target) {
						target.add(projectsTable);
					}
					
				};
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new ProjectChoiceResourceReference()));
			}
			
		});			
		
		AjaxLink<Void> deleteSelected = new AjaxLink<Void>("deleteSelected") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				for (IModel<Project> model: selectionColumn.getSelections()) {
					for (Iterator<Membership> it = getUser().getMemberships().iterator(); it.hasNext();) {
						Membership membership = it.next();
						if (membership.getTeam().getProject().equals(model.getObject())) {
							OneDev.getInstance(MembershipManager.class).delete(membership);
							it.remove();
						}
					}
				}
				target.add(projectsTable);
				selectionColumn.getSelections().clear();
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(selectionColumn != null && !selectionColumn.getSelections().isEmpty());
			}

		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<Project, Void>> columns = new ArrayList<>();
		
		if (SecurityUtils.isAdministrator()) {
			selectionColumn = new SelectionColumn<Project, Void>() {
				
				@Override
				protected void onSelectionChange(AjaxRequestTarget target) {
					target.add(deleteSelected);
				}
				
			};
			columns.add(selectionColumn);
		}
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", ParticipatedProjectsPage.this);
				Project project = rowModel.getObject();
				String projectName = project.getName();
				fragment.add(new ProjectLink("project", project) {

					@Override
					public IModel<?> getBody() {
						return Model.of(projectName);
					}

				});
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Joined Teams")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "teamsFrag", ParticipatedProjectsPage.this);
				fragment.add(new Label("teams", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						Project project = rowModel.getObject();
						List<String> teamNames = new ArrayList<>();
						for (Membership membership: getUser().getMemberships()) {
							if (membership.getTeam().getProject().equals(project))
								teamNames.add(membership.getTeam().getName());
						}
						Collections.sort(teamNames);
						return StringUtils.join(teamNames, ", ");
					}
					
				}));
				fragment.add(new AjaxLink<Void>("edit") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new TeamsEditModal(target) {
							
							@Override
							public Project getProject() {
								return rowModel.getObject();
							}

							@Override
							protected void onSaved(AjaxRequestTarget target) {
								target.add(fragment);
							}
							
						};
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}
					
				});
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<Project, Void> dataProvider = new LoadableDetachableDataProvider<Project, Void>() {

			@Override
			public Iterator<? extends Project> iterator(long first, long count) {
				List<Project> projects = new ArrayList<>();
				for (Project project: getJoinedProjects()) {
					if (searchInput == null || project.getName().toLowerCase().contains(searchInput.toLowerCase()))
						projects.add(project);
				}
				
				if (first + count <= projects.size())
					return projects.subList((int)first, (int)(first+count)).iterator();
				else
					return projects.subList((int)first, projects.size()).iterator();
			}

			@Override
			public long calcSize() {
				return getJoinedProjects().size();
			}

			@Override
			public IModel<Project> model(Project object) {
				Long projectId = object.getId();
				return new LoadableDetachableModel<Project>() {

					@Override
					protected Project load() {
						return OneDev.getInstance(ProjectManager.class).load(projectId);
					}
					
				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getUser());
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(projectsTable = new HistoryAwareDataTable<Project, Void>("projects", columns, 
				dataProvider, WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
	private abstract class TeamsEditModal extends ModalPanel implements ProjectAware {

		public TeamsEditModal(AjaxRequestTarget target) {
			super(target);
		}

		@Override
		protected Component newContent(String id) {
			Fragment fragment = new Fragment(id, "teamsEditFrag", ParticipatedProjectsPage.this);
			Form<?> form = new Form<Void>("form");
			form.add(new NotificationPanel("feedback", form));
			
			TeamsEditBean bean = new TeamsEditBean();
			for (Membership membership: getUser().getMemberships()) {
				if (membership.getTeam().getProject().equals(getProject()))
					bean.getTeams().add(membership.getTeam().getName());
			}
			Collections.sort(bean.getTeams());
			
			form.add(BeanContext.editBean("editor", bean));
			
			form.add(new AjaxButton("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					if (bean.getTeams().isEmpty()) {
						error("At least one team needs to be selected");
						target.add(form);
					} else {
						Collection<Team> teams = new ArrayList<>();
						for (String teamName: bean.getTeams())
							teams.add(OneDev.getInstance(TeamManager.class).find(getProject(), teamName));
						OneDev.getInstance(MembershipManager.class).assignTeams(getUser(), getProject(), teams);
						close();
						onSaved(target);
					}
				}
				
			});
			form.add(new AjaxLink<Void>("close") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					close();
				}
				
			});
			form.add(new AjaxLink<Void>("cancel") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					close();
				}
				
			});
			form.setOutputMarkupId(true);
			fragment.add(form);
			return fragment;
		}

		@Override
		public abstract Project getProject();
		
		protected abstract void onSaved(AjaxRequestTarget target);
		
	}
	
}

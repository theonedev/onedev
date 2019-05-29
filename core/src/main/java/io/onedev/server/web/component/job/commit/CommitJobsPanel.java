package io.onedev.server.web.component.job.commit;

import static io.onedev.server.search.entity.EntityQuery.quote;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;
import static io.onedev.server.util.BuildConstants.FIELD_COMMIT;
import static io.onedev.server.util.BuildConstants.FIELD_JOB;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.job.JobLink;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public class CommitJobsPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final IModel<List<Build>> buildsModel = new LoadableDetachableModel<List<Build>>() {

		@Override
		protected List<Build> load() {
			List<Build> builds = new ArrayList<>(OneDev.getInstance(BuildManager.class).query(getProject(), commitId));
			Collections.sort(builds);
			return builds;
		}
		
	};
	
	public CommitJobsPanel(String id, Project project, ObjectId commitId) {
		super(id);
		projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
	}

	private Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView jobsView = new RepeatingView("jobs");
		CISpec ciSpec = getProject().getCISpec(commitId);
		if (ciSpec != null) {
			for (Job job: ciSpec.getSortedJobs()) {
				String query = "" 
						+ quote(FIELD_COMMIT) + " " + getRuleName(Is) + " " + quote(commitId.name()) 
						+ " " + getRuleName(And) + " "
						+ quote(FIELD_JOB) + " " + getRuleName(Is) + " " + quote(job.getName());
				
				WebMarkupContainer jobContainer = new WebMarkupContainer(jobsView.newChildId());
				jobsView.add(jobContainer);
				
				Link<Void> jobLink = new JobLink("name", getProject(), commitId, job.getName());
				jobLink.add(new Label("label", job.getName()));
				jobContainer.add(jobLink);
				
				jobContainer.add(new BookmarkablePageLink<Void>("list", ProjectBuildsPage.class, 
						ProjectBuildsPage.paramsOf(getProject(), query, 0)));
				
				jobContainer.add(new AjaxLink<Void>("run") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (!job.getParamSpecs().isEmpty()) {
							Serializable paramBean;
							try {
								paramBean = JobParam.defineBeanClass(job.getParamSpecs()).newInstance();
							} catch (InstantiationException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
							
							new ParamEditModalPanel(target, paramBean) {

								@Override
								protected void onSave(AjaxRequestTarget target, Serializable bean) {
									Map<String, List<String>> paramMap = JobParam.getParamMap(
											job, bean, job.getParamSpecMap().keySet());
									Build build = OneDev.getInstance(JobManager.class).submit(getProject(), 
											commitId, job.getName(), paramMap);
									setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build, null));
								}

								@Override
								public List<String> getInputNames() {
									return new ArrayList<>(job.getParamSpecMap().keySet());
								}

								@Override
								public InputSpec getInputSpec(String inputName) {
									return Preconditions.checkNotNull(job.getParamSpecMap().get(inputName));
								}

								@Override
								public void validateName(String inputName) {
									throw new UnsupportedOperationException();
								}
								
							};
						} else {
							Build build = OneDev.getInstance(JobManager.class).submit(getProject(), commitId, 
									job.getName(), new HashMap<>());
							setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build, null));
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canWriteCode(getProject().getFacade()));
					}
					
				});
				
				IModel<List<Build>> jobBuildsModel = new LoadableDetachableModel<List<Build>>() {

					@Override
					protected List<Build> load() {
						return buildsModel.getObject()
								.stream()
								.filter(it->it.getJobName().equals(job.getName()))
								.collect(Collectors.toList());
					}
					
				};
				
				jobContainer.add(new ListView<Build>("builds", jobBuildsModel) {

					@Override
					protected void populateItem(ListItem<Build> item) {
						QueryPosition position = new QueryPosition(query, getList().size(), item.getIndex());
						Link<Void> link = new BookmarkablePageLink<Void>("build", BuildLogPage.class, 
								BuildLogPage.paramsOf(item.getModelObject(), position));
						link.add(new BuildStatusIcon("status", item.getModelObject(), false) {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("title", new AbstractReadOnlyModel<String>() {

									@Override
									public String getObject() {
										Build build = getBuild();
										StringBuilder title = new StringBuilder("Build #" + build.getNumber());
										if (build.getVersion() != null)
											title.append(" (").append(build.getVersion()).append(")");
										title.append(" is ");
										if (build.getStatus() == Status.WAITING) 
											title.append("waiting for completion of dependency builds");
										else if (build.getStatus() == Status.QUEUEING) 
											title.append("queued due to limited capacity");
										else
											title.append(build.getStatus().getDisplayName().toLowerCase());
										
										return title.append(", click for details").toString();
									}
									
								}));								
							}
							
						});
						
						item.add(link);
					}
					
				});
				
				jobContainer.add(new WebMarkupContainer("noBuilds") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(jobBuildsModel.getObject().isEmpty());
					}
					
				});
			}
		}
		add(jobsView);
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet("commit-builds:" + getProject().getId() + ":" + commitId.name());
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CommitJobsResourceReference()));
		String script = String.format("onedev.server.commitJobs.onDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		CISpec ciSpec = getProject().getCISpec(commitId);
		setVisible(ciSpec != null && !ciSpec.getJobs().isEmpty());
	}

	@Override
	protected void onAfterRender() {
		OneDev.getInstance(WebSocketManager.class).notifyObserverChange((BasePage) getPage());
		super.onAfterRender();
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		buildsModel.detach();
		super.onDetach();
	}

	private abstract class ParamEditModalPanel extends BeanEditModalPanel implements InputContext {

		public ParamEditModalPanel(AjaxRequestTarget target, Serializable bean) {
			super(target, bean);
		}

	}
}

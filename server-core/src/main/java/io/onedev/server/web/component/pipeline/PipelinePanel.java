package io.onedev.server.web.component.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;

public abstract class PipelinePanel extends Panel {

	private final IModel<List<List<Job>>> pipelineModel = new LoadableDetachableModel<List<List<Job>>>() {

		@Override
		protected List<List<Job>> load() {
			if (!getJobs().isEmpty()) {
				return buildPipeline(new ArrayList<>(getJobs()));
			} else {
				// add an empty column as job add action is rendered in first column
				List<List<Job>> pipeline = new ArrayList<>();
				pipeline.add(new ArrayList<>());
				return pipeline;
			}
		}
		
	};
	
	public PipelinePanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		pipelineModel.detach();
		super.onDetach();
	}
	
	private List<List<Job>> getPipeline() {
		return pipelineModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Sortable sortable = getSortable();
		if (sortable != null) { 
			String startScript = "onedev.server.pipeline.onSortStart(ui.item);";
			String stopScript = ""
					+ "if (ui.item.fromList == ui.item.toList && ui.item.fromItem == ui.item.toItem)"
					+ "  onedev.server.pipeline.onSortStop(ui.item);";
			add(new SortBehavior() {

				@Override
				protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
					int fromIndex = getJobs().indexOf(new JobIndex(from.getListIndex(), from.getItemIndex()).getJob(getPipeline()));
					int toIndex = getJobs().indexOf(new JobIndex(to.getListIndex(), to.getItemIndex()).getJob(getPipeline()));
					pipelineModel.detach();
					sortable.onSort(target, fromIndex, toIndex);
				}
				
			}.sortable(".pipeline>.pipeline-column")
					.startScript(startScript)
					.stopScript(stopScript)
					.items(".pipeline-row:not(.pipeline-action)"));
		}
	}

	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		
		RepeatingView columnsView = new RepeatingView("columns");
		int columnIndex = 0;
		for (List<Job> column: getPipeline()) {
			WebMarkupContainer columnContainer = new WebMarkupContainer(columnsView.newChildId());
			RepeatingView rowsView = new RepeatingView("rows");
			for (Job job: column) 
				rowsView.add(renderJob(rowsView.newChildId(), getJobs().indexOf(job)));
			if (columnIndex == 0) {
				Component action = renderAction(rowsView.newChildId());
				if (action != null) {
					action.add(AttributeAppender.append("class", "pipeline-action"));
					rowsView.add(action);
				}
			}
			columnContainer.add(rowsView);
			columnsView.add(columnContainer);
			columnIndex++;
		}
		addOrReplace(columnsView);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		if (event.getPayload() instanceof JobSelectionChange) {
			JobSelectionChange jobSelectionChange = (JobSelectionChange) event.getPayload();
			
			String script = String.format("onedev.server.pipeline.markJobActive($(\"#%s>.pipeline\"), %s);", 
					getMarkupId(), JobIndex.of(getPipeline(), jobSelectionChange.getJob()).toJson());
			jobSelectionChange.getHandler().appendJavaScript(script);
			
			event.stop();
		}
	}

	private List<List<Job>> buildPipeline(List<Job> jobs) {
		List<List<Job>> pipeline = new ArrayList<>();
		List<Job> leafJobs = new ArrayList<>();
		Set<String> jobNames = jobs.stream().map(it->it.getName()).collect(Collectors.toSet()); 
		for (Iterator<Job> itJob = jobs.iterator(); itJob.hasNext();) {
			Job job = itJob.next();
			if (!job.getJobDependencies().stream().anyMatch(it->jobNames.contains(it.getJobName()))) {
				itJob.remove();
				leafJobs.add(job);
			}
		}
		
		if (leafJobs.isEmpty()) 
			leafJobs.add(jobs.remove(0));
		pipeline.add(leafJobs);
		
		if (!jobs.isEmpty())
			pipeline.addAll(buildPipeline(jobs));
		
		return pipeline;
	}
	
	private String buildDependencyMap() {
		Map<String, List<String>> dependencyMap = new HashMap<>();
		
		Map<String, JobIndex> jobIndexMap = new HashMap<>();
		
		int columnIndex = 0;
		for (List<Job> column: getPipeline()) {
			int rowIndex = 0;
			for (Job job: column) {
				jobIndexMap.put(job.getName(), new JobIndex(columnIndex, rowIndex));
				rowIndex++;
			}
			columnIndex++;
		}
		
		for (List<Job> column: getPipeline()) {
			for (Job job: column) {
				JobIndex jobIndex = jobIndexMap.get(job.getName());
				if (jobIndex != null) {
					List<String> dependencyJobIndexStrings = new ArrayList<>();
					for (JobDependency dependency: job.getJobDependencies()) {
						JobIndex dependencyJobIndex = jobIndexMap.get(dependency.getJobName());
						if (dependencyJobIndex != null && dependencyJobIndex.getColumn() < jobIndex.getColumn())
							dependencyJobIndexStrings.add(dependencyJobIndex.toString());
					}
					dependencyMap.put(jobIndex.toString(), dependencyJobIndexStrings);
				}
			}
		}
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(dependencyMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new PipelineResourceReference()));

		String activePipelineJobIndex;
		int activeJobIndex = getActiveJobIndex();
		if (activeJobIndex != -1)
			activePipelineJobIndex = JobIndex.of(getPipeline(), getJobs().get(activeJobIndex)).toJson();
		else
			activePipelineJobIndex = "undefined";
		
		// Run script via OnLoad in order for icons to be fully loaded before drawing 
		// dependency line
		String script = String.format("onedev.server.pipeline.onWindowLoad('%s', %s, %s);", 
				getMarkupId(), buildDependencyMap(), activePipelineJobIndex);
		response.render(OnLoadHeaderItem.forScript(script));
	}
	
	protected abstract List<Job> getJobs();
	
	protected abstract int getActiveJobIndex();

	protected abstract Component renderJob(String componentId, int jobIndex);
	
	@Nullable
	protected Component renderAction(String componentId) {
		return null;
	}
	
	@Nullable
	protected Sortable getSortable() {
		return null;
	}
}

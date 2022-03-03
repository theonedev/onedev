package io.onedev.server.web.component.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;

@SuppressWarnings("serial")
public abstract class PipelinePanel extends Panel {

	private final List<List<Job>> pipeline;
	
	private final Job activeJob;
	
	public PipelinePanel(String id, List<Job> jobs, @Nullable Job activeJob) {
		super(id);
		pipeline = buildPipeline(jobs);
		this.activeJob = activeJob;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepeatingView columnsView = new RepeatingView("columns");
		for (List<Job> column: pipeline) {
			WebMarkupContainer columnContainer = new WebMarkupContainer(columnsView.newChildId());
			RepeatingView rowsView = new RepeatingView("rows");
			for (Job job: column) 
				rowsView.add(renderJob(rowsView.newChildId(), job));
			columnContainer.add(rowsView);
			columnsView.add(columnContainer);
		}
		add(columnsView);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		if (event.getPayload() instanceof JobSelectionChange) {
			JobSelectionChange jobSelectionChange = (JobSelectionChange) event.getPayload();
			
			JobIndex activeJobIndex = JobIndex.of(pipeline, jobSelectionChange.getJob());
			String script = String.format("onedev.server.pipeline.markJobActive($(\"#%s>.pipeline\"), %s);", 
					getMarkupId(), activeJobIndex!=null?activeJobIndex.toJson():"undefined");
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
		Preconditions.checkState(!leafJobs.isEmpty());
		pipeline.add(leafJobs);
		
		if (!jobs.isEmpty())
			pipeline.addAll(buildPipeline(jobs));
		
		return pipeline;
	}
	
	private String buildDependencyMap() {
		Map<String, List<String>> dependencyMap = new HashMap<>();
		
		Map<String, String> jobIndexMap = new HashMap<>();
		
		int columnIndex = 0;
		for (List<Job> column: pipeline) {
			int rowIndex = 0;
			for (Job job: column) {
				jobIndexMap.put(job.getName(), new JobIndex(columnIndex, rowIndex).toString());
				rowIndex++;
			}
			columnIndex++;
		}
		
		for (List<Job> column: pipeline) {
			for (Job job: column) {
				dependencyMap.put(
						jobIndexMap.get(job.getName()), 
						job.getJobDependencies().stream().map(it->jobIndexMap.get(it.getJobName())).collect(Collectors.toList())); 
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
		
		JobIndex activeJobIndex = JobIndex.of(pipeline, activeJob);
		String script = String.format("onedev.server.pipeline.onDomReady('%s', %s, %s);", 
				getMarkupId(), buildDependencyMap(), activeJobIndex!=null?activeJobIndex.toJson():"undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component renderJob(String componentId, Job job);
	
}

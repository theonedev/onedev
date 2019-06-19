package io.onedev.server.web.component.taskbutton;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.joda.time.DateTime;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.commons.utils.schedule.SchedulableTask;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.OneDev;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class TaskButton extends AjaxButton {

	private static final Logger logger = LoggerFactory.getLogger(TaskButton.class);
	
	public TaskButton(String id) {
		super(id);
	}

	@Override
	protected void onError(AjaxRequestTarget target, Form<?> form) {
		target.add(form);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupPlaceholderTag(true);
	}

	private Map<String, TaskFuture> getTaskFutures() {
		return TaskFutureManager.taskFutures;
	}
	
	protected void submitTask(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("$('#%s').closest('form').children('.task-feedback').remove();", getMarkupId()));
		String path = getPath();
		
		ExecutorService executorService = OneDev.getInstance(ExecutorService.class);
		TaskFuture future = getTaskFutures().put(path, new TaskFuture(executorService.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				return runTask();
			}
			
		})));
		if (future != null && !future.isDone())
			future.cancel(true);
		
		new ModalPanel(target) {
			
			@Override
			protected Component newContent(String id) {
				String message = WordUtils.uncamel(TaskButton.this.getId()).toLowerCase();
				return new TaskWaitPanel(id, StringUtils.capitalize(message) + " in progress...") {

					@Override
					protected void check(AjaxRequestTarget target) {
						Future<String> future = getTaskFutures().get(path);
						if (future != null) {
							if (future.isDone()) {
								if (!future.isCancelled()) {
									String feedback;
									try {
										feedback = String.format(
											"<div class='task-feedback alert alert-success'>%s</div>", 
											HtmlEscape.escapeHtml5(future.get()));					
									} catch (Exception e) {
										logger.error("Error " + message, e);
										String suggestedSolution = ExceptionUtils.suggestSolution(e);
										if (suggestedSolution != null)
											logger.warn("!!! " + suggestedSolution);
										feedback = "Error " + message;
										if (e.getMessage() != null)
											feedback += ": " + e.getMessage();
										feedback += ", check server log for details.";
										feedback = String.format(
												"<div class='task-feedback alert alert-danger'>%s</div>", 
												HtmlEscape.escapeHtml5(feedback));					
									} 
									feedback = StringUtils.replace(feedback, "\n", "<br>");
									target.appendJavaScript(String.format(""
											+ "var $form = $('#%s').closest('form');"
											+ "$form.append('%s');"
											+ "$form.children('.task-feedback')[0].scrollIntoView({behavior: 'smooth', block: 'center'})",
											TaskButton.this.getMarkupId(), JavaScriptEscape.escapeJavaScript(feedback)));
								}
								close();
							} 
						} else {
							close();
						}
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Future<?> future = getTaskFutures().remove(path);
						if (future != null && !future.isDone())
							future.cancel(true);
					}

				};
			}
			
		};
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TaskButtonCssResourceReference()));
	}

	@Override
	protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		super.onSubmit(target, form);
		target.add(form);
		target.focusComponent(null);
		submitTask(target);
	}
	
	protected abstract String runTask();

	@Singleton
	public static class TaskFutureManager implements SchedulableTask {

		private static final Map<String, TaskFuture> taskFutures = new ConcurrentHashMap<>();
		
		private final TaskScheduler taskScheduler;
		
		private String taskId;
		
		@Inject
		public TaskFutureManager(TaskScheduler taskScheduler) {
			this.taskScheduler = taskScheduler;
		}
		
		@Listen
		public void on(SystemStarted event) {
			taskId = taskScheduler.schedule(this);
		}
		
		@Listen
		public void on(SystemStopping event) {
			taskScheduler.unschedule(taskId);
		}

		@Override
		public void execute() {
			for (Iterator<Map.Entry<String, TaskFuture>> it = taskFutures.entrySet().iterator(); it.hasNext();) {
				TaskFuture taskFuture = it.next().getValue();
				if (taskFuture.isTimedout()) {
					if (!taskFuture.isDone())
						taskFuture.cancel(true);
					it.remove();
				}
			}
		}

		@Override
		public ScheduleBuilder<?> getScheduleBuilder() {
			return SimpleScheduleBuilder.repeatHourlyForever();
		}
		
	}
	
	private static class TaskFuture implements Future<String> {

		private final Date timestamp = new Date();
		
		private final Future<String> wrapped;
		
		public TaskFuture(Future<String> wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return wrapped.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return wrapped.isCancelled();
		}

		@Override
		public boolean isDone() {
			return wrapped.isDone();
		}
		
		public boolean isTimedout() {
			return timestamp.before(new DateTime().minusHours(1).toDate());
		}

		@Override
		public String get() throws InterruptedException, ExecutionException {
			return wrapped.get();
		}

		@Override
		public String get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return get(timeout, unit);
		}
		
	}
}

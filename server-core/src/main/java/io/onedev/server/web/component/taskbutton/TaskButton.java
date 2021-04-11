package io.onedev.server.web.component.taskbutton;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import org.apache.wicket.markup.html.form.Form;
import org.joda.time.DateTime;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
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
		String path = getPath();
		String title = WordUtils.uncamel(getId()).toLowerCase();
		
		ExecutorService executorService = OneDev.getInstance(ExecutorService.class);
		List<String> messages = Lists.newArrayList("Please wait...");
		TaskFuture future = getTaskFutures().put(path, new TaskFuture(executorService.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				String result;
				try {
					result = String.format(
						"<div class='task-result text-break alert alert-light-success'>%s</div>", 
						HtmlEscape.escapeHtml5(runTask(new SimpleLogger() {

							@Override
							public void log(String message) {
								synchronized (messages) {
									messages.add(message);
								}
							}
							
						})));					
				} catch (Exception e) {	
					logger.error("Error " + title, e);
					String suggestedSolution = ExceptionUtils.suggestSolution(e);
					if (suggestedSolution != null)
						logger.warn("!!! " + suggestedSolution);
					if (e.getMessage() != null)
						result = e.getMessage();
					else
						result = "Error " + title;
					result = String.format(
							"<div class='task-result text-break alert alert-light-danger'>%s</div>", 
							HtmlEscape.escapeHtml5(result));					
				} 
				result = StringUtils.replace(result, "\n", "<br>");
				return result;
			}
			
		}), messages));
		
		if (future != null && !future.isDone())
			future.cancel(true);
		
		new ModalPanel(target) {
			
			@Override
			protected void onClosed() {
				super.onClosed();
				Future<?> future = getTaskFutures().remove(path);
				if (future != null && !future.isDone())
					future.cancel(true);
			}

			@Override
			protected Component newContent(String id) {
				return new TaskFeedbackPanel(id, StringUtils.capitalize(title)) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						close();
					}

					@Override
					protected List<String> getMessages() {
						TaskFuture future = getTaskFutures().get(path);
						if (future != null) 
							return future.getMessages();
						else
							return new ArrayList<>();
					}

					@Override
					protected String getResult() {
						TaskFuture future = getTaskFutures().get(path);
						if (future != null && future.isDone() && !future.isCancelled()) { 
							try {
								return future.get();
							} catch (InterruptedException | ExecutionException e) {
								throw new RuntimeException(e);
							}
						} else {
							return null;
						}
					}

				};
			}
			
		};
	}
	
	@Override
	protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		super.onSubmit(target, form);
		target.focusComponent(null);
		submitTask(target);
	}
	
	protected abstract String runTask(SimpleLogger logger);

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
		
		private final List<String> messages;
		
		public TaskFuture(Future<String> wrapped, List<String> messages) {
			this.wrapped = wrapped;
			this.messages = messages;
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
		
		public List<String> getMessages() {
			synchronized (messages) {
				return new ArrayList<>(messages);
			}
		}
		
	}
}

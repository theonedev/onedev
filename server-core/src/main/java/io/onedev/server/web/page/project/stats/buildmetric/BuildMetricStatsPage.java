package io.onedev.server.web.page.project.stats.buildmetric;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.Day;
import io.onedev.server.util.MetricIndicator;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.BuildMetricQueryBehavior;
import io.onedev.server.web.component.chart.line.LineChartPanel;
import io.onedev.server.web.component.chart.line.LineSeries;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class BuildMetricStatsPage<T extends AbstractEntity> extends ProjectPage {

	private static final String PARAM_QUERY = "query";
	
	private final Class<T> metricClass;
	
	private String query;
	
	private BuildMetricQuery parsedQuery;
	
	private Form<?> form;
	
	private Component feedback;
	
	private WebMarkupContainer content;
	
	@SuppressWarnings("unchecked")
	public BuildMetricStatsPage(PageParameters params) {
		super(params);
		
		metricClass = (Class<T>) ReflectionUtils.getTypeArguments(BuildMetricStatsPage.class, getClass()).get(0);
		
		query = params.get(PARAM_QUERY).toOptionalString();
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(getClass(), paramsOf(getProject(), query));
		pushState(target, url.toString(), query);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		query = (String) data;
		parseQuery();
		target.add(form);
		target.add(content);
	}
	
	private void parseQuery() {
		try {
			parsedQuery = BuildMetricQuery.parse(getProject(), query);
		} catch (Exception e) {
			parsedQuery = null;
			form.error("Malformed metric query");
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form = new Form<Void>("form");
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "query"));
		input.add(new BuildMetricQueryBehavior(projectModel, metricClass));
		form.add(input);
		
		input.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				pushState(target);
				parseQuery();
				target.add(feedback);
				target.add(content);
			}
			
		});
		
		form.add(feedback = new FencedFeedbackPanel("feedback", form));
		feedback.setOutputMarkupPlaceholderTag(true);

		form.add(new AjaxButton("submit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(this));
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				pushState(target);
				parseQuery();
				target.add(feedback);
				target.add(content);
			}
			
		});
		
		add(form);
		
		parseQuery();
		
		add(content = new WebMarkupContainer("content"));
		content.setOutputMarkupId(true);
		
		content.add(new ListView<LineSeries>("charts", new LoadableDetachableModel<List<LineSeries>>() {

			@Override
			protected List<LineSeries> load() {
				if (parsedQuery != null) {
					List<Pair<LineSeries, Integer>> serieses = new ArrayList<>();
					Map<Integer, T> stats = OneDev.getInstance(BuildMetricManager.class).queryStats(getProject(), metricClass, parsedQuery);
					for (Method getter: BeanUtils.findGetters(metricClass)) {
						MetricIndicator indicator = getter.getAnnotation(MetricIndicator.class);
						if (indicator != null) {
							Integer maxValue = indicator.maxValue();
							if (maxValue == Integer.MIN_VALUE)
								maxValue = null;
							Integer minValue = indicator.minValue();
							if (minValue == Integer.MAX_VALUE)
								minValue = null;
							String name = indicator.name();
							if (name.length() == 0) 
								name = BeanUtils.getDisplayName(getter);
							
							Map<Integer, Integer> discreteValues = new HashMap<>();
							for (Map.Entry<Integer, T> entry: stats.entrySet()) {
								try {
									discreteValues.put(entry.getKey(), (int) getter.invoke(entry.getValue()));
								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									throw new RuntimeException(e);
								}
							}
							Map<String, Integer> completeValues = new LinkedHashMap<>();
							if (!discreteValues.isEmpty()) {
								int minDayValue = Collections.min(discreteValues.keySet());
								int maxDayValue = Collections.max(discreteValues.keySet());
								int lastValue = 0;
								int currentDayValue = minDayValue;
								while (currentDayValue<=maxDayValue) {
									Integer currentValue = discreteValues.get(currentDayValue);
									if (currentValue == null)
										currentValue = lastValue;
									else 
										lastValue = currentValue;
									Day currentDay = new Day(currentDayValue);
									String currentDayLabel = String.format("%s-%s-%s", 
											currentDay.getYear()%100, currentDay.getMonthOfYear()+1, currentDay.getDayOfMonth());
									completeValues.put(currentDayLabel, currentValue);
									currentDayValue = new Day(currentDay.getDate().plusDays(1)).getValue();
								}
							} 
							String valueFormatter = indicator.valueFormatter();
							if (valueFormatter.length() == 0)
								valueFormatter = null;
							LineSeries series = new LineSeries(name, completeValues, valueFormatter, minValue, maxValue);
							serieses.add(new Pair<>(series, indicator.order()));
						}
					}
					Collections.sort(serieses, new Comparator<Pair<LineSeries, Integer>>() {

						@Override
						public int compare(Pair<LineSeries, Integer> o1, Pair<LineSeries, Integer> o2) {
							return o1.getSecond() - o2.getSecond();
						}
						
					});
					return serieses.stream().map(it->it.getFirst()).collect(Collectors.toList());
				} else {
					return new ArrayList<>();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<LineSeries> item) {
				item.add(new LineChartPanel("chart", item.getModel()));
			}
			
		});
	}

	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}

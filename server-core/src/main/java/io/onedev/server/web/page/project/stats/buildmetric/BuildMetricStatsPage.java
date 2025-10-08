package io.onedev.server.web.page.project.stats.buildmetric;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.service.BuildMetricService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.MetricIndicator;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.BuildMetricQueryBehavior;
import io.onedev.server.web.component.chart.line.Line;
import io.onedev.server.web.component.chart.line.LineChartPanel;
import io.onedev.server.web.component.chart.line.LineSeries;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.onedev.server.web.translation.Translation._T;
import static io.onedev.server.web.util.StatsGroup.BY_DAY;
import static java.util.Comparator.comparingInt;

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
			form.error(_T("Malformed query"));
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
		
		content.add(new ListView<>("charts", new LoadableDetachableModel<List<LineSeries>>() {

			@Override
			protected List<LineSeries> load() {
				List<LineSeries> serieses = new ArrayList<>();
				if (parsedQuery != null) {
					List<Pair<Method, Integer>> metricGetters = new ArrayList<>();
					for (Method getter : BeanUtils.findGetters(metricClass)) {
						MetricIndicator indicator = getter.getAnnotation(MetricIndicator.class);
						if (indicator != null)
							metricGetters.add(new Pair<>(getter, indicator.order()));
					}
					metricGetters.sort(comparingInt(Pair::getRight));

					List<List<Method>> groupedMetricGetters = new ArrayList<>();
					metricGetters.stream().map(it -> it.getLeft()).forEach(it -> {
						MetricIndicator indicator = Preconditions.checkNotNull(it.getAnnotation(MetricIndicator.class));
						if (indicator.group().length() != 0) {
							List<Method> metricGettersOfGroup = null;
							for (List<Method> each : groupedMetricGetters) {
								if (each.get(0).getAnnotation(MetricIndicator.class).group().equals(indicator.group())) {
									metricGettersOfGroup = each;
									break;
								}
							}
							if (metricGettersOfGroup == null) {
								metricGettersOfGroup = new ArrayList<>();
								groupedMetricGetters.add(metricGettersOfGroup);
							}
							metricGettersOfGroup.add(it);
						} else {
							groupedMetricGetters.add(Lists.newArrayList(it));
						}
					});

					Map<Integer, T> stats = OneDev.getInstance(BuildMetricService.class)
							.queryStats(getProject(), metricClass, parsedQuery);

					for (List<Method> group : groupedMetricGetters) {
						MetricIndicator indicator = group.get(0).getAnnotation(MetricIndicator.class);

						String groupName = indicator.group();
						if (groupName.length() == 0)
							groupName = null;

						Integer maxValue = indicator.maxValue();
						if (maxValue == Integer.MIN_VALUE)
							maxValue = null;

						Integer minValue = indicator.minValue();
						if (minValue == Integer.MAX_VALUE)
							minValue = null;

						String valueFormatter = indicator.valueFormatter();
						if (valueFormatter.length() == 0)
							valueFormatter = null;

						Map<Integer, List<Integer>> data = new HashMap<>();
						for (Map.Entry<Integer, T> entry : stats.entrySet()) {
							List<Integer> lineValues = new ArrayList<>();
							for (Method getter : group) {
								try {
									lineValues.add((int) getter.invoke(entry.getValue()));
								} catch (IllegalAccessException | IllegalArgumentException |
										 InvocationTargetException e) {
									throw new RuntimeException(e);
								}
							}
							data.put(entry.getKey(), lineValues);
						}

						var normalizedData = BY_DAY.normalizeData(data, null);
						List<String> xAxisValues = normalizedData.stream()
								.map(org.apache.commons.lang3.tuple.Pair::getLeft)
								.collect(Collectors.toList());
						List<Line> lines = new ArrayList<>();

						int lineIndex = 0;
						for (Method getter : group) {
							indicator = getter.getAnnotation(MetricIndicator.class);
							String name = indicator.name();
							if (name.length() == 0)
								name = BeanUtils.getDisplayName(getter);
							var finalLineIndex = lineIndex;
							var yAxisValues = normalizedData.stream()
									.map(it->it.getRight().get(finalLineIndex))
									.collect(Collectors.toList());
							lines.add(new Line(_T(name), yAxisValues, indicator.color(), null, null));
							lineIndex++;
						}

						serieses.add(new LineSeries(_T(groupName), xAxisValues, lines, valueFormatter, minValue, maxValue));
					}
				}
				return serieses;
			}

		}) {

			@Override
			protected void populateItem(ListItem<LineSeries> item) {
				item.add(new LineChartPanel("chart", item.getModel()));
			}

		});
	}

	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = ProjectPage.paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
	public static PageParameters paramsOf(Project project) {
		String query = String.format("%s \"last month\"", BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since));
		return paramsOf(project, query);
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (!OneDev.getInstance(BuildMetricService.class).getAccessibleReportNames(project, metricClass).isEmpty())
			return new ViewStateAwarePageLink<>(componentId, getPageClass(), paramsOf(project));
		else 
			return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}

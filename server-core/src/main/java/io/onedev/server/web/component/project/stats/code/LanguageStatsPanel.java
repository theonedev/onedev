package io.onedev.server.web.component.project.stats.code;

import static io.onedev.server.web.translation.Translation._T;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.xodus.CommitInfoService;

public class LanguageStatsPanel extends GenericPanel<Project> {

	private static final Map<String, String> LANGUAGE_COLORS = new HashMap<>();
	
	static {
		LANGUAGE_COLORS.put("Java", "#b07219");
		LANGUAGE_COLORS.put("JavaScript", "#f1e05a");
		LANGUAGE_COLORS.put("TypeScript", "#3178c6");
		LANGUAGE_COLORS.put("TypeScript-JSX", "#3178c6");
		LANGUAGE_COLORS.put("JSX", "#f1e05a");
		LANGUAGE_COLORS.put("Python", "#3572A5");
		LANGUAGE_COLORS.put("Go", "#00ADD8");
		LANGUAGE_COLORS.put("Rust", "#dea584");
		LANGUAGE_COLORS.put("C", "#555555");
		LANGUAGE_COLORS.put("C++", "#f34b7d");
		LANGUAGE_COLORS.put("CSharp", "#178600");
		LANGUAGE_COLORS.put("Ruby", "#701516");
		LANGUAGE_COLORS.put("PHP", "#4F5D95");
		LANGUAGE_COLORS.put("CSS", "#563d7c");
		LANGUAGE_COLORS.put("HTML", "#e34c26");
		LANGUAGE_COLORS.put("Shell", "#89e051");
		LANGUAGE_COLORS.put("Kotlin", "#A97BFF");
		LANGUAGE_COLORS.put("Swift", "#F05138");
		LANGUAGE_COLORS.put("Markdown", "#083fa1");
		LANGUAGE_COLORS.put("Vue.js Component", "#41b883");
		LANGUAGE_COLORS.put("JSON", "#292929");
		LANGUAGE_COLORS.put("Yaml", "#cb171e");
		LANGUAGE_COLORS.put("Scala", "#c22d40");
		LANGUAGE_COLORS.put("Dart", "#00B4AB");
		LANGUAGE_COLORS.put("Objective-C", "#438eff");
		LANGUAGE_COLORS.put("Perl", "#0298c3");
		LANGUAGE_COLORS.put("R", "#198CE7");
		LANGUAGE_COLORS.put("Haskell", "#5e5086");
		LANGUAGE_COLORS.put("Lua", "#000080");
		LANGUAGE_COLORS.put("Groovy", "#4298b8");
		LANGUAGE_COLORS.put("Clojure", "#db5855");
		LANGUAGE_COLORS.put("Erlang", "#B83998");
		LANGUAGE_COLORS.put("SQL", "#e38c00");
		LANGUAGE_COLORS.put("PowerShell", "#012456");
		LANGUAGE_COLORS.put("XML", "#0060ac");
		LANGUAGE_COLORS.put("LESS", "#1d365d");
		LANGUAGE_COLORS.put("Sass", "#a53b70");
		LANGUAGE_COLORS.put("Scss", "#c6538c");
		LANGUAGE_COLORS.put("CoffeeScript", "#244776");
		LANGUAGE_COLORS.put("Fortran", "#4d41b1");
		LANGUAGE_COLORS.put("Pascal", "#E3F171");
		LANGUAGE_COLORS.put("ProtoBuf", "#e6871e");
		LANGUAGE_COLORS.put("ASP.NET", "#9400ff");
		LANGUAGE_COLORS.put("Java Server Pages", "#2A6286");
	}

	private static final String[] FALLBACK_COLORS = {
			"#6e7781", "#9558b2", "#cf222e", "#116329", "#bf8700", "#0550ae", "#a40e26", "#1a7f37"
	};

	private final IModel<List<Map.Entry<String, Integer>>> languagesModel =
			new LoadableDetachableModel<>() {

				@Override
				protected List<Map.Entry<String, Integer>> load() {
					Map<String, Integer> lineStats = OneDev.getInstance(CommitInfoService.class)
							.getLineStats(getProject().getId());
					List<Map.Entry<String, Integer>> languages = new ArrayList<>(lineStats.entrySet());
					languages.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
							.thenComparing(Map.Entry.comparingByKey()));
					return languages;
				}

			};

	public LanguageStatsPanel(String id, IModel<Project> projectModel) {
		super(id, projectModel);
	}

	private Project getProject() {
		return getModelObject();
	}

	private static String getColor(String language) {
		String color = LANGUAGE_COLORS.get(language);
		if (color != null)
			return color;
		return FALLBACK_COLORS[Math.floorMod(language.hashCode(), FALLBACK_COLORS.length)];
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView barsView = new RepeatingView("bars");
		add(barsView);

		add(new ListView<>("languages", languagesModel) {

			@Override
			protected void populateItem(ListItem<Map.Entry<String, Integer>> item) {
				Map.Entry<String, Integer> entry = item.getModelObject();
				String color = getColor(entry.getKey());
				item.add(new Label("dot", "").add(AttributeAppender.append("style",
						"background-color: " + color)));
				item.add(new Label("name", entry.getKey()));

				long total = languagesModel.getObject().stream().mapToLong(Map.Entry::getValue).sum();
				double percent = total == 0 ? 0 : entry.getValue() * 100.0 / total;
				String percentLabel = NumberFormat.getNumberInstance(Locale.US).format(
						Math.round(percent * 10) / 10.0) + "%";
				item.add(new Label("percent", percentLabel));
				item.add(AttributeAppender.append("title",
						entry.getKey() + ": "
								+ NumberFormat.getIntegerInstance().format(entry.getValue())
								+ " " + _T("lines") + " (" + percentLabel + ")"));
			}

		});
	}

	@Override
	protected void onBeforeRender() {
		RepeatingView barsView = (RepeatingView) get("bars");
		barsView.removeAll();

		List<Map.Entry<String, Integer>> languages = languagesModel.getObject();
		long total = languages.stream().mapToLong(Map.Entry::getValue).sum();
		if (total != 0) {
			for (Map.Entry<String, Integer> entry : languages) {
				Label bar = new Label(barsView.newChildId(), "");
				bar.add(AttributeAppender.append("style",
						"background-color: " + getColor(entry.getKey()) + ";"
								+ "width: " + (entry.getValue() * 100.0 / total) + "%;"));
				bar.add(AttributeAppender.append("title",
						entry.getKey() + ": "
								+ NumberFormat.getIntegerInstance().format(entry.getValue())
								+ " " + _T("lines")));
				barsView.add(bar);
			}
		}
		super.onBeforeRender();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!languagesModel.getObject().isEmpty());
	}

	@Override
	protected void onDetach() {
		languagesModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new LanguageStatsCssResourceReference()));
	}

}

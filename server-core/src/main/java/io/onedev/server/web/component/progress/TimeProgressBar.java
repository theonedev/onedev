package io.onedev.server.web.component.progress;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

/** A time-based estimate, advanced in the browser every second without polling. */
public class TimeProgressBar extends Panel {

	private static final long serialVersionUID = 1L;

	private final double estimatedDuration;
	private double elapsed;
	private final String color;
	private final boolean roundCorner;

	/**
	 * @param estimatedDuration estimated duration in seconds; non-positive values hide the bar
	 * @param color CSS color of the fill
	 * @param roundCorner whether to round the track's corners
	 */
	public TimeProgressBar(String id, double estimatedDuration, String color, boolean roundCorner) {
		super(id);
		this.estimatedDuration = estimatedDuration;
		this.color = color;
		this.roundCorner = roundCorner;
		setOutputMarkupId(true);
	}

	/** Set the elapsed time in seconds for the next render. */
	public TimeProgressBar setElapsed(double elapsed) {
		this.elapsed = Math.max(0, elapsed);
		return this;
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.append("class", "time-progress" + (roundCorner ? " time-progress-rounded" : ""), " ");
		tag.append("style", "--time-progress-color: " + color, ";");
		tag.put("role", "progressbar");
		tag.put("aria-valuemin", "0");
		tag.put("aria-valuemax", "100");
		tag.put("aria-valuenow", estimatedDuration > 0
				? String.valueOf(Math.min(99, Math.round(elapsed * 100 / estimatedDuration))) : "0");
		if (tag.getAttribute("aria-label") == null)
			tag.put("aria-label", _T("Estimated progress"));
		tag.put("data-estimated-duration", String.valueOf(estimatedDuration));
		tag.put("data-elapsed", String.valueOf(elapsed));
		if (estimatedDuration <= 0)
			tag.put("hidden", "hidden");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TimeProgressResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(
				"onedev.server.timeProgress.onDomReady('" + getMarkupId() + "');"));
	}
}

package io.onedev.server.web.component.entity.labels;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.web.component.LabelBadge;

public class EntityLabelsPanel<T extends EntityLabel> extends Panel {

	private final IModel<? extends LabelSupport<T>> model;
	
	public EntityLabelsPanel(String id, IModel<? extends LabelSupport<T>> model) {
		super(id);
		this.model = model;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<LabelSpec>("labels", new LoadableDetachableModel<List<LabelSpec>>() {

			@Override
			protected List<LabelSpec> load() {
				return model.getObject().getLabels().stream()
						.map(it->it.getSpec())
						.sorted(Comparator.comparing(LabelSpec::getName))
						.collect(Collectors.toList());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<LabelSpec> item) {
 				item.add(new LabelBadge("label", item.getModel()));
			}
			
		});
		add(AttributeAppender.append("class", "d-inline-flex align-items-center flex-wrap row-gap-1"));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!model.getObject().getLabels().isEmpty());
	}

	@Override
	protected void onDetach() {
		model.detach();
		super.onDetach();
	}

}

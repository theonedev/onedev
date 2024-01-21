package io.onedev.server.web.component.stringchoice;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.util.*;

@SuppressWarnings("serial")
public class StringRadioChoice extends FormComponentPanel<String> {
	
	private final IModel<Map<String, String>> choicesModel;
	
	private RadioGroup<String> radioGroup;
	
	private Collection<Behavior> behaviors = new ArrayList<>();
	
	public StringRadioChoice(String id, IModel<String> selectionModel,
                             IModel<Map<String, String>> choicesModel) {
		super(id, selectionModel);
		this.choicesModel = choicesModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		radioGroup = new RadioGroup<>("radioGroup", Model.of(getModelObject())) {
			@Override
			protected boolean wantOnSelectionChangedNotifications() {
				return StringRadioChoice.this.wantOnSelectionChangedNotifications();
			}

			@Override
			protected void onSelectionChanged(String newSelection) {
				StringRadioChoice.this.onSelectionChanged(newSelection);
			}
		};
		radioGroup.setRenderBodyOnly(false);
		radioGroup.setLabel(getLabel());
		for (var behavior: behaviors)
			radioGroup.add(behavior);
		radioGroup.add(new ListView<>("options", new LoadableDetachableModel<List<Map.Entry<String, String>>>() {
			@Override
			protected List<Map.Entry<String, String>> load() {
				return new ArrayList<>(choicesModel.getObject().entrySet());
			}
		}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<String, String>> item) {
				var entry = item.getModelObject();
				item.add(new Radio<>("option", Model.of(entry.getKey())));
				item.add(new Label("label", Model.of(entry.getValue())));
			}
		});
		add(radioGroup);
	}

	@Override
	public FormComponent<String> setLabel(IModel<String> labelModel) {
		super.setLabel(labelModel);
		if (radioGroup != null)
			radioGroup.setLabel(labelModel);
		return this;
	}
	
	@Override
	public void convertInput() {
		setConvertedInput(radioGroup.getConvertedInput());
	}

	protected boolean wantOnSelectionChangedNotifications() {
		return false;
	}

	@Override
	public Component add(Behavior... behaviors) {
		this.behaviors.addAll(Arrays.asList(behaviors));
		if (radioGroup != null)
			radioGroup.add(behaviors);
		return this;
	}

	protected void onSelectionChanged(String newSelection) {
	}
	
	@Override
	protected void onDetach() {
		choicesModel.detach();
		super.onDetach();
	}
}

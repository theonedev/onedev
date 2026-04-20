package io.onedev.server.web.editable.drawcardbeanlist;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

/**
 * Modal form for adding or editing a single item of type {@code T} that belongs to a
 * {@link DrawCardBeanListEditPanel}.
 *
 * <p>Subclasses must provide {@link #newItem()} and {@link #getTitle()}. They may optionally
 * override {@link #newEditingBean(Serializable)} / {@link #extractItem(Object)} when the
 * underlying form needs to operate on a wrapper bean (typical for polymorphic element types),
 * {@link #validate(BeanEditor, Serializable)} to add additional save-time checks, and
 * {@link #additionalFormCssClass()} to keep legacy CSS hooks attached to the form element.</p>
 */
public abstract class DrawCardBeanItemEditPanel<T extends Serializable> extends Panel {

	private static final long serialVersionUID = 1L;

	private final List<T> items;

	private final int itemIndex;

	private final EditCallback callback;

	public DrawCardBeanItemEditPanel(String id, List<T> items, int itemIndex, EditCallback callback) {
		super(id);
		this.items = items;
		this.itemIndex = itemIndex;
		this.callback = callback;
	}

	/**
	 * The full list of items the modal is operating against. Useful for save-time uniqueness
	 * checks; combine with {@link #getItemIndex()} to skip the item currently being edited.
	 */
	protected final List<T> getItems() {
		return items;
	}

	/**
	 * Index of the item being edited, or {@code -1} when adding a new item.
	 */
	protected final int getItemIndex() {
		return itemIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final T initialItem;
		if (itemIndex != -1)
			initialItem = items.get(itemIndex);
		else
			initialItem = newItem();

		final Serializable editingBean = newEditingBean(initialItem);

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}

		};

		String extraClass = additionalFormCssClass();
		if (extraClass != null && extraClass.length() != 0)
			form.add(AttributeModifier.append("class", extraClass));

		form.add(new Label("title", getTitle()));
		form.add(new FencedFeedbackPanel("feedback", form));

		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(DrawCardBeanItemEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				callback.onCancel(target);
			}

		});

		final BeanEditor editor = BeanContext.edit("editor", editingBean);
		form.add(editor);

		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				T item = extractItem(editingBean);
				validateItem(editor, item);
				if (editor.isValid()) {
					if (itemIndex != -1)
						items.set(itemIndex, item);
					else
						items.add(item);
					callback.onSave(target);
				} else {
					target.add(form);
				}
			}

		});

		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(DrawCardBeanItemEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				callback.onCancel(target);
			}

		});
		form.setOutputMarkupId(true);

		add(form);
	}

	/**
	 * Create a new item to seed the editor when no existing item is being edited.
	 */
	protected abstract T newItem();

	/**
	 * Title shown in the modal header. Will be passed through translation.
	 */
	protected abstract String getTitle();

	/**
	 * Returns the bean that is bound to the form's {@link BeanEditor}. Default returns the
	 * item itself; polymorphic subclasses typically wrap the item in a bean exposing the
	 * polymorphic field as {@code @Editable}.
	 */
	protected Serializable newEditingBean(T item) {
		return item;
	}

	/**
	 * Extract the item to persist from the editing bean. Inverse of {@link #newEditingBean(Serializable)}.
	 * Default casts the editing bean back to {@code T}.
	 */
	@SuppressWarnings("unchecked")
	protected T extractItem(Serializable editingBean) {
		return (T) editingBean;
	}

	/**
	 * Hook for additional save-time validation. Subclasses can call {@code editor.error(...)}
	 * here to short-circuit the save. Invoked just before {@link BeanEditor#isValid()} is
	 * checked. Default is a no-op.
	 */
	protected void validateItem(BeanEditor editor, T item) {
	}

	/**
	 * Optional extra CSS class names to append to the modal form element. Default returns
	 * {@code null}.
	 */
	protected String additionalFormCssClass() {
		return null;
	}

	public interface EditCallback extends Serializable {

		void onSave(AjaxRequestTarget target);

		void onCancel(AjaxRequestTarget target);

	}

}

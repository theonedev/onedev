package com.pmease.gitop.web.common.component.datagrid.toolbar;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.web.common.component.datagrid.event.PageChanged;
import com.pmease.gitop.web.common.component.datagrid.event.SearchStringChanged;

public class SearchNavToolbar extends AbstractToolbar {

	private static final long serialVersionUID = 1L;

	public SearchNavToolbar(DataTable<?, ?> table) {
		super(table);

		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer span = new WebMarkupContainer("td");
		add(span);
		span.add(AttributeModifier.replace("colspan",
				new AbstractReadOnlyModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return String.valueOf(getTable().getColumns().size());
					}
				}));

		span.add(newSearchForm("search"));
		span.add(newNavigationForm("nav"));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		this.setVisibilityAllowed(getTable().getRowCount() > 0);
	}

	protected Component newSearchForm(String id) {
		Fragment frag = new Fragment(id, "searchFrag", this);
		frag.add(new SearchForm("searchForm"));
		return frag;
	}

	@SuppressWarnings({ "serial" })
	private class SearchForm extends Form<Void> {

		private String pattern;

		public SearchForm(String id) {
			super(id);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();

			TextField<String> input = new TextField<String>("input",
					new PropertyModel<String>(this, "pattern"));
			add(input);
			IndicatingAjaxButton submit = new IndicatingAjaxButton("submit",
					this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					send(getTable(), Broadcast.BREADTH,
							new SearchStringChanged(target, pattern));
				}
			};
			add(submit);
			setDefaultButton(submit);
		}

		@Override
		public void onEvent(IEvent<?> event) {
			if (event.getPayload() instanceof SearchStringChanged) {
				SearchStringChanged e = (SearchStringChanged) event
						.getPayload();
				this.pattern = e.getPattern();
				e.getTarget().add(this);
			}
		}
	}

	protected void onPageChanged(AjaxRequestTarget target) {
		target.add(getTable());
		target.add(this);
	}

	protected Component newNavigationForm(String id) {
		Fragment frag = new Fragment(id, "navFrag", this);
		frag.add(new NavigationForm("form"));
		return frag;
	}

	static final List<Integer> ROWS_PER_PAGE = ImmutableList.<Integer> of(10,
			25, 50, 100, 250, 500);

	@SuppressWarnings({ "serial" })
	private class NavigationForm extends Form<Void> {

		private int rowsPerPage = 10;
		private int currentPage = 1;

		public NavigationForm(String id) {
			super(id);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();

			rowsPerPage = (int) getTable().getItemsPerPage();
			currentPage = (int) getTable().getCurrentPage() + 1;
			
			DropDownChoice<Integer> rowsSelector = new DropDownChoice<Integer>(
					"rowsSelector", new PropertyModel<Integer>(this,
							"rowsPerPage"), ROWS_PER_PAGE);
			add(rowsSelector);
			rowsSelector.add(new AjaxFormComponentUpdatingBehavior("onchange") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					currentPage = 1;
					getTable().setItemsPerPage(rowsPerPage);
					send(getTable(), Broadcast.BREADTH, new PageChanged(target,
							currentPage - 1));
				}
			});

			add(new AjaxLink<Void>("previousLink") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					long current = getTable().getCurrentPage();
					if (current == 0) {
						return;
					}

					current--;
					if (current < 0) {
						current = 0;
					}

					send(getTable(), Broadcast.BREADTH, new PageChanged(target,
							(int) current));
				}
			});

			add(new AjaxLink<Void>("nextLink") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					long current = getTable().getCurrentPage();
					long totals = getTable().getPageCount();
					if (current == totals) {
						return;
					}

					current++;
					if (current >= totals) {
						current = totals - 1;
					}

					send(getTable(), Broadcast.BREADTH, new PageChanged(target,
							(int) current));
				}

			});

			add(new TextField<Integer>("pageInput", new PropertyModel<Integer>(
					this, "currentPage")));

			AjaxButton btn = new AjaxButton("submit", this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					if (currentPage == getTable().getCurrentPage() + 1) {
						return;
					}

					long totals = getTable().getPageCount();
					currentPage = Math.min(currentPage, (int) totals);
					currentPage = Math.max(currentPage, 0);

					send(getTable(), Broadcast.BREADTH, new PageChanged(target,
							currentPage - 1));
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(form);
				}
			};
			add(btn);
			setDefaultButton(btn);

			add(new Label("navlabel", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					long totals = getTable().getRowCount();
					long items = getTable().getItemsPerPage();
					long start = (currentPage - 1) * items + 1;
					long end = start + items - 1;
					end = Math.min(end, totals);
					return start + " - " + end + " of " + totals;
				}
			}));
		}

		@Override
		public void onEvent(IEvent<?> event) {
			if (event.getPayload() instanceof PageChanged) {
				PageChanged e = (PageChanged) event.getPayload();
				currentPage = e.getPage() + 1;
				rowsPerPage = (int) getTable().getItemsPerPage();
				e.getTarget().add(this);
			}
		}
	}
}

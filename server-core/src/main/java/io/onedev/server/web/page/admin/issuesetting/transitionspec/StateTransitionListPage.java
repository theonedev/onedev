package io.onedev.server.web.page.admin.issuesetting.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class StateTransitionListPage extends IssueSettingPage {

	private DataTable<TransitionSpec, Void> transitionsTable;
	
	public StateTransitionListPage(PageParameters params) {
		super(params);
	}

	private WebMarkupContainer newEditLink(String componentId, int transitionIndex, 
			@Nullable TransitionSpec transition) {
		return new ModalLink(componentId) {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new TransitionEditPanel(id, transitionIndex, transition) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(transitionsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected GlobalIssueSetting getSetting() {
						return StateTransitionListPage.this.getSetting();
					}

				};
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newEditLink("addNew", -1, null));
		
		List<IColumn<TransitionSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				cellItem.add(new SpriteImage(componentId, "grip") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("svg");
						tag.put("class", "icon drag-indicator");
					}

				});
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}

		});		
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				TransitionSpec transition = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "descriptionFrag", StateTransitionListPage.this);
				int transitionIndex = cellItem.findParent(LoopItem.class).getIndex();
				var link = newEditLink("link", transitionIndex, SerializationUtils.clone(transition));
				if (transition.getFromStates().isEmpty())
					link.add(new Label("fromStates", _T("[Any state]")));
				else					
					link.add(new Label("fromStates", "[" + StringUtils.join(transition.getFromStates(), ",") + "]"));
				if (transition.getToStates().isEmpty())
					link.add(new Label("toStates", _T("[Any state]")));
				else
					link.add(new Label("toStates", "[" + StringUtils.join(transition.getToStates(), ",") + "]"));

				fragment.add(link);

				fragment.add(new Label("when", MessageFormat.format(_T("When {0}"), transition.getTriggerDescription())));

				if (transition.getIssueQuery() != null)
					fragment.add(new Label("applicable", _T("For issues matching: ") + transition.getIssueQuery()));
				else
					fragment.add(new Label("applicable", _T("For all issues")));

				cellItem.add(fragment);
			}

		});		
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				int transitionIndex = cellItem.findParent(LoopItem.class).getIndex();
				var transitionClone = SerializationUtils.clone(rowModel.getObject());
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", StateTransitionListPage.this);
				fragment.add(newEditLink("edit", transitionIndex, transitionClone));

				fragment.add(newEditLink("copy", -1, transitionClone));
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this transition?")));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						var transition = getSetting().getTransitionSpecs().remove(transitionIndex);
						var oldAuditContent = VersionedXmlDoc.fromBean(transition).toXML();
						OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
						auditService.audit(null, "deleted issue transition", oldAuditContent, null);
						target.add(transitionsTable);
					}

				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-nowrap";
			}

		});		
		
		IDataProvider<TransitionSpec> dataProvider = new ListDataProvider<TransitionSpec>() {

			@Override
			protected List<TransitionSpec> getData() {
				return getSetting().getTransitionSpecs();			
			}

		};
		
		add(transitionsTable = new DataTable<>("stateTransitions", columns, dataProvider, Integer.MAX_VALUE));
		transitionsTable.addTopToolbar(new HeadersToolbar<Void>(transitionsTable, null));
		transitionsTable.addBottomToolbar(new NoRecordsToolbar(transitionsTable));
		transitionsTable.add(new NoRecordsBehavior());
		
		transitionsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getSetting().getTransitionSpecs()).toXML();
				CollectionUtils.move(getSetting().getTransitionSpecs(), from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(getSetting().getTransitionSpecs()).toXML();
				OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
				auditService.audit(null, "changed order of issue transitions", oldAuditContent, newAuditContent);
				target.add(transitionsTable);
			}
			
		}.sortable("tbody"));
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Issue State Transitions") + "</span>").setEscapeModelStrings(false);
	}
	
}

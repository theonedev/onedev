package io.onedev.server.web.page.admin.groupmanagement.profile;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.GroupService;
import io.onedev.server.model.Group;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.groupmanagement.GroupListPage;
import io.onedev.server.web.page.admin.groupmanagement.GroupPage;
import io.onedev.server.web.util.ConfirmClickModifier;

public class GroupProfilePage extends GroupPage {

	private BeanEditor editor;
		
	private String oldName;
	
	public GroupProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var oldAuditContent = VersionedXmlDoc.fromBean(getGroup()).toXML();
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getGroup();
			}

			@Override
			public void setObject(Serializable object) {
				// check contract of GroupManager.save on why we assign oldName here
				oldName = getGroup().getName();
				editor.getDescriptor().copyProperties(object, getGroup());
			}
			
		});

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Group group = getGroup();
				GroupService groupService = OneDev.getInstance(GroupService.class);
				Group groupWithSameName = groupService.find(group.getName());
				if (groupWithSameName != null && !groupWithSameName.equals(group)) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another group"));
				} 
				if (editor.isValid()) {
					var newAuditContent = VersionedXmlDoc.fromBean(group).toXML();
					groupService.update(group, oldName);
					OneDev.getInstance(AuditService.class).audit(null, "changed basic settings of group \"" + group.getName() + "\"", oldAuditContent, newAuditContent);
					setResponsePage(GroupProfilePage.class, GroupProfilePage.paramsOf(group));
					Session.get().success(_T("Basic settings updated"));
				}
			}
			
		};	
		form.add(editor);
		form.add(new FencedFeedbackPanel("feedback", form));

		form.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				var oldAuditContent = VersionedXmlDoc.fromBean(getGroup()).toXML();
				OneDev.getInstance(GroupService.class).delete(getGroup());
				OneDev.getInstance(AuditService.class).audit(null, "deleted group \"" + getGroup().getName() + "\"", oldAuditContent, null);

				Session.get().success(MessageFormat.format(_T("Group \"{0}\" deleted"), getGroup().getName()));
				
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Group.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(GroupListPage.class);
			}
			
		}.add(new ConfirmClickModifier(MessageFormat.format(_T("Do you really want to delete group \"{0}\"?"), getGroup().getName()))));
		
		add(form);
	}

}

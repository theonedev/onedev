package com.pmease.gitop.web.page.home;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmButton;
import com.pmease.gitop.web.common.component.vex.VexLinkBehavior.VexIcon;
import com.pmease.gitop.web.common.form.flatcheckbox.FlatCheckBoxElement;
import com.pmease.gitop.web.common.form.flatradio.FlatRadioElement;
import com.pmease.gitop.web.page.AbstractLayoutPage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
//		add(new BookmarkablePageLink<>("accountLink", AccountHomePage.class, AccountHomePage.paramsOf(Gitop.getInstance(UserManager.class).getRootUser())));
//		add(new BookmarkablePageLink<>("projectLink", ProjectHomePage.class, ProjectHomePage.paramsOf(Gitop.getInstance(ProjectManager.class).load(1L))));
		
		add(new AjaxLink<Void>("ajaxinfo") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Messenger.info("ajax info clicked").execute(target);;
			}
		});
		
		add(new AjaxLink<Void>("ajaxerror") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Messenger.error("ajax error clicked").execute(target);;
			}
		});
		
		add(new AjaxLink<Void>("ajaxsuccess") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Messenger.success("ajax success clicked!").execute(target);;
			}
		});
	
		Form<?> form = new Form<Void>("form");
		add(form);
		form.add(new FlatCheckBoxElement("check", new PropertyModel<Boolean>(this, "displayed"), 
				Model.of("Displayed screen")));
		form.add(new AjaxConfirmButton("btn", form, Model.of("Are you want to save this form?")) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				System.out.println("Here is " + vexIcon);
			}
		});
		
		RadioGroup<VexIcon> group = new RadioGroup<VexIcon>("group", new PropertyModel<VexIcon>(this, "vexIcon"));
		form.add(group);
		group.add(new ListView<VexIcon>("radio", ImmutableList.<VexIcon>copyOf(VexIcon.values())) {

			@Override
			protected void populateItem(ListItem<VexIcon> item) {
//				item.add(new Radio<VexIcon>("element", item.getModel()));
				item.add(new FlatRadioElement<VexIcon>("element", item.getModel(), item.getDefaultModelObjectAsString()));
			}
		});
	}

	VexIcon vexIcon = VexIcon.INFO;
	boolean displayed = true;
}

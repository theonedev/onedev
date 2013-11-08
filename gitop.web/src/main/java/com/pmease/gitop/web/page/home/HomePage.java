package com.pmease.gitop.web.page.home;

import org.apache.wicket.markup.head.IHeaderResponse;
import com.pmease.gitop.web.page.AbstractLayoutPage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;
	
	public HomePage() {
		this.setStatelessHint(true);
	}

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
//		
//		add(new DropZoneBehavior());
//		add(new FoundationDropDownBehavior());
//		
//		Form<?> form = new Form<Void>("testForm");
//		add(form);
//		form.add(new FlatCheckBoxElement("check1", new Model<Boolean>(true), 
//				Model.of("Enable public access")));
//		form.add(new FlatCheckBoxElement("check2", new Model<Boolean>(false), 
//				Model.of("Repository is forkable")));
//		
//		RadioGroup<VexIcon> rg = new RadioGroup<VexIcon>("radiogroup", new PropertyModel<VexIcon>(this, "vexIcon"));
//		form.add(rg);
//		
//		rg.add(new ListView<VexIcon>("radios", ImmutableList.<VexIcon>copyOf(VexIcon.values())) {
//
//			@Override
//			protected void populateItem(ListItem<VexIcon> item) {
//				item.add(new FlatRadioElement<VexIcon>("radio", 
//						item.getModel(), 
//						Model.of(item.getDefaultModelObjectAsString())));
//			}
//			
//		});
//		
//		form.add(new AjaxConfirmButton("submit", form,
//				Model.of("Are you sure you want to submit the form?"),
//				new AbstractReadOnlyModel<VexIcon>() {
//
//					@Override
//					public VexIcon getObject() {
//						return vexIcon;
//					}
//			
//				},
//				Model.of("Yes"),
//				Model.of("No"),
//				null) {
//			@Override
//			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
//				Messenger.success("Yes, your form is submitted").run(target);
//				target.add(form);
//			}
//		});
//		
//		final WebMarkupContainer c = new WebMarkupContainer("modalContent");
//		c.setOutputMarkupId(true);
//		add(c);
//		
//		add(new VexAjaxLink<Void>("prompt") {
//
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//				System.out.println("received");
//			}
//
//			@Override
//			protected String getContentMarkupId() {
//				return c.getMarkupId(true);
//			}
//			
//		});
//		
//		add(new AjaxLink<Void>("vexlink") {
//
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//				target.appendJavaScript("vex.open({"
//						+ "content: '<div>content</div>',"
//						+ "afterOpen: function($vexContent){$vexContent.append($('<div>yes</div>')); }, "
//						+ "afterClose: function() { console.log('vex closed'); } })");
//			}
//			
//		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
}

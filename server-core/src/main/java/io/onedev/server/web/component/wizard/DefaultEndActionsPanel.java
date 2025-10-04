package io.onedev.server.web.component.wizard;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.persistence.TransactionService;

public abstract class DefaultEndActionsPanel extends Panel {

	private final WizardPanel wizard;
	
	public DefaultEndActionsPanel(String id, WizardPanel wizard) {
		super(id);
		this.wizard = wizard;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Button("finish") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(TransactionService.class).run(new Runnable() {

					@Override
					public void run() {
						wizard.getActiveStep().complete();
					}
					
				});
				finished();
			}
			
		});
		
	}

	protected abstract void finished();
	
}

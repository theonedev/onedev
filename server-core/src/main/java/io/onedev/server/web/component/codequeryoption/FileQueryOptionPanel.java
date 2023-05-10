package io.onedev.server.web.component.codequeryoption;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.search.code.insidecommit.query.BlobQuery;
import io.onedev.server.search.code.insidecommit.query.FileQuery;
import io.onedev.server.search.code.query.FileQueryOption;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

public class FileQueryOptionPanel extends FormComponentPanel<FileQueryOption> {

	private TextField<String> term;
	
	private CheckBox caseSensitive;

	public FileQueryOptionPanel(String id, IModel<FileQueryOption> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var option = getModelObject();
		WebMarkupContainer termContainer = new WebMarkupContainer("term");
		add(termContainer);
		term = new TextField<>("term", Model.of(option.getTerm()));
		term.add(validatable -> {
			if (StringUtils.isBlank(validatable.getValue())) {
				validatable.error(messageSource -> "This field is required");
			} else {
				BlobQuery query = new FileQuery.Builder()
						.fileNames(validatable.getValue())
						.count(1)
						.build();
				try {
					query.asLuceneQuery();
				} catch (TooGeneralQueryException e) {
					validatable.error(messageSource -> "Search is too general");
				}
			}
		});

		termContainer.add(term);
		termContainer.add(new FencedFeedbackPanel("feedback", term));
		termContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (term.hasErrorMessage())
					return " is-invalid";
				else
					return "";
			}

		}));

		add(caseSensitive = new CheckBox("caseSensitive", Model.of(option.isCaseSensitive())));
	}

	@Override
	public void convertInput() {
		var option = new FileQueryOption();
		option.setTerm(term.getConvertedInput());
		option.setCaseSensitive(caseSensitive.getConvertedInput());
		setConvertedInput(option);
	}
	
}

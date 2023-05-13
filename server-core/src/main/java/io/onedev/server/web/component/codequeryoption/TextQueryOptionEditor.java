package io.onedev.server.web.component.codequeryoption;

import io.onedev.server.search.code.query.TextQuery;
import io.onedev.server.search.code.query.TextQueryOption;
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
import org.apache.wicket.validation.IValidationError;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TextQueryOptionEditor extends FormComponentPanel<TextQueryOption> {
	
	private TextField<String> term;
	
	private CheckBox regex;
	
	private CheckBox wholeWord;
	
	private CheckBox caseSensitive;
	
	private TextField<String> fileNames;
	
	public TextQueryOptionEditor(String id, IModel<TextQueryOption> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var option = getModelObject();
		WebMarkupContainer termContainer = new WebMarkupContainer("term");
		add(termContainer);
		term = new TextField<>("term", Model.of(option.getTerm()));
		term.setRequired(true).setLabel(Model.of("Search for"));
		term.add(validatable -> {
			boolean regex = this.regex.getInput() != null;
			try {
				if (regex)
					Pattern.compile(validatable.getValue());
				new TextQuery.Builder(validatable.getValue())
						.regex(regex)
						.count(1)
						.build()
						.asLuceneQuery();
			} catch (PatternSyntaxException e) {
				validatable.error((IValidationError) messageSource -> "Invalid PCRE syntax");
			} catch (TooGeneralQueryException e) {
				validatable.error((IValidationError) messageSource -> "Search is too general");
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

		add(regex = new CheckBox("regex", Model.of(option.isRegex())));
		add(wholeWord = new CheckBox("wholeWord", Model.of(option.isWholeWord())));
		add(caseSensitive = new CheckBox("caseSensitive", Model.of(option.isCaseSensitive())));
		add(fileNames = new TextField<>("fileNames", Model.of(option.getFileNames())));
	}

	@Override
	public void convertInput() {
		setConvertedInput(new TextQueryOption(term.getConvertedInput(), regex.getConvertedInput(), wholeWord.getConvertedInput(), caseSensitive.getConvertedInput(), fileNames.getConvertedInput()));
	}
}

package io.onedev.server.web.component.codequeryoption;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.search.code.insidecommit.query.BlobQuery;
import io.onedev.server.search.code.insidecommit.query.TextQuery;
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

public class TextQueryOptionPanel extends FormComponentPanel<TextQueryOption> {
	
	private TextField<String> term;
	
	private CheckBox regex;
	
	private CheckBox wholeWord;
	
	private CheckBox caseSensitive;
	
	private TextField<String> fileNames;
	
	public TextQueryOptionPanel(String id, IModel<TextQueryOption> model) {
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
				validatable.error((IValidationError) messageSource -> "This field is required");
			} else {
				boolean regex = this.regex.getInput()!=null?true:false;
				BlobQuery query = new TextQuery.Builder()
						.term(validatable.getValue()).regex(regex)
						.count(1)
						.build();
				try {
					if (regex)
						Pattern.compile(validatable.getValue());
					query.asLuceneQuery();
				} catch (PatternSyntaxException e) {
					validatable.error((IValidationError) messageSource -> "Invalid PCRE syntax");
				} catch (TooGeneralQueryException e) {
					validatable.error((IValidationError) messageSource -> "Search is too general");
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

		add(regex = new CheckBox("regex", Model.of(option.isRegex())));
		add(wholeWord = new CheckBox("wholeWord", Model.of(option.isWholeWord())));
		add(caseSensitive = new CheckBox("caseSensitive", Model.of(option.isCaseSensitive())));
		add(fileNames = new TextField<>("fileNames", Model.of(option.getFileNames())));
	}

	@Override
	public void convertInput() {
		var option = new TextQueryOption();
		option.setTerm(term.getConvertedInput());
		option.setRegex(regex.getConvertedInput());
		option.setWholeWord(wholeWord.getConvertedInput());
		option.setCaseSensitive(caseSensitive.getConvertedInput());
		option.setFileNames(fileNames.getConvertedInput());
		setConvertedInput(option);
	}
}

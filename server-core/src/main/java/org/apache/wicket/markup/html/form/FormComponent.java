/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.form;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.Localizer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AutoLabelResolver.AutoLabelMarker;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.parameter.EmptyRequestParameters;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitFilter;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.ValidatorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An HTML form component knows how to validate itself. Validators that implement IValidator can be
 * added to the component. They will be evaluated in the order they were added and the first
 * Validator that returns an error message determines the error message returned by the component.
 * <p>
 * FormComponents are not versioned by default. If you need versioning for your FormComponents, you
 * will need to call Form.setVersioned(true), which will set versioning on for the form and all form
 * component children.
 * <p>
 * If this component is required and that fails, the error key that is used is the "Required"; if
 * the type conversion fails, it will use the key "IConverter" if the conversion failed in a
 * converter, or "ConversionError" if type was explicitly specified via {@link #setType(Class)} or a
 * {@link IPropertyReflectionAwareModel} was used. Notice that both "IConverter" and
 * "ConversionError" have a more specific variant of "key.classname" where classname is the type
 * that we failed to convert to. Classname is not full qualified, so only the actual name of the
 * class is used.
 * 
 * Property expressions that can be used in error messages are:
 * <ul>
 * <li>${input}: the input the user did give</li>
 * <li>${name}: the name of the component that failed</li>
 * <li>${label}: the label of the component</li>
 * </ul>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 * 
 */
public abstract class FormComponent<T> extends LabeledWebMarkupContainer implements
	IFormVisitorParticipant, IFormModelUpdateListener, IGenericComponent<T>
{
	private static final Logger logger = LoggerFactory.getLogger(FormComponent.class);

	/**
	 * {@link IErrorMessageSource} used for error messages against this form components.
	 * 
	 * @author ivaynberg
	 */
	private class MessageSource implements IErrorMessageSource
	{
		private final Set<String> triedKeys = new LinkedHashSet<>();

		/**
		 * @see org.apache.wicket.validation.IErrorMessageSource#getMessage(String, java.util.Map)
		 */
		@Override
		public String getMessage(String key, Map<String, Object> vars)
		{
			final FormComponent<T> formComponent = FormComponent.this;

			// Use the following log4j config for detailed logging on the property resolution
			// process
			// log4j.logger.org.apache.wicket.resource.loader=DEBUG
			// log4j.logger.org.apache.wicket.Localizer=DEBUG

			final Localizer localizer = formComponent.getLocalizer();

			// retrieve prefix that will be used to construct message keys
			String prefix = formComponent.getValidatorKeyPrefix();
			String message;

			// first try the full form of key [form-component-id].[prefix].[key]
			String resource = getId() + "." + prefix(prefix, key);
			message = getString(localizer, resource, formComponent);

			// if not found, try a more general form (without prefix)
			// [form-component-id].[key]
			if (Strings.isEmpty(message) && Strings.isEmpty(prefix))
			{
				resource = getId() + "." + key;
				message = getString(localizer, resource, formComponent);
			}

			// If not found try a more general form [prefix].[key]
			if (Strings.isEmpty(message))
			{
				resource = prefix(prefix, key);
				message = getString(localizer, resource, formComponent);
			}

			// If not found try the most general form [key]
			if (Strings.isEmpty(message))
			{
				// Try a variation of the resource key
				message = getString(localizer, key, formComponent);
			}

			// convert empty string to null in case our default value of "" was
			// returned from localizer
			if (Strings.isEmpty(message))
			{
				message = null;
			}
			else
			{
				message = substitute(message, addDefaultVars(vars));
			}
			return message;
		}

		private String prefix(String prefix, String key)
		{
			if (!Strings.isEmpty(prefix))
			{
				return prefix + "." + key;
			}
			else
			{
				return key;
			}
		}

		/**
		 * 
		 * @param localizer
		 * @param key
		 * @param component
		 * @return string
		 */
		private String getString(Localizer localizer, String key, Component component)
		{
			triedKeys.add(key);

			// Note: It is important that the default value of "" is
			// provided to getString() not to throw a MissingResourceException or to
			// return a default string like "[Warning: String ..."
			return localizer.getString(key, component, "");
		}

		private String substitute(String string, final Map<String, Object> vars)
			throws IllegalStateException
		{
			return new VariableInterpolator(string, Application.get()
				.getResourceSettings()
				.getThrowExceptionOnMissingResource())
			{
				private static final long serialVersionUID = 1L;

				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				protected String getValue(String variableName)
				{
					Object value = vars.get(variableName);
					
					if (value == null ||value instanceof String)
					{
						return String.valueOf(value);
					}
					
					IConverter converter = getConverter(value.getClass());
					
					if (converter == null)
					{
						return Strings.toString(value);
					}
					else
					{
						return converter.convertToString(value, getLocale());
					}
				}
			}.toString();
		}

		/**
		 * Creates a new params map that additionally contains the default input, name, label
		 * parameters
		 * 
		 * @param params
		 *            original params map
		 * @return new params map
		 */
		private Map<String, Object> addDefaultVars(Map<String, Object> params)
		{
			// create and fill the new params map
			final HashMap<String, Object> fullParams;
			if (params == null)
			{
				fullParams = new HashMap<>(6);
			}
			else
			{
				fullParams = new HashMap<>(params.size() + 6);
				fullParams.putAll(params);
			}

			// add the input param if not already present
			if (!fullParams.containsKey("input"))
			{
				fullParams.put("input", getInput());
			}

			// add the name param if not already present
			if (!fullParams.containsKey("name"))
			{
				fullParams.put("name", getId());
			}

			// add the label param if not already present
			if (!fullParams.containsKey("label"))
			{
				fullParams.put("label", getLabel());
			}
			return fullParams;
		}

		/**
		 * @return value of label param for this form component
		 */
		private String getLabel()
		{
			final FormComponent<T> fc = FormComponent.this;
			String label = null;

			// first try the label model ...
			if (fc.getLabel() != null)
			{
				label = fc.getLabel().getObject();
			}
			// ... then try a resource of format [form-component-id] with
			// default of '[form-component-id]'
			if (label == null)
			{

				label = fc.getDefaultLabel();
			}
			return label;
		}
	}


	/**
	 * Adapter that makes this component appear as {@link IValidatable}
	 * 
	 * @author ivaynberg
	 */
	private class ValidatableAdapter implements IValidatable<T>
	{
		/**
		 * @see org.apache.wicket.validation.IValidatable#error(org.apache.wicket.validation.IValidationError)
		 */
		@Override
		public void error(IValidationError error)
		{
			FormComponent.this.error(error);
		}

		/**
		 * @see org.apache.wicket.validation.IValidatable#getValue()
		 */
		@Override
		public T getValue()
		{
			return getConvertedInput();
		}

		/**
		 * @see org.apache.wicket.validation.IValidatable#isValid()
		 */
		@Override
		public boolean isValid()
		{
			return FormComponent.this.isValid();
		}

		@Override
		public IModel<T> getModel()
		{
			return FormComponent.this.getModel();
		}
	}

	/**
	 * The value separator
	 */
	public static final String VALUE_SEPARATOR = ";";

	private static final String[] EMPTY_STRING_ARRAY = new String[] { "" };

	/** Whether or not this component's value is required (non-empty) */
	private static final short FLAG_REQUIRED = FLAG_RESERVED3;

	private static final String NO_RAW_INPUT = "[-NO-RAW-INPUT-]";

	private static final long serialVersionUID = 1L;

	/**
	 * Make empty strings null values boolean. Used by AbstractTextComponent subclass.
	 */
	protected static final short FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL = FLAG_RESERVED1;

	/**
	 * Visits any form components inside component if it is a container, or component itself if it
	 * is itself a form component
	 * 
	 * @param <R>
	 *            the type of the visitor's result
	 * 
	 * @param component
	 *            starting point of the traversal
	 * 
	 * @param visitor
	 *            The visitor to call
	 * @return the visitor's result
	 */
	public static <R> R visitFormComponentsPostOrder(Component component,
		final IVisitor<? extends FormComponent<?>, R> visitor)
	{
		return Visits.visitPostOrder(component, visitor, new IVisitFilter()
		{

			@Override
			public boolean visitChildren(Object object)
			{
				if (object instanceof IFormVisitorParticipant)
				{
					return ((IFormVisitorParticipant)object).processChildren();
				}
				return true;
			}

			@Override
			public boolean visitObject(Object object)
			{
				return (object instanceof FormComponent<?>);
			}

		});

	}

	/**
	 * Visits any form components inside component if it is a container, or component itself if it
	 * is itself a form component
	 * 
	 * @param <R>
	 *            the type of the visitor's result
	 * @param component
	 *            starting point of the traversal
	 * 
	 * @param visitor
	 *            The visitor to call
	 * @return the visitor's result
	 */
	public static <R> R visitComponentsPostOrder(Component component,
		final org.apache.wicket.util.visit.IVisitor<Component, R> visitor)
	{
		Args.notNull(visitor, "visitor");

		return Visits.visitPostOrder(component, visitor, new IVisitFilter()
		{
			@Override
			public boolean visitObject(Object object)
			{
				return true;
			}

			@Override
			public boolean visitChildren(Object object)
			{
				if (object instanceof IFormVisitorParticipant)
				{
					return ((IFormVisitorParticipant)object).processChildren();
				}
				return true;
			}

		});
	}

	private T convertedInput;

	/**
	 * Raw Input entered by the user or NO_RAW_INPUT if nothing is filled in.
	 */
	private String rawInput = NO_RAW_INPUT;

	/**
	 * Type that the raw input string will be converted to
	 */
	private String typeName;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public FormComponent(final String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public FormComponent(final String id, IModel<T> model)
	{
		super(id, model);
		if (model != null)
			setConvertedInput(model.getObject());
		// the form decides whether form components are versioned or not
		// see Form.setVersioned
		setVersioned(false);
	}

	/**
	 * Gets the string the component would use as a label when one was requested but no label model
	 * was set via {@link #getLabel()}. The value of this string is usually set in a property file;
	 * if the value is not set the default value equivalent to component id will be returned.
	 * 
	 * @return localized label
	 */
	public final String getDefaultLabel()
	{
		return getDefaultLabel(getId());
	}

	/**
	 * Gets the string the component would use as a label when one was requested but no label model
	 * was set via {@link #getLabel()}. The value of this string is usually set in a property file;
	 * if the value is not set the {@code defaultValue} will be returned.
	 * 
	 * @param defaultValue
	 * 
	 * @return localized label
	 */
	public final String getDefaultLabel(String defaultValue)
	{
		return getLocalizer().getString(getId(), getParent(), defaultValue);
	}


	/**
	 * Adds a validator to this form component
	 * 
	 * @param validator
	 *            validator to be added
	 * @return <code>this</code> for chaining
	 * @throws IllegalArgumentException
	 *             if validator is null
	 * @see IValidator
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final FormComponent<T> add(final IValidator<? super T> validator)
	{
		Args.notNull(validator, "validator");

		if (validator instanceof Behavior)
		{
			add((Behavior)validator);
		}
		else
		{
			add((Behavior)new ValidatorAdapter(validator));
		}
		return this;
	}

	/**
	 * Removes a validator from the form component.
	 * 
	 * @param validator
	 *            validator
	 * @throws IllegalArgumentException
	 *             if validator is null or not found
	 * @see IValidator
	 * @see #add(IValidator)
	 * @return form component for chaining
	 */
	public final FormComponent<T> remove(final IValidator<? super T> validator)
	{
		Args.notNull(validator, "validator");
		Behavior match = null;
		for (Behavior behavior : getBehaviors())
		{
			if (behavior.equals(validator))
			{
				match = behavior;
				break;
			}
			else if (behavior instanceof ValidatorAdapter)
			{
				if (((ValidatorAdapter<?>)behavior).getValidator().equals(validator))
				{
					match = behavior;
					break;
				}
			}
		}

		if (match != null)
		{
			remove(match);
		}
		else
		{
			throw new IllegalStateException(
				"Tried to remove validator that was not previously added. "
					+ "Make sure your validator's equals() implementation is sufficient");
		}
		return this;
	}

	/**
	 * Adds a validator to this form component.
	 * 
	 * @param validators
	 *            The validator(s) to be added
	 * @return This
	 * @throws IllegalArgumentException
	 *             if validator is null
	 * @see IValidator
	 */
	@SafeVarargs
	public final FormComponent<T> add(final IValidator<? super T>... validators)
	{
		Args.notNull(validators, "validators");

		for (IValidator<? super T> validator : validators)
		{
			add(validator);
		}

		// return this for chaining
		return this;
	}

	/**
	 * Checks if the form component's 'required' requirement is met by first checking
	 * {@link #isRequired()} to see if it has to check for requirement. If that is true then by
	 * default it checks if the input is null or an empty String
	 * {@link Strings#isEmpty(CharSequence)}
	 * <p>
	 * Subclasses that overwrite this method should also call {@link #isRequired()} first.
	 * </p>
	 * 
	 * @return true if the 'required' requirement is met, false otherwise
	 * 
	 * @see Strings#isEmpty(CharSequence)
	 * @see #isInputNullable()
	 */
	public boolean checkRequired()
	{
		if (isRequired())
		{
			final String input = getInput();

			// when null, check whether this is natural for that component, or
			// whether - as is the case with text fields - this can only happen
			// when the component was disabled
			if (input == null && !isInputNullable() && !isEnabledInHierarchy())
			{
				// this value must have come from a disabled field
				// do not perform validation
				return true;
			}

			// perform validation by looking whether the value is null or empty
			return !Strings.isEmpty(input);
		}
		return true;
	}

	/**
	 * Clears the user input.
	 */
	public final void clearInput()
	{
		rawInput = NO_RAW_INPUT;
	}

	/**
	 * Reports a validation error against this form component.
	 * 
	 * The actual error is reported by creating a {@link ValidationErrorFeedback} object that holds
	 * both the validation error and the generated error message - so a custom feedback panel can
	 * have access to both.
	 * 
	 * @param error
	 *            validation error
	 */
	public void error(IValidationError error)
	{
		Args.notNull(error, "error");

		MessageSource source = new MessageSource();
		Serializable message = error.getErrorMessage(source);

		if (message == null)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("Could not locate error message for component: ");
			buffer.append(Classes.simpleName(getClass()));
			buffer.append("@");
			buffer.append(getPageRelativePath());
			buffer.append(" and error: ");
			buffer.append(error.toString());
			buffer.append(". Tried keys: ");
			Iterator<String> keys = source.triedKeys.iterator();
			while (keys.hasNext())
			{
				buffer.append(keys.next());
				if (keys.hasNext())
				{
					buffer.append(", ");
				}
			}
			buffer.append('.');
			message = buffer.toString();
			logger.warn(message.toString());
		}
		error(message);
	}

	/**
	 * Gets the converted input. The converted input is set earlier though the implementation of
	 * {@link #convertInput()}.
	 * 
	 * {@link FormComponentPanel} often access this method when constructing their converted input
	 * value which is often the combination of converted values of the embedded FormComponents
	 * 
	 * To access the model object resulted by the full form processing, use
	 * {@link #getModelObject()} instead, that is an generified version of
	 * {@link #getDefaultModelObject()}
	 * 
	 * @return value of input possibly converted into an appropriate type
	 */
	public final T getConvertedInput()
	{
		return convertedInput;
	}

	/**
	 * Sets the converted input. This method is typically not called by clients, unless they
	 * override {@link #convertInput()}, in which case they should call this method to update the
	 * input for this component instance.
	 * 
	 * @param convertedInput
	 *            the converted input
	 */
	public final void setConvertedInput(T convertedInput)
	{
		this.convertedInput = convertedInput;
	}

	/**
	 * @return The parent form for this form component
	 */
	public Form<?> getForm()
	{
		Form<?> form = Form.findForm(this);
		if (form == null)
		{
			throw new WicketRuntimeException("Could not find Form parent for " + this);
		}
		return form;
	}


	/**
	 * Gets the request parameter for this component as a string.
	 * 
	 * @return The value in the request for this component
	 */
	public String getInput()
	{
		String[] input = getInputAsArray();
		if (input == null || input.length == 0)
		{
			return null;
		}
		else
		{
			return trim(input[0]);
		}
	}

	/**
	 * Gets the request parameters for this component as strings.
	 * 
	 * @return The values in the request for this component
	 */
	public String[] getInputAsArray()
	{
		List<StringValue> list = getParameterValues(getInputName());

		String[] values = null;
		if (list != null)
		{
			values = new String[list.size()];
			for (int i = 0; i < list.size(); ++i)
			{
				values[i] = list.get(i).toString();
			}
		}

		if (!isInputNullable())
		{
			if (values != null && values.length == 1 && values[0] == null)
			{
				// we the key got passed in (otherwise values would be null),
				// but the value was set to null.
				// As the servlet spec isn't clear on what to do with 'empty'
				// request values - most return an empty string, but some null -
				// we have to workaround here and deliberately set to an empty
				// string if the the component is not nullable (text components)
				return EMPTY_STRING_ARRAY;
			}
		}
		return values;
	}

	/**
	 * Reads the value(s) of the request parameter with name <em>inputName</em>
	 * from either the query parameters for <em>GET</em> method or the request body
	 * for <em>POST</em> method.
	 *
	 * @param inputName
	 *      The name of the request parameter
	 * @return The parameter's value(s)
	 */
	protected List<StringValue> getParameterValues(String inputName)
	{
		String method = Form.METHOD_POST;
		final Request request = getRequest();
		if (getRequest().getContainerRequest() instanceof HttpServletRequest)
		{
			method = ((HttpServletRequest)getRequest().getContainerRequest()).getMethod();
		}
		else
		{
			final Form<?> form = findParent(Form.class);
			if (form != null)
			{
				method = form.getMethod();
			}
		}

		final IRequestParameters parameters;
		switch (method.toLowerCase(Locale.ROOT))
		{
			case Form.METHOD_POST:
				parameters = request.getPostParameters();
				break;
			case Form.METHOD_GET:
				parameters = request.getQueryParameters();
				break;
			default:
				parameters = EmptyRequestParameters.INSTANCE;
		}

		return parameters.getParameterValues(inputName);
	}
	
	/**
	 * Gets the string to be used for the <tt>name</tt> attribute of the form element. Generated
	 * using the path from the form to the component, excluding the form itself. Override it if you
	 * want even a smaller name. E.g. if you know for sure that the id is unique within a form.
	 * 
	 * @return The string to use as the form element's name attribute
	 */
	public String getInputName()
	{
		String inputName = Form.getRootFormRelativeId(this);
		Form<?> form = findParent(Form.class);

		if (form != null)
		{
			return form.getInputNamePrefix() + inputName;
		}
		else
		{
			return inputName;
		}
	}

	/**
	 * Use hasRawInput() to check if this component has raw input because null can mean 2 things: It
	 * doesn't have rawinput or the rawinput is really null.
	 * 
	 * @return The raw form input that is stored for this formcomponent
	 */
	public final String getRawInput()
	{
		return NO_RAW_INPUT.equals(rawInput) ? null : rawInput;
	}

	/**
	 * @return the type to use when updating the model for this form component
	 */
	@SuppressWarnings("unchecked")
	public final Class<T> getType()
	{
		return typeName == null ? null : (Class<T>)WicketObjects.resolveClass(typeName);
	}

	/**
	 * @see Form#getValidatorKeyPrefix()
	 * @return prefix used when constructing validator key messages
	 */
	public String getValidatorKeyPrefix()
	{
		Form<?> form = findParent(Form.class);
		if (form != null)
		{
			return getForm().getValidatorKeyPrefix();
		}
		return null;
	}

	/**
	 * Gets an unmodifiable list of validators for this FormComponent.
	 * 
	 * @return List of validators
	 */
	@SuppressWarnings("unchecked")
	public final List<IValidator<? super T>> getValidators()
	{
		final List<IValidator<? super T>> list = new ArrayList<>();

		for (Behavior behavior : getBehaviors())
		{
			if (behavior instanceof IValidator)
			{
				list.add((IValidator<? super T>)behavior);
			}
		}

		return Collections.unmodifiableList(list);
	}

	/**
	 * Gets current value for a form component, which can be either input data entered by the user,
	 * or the component's model object if no input was provided.
	 * 
	 * @return The value
	 */
	public final String getValue()
	{
		if (NO_RAW_INPUT.equals(rawInput))
		{
			return getModelValue();
		}
		else
		{
			if (getEscapeModelStrings() && rawInput != null)
			{
				return Strings.escapeMarkup(rawInput).toString();
			}
			return rawInput;
		}
	}

	/**
	 * Returns whether this component has raw input. Raw input is unconverted input straight from
	 * the client.
	 * 
	 * @return boolean whether this component has raw input.
	 */
	public final boolean hasRawInput()
	{
		return !NO_RAW_INPUT.equals(rawInput);
	}

	/**
	 * Used by Form to tell the FormComponent that a new user input is available
	 */
	public final void inputChanged()
	{
		if (isVisibleInHierarchy() && isEnabledInHierarchy())
		{
			// Get input as String array
			final String[] input = getInputAsArray();

			// If there is any input
			if (input != null && input.length > 0 && input[0] != null)
			{
				// join the values together with ";", for example, "id1;id2;id3"
				rawInput = StringList.valueOf(input).join(VALUE_SEPARATOR);
			}
			else if (isInputNullable())
			{
				// no input
				rawInput = null;
			}
			else
			{
				rawInput = NO_RAW_INPUT;
			}
		}
	}

	/**
	 * Indicate that validation of this form component failed.
	 */
	public final void invalid()
	{
		onInvalid();
	}

	/**
	 * Gets whether this component's input can be null. By default, components that do not get input
	 * will have null values passed in for input. However, component TextField is an example
	 * (possibly the only one) that never gets a null passed in, even if the field is left empty
	 * UNLESS it had attribute <code>disabled="disabled"</code> set.
	 * 
	 * @return True if this component's input can be null. Returns true by default.
	 */
	public boolean isInputNullable()
	{
		return true;
	}

	/**
	 * @return True if this component encodes data in a multipart form submit
	 */
	public boolean isMultiPart()
	{
		return false;
	}

	/**
	 * @return whether or not this component's value is required
	 */
	public boolean isRequired()
	{
		return getFlag(FLAG_REQUIRED);
	}

	/**
	 * Gets whether this component is 'valid'. Valid in this context means that no validation errors
	 * were reported the last time the form component was processed. This variable not only is
	 * convenient for 'business' use, but is also necessary as we don't want the form component
	 * models updated with invalid input.
	 * 
	 * @return valid whether this component is 'valid'
	 */
	public final boolean isValid()
	{
		class IsValidVisitor implements IVisitor<Component, Boolean>
		{
			@Override
			public void component(final Component component, final IVisit<Boolean> visit)
			{
				if (component.hasErrorMessage())
				{
					visit.stop(Boolean.FALSE);
				}
			}
		}
		IsValidVisitor tmp = new IsValidVisitor();
		// Visit Component instead of FormComponent as sometimes we add error message to 
		// ordinary components inside a FormComponentPanel
		final Object result = visitComponentsPostOrder(this, tmp);
		return (Boolean.FALSE != result);
	}

	/**
	 * @see IFormVisitorParticipant#processChildren()
	 */
	@Override
	public boolean processChildren()
	{
		return true;
	}

	/**
	 * This method will retrieve the request parameter, validate it, and if valid update the model.
	 * These are the same steps as would be performed by the form.
	 * 
	 * This is useful when a formcomponent is used outside a form.
	 * 
	 */
	public final void processInput()
	{
		inputChanged();
		validate();
		if (hasErrorMessage())
		{
			invalid();
		}
		else
		{
			valid();
			updateModel();
		}
	}

	/**
	 * The value will be made available to the validator property by means of ${label}. It does not
	 * have any specific meaning to FormComponent itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	@Override
	public FormComponent<T> setLabel(IModel<String> labelModel)
	{
		super.setLabel(labelModel);
		return this;
	}

	/**
	 * Sets the value for a form component.
	 * 
	 * @param value
	 *            The value
	 */
	public void setModelValue(final String[] value)
	{
		convertedInput = convertValue(value);
		updateModel();
	}

	/**
	 * Sets the required flag
	 * 
	 * @param required
	 * @return this for chaining
	 */
	public final FormComponent<T> setRequired(final boolean required)
	{
		if (!required && getType() != null && getType().isPrimitive())
		{
			throw new WicketRuntimeException(
					"FormComponent has to be required when the type is primitive class: " + this);
		}
		if (required != isRequired())
		{
			addStateChange();
		}
		setFlag(FLAG_REQUIRED, required);
		return this;
	}

	/**
	 * Sets the type that will be used when updating the model for this component. If no type is
	 * specified String type is assumed.
	 * 
	 * @param type
	 * @return this for chaining
	 */
	public FormComponent<T> setType(Class<?> type)
	{
		typeName = type == null ? null : type.getName();
		if (type != null && type.isPrimitive())
		{
			setRequired(true);
		}
		return this;
	}

	/**
	 * Updates this components model from the request, it expects that the object is already
	 * converted through the convertInput() call that is called by the validate() method when a form
	 * is being processed.
	 * 
	 * By default it just does this:
	 * 
	 * <pre>
	 * setModelObject(getConvertedInput());
	 * </pre>
	 * 
	 * <strong>DO NOT CALL THIS METHOD DIRECTLY UNLESS YOU ARE SURE WHAT YOU ARE DOING. USUALLY UPDATING
	 * YOUR MODEL IS HANDLED BY THE FORM, NOT DIRECTLY BY YOU.</strong>
	 */
	@Override
	public void updateModel()
	{
		setModelObject(getConvertedInput());
	}


	/**
	 * Called to indicate that the user input is valid.
	 */
	public final void valid()
	{
		clearInput();

		onValid();
	}

	/**
	 * Performs full validation of the form component, which consists of calling validateRequired(),
	 * convertInput(), and validateValidators(). This method should only be used if the form
	 * component needs to be fully validated outside the form process.
	 */
	public void validate()
	{
		// clear any previous feedback messages

		if (hasFeedbackMessage())
		{
			getFeedbackMessages().clear();
		}

		// validate

		validateRequired();
		if (isValid())
		{
			convertInput();
			if (isValid())
			{
				if (isRequired() && getConvertedInput() == null && isInputNullable())
				{
					reportRequiredError();
				}
				else
				{
					validateValidators();
				}
			}
		}
	}

	/**
	 * Converts and validates the conversion of the raw input string into the object specified by
	 * {@link FormComponent#getType()} and records any thrown {@link ConversionException}s.
	 * Converted value is available through {@link FormComponent#getConvertedInput()}.
	 * 
	 * <p>
	 * Usually the user should do custom conversions by specifying an {@link IConverter} by
	 * registering it with the application by overriding {@link Application#getConverterLocator()},
	 * or at the component level by overriding {@link #getConverter(Class)} .
	 * </p>
	 *
	 * <strong>DO NOT CALL THIS METHOD DIRECTLY UNLESS YOU ARE SURE WHAT YOU ARE DOING. USUALLY UPDATING
	 * YOUR MODEL IS HANDLED BY THE FORM, NOT DIRECTLY BY YOU.</strong>
	 *
	 * @see IConverterLocator
	 * @see Application#newConverterLocator()
	 * @see IConverter#convertToObject(String, Locale)
	 * @see #newValidationError(ConversionException)
	 */
	public void convertInput()
	{
		if (typeName == null)
		{
			try
			{
				convertedInput = convertValue(getInputAsArray());
			}
			catch (ConversionException e)
			{
				error(newValidationError(e));
			}
		}
		else
		{
			final IConverter<T> converter = getConverter(getType());

			try
			{
				convertedInput = converter.convertToObject(getInput(), getLocale());
			}
			catch (ConversionException e)
			{
				error(newValidationError(e));
			}
		}
	}

	/**
	 * This method is called, when the validation triggered by {@link FormComponent#convertInput()}
	 * failed with a {@link ConversionException}, to construct a {@link ValidationError} based on
	 * the exception.
	 * <p>
	 * Override this method to modify the ValidationError object, e.g. add a custom variable for
	 * message substitution:
	 * <p>
	 * 
	 * <pre>
	 * new FormComponent&lt;T&gt;(id)
	 * {
	 * 	protected ValidationError newValidationError(ConversionException cause)
	 * 	{
	 * 		return super.newValidationError(cause).setVariable(&quot;foo&quot;, foovalue);
	 * 	}
	 * };
	 * </pre>
	 * 
	 * @param cause
	 *            the original cause
	 * @return {@link ValidationError}
	 */
	protected ValidationError newValidationError(ConversionException cause)
	{
		ValidationError error = new ValidationError(cause.getMessage());

		if (cause.getResourceKey() != null)
		{
			error.addKey(cause.getResourceKey());
		}

		if (typeName == null)
		{
			if (cause.getTargetType() != null)
			{
				error.addKey("ConversionError." + Classes.simpleName(cause.getTargetType()));
			}
			error.addKey("ConversionError");
		}
		else
		{
			String simpleName = Classes.simpleName(getType());
			error.addKey("IConverter." + simpleName);
			error.addKey("IConverter");
			error.setVariable("type", simpleName);
		}

		final Locale locale = cause.getLocale();
		if (locale != null)
		{
			error.setVariable("locale", locale);
		}

		error.setVariable("exception", cause);

		Format format = cause.getFormat();
		if (format instanceof SimpleDateFormat)
		{
			error.setVariable("format", ((SimpleDateFormat)format).toLocalizedPattern());
		}

		Map<String, Object> variables = cause.getVariables();
		if (variables != null)
		{
			error.getVariables().putAll(variables);
		}

		return error;
	}

	/**
	 * Subclasses should overwrite this if the conversion is not done through the type field and the
	 * {@link IConverter}. <strong>WARNING: this method may be removed in future versions.</strong>
	 * 
	 * If conversion fails then a ConversionException should be thrown
	 * 
	 * @param value
	 *            The value can be the getInput() or through a cookie
	 * 
	 * @return The converted value. default returns just the given value
	 * @throws ConversionException
	 *             If input can't be converted
	 */
	@SuppressWarnings("unchecked")
	protected T convertValue(String[] value) throws ConversionException
	{
		return (T)(value != null && value.length > 0 && value[0] != null ? trim(value[0]) : null);
	}

	/**
	 * @return Value to return when model value is needed
	 */
	protected String getModelValue()
	{
		return getDefaultModelObjectAsString();
	}

	/**
	 * Gets the request parameter for this component as an int.
	 * 
	 * @return The value in the request for this component
	 */
	protected final int inputAsInt()
	{
		final String string = getInput();
		try
		{
			return Integer.parseInt(string);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(
				exceptionMessage("Internal error.  Request string '" + string +
					"' not a valid integer"));
		}
	}

	/**
	 * Gets the request parameter for this component as an int, using the given default in case no
	 * corresponding request parameter was found.
	 * 
	 * @param defaultValue
	 *            Default value to return if request does not have an integer for this component
	 * @return The value in the request for this component
	 */
	protected final int inputAsInt(final int defaultValue)
	{
		final String string = getInput();
		if (string != null)
		{
			try
			{
				return Integer.parseInt(string);
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException(exceptionMessage("Request string '" + string +
					"' is not a valid integer"));
			}
		}
		else
		{
			return defaultValue;
		}
	}

	/**
	 * Gets the request parameters for this component as ints.
	 * 
	 * @return The values in the request for this component
	 */
	protected final int[] inputAsIntArray()
	{
		final String[] strings = getInputAsArray();
		if (strings != null)
		{
			final int[] ints = new int[strings.length];
			for (int i = 0; i < strings.length; i++)
			{
				ints[i] = Integer.parseInt(strings[i]);
			}
			return ints;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.Component#internalOnModelChanged()
	 */
	@Override
	protected void internalOnModelChanged()
	{
		// If the model for this form component changed, we should make it
		// valid again because there can't be any invalid input for it anymore.
		valid();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		tag.put("name", getInputName());

		if (!isEnabledInHierarchy())
		{
			onDisabled(tag);
		}

		if (isRequired())
		{
			onRequired(tag);
		}

		super.onComponentTag(tag);
	}

	/**
	 * Sets the temporary converted input value to null.
	 * 
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();
//		convertedInput = null;
	}

	/**
	 * Called by {@link #onComponentTag(ComponentTag)} when the component is disabled. By default,
	 * this method will add a disabled="disabled" attribute to the tag. Components may override this
	 * method to tweak the tag as they think is fit.
	 * 
	 * @param tag
	 *            the tag that is being rendered
	 */
	protected void onDisabled(final ComponentTag tag)
	{
		tag.put("disabled", "disabled");
	}

	/**
	 * Called by {@link #onComponentTag(ComponentTag)} when the component is required.
	 * 
	 * @param tag
	 *            the tag that is being rendered
	 */
	@Deprecated
	protected void onRequired(final ComponentTag tag)
	{
	}

	/**
	 * Handle invalidation
	 */
	protected void onInvalid()
	{
	}

	/**
	 * Handle validation
	 */
	protected void onValid()
	{
	}

	/**
	 * Determines whether or not this component should trim its input prior to processing it. The
	 * default value is <code>true</code>
	 * 
	 * @return True if the input should be trimmed.
	 */
	protected boolean shouldTrimInput()
	{
		return true;
	}

	/**
	 * Trims the input according to {@link #shouldTrimInput()}
	 * 
	 * @param string
	 * @return trimmed input if {@link #shouldTrimInput()} returns true, unchanged input otherwise
	 */
	protected String trim(String string)
	{
		String trimmed = string;
		if (trimmed != null && shouldTrimInput())
		{
			trimmed = trimmed.trim();
		}
		return trimmed;
	}

	/**
	 * Checks if the raw input value is not null if this component is required.
	 */
	protected final void validateRequired()
	{
		if (!checkRequired())
		{
			reportRequiredError();
		}
	}

	/**
	 * Reports required error against this component
	 */
	protected void reportRequiredError()
	{
		error(new ValidationError().addKey("Required"));
	}

	/**
	 * Validates this component using the component's validators.
	 */
	@SuppressWarnings("unchecked")
	protected final void validateValidators()
	{
		final IValidatable<T> validatable = newValidatable();

		boolean isNull = getConvertedInput() == null;

		IValidator<T> validator;

		for (Behavior behavior : getBehaviors())
		{
			if (isBehaviorAccepted(behavior) == false)
			{
				continue;
			}

			validator = null;
			if (behavior instanceof ValidatorAdapter)
			{
				validator = ((ValidatorAdapter<T>)behavior).getValidator();
			}
			else if (behavior instanceof IValidator)
			{
				validator = (IValidator<T>)behavior;
			}
			if (validator != null)
			{
				if (isNull == false || validator instanceof INullAcceptingValidator<?>)
				{
					try
					{
						validator.validate(validatable);
					}
					catch (Exception e)
					{
						throw new WicketRuntimeException("Exception '" + e.getMessage() +
								"' occurred during validation " + validator.getClass().getName() +
								" on component " + getPath(), e);
					}
				}
				if (!isValid())
				{
					break;
				}
			}
		}
	}

	/**
	 * Creates an IValidatable that can be used to validate this form component. This validatable
	 * incorporates error key lookups that correspond to this form component.
	 * 
	 * This method is useful when validation needs to happen outside the regular validation workflow
	 * but error messages should still be properly reported against the form component.
	 * 
	 * @return IValidatable<T> for this form component
	 */
	public final IValidatable<T> newValidatable()
	{
		return new ValidatableAdapter();
	}

	@Override
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@Override
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

	/**
	 * Updates auto label css classes such as error/required during ajax updates when the labels may
	 * not be directly repainted in the response.
	 * 
	 * @param target
	 */
	public final void updateAutoLabels(AjaxRequestTarget target)
	{
		AutoLabelMarker marker = getMetaData(AutoLabelResolver.MARKER_KEY);
	
		if (marker == null)
		{
			// this component does not have an auto label
			return;
		}

		marker.updateFrom(this, target);
	}

	/**
	 * Update the model of a {@link FormComponent} containing a {@link Collection}.
	 * 
	 * If the model object does not yet exists, a new {@link ArrayList} is filled with the converted
	 * input and used as the new model object. Otherwise the existing collection is modified
	 * in-place, then {@link Model#setObject(Object)} is called with the same instance: it allows
	 * the Model to be notified of changes even when {@link Model#getObject()} returns a different
	 * {@link Collection} at every invocation.
	 * 
	 * @param <S>
	 *            collection type
	 * @param formComponent
	 *            the form component to update
	 * @see FormComponent#updateModel()
	 * @throws WicketRuntimeException
	 *             if the existing model object collection is unmodifiable and no setter exists
	 */
	public static <S> void updateCollectionModel(FormComponent<Collection<S>> formComponent)
	{
		Collection<S> convertedInput = formComponent.getConvertedInput();
		if (convertedInput == null) {
			convertedInput = Collections.emptyList();
		}

		Collection<S> collection = formComponent.getModelObject();
		if (collection == null)
		{
			collection = new ArrayList<>(convertedInput);
			formComponent.setModelObject(collection);
		}
		else
		{
			boolean modified = false;

			formComponent.modelChanging();

			try
			{
				collection.clear();
				collection.addAll(convertedInput);
				modified = true;
			}
			catch (UnsupportedOperationException unmodifiable)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("An error occurred while trying to modify the collection attached to "
							+ formComponent, unmodifiable);
				}
				collection = new ArrayList<>(convertedInput); 
			}
			
			try
			{
				formComponent.getModel().setObject(collection);
			}
			catch (Exception noSetter)
			{
				if (!modified)
				{
					throw new WicketRuntimeException("An error occurred while trying to set the collection attached to "
							+ formComponent, noSetter);
				}
				else if (logger.isDebugEnabled())
				{
					logger.debug("An error occurred while trying to set the collection attached to "
							+ formComponent, noSetter);
				}
			}
			
			formComponent.modelChanged();
		}
	}
}

package io.onedev.server.web.page.help;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.ws.rs.Path;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.launcher.loader.ImplementationRegistry;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityId;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;

@SuppressWarnings("serial")
public class ExampleValuePanel extends Panel {

	private static final Map<Class<?>, Class<?>> resourceMap = new HashMap<>();
	
	static {
		ResourceConfig config = OneDev.getInstance(ResourceConfig.class);
		
		for (Class<?> clazz: config.getClasses()) {
			if (clazz.getAnnotation(Path.class) != null) { 
				Api api = clazz.getAnnotation(Api.class);
				if (api == null || !api.exclude()) {
					try {
						Method method = clazz.getMethod("get", Long.class);
						if (AbstractEntity.class.isAssignableFrom(method.getReturnType()))
							resourceMap.put(method.getReturnType(), clazz);
					} catch (NoSuchMethodException | SecurityException e) {
					}
				}
			}
		} 
	}
	
	private final Object value;
	
	private final IModel<ValueInfo> valueInfoModel;
	
	private final boolean requestBody;
	
	public ExampleValuePanel(String id, Object value, boolean requestBody) {
		this(id, value, newNullValueModel(), requestBody);
	}
	
	public ExampleValuePanel(String id, Object value, IModel<ValueInfo> valueInfoModel, boolean requestBody) {
		super(id);
		this.value = value;
		this.valueInfoModel = valueInfoModel;
		this.requestBody = requestBody;
	}

	@Override
	protected void onDetach() {
		valueInfoModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (value == null || value.getClass() == String.class
				|| value.getClass() == boolean.class || value.getClass() == Boolean.class
				|| value.getClass() == int.class || value.getClass() == Integer.class
				|| value.getClass() == long.class || value.getClass() == Long.class
				|| value.getClass() == Date.class || value instanceof Enum) {
			add(newScalarFragment(value));
		} else if (getField() != null && getField().getAnnotation(ManyToOne.class) != null) {
			add(newScalarFragment(((AbstractEntity)value).getId()));
		} else if (value instanceof Collection) {
			add(newArrayFragment());
		} else if (value instanceof Map) {
			add(newMapFragment());
		} else {
			add(newObjectFragment());
		}
		
		if (newValueHint("psuedoId") != null) {
			add(new DropdownLink("hint") {
	
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return newValueHint(id);
				}
				
			});
		} else {
			add(new WebMarkupContainer("hint").setVisible(false));
		}
		
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Nullable
	private Component newValueHint(String componentId) {
		Fragment fragment = new Fragment(componentId, "hintFrag", this);
		
		boolean hasHint = false;
		
		if (value instanceof Date) {
			fragment.add(new Fragment("typeHint", "dateHintFrag", this));
			hasHint = true;
		} else if (value instanceof Enum && getDeclaredClass() != null) {
			Fragment typeHintFrag = new Fragment("typeHint", "enumHintFrag", this);
			List<String> possibleValues = new ArrayList<>();
			for (Object each: EnumSet.allOf((Class<Enum>) getDeclaredClass()))
				possibleValues.add(((Enum<?>)each).name());
			
			typeHintFrag.add(new Label("possibleValues", StringUtils.join(possibleValues)));
			fragment.add(typeHintFrag);
			hasHint = true;
		} else if (getField() != null) {
			if (getField().getAnnotation(ManyToOne.class) != null 
					|| getField().getAnnotation(EntityId.class) != null) {
				Class<?> entityClass;
				if (value instanceof AbstractEntity)
					entityClass = value.getClass();
				else
					entityClass = getField().getAnnotation(EntityId.class).value();
				Class<?> resourceClass = resourceMap.get(entityClass);
				if (resourceClass != null) {
					Fragment typeHintFrag = new Fragment("typeHint", "entityIdHintFrag", this);
					Link<Void> link = new ViewStateAwarePageLink<Void>("entity", ResourceDetailPage.class, 
							ResourceDetailPage.paramsOf(resourceClass));
					link.add(new Label("label", WordUtils.uncamel(resourceClass.getSimpleName()).toLowerCase()));
					typeHintFrag.add(link);
					fragment.add(typeHintFrag);
					hasHint = true;
				} else {
					fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
				}
			} else if (getField().getAnnotation(Id.class) != null) {
				if (requestBody) {
					fragment.add(new Fragment("typeHint", "idOfCurrentEntityInRequestBodyHintFrag", this));
					hasHint = true;
				} else {
					fragment.add(new Fragment("typeHint", "idOfCurrentEntityHintFrag", this));
					hasHint = true;
				}
			} else {
				fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
			}
		} else {
			fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
		}
		
		String description = "";
		if (getField() != null && getField().getAnnotation(Api.class) != null)
			description = getField().getAnnotation(Api.class).description();
		if (description.length() == 0 && value != null 
				&& value.getClass().getAnnotation(Api.class) != null) {
			description = value.getClass().getAnnotation(Api.class).description();
		}
		if (description.length() != 0) {
			hasHint = true;
			fragment.add(new Label("description", description));
		} else {
			fragment.add(new WebMarkupContainer("description").setVisible(false));
		}
		
		if (hasHint)
			return fragment;
		else
			return null;
	}
	
	private Fragment newScalarFragment(Object value) {
		Fragment fragment = new Fragment("content", "scalarFrag", this);
		fragment.add(new Label("value", toJson(value)));
		return fragment;
	}
	
	private static IModel<ValueInfo> newNullValueModel() {
		return new AbstractReadOnlyModel<ValueInfo>() {

			@Override
			public ValueInfo getObject() {
				return null;
			}
			
		};
	}
	
	private Fragment newArrayFragment() {
		Fragment fragment = new Fragment("content", "arrayFrag", this);
		fragment.add(new ListView<Object>("elements", new ArrayList<Object>(((Collection<?>)value))) {

			@Override
			protected void populateItem(ListItem<Object> item) {
				IModel<ValueInfo> valueInfoModel;
				if (getValueInfo() != null) {
					valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

						@Override
						protected ValueInfo load() {
							Field field = getField();
							Type declaredType = ReflectionUtils.getCollectionElementType(getDeclaredType());
							return new ValueInfo(field, declaredType);
						}
						
					};
				} else {
					valueInfoModel = newNullValueModel();
				}
				item.add(new ExampleValuePanel("element", item.getModelObject(), valueInfoModel, requestBody));
				item.add(new WebMarkupContainer("comma")
						.setVisible(item.getIndex() < getModelObject().size()-1));
			}
			
		});
		return fragment;
	}
	
	private Fragment newMapFragment() {
		Fragment fragment = new Fragment("content", "objectFrag", this);
		
		fragment.add(new WebMarkupContainer("typeInfo").setVisible(false));
		
		IModel<List<Map.Entry<?, ?>>> entriesModel = new LoadableDetachableModel<List<Map.Entry<?, ?>>>() {

			@Override
			protected List<Map.Entry<?, ?>> load() {
				return new ArrayList<Map.Entry<?, ?>>(((Map<?, ?>)value).entrySet());
			}
			
		};
		
		fragment.add(new ListView<Map.Entry<?, ?>>("properties", entriesModel) {

			@Override
			protected void populateItem(ListItem<Map.Entry<?, ?>> item) {
				IModel<ValueInfo> valueInfoModel;
				if (getValueInfo() != null) {
					valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

						@Override
						protected ValueInfo load() {
							Field field = getField();
							Type declaredType = ReflectionUtils.getMapKeyType(getDeclaredType());
							return new ValueInfo(field, declaredType);
						}
						
					};
				} else {
					valueInfoModel = newNullValueModel();
				}
				item.add(new ExampleValuePanel("name", item.getModelObject().getKey(), valueInfoModel, requestBody));
				
				if (getValueInfo() != null) {
					valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

						@Override
						protected ValueInfo load() {
							Field field = getField();
							Type declaredType = ReflectionUtils.getMapValueType(getDeclaredType());
							return new ValueInfo(field, declaredType);
						}
						
					};
				} else {
					valueInfoModel = newNullValueModel();
				}
				item.add(new ExampleValuePanel("value", item.getModelObject().getValue(), valueInfoModel, requestBody));
				item.add(new WebMarkupContainer("comma")
						.setVisible(item.getIndex() < getModelObject().size()-1));
			}
			
		});
		return fragment;
	}
	
	private Fragment newObjectFragment() {
		Fragment fragment = new Fragment("content", "objectFrag", this);
		
		IModel<List<Field>> fieldsModel = new LoadableDetachableModel<List<Field>>() {

			@Override
			protected List<Field> load() {
				return ApiHelpUtils.getJsonFields(value.getClass());
			}
			
		};
		
		if (getDeclaredClass() != null 
				&& Modifier.isAbstract(getDeclaredClass().getModifiers())
				&& !Collection.class.isAssignableFrom(getDeclaredClass())
				&& !Map.class.isAssignableFrom(getDeclaredClass())) {
			Fragment typeInfoFragment = new Fragment("typeInfo", "typeInfoFrag", this);
			typeInfoFragment.add(new ExampleValuePanel("name", JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), requestBody));
			typeInfoFragment.add(new ExampleValuePanel("value", value.getClass().getName(), requestBody));
			
			typeInfoFragment.add(new MenuLink("implementations") {

				@Override
				protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
					List<MenuItem> items = new ArrayList<>();
					ImplementationRegistry registry = OneDev.getInstance(ImplementationRegistry.class);
					for (Class<?> clazz: registry.getImplementations(getDeclaredClass())) {
						items.add(new MenuItem() {

							@Override
							public String getLabel() {
								return clazz.getName();
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										Object newValue = ApiHelpUtils.getExampleValue(clazz);
										Component newExampleValuePanel = new ExampleValuePanel(
												ExampleValuePanel.this.getId(), newValue, valueInfoModel, requestBody);
										ExampleValuePanel.this.replaceWith(newExampleValuePanel);
										target.add(newExampleValuePanel);
									}
									
								};
							}
							
						});
					}
					return items;
				}
				
			});
			
			typeInfoFragment.add(new WebMarkupContainer("comma")
					.setVisible(!fieldsModel.getObject().isEmpty()));
			fragment.add(typeInfoFragment);
		} else {
			fragment.add(new WebMarkupContainer("typeInfo").setVisible(false));
		}
		
		fragment.add(new ListView<Field>("properties", fieldsModel) {

			@Override
			protected void populateItem(ListItem<Field> item) {
				Field field = item.getModelObject();
				if (field.getAnnotation(ManyToOne.class) != null)
					item.add(new ExampleValuePanel("name", field.getName() + "Id", requestBody));
				else
					item.add(new ExampleValuePanel("name", field.getName(), requestBody));
				
				field.setAccessible(true);
				try {
					IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

						@Override
						protected ValueInfo load() {
							Field field = item.getModelObject();
							return new ValueInfo(field, field.getGenericType());
						}
						
					};
					item.add(new ExampleValuePanel("value", field.get(value), valueInfoModel, requestBody));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				item.add(new WebMarkupContainer("comma")
						.setVisible(item.getIndex() < getModelObject().size()-1));
			}
			
		});
		return fragment;
	}

	@Nullable
	private ValueInfo getValueInfo() {
		return valueInfoModel.getObject();
	}
	
	@Nullable
	private Field getField() {
		return getValueInfo()!=null? getValueInfo().getField(): null;
	}

	@Nullable
	private Type getDeclaredType() {
		return getValueInfo()!=null? getValueInfo().getDeclaredType(): null;
	}
	
	@Nullable
	private Class<?> getDeclaredClass() {
		if (getDeclaredType() != null) 
			return ReflectionUtils.getClass(getDeclaredType());
		else 
			return null;
	}
	
	private String toJson(Object value) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private static class ValueInfo {
		
		private final Field field;
		
		private final Type declaredType;
		
		public ValueInfo(Field field, Type declaredType) {
			this.field = field;
			this.declaredType = declaredType;
		}

		public Field getField() {
			return field;
		}

		public Type getDeclaredType() {
			return declaredType;
		}
		
	}
}

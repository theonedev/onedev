package io.onedev.server.web.page.help;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.persistence.ManyToOne;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
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
import org.apache.wicket.model.Model;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.launcher.loader.ImplementationRegistry;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
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
	
	private static final Pattern GET_ENTITY_PATH = Pattern.compile("\\/\\{[a-zA-Z_\\-0-9]+\\}");
	
	static {
		ResourceConfig config = OneDev.getInstance(ResourceConfig.class);
		
		for (Class<?> clazz: config.getClasses()) {
			if (clazz.getAnnotation(Path.class) != null) { 
				Api api = clazz.getAnnotation(Api.class);
				if (api == null || !api.internal()) {
					for (Method method: clazz.getMethods()) {
						if (AbstractEntity.class.isAssignableFrom(method.getReturnType()) 
								&& method.getAnnotation(GET.class) != null
								&& method.getAnnotation(Path.class) != null 
								&& GET_ENTITY_PATH.matcher(method.getAnnotation(Path.class).value()).matches()) {
							resourceMap.put(method.getReturnType(), clazz);
						}
					}
				}
			}
		} 
	}
	
	private final IModel<Serializable> valueModel;
	
	private final IModel<ValueInfo> valueInfoModel;
	
	private final Class<?> requestBodyClass;
	
	public ExampleValuePanel(String id, IModel<Serializable> valueModel, IModel<ValueInfo> valueInfoModel, 
			@Nullable Class<?> requestBodyClass) {
		super(id);
		this.valueModel = valueModel;
		this.valueInfoModel = valueInfoModel;
		this.requestBodyClass = requestBodyClass;
	}

	@Override
	protected void onDetach() {
		valueModel.detach();
		valueInfoModel.detach();
		super.onDetach();
	}
	
	@Nullable
	private Serializable getValue() {
		return valueModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		if (getValue() == null || getValue().getClass() == String.class
				|| getValue().getClass() == boolean.class || getValue().getClass() == Boolean.class
				|| getValue().getClass() == int.class || getValue().getClass() == Integer.class
				|| getValue().getClass() == long.class || getValue().getClass() == Long.class
				|| getValue().getClass() == Date.class || getValue() instanceof Enum) {
			addOrReplace(newScalarFragment(getValue()));
		} else if (getField() != null && getField().getAnnotation(ManyToOne.class) != null) {
			addOrReplace(newScalarFragment(((AbstractEntity)getValue()).getId()));
		} else if (getValue() instanceof Collection) {
			addOrReplace(newArrayFragment());
		} else if (getValue() instanceof Map) {
			addOrReplace(newMapFragment());
		} else {
			addOrReplace(newObjectFragment());
		}
		
		if (newValueHint("psuedoId") != null) {
			addOrReplace(new DropdownLink("hint") {
	
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return newValueHint(id);
				}
				
			});
		} else {
			addOrReplace(new WebMarkupContainer("hint").setVisible(false));
		}
		
		super.onBeforeRender();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Nullable
	private Component newValueHint(String componentId) {
		Fragment fragment = new Fragment(componentId, "hintFrag", this);
		
		boolean hasHint = false;
		
		if (getValue() instanceof Date) {
			fragment.add(new Fragment("typeHint", "dateHintFrag", this));
			hasHint = true;
		} else if (getValue() instanceof Enum && getDeclaredClass() != null) {
			Fragment typeHintFrag = new Fragment("typeHint", "enumHintFrag", this);
			List<String> possibleValues = new ArrayList<>();
			for (Object each: EnumSet.allOf((Class<Enum>) getDeclaredClass()))
				possibleValues.add(((Enum<?>)each).name());
			
			typeHintFrag.add(new Label("possibleValues", StringUtils.join(possibleValues)));
			fragment.add(typeHintFrag);
			hasHint = true;
		} else if (getField() != null) {
			if (getField().getAnnotation(ManyToOne.class) != null || getField().getAnnotation(EntityId.class) != null) {
				Class<?> entityClass;
				if (getValue() instanceof AbstractEntity)
					entityClass = getValue().getClass();
				else
					entityClass = getField().getAnnotation(EntityId.class).value();
				Class<?> resourceClass = resourceMap.get(entityClass);
				if (resourceClass != null) {
					Fragment typeHintFrag = new Fragment("typeHint", "entityIdHintFrag", this);
					Link<Void> link = new ViewStateAwarePageLink<Void>("entity", ResourceDetailPage.class, 
							ResourceDetailPage.paramsOf(resourceClass));
					link.add(new Label("label", WordUtils.uncamel(entityClass.getSimpleName()).toLowerCase()));
					typeHintFrag.add(link);
					fragment.add(typeHintFrag);
					hasHint = true;
				} else {
					fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
				}
			} else {
				fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
			}
		} else if (getValue() instanceof Long 
				&& getValueOrigin() == ValueInfo.Origin.RESPONSE_BODY 
				&& requestBodyClass != null) {
			if (AbstractEntity.class.isAssignableFrom(requestBodyClass)) {
				Class<?> resourceClass = resourceMap.get(requestBodyClass);
				if (resourceClass != null) {
					Fragment typeHintFrag = new Fragment("typeHint", "idAsResultOfEntityCreateOrUpdateHintFrag", this);
					
					Link<Void> link = new ViewStateAwarePageLink<Void>("entity", ResourceDetailPage.class, 
							ResourceDetailPage.paramsOf(resourceClass));
					String entityName = WordUtils.uncamel(requestBodyClass.getSimpleName()).toLowerCase();
					link.add(new Label("label", entityName));
					typeHintFrag.add(link);
					fragment.add(typeHintFrag);
					hasHint = true;
				} else {
					fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
				}
			} else if (requestBodyClass.getAnnotation(EntityCreate.class) != null) {
				Class<?> entityClass = requestBodyClass.getAnnotation(EntityCreate.class).value();
				Class<?> resourceClass = resourceMap.get(entityClass);
				if (resourceClass != null) {
					Fragment typeHintFrag = new Fragment("typeHint", "idAsResultOfEntityCreateHintFrag", this);
					Link<Void> link = new ViewStateAwarePageLink<Void>("entity", ResourceDetailPage.class, 
							ResourceDetailPage.paramsOf(resourceClass));
					String entityName = WordUtils.uncamel(entityClass.getSimpleName()).toLowerCase();
					link.add(new Label("label", entityName));
					typeHintFrag.add(link);
					fragment.add(typeHintFrag);
					hasHint = true;
				} else {
					fragment.add(new WebMarkupContainer("typeHint").setVisible(false));
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
		if (description.length() == 0 && getValue() != null 
				&& getValue().getClass().getAnnotation(Api.class) != null) {
			description = getValue().getClass().getAnnotation(Api.class).description();
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
	
	private Fragment newScalarFragment(Serializable value) {
		Fragment fragment = new Fragment("content", "scalarFrag", this);
		if (getValueOrigin() == ValueInfo.Origin.REQUEST_BODY || getValueOrigin() == ValueInfo.Origin.RESPONSE_BODY)
			fragment.add(new Label("value", toJson(value)));
		else
			fragment.add(new Label("value", String.valueOf(value)));
			
		return fragment;
	}
	
	private Fragment newArrayFragment() {
		Fragment fragment = new Fragment("content", "arrayFrag", this);
		fragment.add(new ListView<Serializable>("elements", new AbstractReadOnlyModel<List<Serializable>>() {

			@SuppressWarnings("unchecked")
			@Override
			public List<Serializable> getObject() {
				return new ArrayList<Serializable>((Collection<? extends Serializable>) getValue());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Serializable> item) {
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						Type declaredType = ReflectionUtils.getCollectionElementType(getDeclaredType());
						return new ValueInfo(getValueOrigin(), declaredType, null);
					}
					
				};

				item.add(new ExampleValuePanel("element", new IModel<Serializable>() {

					@Override
					public void detach() {
					}

					@Override
					public Serializable getObject() {
						return item.getModelObject();
					}

					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public void setObject(Serializable object) {
						Collection collection = (Collection)getValue();
						collection.remove(item.getModelObject());
						collection.add(object);
					}
					
				}, valueInfoModel, requestBodyClass));
				
				item.add(new WebMarkupContainer("comma")
						.setVisible(item.getIndex() < getModelObject().size()-1));
			}
			
		});
		return fragment;
	}
	
	private Fragment newMapFragment() {
		Fragment fragment = new Fragment("content", "objectFrag", this);
		
		fragment.add(new WebMarkupContainer("typeInfo").setVisible(false));
		
		IModel<List<Map.Entry<Serializable, Serializable>>> entriesModel = 
				new AbstractReadOnlyModel<List<Map.Entry<Serializable, Serializable>>>() {

			@SuppressWarnings("unchecked")
			@Override
			public List<Map.Entry<Serializable, Serializable>> getObject() {
				return new ArrayList<Map.Entry<Serializable, Serializable>>(((Map<Serializable, Serializable>)getValue()).entrySet());
			}
			
		};
		
		fragment.add(new ListView<Map.Entry<Serializable, Serializable>>("properties", entriesModel) {

			@Override
			protected void populateItem(ListItem<Map.Entry<Serializable, Serializable>> item) {
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						Type declaredType = ReflectionUtils.getMapKeyType(getDeclaredType());
						return new ValueInfo(getValueOrigin(), declaredType, null);
					}
					
				};
				item.add(new ExampleValuePanel("name", Model.of(item.getModelObject().getKey()), valueInfoModel, requestBodyClass));
				
				valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						Type declaredType = ReflectionUtils.getMapValueType(getDeclaredType());
						return new ValueInfo(getValueOrigin(), declaredType, null);
					}
						
				};
				item.add(new ExampleValuePanel("value", new IModel<Serializable>() {

					@Override
					public void detach() {
					}

					@Override
					public Serializable getObject() {
						return item.getModelObject().getValue();
					}

					@SuppressWarnings("unchecked")
					@Override
					public void setObject(Serializable object) {
						Map<Serializable, Serializable> map = (Map<Serializable, Serializable>)getValue();
						map.put(item.getModelObject().getKey(), object);
					}
					
				}, valueInfoModel, requestBodyClass));
				
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
				return ApiHelpUtils.getJsonFields(getValue().getClass());
			}
			
		};
		
		if (getDeclaredClass() != null 
				&& Modifier.isAbstract(getDeclaredClass().getModifiers())
				&& !Collection.class.isAssignableFrom(getDeclaredClass())
				&& !Map.class.isAssignableFrom(getDeclaredClass())) {
			Fragment typeInfoFragment = new Fragment("typeInfo", "typeInfoFrag", this);
			
			IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

				@Override
				protected ValueInfo load() {
					return new ValueInfo(getValueOrigin(), String.class, null);
				}
				
			};
			typeInfoFragment.add(new ExampleValuePanel("name", Model.of(JsonTypeInfo.Id.CLASS.getDefaultPropertyName()), 
					valueInfoModel, requestBodyClass));
			typeInfoFragment.add(new ExampleValuePanel("value", new AbstractReadOnlyModel<Serializable>() {

				@Override
				public Serializable getObject() {
					return getValue().getClass().getSimpleName();
				}
				
			}, valueInfoModel, requestBodyClass));
			
			typeInfoFragment.add(new MenuLink("implementations") {

				@Override
				protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
					List<MenuItem> items = new ArrayList<>();
					List<Class<?>> implementations = new ArrayList<>(
							OneDev.getInstance(ImplementationRegistry.class).getImplementations(getDeclaredClass()));
					Collections.sort(implementations, new Comparator<Class<?>>() {

						@Override
						public int compare(Class<?> o1, Class<?> o2) {
							return o1.getName().compareTo(o2.getName());
						}
						
					});
					
					for (Class<?> clazz: implementations) {
						items.add(new MenuItem() {

							@Override
							public String getLabel() {
								return clazz.getSimpleName();
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										Serializable newValue = ApiHelpUtils.getExampleValue(clazz);
										valueModel.setObject(newValue);
										target.add(ExampleValuePanel.this);
										send(getPage(), Broadcast.BREADTH, new ExampleValueChanged(target));
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
				
				IModel<ValueInfo> valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						return new ValueInfo(getValueOrigin(), String.class, null);
					}
					
				};
				if (field.getAnnotation(ManyToOne.class) != null)
					item.add(new ExampleValuePanel("name", Model.of(field.getName() + "Id"), valueInfoModel, requestBodyClass));
				else
					item.add(new ExampleValuePanel("name", Model.of(field.getName()), valueInfoModel, requestBodyClass));
				
				field.setAccessible(true);
				valueInfoModel = new LoadableDetachableModel<ValueInfo>() {

					@Override
					protected ValueInfo load() {
						Field field = item.getModelObject();
						return new ValueInfo(getValueOrigin(), field.getGenericType(), field);
					}
					
				};
				item.add(new ExampleValuePanel("value", new IModel<Serializable>() {

					@Override
					public void detach() {
					}

					@Override
					public Serializable getObject() {
						try {
							Field field = item.getModelObject();
							field.setAccessible(true);
							return (Serializable) field.get(getValue());
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public void setObject(Serializable object) {
						try {
							Field field = item.getModelObject();
							field.setAccessible(true);
							field.set(getValue(), object);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
					
				}, valueInfoModel, requestBodyClass));
				
				item.add(new WebMarkupContainer("comma")
						.setVisible(item.getIndex() < getModelObject().size()-1));
			}
			
		});
		return fragment;
	}

	private ValueInfo getValueInfo() {
		return valueInfoModel.getObject();
	}
	
	@Nullable
	private Field getField() {
		return getValueInfo().getField();
	}

	private Type getDeclaredType() {
		return getValueInfo().getDeclaredType();
	}
	
	private Class<?> getDeclaredClass() {
		return ReflectionUtils.getClass(getDeclaredType());
	}
	
	private ValueInfo.Origin getValueOrigin() {
		return getValueInfo().getOrigin();
	}
	
	private String toJson(Object value) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}

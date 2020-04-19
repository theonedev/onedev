/**
 * Copyright (c) 2008, http://www.snakeyaml.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yaml.snakeyaml.constructor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import io.onedev.server.util.BeanUtils;
import io.onedev.server.web.editable.annotation.Editable;

public abstract class BaseConstructor {
    /**
     * It maps the node kind to the the Construct implementation. When the
     * runtime class is known then the implicit tag is ignored.
     */
    protected final Map<NodeId, Construct> yamlClassConstructors = new EnumMap<NodeId, Construct>(
            NodeId.class);
    /**
     * It maps the (explicit or implicit) tag to the Construct implementation.
     * It is used:
     * 1) explicit tag - if present.
     * 2) implicit tag - when the runtime class of the instance is unknown (the
     * node has the Object.class)
     */
    protected final Map<Tag, Construct> yamlConstructors = new HashMap<Tag, Construct>();
    /**
     * It maps the (explicit or implicit) tag to the Construct implementation.
     * It is used when no exact match found.
     */
    protected final Map<String, Construct> yamlMultiConstructors = new HashMap<String, Construct>();

    protected Composer composer;
    final Map<Node, Object> constructedObjects;
    private final Set<Node> recursiveObjects;
    private final ArrayList<RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>>> maps2fill;
    private final ArrayList<RecursiveTuple<Set<Object>, Object>> sets2fill;

    protected Tag rootTag;
    private PropertyUtils propertyUtils;
    private boolean explicitPropertyUtils;
    private boolean allowDuplicateKeys = true;

    protected final Map<Class<? extends Object>, TypeDescription> typeDefinitions;
    protected final Map<Tag, Class<? extends Object>> typeTags;

    public BaseConstructor() {
        constructedObjects = new HashMap<Node, Object>();
        recursiveObjects = new HashSet<Node>();
        maps2fill = new ArrayList<RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>>>();
        sets2fill = new ArrayList<RecursiveTuple<Set<Object>, Object>>();
        typeDefinitions = new HashMap<Class<? extends Object>, TypeDescription>();
        typeTags = new HashMap<Tag, Class<? extends Object>>();

        rootTag = null;
        explicitPropertyUtils = false;

        typeDefinitions.put(SortedMap.class, new TypeDescription(SortedMap.class, Tag.OMAP,
                TreeMap.class));
        typeDefinitions.put(SortedSet.class, new TypeDescription(SortedSet.class, Tag.SET,
                TreeSet.class));
    }

    public void setComposer(Composer composer) {
        this.composer = composer;
    }

    /**
     * Check if more documents available
     *
     * @return true when there are more YAML documents in the stream
     */
    public boolean checkData() {
        // If there are more documents available?
        return composer.checkNode();
    }

    /**
     * Construct and return the next document
     *
     * @return constructed instance
     */
    public Object getData() {
        // Construct and return the next document.
        composer.checkNode();
        Node node = composer.getNode();
        if (rootTag != null) {
            node.setTag(rootTag);
        }
        return constructDocument(node);
    }

    /**
     * Ensure that the stream contains a single document and construct it
     *
     * @param type the class of the instance being created
     * @return constructed instance
     * @throws ComposerException in case there are more documents in the stream
     */
    public Object getSingleData(Class<?> type) {
        // Ensure that the stream contains a single document and construct it
        Node node = composer.getSingleNode();
        if (node != null && !Tag.NULL.equals(node.getTag())) {
            if (Object.class != type) {
                node.setTag(new Tag(type));
            } else if (rootTag != null) {
                node.setTag(rootTag);
            }
            return constructDocument(node);
        }
        return null;
    }

    /**
     * Construct complete YAML document. Call the second step in case of
     * recursive structures. At the end cleans all the state.
     *
     * @param node root Node
     * @return Java instance
     */
    protected final Object constructDocument(Node node) {
        Object data = constructObject(node);
        fillRecursive();
        constructedObjects.clear();
        recursiveObjects.clear();
        return data;
    }

    private void fillRecursive() {
        if (!maps2fill.isEmpty()) {
            for (RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>> entry : maps2fill) {
                RecursiveTuple<Object, Object> key_value = entry._2();
                entry._1().put(key_value._1(), key_value._2());
            }
            maps2fill.clear();
        }
        if (!sets2fill.isEmpty()) {
            for (RecursiveTuple<Set<Object>, Object> value : sets2fill) {
                value._1().add(value._2());
            }
            sets2fill.clear();
        }
    }

    /**
     * Construct object from the specified Node. Return existing instance if the
     * node is already constructed.
     *
     * @param node Node to be constructed
     * @return Java instance
     */
    protected Object constructObject(Node node) {
        if (constructedObjects.containsKey(node)) {
            return constructedObjects.get(node);
        }
        return constructObjectNoCheck(node);
    }

    protected Object constructObjectNoCheck(Node node) {
        if (recursiveObjects.contains(node)) {
            throw new ConstructorException(null, null, "found unconstructable recursive node",
                    node.getStartMark());
        }
        recursiveObjects.add(node);
        Construct constructor = getConstructor(node);
        Object data = (constructedObjects.containsKey(node)) ? constructedObjects.get(node)
                : constructor.construct(node);

        finalizeConstruction(node, data);
        constructedObjects.put(node, data);
        recursiveObjects.remove(node);
        if (node.isTwoStepsConstruction()) {
            constructor.construct2ndStep(node, data);
        }
        return data;
    }

    /**
     * Get the constructor to construct the Node. For implicit tags if the
     * runtime class is known a dedicated Construct implementation is used.
     * Otherwise the constructor is chosen by the tag.
     *
     * @param node {@link Node} to construct an instance from
     * @return {@link Construct} implementation for the specified node
     */
    protected Construct getConstructor(Node node) {
        if (node.useClassConstructor()) {
            return yamlClassConstructors.get(node.getNodeId());
        } else {
            Construct constructor = yamlConstructors.get(node.getTag());
            if (constructor == null) {
                for (String prefix : yamlMultiConstructors.keySet()) {
                    if (node.getTag().startsWith(prefix)) {
                        return yamlMultiConstructors.get(prefix);
                    }
                }
                return yamlConstructors.get(null);
            }
            return constructor;
        }
    }

    protected String constructScalar(ScalarNode node) {
        return node.getValue();
    }

    // >>>> DEFAULTS >>>>
    protected List<Object> createDefaultList(int initSize) {
        return new ArrayList<Object>(initSize);
    }

    protected Set<Object> createDefaultSet(int initSize) {
        return new LinkedHashSet<Object>(initSize);
    }

    protected Map<Object, Object> createDefaultMap(int initSize) {
        // respect order from YAML document
        return new LinkedHashMap<Object, Object>(initSize);
    }

    protected Object createArray(Class<?> type, int size) {
        return Array.newInstance(type.getComponentType(), size);
    }

    // <<<< DEFAULTS <<<<

    protected Object finalizeConstruction(Node node, Object data) {
        final Class<? extends Object> type = node.getType();
        if (typeDefinitions.containsKey(type)) {
            return typeDefinitions.get(type).finalizeConstruction(data);
        }
        return data;
    }

    // >>>> NEW instance
    protected Object newInstance(Node node) {
        try {
            return newInstance(Object.class, node);
        } catch (InstantiationException e) {
            throw new YAMLException(e);
        }
    }

    final protected Object newInstance(Class<?> ancestor, Node node) throws InstantiationException {
        return newInstance(ancestor, node, true);
    }

    protected Object newInstance(Class<?> ancestor, Node node, boolean tryDefault)
            throws InstantiationException {
        final Class<? extends Object> type = node.getType();
        if (typeDefinitions.containsKey(type)) {
            TypeDescription td = typeDefinitions.get(type);
            final Object instance = td.newInstance(node);
            if (instance != null) {
                return instance;
            }
        }
        if (tryDefault) {
            /*
             * Removed <code> have InstantiationException in case of abstract
             * type
             */
            if (ancestor.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers())) {
                try {
                    java.lang.reflect.Constructor<?> c = type.getDeclaredConstructor();
                    c.setAccessible(true);
                    Object instance = c.newInstance();
        			if (type.getAnnotation(Editable.class) != null) {
        				for (Method getter: BeanUtils.findGetters(type)) {
        					if (getter.getAnnotation(Editable.class) != null) {
        						Method setter = BeanUtils.findSetter(getter);
        						if (setter != null) {
    								if (Collection.class.isAssignableFrom(getter.getReturnType())) 
    									setter.invoke(instance, new Object[] {new ArrayList<>()});
    								else if (Map.class.isAssignableFrom(getter.getReturnType())) 
    									setter.invoke(instance, new Object[] {new LinkedHashMap<>()});
    								else if (Object.class.isAssignableFrom(getter.getReturnType()))
    									setter.invoke(instance, new Object[] {null});
        						}
        					}
        				}
        			}
        			return instance;
                } catch (NoSuchMethodException e) {
                    throw new InstantiationException("NoSuchMethodException:"
                            + e.getLocalizedMessage());
                } catch (Exception e) {
                    throw new YAMLException(e);
                }
            }
        }
        throw new InstantiationException();
    }

    @SuppressWarnings("unchecked")
    protected Set<Object> newSet(CollectionNode<?> node) {
        try {
            return (Set<Object>) newInstance(Set.class, node);
        } catch (InstantiationException e) {
            return createDefaultSet(node.getValue().size());
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Object> newList(SequenceNode node) {
        try {
            return (List<Object>) newInstance(List.class, node);
        } catch (InstantiationException e) {
            return createDefaultList(node.getValue().size());
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<Object, Object> newMap(MappingNode node) {
        try {
            return (Map<Object, Object>) newInstance(Map.class, node);
        } catch (InstantiationException e) {
            return createDefaultMap(node.getValue().size());
        }
    }

    // <<<< NEW instance

    // >>>> Construct => NEW, 2ndStep(filling)
    protected List<? extends Object> constructSequence(SequenceNode node) {
        List<Object> result = newList(node);
        constructSequenceStep2(node, result);
        return result;
    }

    protected Set<? extends Object> constructSet(SequenceNode node) {
        Set<Object> result = newSet(node);
        constructSequenceStep2(node, result);
        return result;
    }

    protected Object constructArray(SequenceNode node) {
        return constructArrayStep2(node, createArray(node.getType(), node.getValue().size()));
    }

    protected void constructSequenceStep2(SequenceNode node, Collection<Object> collection) {
        for (Node child : node.getValue()) {
            collection.add(constructObject(child));
        }
    }

    protected Object constructArrayStep2(SequenceNode node, Object array) {
        final Class<?> componentType = node.getType().getComponentType();

        int index = 0;
        for (Node child : node.getValue()) {
            // Handle multi-dimensional arrays...
            if (child.getType() == Object.class) {
                child.setType(componentType);
            }

            final Object value = constructObject(child);

            if (componentType.isPrimitive()) {
                // Null values are disallowed for primitives
                if (value == null) {
                    throw new NullPointerException(
                            "Unable to construct element value for " + child);
                }

                // Primitive arrays require quite a lot of work.
                if (byte.class.equals(componentType)) {
                    Array.setByte(array, index, ((Number) value).byteValue());

                } else if (short.class.equals(componentType)) {
                    Array.setShort(array, index, ((Number) value).shortValue());

                } else if (int.class.equals(componentType)) {
                    Array.setInt(array, index, ((Number) value).intValue());

                } else if (long.class.equals(componentType)) {
                    Array.setLong(array, index, ((Number) value).longValue());

                } else if (float.class.equals(componentType)) {
                    Array.setFloat(array, index, ((Number) value).floatValue());

                } else if (double.class.equals(componentType)) {
                    Array.setDouble(array, index, ((Number) value).doubleValue());

                } else if (char.class.equals(componentType)) {
                    Array.setChar(array, index, ((Character) value).charValue());

                } else if (boolean.class.equals(componentType)) {
                    Array.setBoolean(array, index, ((Boolean) value).booleanValue());

                } else {
                    throw new YAMLException("unexpected primitive type");
                }

            } else {
                // Non-primitive arrays can simply be assigned:
                Array.set(array, index, value);
            }

            ++index;
        }
        return array;
    }

    protected Set<Object> constructSet(MappingNode node) {
        final Set<Object> set = newSet(node);
        constructSet2ndStep(node, set);
        return set;
    }

    protected Map<Object, Object> constructMapping(MappingNode node) {
        final Map<Object, Object> mapping = newMap(node);
        constructMapping2ndStep(node, mapping);
        return mapping;
    }

    protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
        List<NodeTuple> nodeValue = node.getValue();
        for (NodeTuple tuple : nodeValue) {
            Node keyNode = tuple.getKeyNode();
            Node valueNode = tuple.getValueNode();
            Object key = constructObject(keyNode);
            if (key != null) {
                try {
                    key.hashCode();// check circular dependencies
                } catch (Exception e) {
                    throw new ConstructorException("while constructing a mapping",
                            node.getStartMark(), "found unacceptable key " + key,
                            tuple.getKeyNode().getStartMark(), e);
                }
            }
            Object value = constructObject(valueNode);
            if (keyNode.isTwoStepsConstruction()) {
                /*
                 * if keyObject is created it 2 steps we should postpone putting
                 * it in map because it may have different hash after
                 * initialization compared to clean just created one. And map of
                 * course does not observe key hashCode changes.
                 */
                maps2fill.add(0,
                        new RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>>(
                                mapping, new RecursiveTuple<Object, Object>(key, value)));
            } else {
                mapping.put(key, value);
            }
        }
    }

    protected void constructSet2ndStep(MappingNode node, Set<Object> set) {
        List<NodeTuple> nodeValue = node.getValue();
        for (NodeTuple tuple : nodeValue) {
            Node keyNode = tuple.getKeyNode();
            Object key = constructObject(keyNode);
            if (key != null) {
                try {
                    key.hashCode();// check circular dependencies
                } catch (Exception e) {
                    throw new ConstructorException("while constructing a Set", node.getStartMark(),
                            "found unacceptable key " + key, tuple.getKeyNode().getStartMark(), e);
                }
            }
            if (keyNode.isTwoStepsConstruction()) {
                /*
                 * if keyObject is created it 2 steps we should postpone putting
                 * it into the set because it may have different hash after
                 * initialization compared to clean just created one. And set of
                 * course does not observe value hashCode changes.
                 */
                sets2fill.add(0, new RecursiveTuple<Set<Object>, Object>(set, key));
            } else {
                set.add(key);
            }
        }
    }

    // <<<< Costruct => NEW, 2ndStep(filling)

    // TODO protected List<Object[]> constructPairs(MappingNode node) {
    // List<Object[]> pairs = new LinkedList<Object[]>();
    // List<Node[]> nodeValue = (List<Node[]>) node.getValue();
    // for (Iterator<Node[]> iter = nodeValue.iterator(); iter.hasNext();) {
    // Node[] tuple = iter.next();
    // Object key = constructObject(Object.class, tuple[0]);
    // Object value = constructObject(Object.class, tuple[1]);
    // pairs.add(new Object[] { key, value });
    // }
    // return pairs;
    // }

    public void setPropertyUtils(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
        explicitPropertyUtils = true;
        Collection<TypeDescription> tds = typeDefinitions.values();
        for (TypeDescription typeDescription : tds) {
            typeDescription.setPropertyUtils(propertyUtils);
        }
    }

    public final PropertyUtils getPropertyUtils() {
        if (propertyUtils == null) {
            propertyUtils = new PropertyUtils();
        }
        return propertyUtils;
    }

    /**
     * Make YAML aware how to parse a custom Class. If there is no root Class
     * assigned in constructor then the 'root' property of this definition is
     * respected.
     *
     * @param definition to be added to the Constructor
     * @return the previous value associated with <tt>definition</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>definition</tt>.
     */
    public TypeDescription addTypeDescription(TypeDescription definition) {
        if (definition == null) {
            throw new NullPointerException("TypeDescription is required.");
        }
        Tag tag = definition.getTag();
        typeTags.put(tag, definition.getType());
        definition.setPropertyUtils(getPropertyUtils());
        return typeDefinitions.put(definition.getType(), definition);
    }

    private static class RecursiveTuple<T, K> {
        private final T _1;
        private final K _2;

        public RecursiveTuple(T _1, K _2) {
            this._1 = _1;
            this._2 = _2;
        }

        public K _2() {
            return _2;
        }

        public T _1() {
            return _1;
        }
    }

    public final boolean isExplicitPropertyUtils() {
        return explicitPropertyUtils;
    }

    public boolean isAllowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    public void setAllowDuplicateKeys(boolean allowDuplicateKeys) {
        this.allowDuplicateKeys = allowDuplicateKeys;
    }
}

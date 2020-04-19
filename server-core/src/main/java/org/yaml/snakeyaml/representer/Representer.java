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
package org.yaml.snakeyaml.representer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Represent JavaBeans
 */
public class Representer extends SafeRepresenter {

    protected Map<Class<? extends Object>, TypeDescription> typeDefinitions = Collections
            .emptyMap();

    public Representer() {
        this.representers.put(null, new RepresentJavaBean());
    }

    public TypeDescription addTypeDescription(TypeDescription td) {
        if (Collections.EMPTY_MAP == typeDefinitions) {
            typeDefinitions = new HashMap<Class<? extends Object>, TypeDescription>();
        }
        if (td.getTag() != null) {
            addClassTag(td.getType(), td.getTag());
        }
        td.setPropertyUtils(getPropertyUtils());
        return typeDefinitions.put(td.getType(), td);
    }

    @Override
    public void setPropertyUtils(PropertyUtils propertyUtils) {
        super.setPropertyUtils(propertyUtils);
        Collection<TypeDescription> tds = typeDefinitions.values();
        for (TypeDescription typeDescription : tds) {
            typeDescription.setPropertyUtils(propertyUtils);
        }
    }

    protected class RepresentJavaBean implements Represent {
        public Node representData(Object data) {
            return representJavaBean(getProperties(data.getClass()), data);
        }
    }

    /**
     * Tag logic:
     * - explicit root tag is set in serializer
     * - if there is a predefined class tag it is used
     * - a global tag with class name is always used as tag. The JavaBean parent
     * of the specified JavaBean may set another tag (tag:yaml.org,2002:map)
     * when the property class is the same as runtime class
     *
     * @param properties
     *            JavaBean getters
     * @param javaBean
     *            instance for Node
     * @return Node to get serialized
     */
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
        List<NodeTuple> value = new ArrayList<NodeTuple>(properties.size());
        Tag tag;
        Tag customTag = classTags.get(javaBean.getClass());
        tag = customTag != null ? customTag : new Tag("!" + javaBean.getClass().getSimpleName());
        // flow style will be chosen by BaseRepresenter
        MappingNode node = new MappingNode(tag, value, FlowStyle.AUTO);
        representedObjects.put(javaBean, node);
        DumperOptions.FlowStyle bestStyle = FlowStyle.FLOW;
        for (Property property : properties) {
            Object memberValue = property.get(javaBean);
            Tag customPropertyTag = memberValue == null ? null
                    : classTags.get(memberValue.getClass());
            NodeTuple tuple = representJavaBeanProperty(javaBean, property, memberValue,
                    customPropertyTag);
            if (tuple == null) {
                continue;
            }
            if (!((ScalarNode) tuple.getKeyNode()).isPlain()) {
                bestStyle = FlowStyle.BLOCK;
            }
            Node nodeValue = tuple.getValueNode();
            if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            value.add(tuple);
        }
        if (defaultFlowStyle != FlowStyle.AUTO) {
            node.setFlowStyle(defaultFlowStyle);
        } else {
            node.setFlowStyle(bestStyle);
        }
        return node;
    }

    /**
     * Represent one JavaBean property.
     *
     * @param javaBean
     *            - the instance to be represented
     * @param property
     *            - the property of the instance
     * @param propertyValue
     *            - value to be represented
     * @param customTag
     *            - user defined Tag
     * @return NodeTuple to be used in a MappingNode. Return null to skip the
     *         property
     */
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
            Object propertyValue, Tag customTag) {
        ScalarNode nodeKey = (ScalarNode) representData(property.getName());
        // the first occurrence of the node must keep the tag
        boolean hasAlias = this.representedObjects.containsKey(propertyValue);

        Node nodeValue = representData(propertyValue);

        if (propertyValue != null && !hasAlias) {
            NodeId nodeId = nodeValue.getNodeId();
            if (customTag == null) {
                if (nodeId == NodeId.scalar) {
                    if (property.getType() == propertyValue.getClass()) {
                        if (propertyValue instanceof Enum<?>) {
                            nodeValue.setTag(Tag.STR);
                        }
                    }
                } else {
                    if (nodeId == NodeId.mapping) {
                        if (property.getType() == propertyValue.getClass()) {
                            if (!(propertyValue instanceof Map<?, ?>)) {
                                if (!nodeValue.getTag().equals(Tag.SET)) {
                                    nodeValue.setTag(Tag.MAP);
                                }
                            }
                        }
                    }
                    checkGlobalTag(property, nodeValue, propertyValue);
                }
            }
        }

        return new NodeTuple(nodeKey, nodeValue);
    }

    /**
     * Remove redundant global tag for a type safe (generic) collection if it is
     * the same as defined by the JavaBean property
     *
     * @param property
     *            - JavaBean property
     * @param node
     *            - representation of the property
     * @param object
     *            - instance represented by the node
     */
    @SuppressWarnings("unchecked")
    protected void checkGlobalTag(Property property, Node node, Object object) {
        // Skip primitive arrays.
        if (object.getClass().isArray() && object.getClass().getComponentType().isPrimitive()) {
            return;
        }

        Class<?>[] arguments = property.getActualTypeArguments();
        if (arguments != null) {
            if (node.getNodeId() == NodeId.sequence) {
                // apply map tag where class is the same
                Class<? extends Object> t = arguments[0];
                SequenceNode snode = (SequenceNode) node;
                Iterable<Object> memberList = Collections.EMPTY_LIST;
                if (object.getClass().isArray()) {
                    memberList = Arrays.asList((Object[]) object);
                } else if (object instanceof Iterable<?>) {
                    // list
                    memberList = (Iterable<Object>) object;
                }
                Iterator<Object> iter = memberList.iterator();
                if (iter.hasNext()) {
                    for (Node childNode : snode.getValue()) {
                        Object member = iter.next();
                        if (member != null) {
                            if (t.equals(member.getClass()))
                                if (childNode.getNodeId() == NodeId.mapping) {
                                    childNode.setTag(Tag.MAP);
                                }
                        }
                    }
                }
            } else if (object instanceof Set) {
                Class<?> t = arguments[0];
                MappingNode mnode = (MappingNode) node;
                Iterator<NodeTuple> iter = mnode.getValue().iterator();
                Set<?> set = (Set<?>) object;
                for (Object member : set) {
                    NodeTuple tuple = iter.next();
                    Node keyNode = tuple.getKeyNode();
                    if (t.equals(member.getClass())) {
                        if (keyNode.getNodeId() == NodeId.mapping) {
                            keyNode.setTag(Tag.MAP);
                        }
                    }
                }
            } else if (object instanceof Map) { // NodeId.mapping ends-up here
                Class<?> keyType = arguments[0];
                Class<?> valueType = arguments[1];
                MappingNode mnode = (MappingNode) node;
                for (NodeTuple tuple : mnode.getValue()) {
                    resetTag(keyType, tuple.getKeyNode());
                    resetTag(valueType, tuple.getValueNode());
                }
            } else {
                // the type for collection entries cannot be
                // detected
            }
        }
    }

    private void resetTag(Class<? extends Object> type, Node node) {
        Tag tag = node.getTag();
        if (tag.matches(type)) {
            if (Enum.class.isAssignableFrom(type)) {
                node.setTag(Tag.STR);
            } else {
                node.setTag(Tag.MAP);
            }
        }
    }

    /**
     * Get JavaBean properties to be serialised. The order is respected. This
     * method may be overridden to provide custom property selection or order.
     *
     * @param type
     *            - JavaBean to inspect the properties
     * @return properties to serialise
     */
    protected Set<Property> getProperties(Class<? extends Object> type) {
        if (typeDefinitions.containsKey(type)) {
            return typeDefinitions.get(type).getProperties();
        }
        return getPropertyUtils().getProperties(type);
    }
}

package io.onedev.server.migration;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Visitor;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.proxy.HibernateProxyHelper;
import org.xml.sax.EntityResolver;

import com.google.common.base.Preconditions;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.Dom4JReader;
import com.thoughtworks.xstream.io.xml.Dom4JWriter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;

public final class VersionedXmlDoc implements Document, Externalizable {

	private static final long serialVersionUID = 1L;

	private transient String xml;
	
	private transient Document wrapped;

	public VersionedXmlDoc() {
		wrapped = DocumentHelper.createDocument();
	}
	
	public VersionedXmlDoc(Document wrapped) {
		this.wrapped = Preconditions.checkNotNull(wrapped);
	}
	
	public synchronized void setWrapped(Document wrapped) {
		this.wrapped = Preconditions.checkNotNull(wrapped);
	}
	
	public Document addComment(String comment) {
		return getWrapped().addComment(comment);
	}

	public Document addDocType(String name, String publicId, String systemId) {
		return getWrapped().addDocType(name, publicId, systemId);
	}

	public Document addProcessingInstruction(String target, String text) {
		return getWrapped().addProcessingInstruction(target, text);
	}

	public Document addProcessingInstruction(String target, Map<String, String> data) {
		return getWrapped().addProcessingInstruction(target, data);
	}

	public DocumentType getDocType() {
		return getWrapped().getDocType();
	}

	public EntityResolver getEntityResolver() {
		return getWrapped().getEntityResolver();
	}

	public Element getRootElement() {
		return getWrapped().getRootElement();
	}

	public String getXMLEncoding() {
		return getWrapped().getXMLEncoding();
	}

	public void setDocType(DocumentType docType) {
		getWrapped().setDocType(docType);
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		getWrapped().setEntityResolver(entityResolver);
	}

	public void setRootElement(Element rootElement) {
		getWrapped().setRootElement(rootElement);
	}

	public void setXMLEncoding(String encoding) {
		getWrapped().setXMLEncoding(encoding);
	}

	public void add(Node node) {
		getWrapped().add(node);
	}

	public void add(Comment comment) {
		getWrapped().add(comment);
	}

	public void add(Element element) {
		getWrapped().add(element);
	}

	public void add(ProcessingInstruction pi) {
		getWrapped().add(pi);
	}

	public Element addElement(String name) {
		return getWrapped().addElement(name);
	}

	public Element addElement(QName qname) {
		return getWrapped().addElement(qname);
	}

	public Element addElement(String qualifiedName, String namespaceURI) {
		return getWrapped().addElement(qualifiedName, namespaceURI);
	}

	public void appendContent(Branch branch) {
		getWrapped().appendContent(branch);
	}

	public void clearContent() {
		getWrapped().clearContent();
	}

	public List<Node> content() {
		return getWrapped().content();
	}

	public Element elementByID(String elementID) {
		return getWrapped().elementByID(elementID);
	}

	public int indexOf(Node node) {
		return getWrapped().indexOf(node);
	}

	public Node node(int index) throws IndexOutOfBoundsException {
		return getWrapped().node(index);
	}

	public int nodeCount() {
		return getWrapped().nodeCount();
	}

	public Iterator<Node> nodeIterator() {
		return getWrapped().nodeIterator();
	}

	public void normalize() {
		getWrapped().normalize();
	}

	public ProcessingInstruction processingInstruction(String target) {
		return getWrapped().processingInstruction(target);
	}

	public List<ProcessingInstruction> processingInstructions() {
		return getWrapped().processingInstructions();
	}

	public List<ProcessingInstruction> processingInstructions(String target) {
		return getWrapped().processingInstructions(target);
	}

	public boolean remove(Node node) {
		return getWrapped().remove(node);
	}

	public boolean remove(Comment comment) {
		return getWrapped().remove(comment);
	}

	public boolean remove(Element element) {
		return getWrapped().remove(element);
	}

	public boolean remove(ProcessingInstruction pi) {
		return getWrapped().remove(pi);
	}

	public boolean removeProcessingInstruction(String target) {
		return getWrapped().removeProcessingInstruction(target);
	}

	public void setContent(List<Node> content) {
		getWrapped().setContent(content);
	}

	public void setProcessingInstructions(List<ProcessingInstruction> listOfPIs) {
		getWrapped().setProcessingInstructions(listOfPIs);
	}

	public void accept(Visitor visitor) {
		getWrapped().accept(visitor);
	}

	public String asXML() {
		return getWrapped().asXML();
	}

	public Node asXPathResult(Element parent) {
		return getWrapped().asXPathResult(parent);
	}

	public XPath createXPath(String xpathExpression)
			throws InvalidXPathException {
		return getWrapped().createXPath(xpathExpression);
	}

	public Node detach() {
		return getWrapped().detach();
	}

	public Document getDocument() {
		return getWrapped().getDocument();
	}

	public String getName() {
		return getWrapped().getName();
	}

	public short getNodeType() {
		return getWrapped().getNodeType();
	}

	public String getNodeTypeName() {
		return getWrapped().getNodeTypeName();
	}

	public Element getParent() {
		return getWrapped().getParent();
	}

	public String getPath() {
		return getWrapped().getPath();
	}

	public String getPath(Element context) {
		return getWrapped().getPath(context);
	}

	public String getStringValue() {
		return getWrapped().getStringValue();
	}

	public String getText() {
		return getWrapped().getText();
	}

	public String getUniquePath() {
		return getWrapped().getUniquePath();
	}

	public String getUniquePath(Element context) {
		return getWrapped().getUniquePath(context);
	}

	public boolean hasContent() {
		return getWrapped().hasContent();
	}

	public boolean isReadOnly() {
		return getWrapped().isReadOnly();
	}

	public boolean matches(String xpathExpression) {
		return getWrapped().matches(xpathExpression);
	}

	public Number numberValueOf(String xpathExpression) {
		return getWrapped().numberValueOf(xpathExpression);
	}

	public List<Node> selectNodes(String xpathExpression) {
		return getWrapped().selectNodes(xpathExpression);
	}

	public List<Node> selectNodes(String xpathExpression,
			String comparisonXPathExpression) {
		return getWrapped().selectNodes(xpathExpression, comparisonXPathExpression);
	}

	public List<Node> selectNodes(String xpathExpression,
			String comparisonXPathExpression, boolean removeDuplicates) {
		return getWrapped().selectNodes(xpathExpression, comparisonXPathExpression, 
				removeDuplicates);
	}

	public Object selectObject(String xpathExpression) {
		return getWrapped().selectObject(xpathExpression);
	}

	public Node selectSingleNode(String xpathExpression) {
		return getWrapped().selectSingleNode(xpathExpression);
	}

	public void setDocument(Document document) {
		getWrapped().setDocument(document);
	}

	public void setName(String name) {
		getWrapped().setName(name);
	}

	public void setParent(Element parent) {
		getWrapped().setParent(parent);
	}

	public void setText(String text) {
		getWrapped().setText(text);
	}

	public boolean supportsParent() {
		return getWrapped().supportsParent();
	}

	public String valueOf(String xpathExpression) {
		return getWrapped().valueOf(xpathExpression);
	}

	public void write(Writer writer) throws IOException {
		getWrapped().write(writer);
	}
	
    @Override
	public Object clone() {
    	return new VersionedXmlDoc((Document) getWrapped().clone());
    }

    public String toXML() {
    	return toXML(true);
    }
    
	public String toXML(boolean pretty) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			OutputFormat format = new OutputFormat();
			format.setEncoding(StandardCharsets.UTF_8.name());
			if (pretty) {
				format.setIndent(true);
				format.setIndentSize(4);
		        format.setNewlines(true);
			} else {
		        format.setIndent(false);
		        format.setNewlines(false);
			}
			new XMLWriter(baos, format).write(getWrapped());
			return baos.toString(StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	public void writeToFile(File file, boolean pretty) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			OutputFormat format = new OutputFormat();
			format.setIndent(pretty);
			format.setNewlines(pretty);
			format.setEncoding(StandardCharsets.UTF_8.name());
			XMLWriter writer = new XMLWriter(os, format);
			writer.write(this);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	public static VersionedXmlDoc fromXML(String xml) {
		try {
			// May contain some invalid characters, parse with 1.1
			xml = StringUtils.replace(xml, "<?xml version=\"1.0\"", "<?xml version=\"1.1\"");
			return new VersionedXmlDoc(new SAXReader().read(new StringReader(xml)));
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	public static VersionedXmlDoc fromFile(File file) {
		try {
			return fromXML(FileUtils.readFileToString(file, StandardCharsets.UTF_8.name()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized Document getWrapped() {
		if (wrapped == null) {
			wrapped = fromXML(Preconditions.checkNotNull(xml)).getWrapped();
		}
		return wrapped;
	}
	
	public static VersionedXmlDoc fromBean(@Nullable Object bean) {
		Document dom = DocumentHelper.createDocument();
		AppLoader.getInstance(XStream.class).marshal(bean, new Dom4JWriter(dom));
		VersionedXmlDoc versionedDom = new VersionedXmlDoc(dom);
		if (bean != null) {
			versionedDom.setVersion(MigrationHelper.getVersion(HibernateProxyHelper.getClassWithoutInitializingProxy(bean)));
		}
		return versionedDom;
	}
	
	/**
	 * Convert this document to bean. Migration will performed if necessary.
	 * During the migration, content of the document will also get updated 
	 * to reflect current migration result.
	 * @return
	 */
	public Object toBean() {
		return toBean(null);
	}

	/**
	 * Convert this document to bean. Migration will be performed if necessary.
	 * During the migration, content of the document will also get updated 
	 * to reflect current migration result.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T toBean(@Nullable Class<T> beanClass) {	
		XStream xstream = AppLoader.getInstance(XStream.class);
		Dom4JReader domReader = new Dom4JReader(this);
		Class<?> origBeanClass;
		try {
			origBeanClass = HierarchicalStreams.readClassType(domReader, xstream.getMapper());
		} catch (CannotResolveClassException e) {
			if (beanClass == null)
				throw e;
			else
				origBeanClass = beanClass;
		}

		if (origBeanClass == null)
			return null;
		else if (origBeanClass == Null.class)
			return (T) ObjectUtils.NULL;
		
		if (beanClass == null)
			beanClass = (Class<T>) origBeanClass;
		else 
			getRootElement().setName(xstream.getMapper().serializedClass(beanClass));
		if (getVersion() != null) {
			try {
				Object migrator = beanClass.newInstance();
				if (MigrationHelper.migrate(getVersion(), migrator, this)) {
					setVersion(MigrationHelper.getVersion(migrator.getClass()));
					Object bean = xstream.unmarshal(domReader);
					return (T) bean;
				} else {
					return (T) xstream.unmarshal(domReader);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			return (T) xstream.unmarshal(domReader);
		}
	}
	
	public String getVersion() {
		Preconditions.checkNotNull(getRootElement());
		return getRootElement().attributeValue("revision");
	}
	
	public void setVersion(String version) {
		Preconditions.checkNotNull(getRootElement());
		getRootElement().addAttribute("revision", version);
	}
	
	@Override
    public synchronized void writeExternal(ObjectOutput out) throws IOException {
    	if (wrapped != null)
    		out.writeObject(toXML(false));
    	else
    		out.writeObject(xml);
	}

	@Override
	public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		xml = Preconditions.checkNotNull((String) in.readObject());
		wrapped = null;
	}
	
	public void marshall(HierarchicalStreamWriter writer) {
		marshallElement(writer, getRootElement());
	}
	
	private void marshallElement(HierarchicalStreamWriter writer, Element element) {
		writer.startNode(element.getName());
		for (Attribute attribute: (List<Attribute>)element.attributes())
			writer.addAttribute(attribute.getName(), attribute.getValue());
		if (element.getText().trim().length() != 0)
			writer.setValue(element.getText().trim());
		for (Element child: (List<Element>)element.elements())
			marshallElement(writer, child);
		writer.endNode();
	}
	
	public static VersionedXmlDoc unmarshall(HierarchicalStreamReader reader) {
		VersionedXmlDoc dom = new VersionedXmlDoc();
		unmarshallElement(reader, dom);
		return dom;
	}
	
	private static void unmarshallElement(HierarchicalStreamReader reader, Branch branch) {
		Element element = branch.addElement(reader.getNodeName());
		for (int i=0; i<reader.getAttributeCount(); i++) {
			String attributeName = reader.getAttributeName(i);
			String attributeValue = reader.getAttribute(i);
			element.addAttribute(attributeName, attributeValue);
		}
		if (StringUtils.isNotBlank(reader.getValue()))
			element.setText(reader.getValue().trim());
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			unmarshallElement(reader, element);
			reader.moveUp();
		}
	}
	
}

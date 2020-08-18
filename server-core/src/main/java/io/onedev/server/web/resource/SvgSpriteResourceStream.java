package io.onedev.server.web.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import io.onedev.commons.utils.StringUtils;

public class SvgSpriteResourceStream implements IResourceStream {

	private static final long serialVersionUID = 1L;
	
	private final Class<?> scope;
	
	private transient File codeSource;
	
	private static final Map<Class<?>, byte[]> contentCache = new ConcurrentHashMap<>();
	
	public SvgSpriteResourceStream(Class<?> scope) {
		this.scope = scope;
	}
	
	private File getCodeSource() {
		if (codeSource == null) {
			try {
				codeSource = new File(scope.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
		return codeSource;
	}
	
	@Override
	public Time lastModifiedTime() {
		if (getCodeSource().isFile()) { 
			return Files.getLastModified(getCodeSource());
		} else {
			String packagePath = scope.getPackage().getName().replace('.', File.separatorChar);
			File packageDir = new File(getCodeSource(), packagePath);
			
			if (packageDir.exists()) {
				Time time = Files.getLastModified(getCodeSource());
				for (File file: packageDir.listFiles()) {
					if (file.getName().endsWith(".svg")) {
						Time childTime = Files.getLastModified(file);
						if (childTime.after(time))
							time = childTime;
					}
				}
				return time;
			} else {
				throw new IllegalStateException("Unable to find package directory: " + 
						packageDir.getAbsolutePath());
			}				
		} 
		
	}

	@Override
	public String getContentType() {
		return "image/svg+xml";
	}

	@Override
	public Bytes length() {
		return Bytes.bytes(getContent().length);
	}
	
	private byte[] getContent() {
		byte[] content = contentCache.get(scope);
		if (content == null) {
			Map<String, String> files = new HashMap<>(); 
			
			if (getCodeSource().isFile()) {
				String packagePath = scope.getPackage().getName().replace('.', '/') + "/";
				try (JarFile jarFile = new JarFile(getCodeSource())) {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (entry.getName().startsWith(packagePath) && entry.getName().endsWith(".svg")) {
							String relativeEntryName = entry.getName().substring(packagePath.length());
							if (!relativeEntryName.contains("/")) {
								try (InputStream is = jarFile.getInputStream(entry)) {
									files.put(relativeEntryName, new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8));
								}
							}
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
			} else {
				String packagePath = scope.getPackage().getName().replace('.', File.separatorChar);
				File packageDir = new File(getCodeSource(), packagePath);
				if (packageDir.exists()) {
					for (File file: packageDir.listFiles()) {
						if (file.getName().endsWith(".svg")) {
							try {
								files.put(file.getAbsolutePath().substring(packageDir.getAbsolutePath().length()+1), 
										FileUtils.readFileToString(file, StandardCharsets.UTF_8));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				} else {
					throw new IllegalStateException("Unable to find package directory: " + 
							packageDir.getAbsolutePath());
				}				
			}
			
			Document spriteDoc = DocumentHelper.createDocument();
			Element spriteSvgElement = spriteDoc.addElement("svg", "http://www.w3.org/2000/svg");
			spriteSvgElement.addAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			
			SAXReader reader = new SAXReader();
			for (Map.Entry<String, String> entry: files.entrySet()) {
				Element symbolElement = spriteSvgElement.addElement("symbol");
				symbolElement.addAttribute("id", StringUtils.substringBeforeLast(entry.getKey(), "."));
				try {
					Document svgDoc = reader.read(new StringReader(entry.getValue()));
					
					String viewBox = svgDoc.getRootElement().attributeValue("viewBox");
					if (viewBox != null)
						symbolElement.addAttribute("viewBox", viewBox);
					for (Element element: svgDoc.getRootElement().elements()) {
						element.detach();
						symbolElement.add(element);
					}
				} catch (DocumentException e) {
					throw new RuntimeException(e);
				}
			}			
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputFormat format = new OutputFormat();
			format.setEncoding(StandardCharsets.UTF_8.name());
			format.setIndent(true);
			format.setIndentSize(4);
	        format.setNewlines(true);
			try {
				new XMLWriter(baos, format).write(spriteDoc);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			content = baos.toByteArray();
			
			if (getCodeSource().isFile())
				contentCache.put(scope, content);
		}
		return content;
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		return new ByteArrayInputStream(getContent());		
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void setLocale(Locale locale) {
	}

	@Override
	public String getStyle() {
		return null;
	}

	@Override
	public void setStyle(String style) {
	}

	@Override
	public String getVariation() {
		return null;
	}

	@Override
	public void setVariation(String variation) {
	}

}

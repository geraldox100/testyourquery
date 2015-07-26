package br.com.geraldoferraz.testyourquery.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RuntimePersistenceGenerator {
	private final String unitName;
	private final PersistenceUnitTransactionType transactionType;

	private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
	private final Map<String, String> properties = new LinkedHashMap<String, String>();
	private final String providerName;

	public RuntimePersistenceGenerator(String unitName, PersistenceUnitTransactionType transactionType, String providerName) {
		this.unitName = unitName;
		this.transactionType = transactionType;
		this.providerName = providerName;
		try {
			ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(createProxy(originalClassLoader));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public RuntimePersistenceGenerator addProperty(final String key, final String value) {

		if (key == null) {
			throw new IllegalArgumentException("Property key cannot be null");
		}

		if (value == null) {
			throw new IllegalArgumentException("Property value cannot be null");
		}
		properties.put(key, value);
		return this;
	}

	public RuntimePersistenceGenerator addAnnotatedClass(final Class<?> clazz) {
		classes.add(clazz);
		return this;
	}

	public EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(this.unitName);
	}

	protected Document createDocument() throws ParserConfigurationException {
		final Document doc = createXmlDocument();
		final Element persistence = createPersistenceElement(doc);
		doc.appendChild(persistence);
		final Element unit = createPersistenceUnitElement(doc);
		persistence.appendChild(unit);
		unit.appendChild(createProviderElement(doc, providerName));

		for (final Class<?> clazz : this.classes) {
			final Element classElement = doc.createElement("class");
			classElement.setTextContent(clazz.getName());
			unit.appendChild(classElement);
		}

		if (!properties.isEmpty()) {
			final Element propertiesElement = createPropertiesElement(doc);
			unit.appendChild(propertiesElement);
		}
		return doc;
	}

	private Element createPersistenceUnitElement(Document doc) {
		final Element unit = doc.createElement("persistence-unit");
		unit.setAttribute("name", unitName);
		unit.setAttribute("transaction-type", transactionType.name());
		return unit;
	}

	private Element createPropertiesElement(Document doc) {
		final Element propertiesElement = doc.createElement("properties");
		for (final Map.Entry<String, String> entry : properties.entrySet()) {
			final Element property = createPropertyElement(doc, entry);
			propertiesElement.appendChild(property);
		}
		return propertiesElement;
	}

	private Element createPropertyElement(Document doc, Map.Entry<String, String> entry) {
		final Element property = doc.createElement("property");
		property.setAttribute("name", entry.getKey());
		property.setAttribute("value", entry.getValue());
		return property;
	}

	protected Element createPersistenceElement(final Document doc) {
		final Element persistence = doc.createElement("persistence");
		
//		persistence.setAttribute("version", "2.0");
		persistence.setAttribute("version", "1.0");
		persistence.setAttribute("xmlns", "http://java.sun.com/xml/ns/persistence");
		persistence.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
//		persistence.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd");
		persistence.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd");
		return persistence;
	}

	protected Element createProviderElement(final Document doc, final String provider) {
		final Element element = doc.createElement("provider");
		element.setTextContent(provider);
		return element;
	}

	protected Document createXmlDocument() throws ParserConfigurationException {
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.newDocument();
	}

	public ClassLoader createClassLoader(final String persistenceContent, final ClassLoader originalClassLoader) {
		return new ClassLoader() {
			@Override
			protected Enumeration<URL> findResources(final String name) throws IOException {
				if ("META-INF/persistence.xml".equals(name)) {
					final File file = getTempFile();

					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
					bufferedWriter.write(persistenceContent);
					bufferedWriter.close();

					final URL url = file.toURI().toURL();
					return java.util.Collections.enumeration(new HashSet<URL>(Arrays.asList(url)));
				}
				return originalClassLoader.getResources(name);
			}
		};
	}

	private static File getTempFile() throws IOException {
		File temp = File.createTempFile("meta", Long.toString(System.nanoTime()));
		temp.delete();
		temp.mkdir();
		File directory = new File(temp, "META-INF");
		directory.delete();
		directory.mkdir();
		File persistenceXMLFile = new File(directory, "persistence.xml");

		return persistenceXMLFile;
	}

	public String generateXml() throws TransformerException, ParserConfigurationException {
		final Document doc = createDocument();
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		final DOMSource source = new DOMSource(doc);
		final StringWriter writer = new StringWriter();
		final StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
		return writer.toString();
	}

	@Override
	public String toString() {
		try {
			return generateXml();
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private URLClassLoader createProxy(final ClassLoader cl) throws MalformedURLException {
		Enhancer e = new Enhancer();
		e.setSuperclass(URLClassLoader.class);
		e.setCallback(new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("getResources") && "META-INF/persistence.xml".equals((String) args[0])) {
					final String persistenceContent = generateXml();

					final File file = getTempFile();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
					bufferedWriter.write(persistenceContent);
					bufferedWriter.close();
					final URL url = file.toURI().toURL();
					return java.util.Collections.enumeration(new HashSet<URL>(Arrays.asList(url)));
				} else {
					return method.invoke(cl, args);
				}
			}
		});

		URL[] arrayDeURL = new URL[] { new URL("http://google.com.br") };
		Class<?>[] array = new Class<?>[] { arrayDeURL.getClass() };

		URLClassLoader f = (URLClassLoader) e.create(array, new Object[] { arrayDeURL });
		return f;
	}
}
package br.com.geraldoferraz.testyourquery.config;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultEntityManagerProvider implements EntityManagerProvider {

	private String url;
	private String user;
	private String password;
	private Set<Class<?>> entities = new HashSet<Class<?>>();
	private EntityManagerProvider other;

	public DefaultEntityManagerProvider(EntityManagerProvider entityManagerProvider) {
		this.other = entityManagerProvider;
		carregarXML();
	}

	private void carregarXML() {
		try {
			Enumeration<URL> resources = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getResources("META-INF/persistence.xml");
			if (resources.hasMoreElements()) {
				URL persistenceXMLURL = resources.nextElement();

				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(new File(persistenceXMLURL.toURI()));

				NodeList listOfPersistenceUnits = doc.getElementsByTagName("persistence-unit");
				for (int i = 0; i < listOfPersistenceUnits.getLength(); i++) {
					Node persistenceUnitNode = listOfPersistenceUnits.item(i);
					NamedNodeMap attributes = persistenceUnitNode.getAttributes();
					Node atributoName = attributes.getNamedItem("name");
					if (atributoName.getTextContent().equals(other.getPersistenceUnit())) {
						findEntities(persistenceUnitNode);
						findUserPasswordAndURL(persistenceUnitNode);
						break;
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findUserPasswordAndURL(Node persistenceUnitNode) {

		NodeList childNodes = persistenceUnitNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) item;
				if(element.getTagName().equals("properties")){
					NodeList properties = element.getElementsByTagName("property");
					for (int z = 0; i < properties.getLength(); z++) {
						NamedNodeMap attributes = properties.item(z).getAttributes();
						
						String name = attributes.getNamedItem("name").getTextContent();
						String value = attributes.getNamedItem("value").getTextContent();
						
						if (name.length() > 0 && name.contains("password")) {
							password = value;
						}
						
						if (name.length() > 0 && name.contains("user")) {
							user = value;
						}
						
						if (name.length() > 0 && name.contains("url")) {
							url = value;
						}
						if (user != null && password != null && url != null) {
							break;
						}
					}
					
				}
			}
		}

	}

	private void findEntities(Node persistenceUnit) throws ClassNotFoundException {
		NodeList childNodes = persistenceUnit.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) item;
				if (element.getTagName().equals("class")) {

					entities.add(Class.forName(element.getTextContent()));
				}

			}

		}
	}

	public Connection getJDBCConnection() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Set<Class<?>> getEntities() {
		return entities;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return other.getEntityManagerFactory();
	}

	public String getPersistenceUnit() {
		return other.getPersistenceUnit();
	}
}

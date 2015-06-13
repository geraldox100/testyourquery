package br.com.geraldoferraz.testyourquery.config;

import static br.com.geraldoferraz.scanyourpath.searches.filters.arguments.SearchArguments.annotedWith;
import static br.com.geraldoferraz.scanyourpath.searches.loaders.ClassPathLoaderTypes.full;

import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.Ejb3Configuration;

import br.com.geraldoferraz.scanyourpath.Scanner;
import br.com.geraldoferraz.testyourquery.util.ConnectionManager;

public class HSQLDBProvider implements EntityManagerProvider {

	private String schema;
	private String showSQL = "false";
	private Set<Class<?>> entities;

	@SuppressWarnings("deprecation")
	public EntityManagerFactory getEntityManagerFactory() {
		Ejb3Configuration config = new Ejb3Configuration();

		config.setProperties(getProperties());

		Set<Class<?>> annotedClasses = getAnnotedClasses();
		for (Class<?> annotedClass : annotedClasses) {
			config.addAnnotatedClass(annotedClass);
		}

		if (haveSchema()) {
			try {
				ConnectionManager cm = new ConnectionManager();
				cm.executeStatement("CREATE SCHEMA " + schema + " AUTHORIZATION DBA;");
			} catch (Exception e) {
			}
		}

		return config.createEntityManagerFactory();
	}

	private Properties getProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:ctaTeste;shutdown=true");
		properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
		properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		properties.put("hibernate.connection.username", "sa");
		properties.put("hibernate.connection.password", "");
		properties.put("hibernate.connection.shutdown", "true");
		properties.put("hibernate.connection.autocommit", "true");
		properties.put("hibernate.jdbc.batch_size", 0);
		properties.put("hibernate.hbm2ddl.auto", "create-drop");

		if (haveSchema()) {
			properties.put("hibernate.default_schema", schema);
		}

		properties.put("hibernate.show_sql", showSQL);
		properties.put("hibernate.format_sql", "true");
		return properties;
	}

	private Set<Class<?>> getAnnotedClasses() {
		if (haveEntities()) {
			return entities;
		} else {
			Scanner scan = new Scanner();
			scan.limitSearchingPathTo(full());
			Set<Class<?>> entities = scan.allClasses(annotedWith(Entity.class)).anyWhere();
			return entities;
		}
	}

	private boolean haveEntities() {
		return entities != null && !entities.isEmpty();
	}

	private boolean haveSchema() {
		return notEmpty(schema);
	}

	private boolean notEmpty(String string) {
		return string != null && !string.isEmpty();
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setShowSQL(String showSQL) {
		this.showSQL = showSQL;
	}

	public void setEntities(Set<Class<?>> entities) {
		this.entities = entities;
	}

}

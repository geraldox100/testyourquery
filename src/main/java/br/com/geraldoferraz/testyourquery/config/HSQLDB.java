package br.com.geraldoferraz.testyourquery.config;

import static br.com.geraldoferraz.scanyourpath.searches.filters.arguments.SearchArguments.annotedWith;
import static br.com.geraldoferraz.scanyourpath.searches.loaders.ClassPathLoaderTypes.full;

import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;

import br.com.geraldoferraz.scanyourpath.Scanner;
import br.com.geraldoferraz.testyourquery.util.ConnectionManager;

public class HSQLDB extends JPAEntityManagerFactory {

	private Configuration configuration;

	public HSQLDB(Configuration configuration) {
		this.configuration = configuration;
		if (configuration.hasSchmea()) {
			try {
				ConnectionManager cm = new ConnectionManager();
				cm.executeStatement("CREATE SCHEMA " + configuration.getSchema() + " AUTHORIZATION DBA;");
			} catch (Exception e) {
			}
		}
	}
	
	public HSQLDB() {
	}

	@Override
	protected Properties getProperties() {
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

		if (configuration.hasSchmea()) {
			properties.put("hibernate.default_schema", configuration.getSchema());

		}
		properties.put("hibernate.show_sql", configuration.getShowSQL());
		properties.put("hibernate.format_sql", "true");

		return properties;
	}

	@Override
	protected Set<Class<?>> getAnnotedClasses() {
		if (configuration.hasEntities()) {
			return configuration.getEntities();
		} else {
			Scanner scan = new Scanner();
			scan.limitSearchingPathTo(full());
			Set<Class<?>> entities = scan.allClasses(annotedWith(Entity.class)).anyWhere();
			return entities;
		}
	}

}

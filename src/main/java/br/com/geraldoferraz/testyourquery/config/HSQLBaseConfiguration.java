package br.com.geraldoferraz.testyourquery.config;

import static br.com.geraldoferraz.scanyourpath.searches.filters.arguments.SearchArguments.annotedWith;
import static br.com.geraldoferraz.scanyourpath.searches.loaders.ClassPathLoaderTypes.full;

import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;

import br.com.geraldoferraz.scanyourpath.Scanner;

public class HSQLBaseConfiguration extends Configuration {
	
	public HSQLBaseConfiguration() {
//		try {
//			ConnectionManager cm = new ConnectionManager();
//			cm.executeStatement("CREATE SCHEMA GF AUTHORIZATION DBA;");
//		} catch (Exception e) {
//		}
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
//		properties.put("hibernate.default_schema", "GF");
		properties.put("hibernate.hbm2ddl.auto", "create-drop");
		properties.put("hibernate.show_sql", "false");
		properties.put("hibernate.format_sql", "false");
		return properties;
	}

	@Override
	protected Set<Class<?>> getAnnotedClasses() {
		Scanner scan = new Scanner();
		scan.limitSearchingPathTo(full());
		Set<Class<?>> entities = scan.allClasses(annotedWith(Entity.class)).anyWhere();
		return entities;
	}


}

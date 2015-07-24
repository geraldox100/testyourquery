package br.com.geraldoferraz.testyourquery.config;

import static br.com.geraldoferraz.scanyourpath.searches.filters.arguments.SearchArguments.annotatedWith;
import static br.com.geraldoferraz.scanyourpath.searches.loaders.ClassPathLoaderTypes.full;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;

import br.com.geraldoferraz.scanyourpath.Scanner;
import br.com.geraldoferraz.testyourquery.util.RuntimePersistenceGenerator;

public class HSQLDBProvider implements EntityManagerProvider {

	private String schema;
	private String showSQL = "false";
	private Set<Class<?>> entities;
	private static final String DRIVER = "org.hsqldb.jdbcDriver";
	private static final String URL = "jdbc:hsqldb:mem:testYourQueryDataBase;";
	
	public EntityManagerFactory getEntityManagerFactory() {
		RuntimePersistenceGenerator generator = new RuntimePersistenceGenerator("test", PersistenceUnitTransactionType.RESOURCE_LOCAL, "org.hibernate.ejb.HibernatePersistence");
		
        setProperties(generator);

		Set<Class<?>> annotated = getAnnotedClasses();
		for (Class<?> annotatedClass : annotated) {
			generator.addAnnotatedClass(annotatedClass);
		}

		if (haveSchema()) {
			try {
				executeStatement("CREATE SCHEMA " + schema + " AUTHORIZATION DBA;");
			} catch (Exception e) {
			}
		}
		return generator.createEntityManagerFactory();
	}
	
	private void executeStatement(String statement) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL, "sa", "");
			pstmt = conn.prepareStatement(statement);
		} finally { 
			if(pstmt != null){
				pstmt.execute();
			}
			if(conn != null){
				conn.close();
			}
		}
	}
	
	private void setProperties(RuntimePersistenceGenerator generator) {
		generator.addProperty("hibernate.connection.url", URL+"shutdown=true;");
		generator.addProperty("hibernate.connection.driver_class", DRIVER);
		generator.addProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		generator.addProperty("hibernate.connection.username", "sa");
		generator.addProperty("hibernate.connection.password", "");
		generator.addProperty("hibernate.connection.shutdown", "true");
		generator.addProperty("hibernate.connection.autocommit", "true");
		generator.addProperty("hibernate.jdbc.batch_size", "0");
		generator.addProperty("hibernate.hbm2ddl.auto", "create-drop");

		if (haveSchema()) {
			generator.addProperty("hibernate.default_schema", schema);
		}

		generator.addProperty("hibernate.show_sql", showSQL);
		generator.addProperty("hibernate.format_sql", "true");
	}
	
	

	private Set<Class<?>> getAnnotedClasses() {
		if (haveEntities()) {
			return entities;
		} else {
			Scanner scan = new Scanner();
			scan.limitSearchingPathTo(full());
			Set<Class<?>> entities = scan.allClasses(annotatedWith(Entity.class)).anyWhere();
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
		return string != null && !(string.length() == 0);
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

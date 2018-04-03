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
	private static Scanner scan = new Scanner();
	private RuntimePersistenceGenerator generator;

	public HSQLDBProvider() {
		this(null,null);
	}
	public HSQLDBProvider(Set<Class<?>> entities, String schema) {
		setSchema(schema);
		setEntities(entities);
		scan.limitSearchingPathTo(full());
		generator = new RuntimePersistenceGenerator("test", PersistenceUnitTransactionType.RESOURCE_LOCAL, getProvider());
		setProperties(generator);
		
		generateAnnotedClasses();
		for (Class<?> annotatedClass : entities) {
			generator.addAnnotatedClass(annotatedClass);
		}
		
		if (haveSchema()) {
			try {
				executeStatement("CREATE SCHEMA " + schema + " AUTHORIZATION DBA;");
			} catch (Exception e) {
			}
		}
	}

	public EntityManagerFactory getEntityManagerFactory() {

		return generator.createEntityManagerFactory();
	}

	private String getProvider() {
		return chooseProvider("org.hibernate.ejb.HibernatePersistence","org.apache.openjpa.persistence.PersistenceProviderImpl","org.eclipse.persistence.jpa.PersistenceProvider");
	}

	private String chooseProvider(String... providers) {
		String retorno = "";
		for (String provider : providers) {
			try {
				Class<?> clazz = Class.forName(provider);
				if(clazz !=null){
					retorno = clazz.getName();
					break;
				}
			} catch (ClassNotFoundException e) {
				
			}
		}
		return retorno;
	}

	private void executeStatement(String statement) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL, "sa", "");
			pstmt = conn.prepareStatement(statement);
		} finally {
			if (pstmt != null) {
				pstmt.execute();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	private void setProperties(RuntimePersistenceGenerator generator) {
		generator.addProperty("hibernate.connection.url", URL + "shutdown=true;");
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

	private void generateAnnotedClasses() {
		if (!haveEntities()) {
			entities = scan.allClasses(annotatedWith(Entity.class)).anyWhere();
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

	public Connection getJDBCConnection() {
		try {
			return DriverManager.getConnection(URL, "sa", "");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Set<Class<?>> getEntities() {
		return entities;
	}

	public String getPersistenceUnit() {
		return "test";
	}

}

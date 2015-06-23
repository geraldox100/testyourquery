package br.com.geraldoferraz.testyourquery.runner;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.file.ScriptLoader;
import br.com.geraldoferraz.testyourquery.util.database.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class RunnerSessionPerTest implements Runner {
	private ConnectionManager connectionManager;
	private ConnectionFactory connectionFactory;
	private EntityManagerConnectionInjector injector;
	private Configuration configuration;

	public RunnerSessionPerTest(ClassReflector clazzReflector, Configuration configuration) throws Exception {
		this.configuration = configuration;
		inistializeConnectionManager();
		initializeConnectionFactory();
		initializeInjector(clazzReflector);
	}

	private void inistializeConnectionManager() throws Exception {
		connectionManager = new ConnectionManager(configuration.getEntityManagerProvider());
	}

	private void initializeConnectionFactory() {
		connectionFactory = new ConnectionFactory() {

			public EntityManager getEntityManager() {
				return connectionManager.getNewEntityManager();
			}

			public Connection getConnection() {
				return connectionManager.getNewConnection();
			}
		};

	}

	private void initializeInjector(ClassReflector clazzReflector) throws Exception {
		injector = new EntityManagerConnectionInjector(clazzReflector, connectionFactory);
	}

	public RunnerSessionPerTest(ClassReflector classRelector) throws Exception {
		this(classRelector, new ConfigurationFactory().build());
	}

	public void beforeRunTest() {
		try {
			runScriptIfAny(configuration);
		} catch (Exception e) {
		}
	}
	
	private void runScriptIfAny(Configuration configuration) throws SQLException, Exception {
		if (configuration.getScript() != null && !configuration.getScript().isEmpty()) {
			connectionManager.executeScript(new ScriptLoader(configuration.getScript()).load());
		}
	}

	public void afterRunTest() {
		connectionManager.freeMemorySpace();
		connectionManager.clearData();
	}

	public void testObjectCreated(Object testObject) {
		try {
			injector.injectOn(testObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

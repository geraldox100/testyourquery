package br.com.geraldoferraz.testyourquery.runner;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.junit.runners.model.FrameworkMethod;

import br.com.geraldoferraz.testyourquery.annotations.MassPreparer;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.file.ScriptLoader;
import br.com.geraldoferraz.testyourquery.util.ScriptRunner;
import br.com.geraldoferraz.testyourquery.util.database.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class RunnerSessionPerTest implements Runner {
	private ConnectionManager connectionManager;
	private ConnectionFactory connectionFactory;
	private EntityManagerConnectionInjector injector;
	private Configuration configuration;
	private ClassReflector clazzReflector;

	public RunnerSessionPerTest(ClassReflector clazzReflector, Configuration configuration) throws Exception {
		this.clazzReflector = clazzReflector;
		this.configuration = configuration;
		inistializeConnectionManager();
		initializeConnectionFactory();
		initializeInjector();
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

	private void initializeInjector() throws Exception {
		injector = new EntityManagerConnectionInjector(clazzReflector, connectionFactory);
	}

	public RunnerSessionPerTest(ClassReflector classRelector) throws Exception {
		this(classRelector, new ConfigurationFactory().build());
	}

	public void beforeRunTest(FrameworkMethod method) {
		connectionManager.clearData();
		MassPreparer massPreparer = method.getAnnotation(MassPreparer.class);
		if(massPreparer != null){
			Class<? extends ScriptRunner> scriptRunnerClass = massPreparer.value();
			if(scriptRunnerClass != null){
				try {
					ScriptRunner scriptRunner = scriptRunnerClass.newInstance();
					scriptRunner.run(connectionFactory.getEntityManager());
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		try {
			runScriptIfAny(configuration);
		} catch (Exception e) {
		}
	}
	
	private void runScriptIfAny(Configuration configuration) throws SQLException, Exception {
		if (configuration.getScript() != null && !(configuration.getScript().length() == 0)) {
			connectionManager.executeScript(new ScriptLoader(configuration.getScript()).load());
		}
	}

	public void afterRunTest() {
		connectionManager.freeMemorySpace();
	}

	public Object createTestObject() {
		try {
			Object testObject = clazzReflector.createInstance();
			injector.injectOn(testObject);
			return testObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

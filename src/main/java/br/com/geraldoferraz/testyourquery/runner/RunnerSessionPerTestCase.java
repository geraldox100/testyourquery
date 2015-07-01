package br.com.geraldoferraz.testyourquery.runner;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.junit.runners.model.FrameworkMethod;

import br.com.geraldoferraz.testyourquery.annotations.MassPreparer;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.file.ScriptLoader;
import br.com.geraldoferraz.testyourquery.util.ScriptRunner;
import br.com.geraldoferraz.testyourquery.util.database.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class RunnerSessionPerTestCase implements Runner {

	private EntityManager em;
	private Connection conn;
	private ConnectionFactory connectionFactory;
	private EntityManagerConnectionInjector injector;
	private Object testObject;

	public RunnerSessionPerTestCase(ClassReflector clazzReflector, Configuration configuration) throws Exception {
		testObject = clazzReflector.createInstance();
		ConnectionManager connectionManager = new ConnectionManager(configuration.getEntityManagerProvider());
		runScriptIfAny(configuration, connectionManager);
		em = connectionManager.getNewEntityManager();
		conn = ConnectionManager.getConnection(em);
		initializeConnectionFactory();
		initializeInjector(clazzReflector);
	}

	private void runScriptIfAny(Configuration configuration, ConnectionManager connectionManager) throws SQLException, Exception {
		if (configuration.getScript() != null && !(configuration.getScript().length() == 0)){
			connectionManager.executeScript(new ScriptLoader(configuration.getScript()).load());
		}
	}
	
	private void initializeConnectionFactory() {
		connectionFactory = new ConnectionFactory() {
			
			public EntityManager getEntityManager() {
				return em;
			}
			
			public Connection getConnection() {
				return conn;
			}
		};
		
	}
	
	private void initializeInjector(ClassReflector clazzReflector) throws Exception {
		injector = new EntityManagerConnectionInjector(clazzReflector, connectionFactory);
	}

	public void beforeRunTest(FrameworkMethod method) {
		if(!em.getTransaction().isActive()){
			em.getTransaction().begin();
		}
		
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
		
	}

	public void afterRunTest() {
		em.getTransaction().commit();

	}

	public Object createTestObject() {
		try {
			injector.injectOn(testObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testObject;
	}



}

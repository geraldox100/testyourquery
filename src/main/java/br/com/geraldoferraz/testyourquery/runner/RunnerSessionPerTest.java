package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.util.database.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class RunnerSessionPerTest implements Runner {

	private ClassReflector clazzReflector;
	private ConnectionManager connectionManager;

	public RunnerSessionPerTest(ClassReflector clazzReflector, Configuration configuration) {
		this.clazzReflector = clazzReflector;
		connectionManager = new ConnectionManager(configuration.getEntityManagerProvider());
	}

	public RunnerSessionPerTest(ClassReflector classRelector) {
		this(classRelector, new ConfigurationFactory().build());
	}

	public void beforeRunTest() {
	}

	public void afterRunTest() {
		connectionManager.freeMemorySpace();
	}

	public void testObjectCreated(Object testObject) {
		try {
			injectDependenciesOn(testObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void injectDependenciesOn(Object createdTest) throws Exception {
		injectEntityManagerOn(createdTest);
		injectConnectionOn(createdTest);
		injectEntityManagerOnDAOs(createdTest);
	}

	private void injectEntityManagerOnDAOs(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(Dao.class);
		for (Field field : fields) {

			Object daoObject = ClassReflector.newInstanceOf(field);
			ClassReflector.injectOn(field, createdTest, daoObject);

			List<Field> entityManagers = ClassReflector.getFieldsByType(EntityManager.class, daoObject);
			for (Field entityManagerField : entityManagers) {
				ClassReflector.injectOn(entityManagerField, daoObject, connectionManager.getNewEntityManager());
			}
		}
	}

	private void injectEntityManagerOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(PersistenceContext.class);
		for (Field field : fields) {
			ClassReflector.injectOn(field, createdTest, connectionManager.getNewEntityManager());
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JDBCConnection.class);
		for (Field field : fields) {
			ClassReflector.injectOn(field, createdTest, connectionManager.getNewConnection());
		}
	}

}

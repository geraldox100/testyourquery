package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.annotations.JPAEntityManager;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.util.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassRelector;

public class RunnerSessionPerTest implements Runner {

	private ClassRelector clazzReflector;
	private ConnectionManager connectionManager;

	public RunnerSessionPerTest(ClassRelector clazzReflector, Configuration configuration) {
		this.clazzReflector = clazzReflector;
		connectionManager = new ConnectionManager(configuration.getEntityManagerProvider());
	}

	public RunnerSessionPerTest(ClassRelector classRelector) {
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

			Object daoObject = ClassRelector.newInstanceOf(field);
			ClassRelector.injectOn(field, createdTest, daoObject);

			List<Field> entityManagers = ClassRelector.getFieldsByType(EntityManager.class, daoObject);
			for (Field entityManagerField : entityManagers) {
				ClassRelector.injectOn(entityManagerField, daoObject, connectionManager.getNewEntityManager());
			}
		}
	}

	private void injectEntityManagerOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JPAEntityManager.class);
		for (Field field : fields) {
			ClassRelector.injectOn(field, createdTest, connectionManager.getNewEntityManager());
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JDBCConnection.class);
		for (Field field : fields) {
			ClassRelector.injectOn(field, createdTest, connectionManager.getNewConnection());
		}
	}

}

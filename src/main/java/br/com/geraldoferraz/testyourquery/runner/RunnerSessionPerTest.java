package br.com.geraldoferraz.testyourquery.runner;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
		this(classRelector,new ConfigurationFactory().build());
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
			field.setAccessible(true);

			Object daoObject = field.getType().newInstance();
			field.set(createdTest, daoObject);

			List<Field> entityManagers = getEntityManagerFields(daoObject);
			for (Field entityManagerField : entityManagers) {
				entityManagerField.setAccessible(true);
				entityManagerField.set(daoObject, connectionManager.getNewEntityManager());
			}
		}
	}

	private List<Field> getEntityManagerFields(Object daoObject) throws IllegalAccessException {

		List<Field> tmp = new ArrayList<Field>();

		tmp.addAll(asList(daoObject.getClass().getDeclaredFields()));

		Class<?> currerntSuper = daoObject.getClass().getSuperclass();
		while (currerntSuper != null) {
			tmp.addAll(asList(currerntSuper.getDeclaredFields()));
			currerntSuper = currerntSuper.getSuperclass();
		}

		List<Field> retorno = new ArrayList<Field>();

		for (Field field : tmp) {
			if (field.getType().isAssignableFrom(EntityManager.class)) {
				retorno.add(field);
			}
		}

		return retorno;
	}

	private void injectEntityManagerOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JPAEntityManager.class);
		for (Field field : fields) {
			field.setAccessible(true);
			field.set(createdTest, connectionManager.getNewEntityManager());
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JDBCConnection.class);
		for (Field field : fields) {
			field.setAccessible(true);
			field.set(createdTest, connectionManager.getNewConnection());
		}
	}

	

}

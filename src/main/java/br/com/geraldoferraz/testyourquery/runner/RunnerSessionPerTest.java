package br.com.geraldoferraz.testyourquery.runner;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.annotations.JPAEntityManager;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.config.EntityManagerProvider;
import br.com.geraldoferraz.testyourquery.config.HSQLDB;

public class RunnerSessionPerTest implements Runner {
	
	private List<Connection> connections = new ArrayList<Connection>();;
	private List<EntityManager> entityManagers = new ArrayList<EntityManager>();
	private EntityManagerFactory emf;
	private EntityManagerProvider emProvider;
	private TestClass testClass;
	
	public RunnerSessionPerTest(TestClass testClass) {
		this.testClass = testClass;
		emProvider = new HSQLDB(new ConfigurationFactory().build());
	}

	public void beforeRunTest() {
	}

	public void afterRunTest() {
		freeMemorySpace();
	}

	public void testObjectCreated(Object testObject) {
		try {
			injectDependenciesOn(testObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void injectDependenciesOn(Object createdTest) throws Exception {
		emf = getEntityManagerFactory();
		injectEntityManagerOn(createdTest);
		injectConnectionOn(createdTest);
		injectEntityManagerOnDAOs(createdTest);
	}

	private EntityManagerFactory getEntityManagerFactory() throws Exception {
		return emProvider.getEntityManagerFactory();
	}

	private void injectEntityManagerOnDAOs(Object createdTest) throws Exception {
		List<FrameworkField> fields = testClass.getAnnotatedFields(Dao.class);
		for (FrameworkField fField : fields) {
			Field field = fField.getField();
			field.setAccessible(true);

			Object daoObject = field.getType().newInstance();
			field.set(createdTest, daoObject);

			List<Field> entityManagers = getEntityManagerFields(daoObject);
			for (Field entityManagerField : entityManagers) {
				entityManagerField.setAccessible(true);
				entityManagerField.set(daoObject, createEntityManager());
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
		List<FrameworkField> fields = testClass.getAnnotatedFields(JPAEntityManager.class);
		for (FrameworkField fField : fields) {
			Field field = fField.getField();
			field.setAccessible(true);
			field.set(createdTest, createEntityManager());
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<FrameworkField> fields = testClass.getAnnotatedFields(JDBCConnection.class);
		for (FrameworkField fField : fields) {
			Field field = fField.getField();
			field.setAccessible(true);
			field.set(createdTest, createConnection());
		}
	}

	@SuppressWarnings("deprecation")
	private Connection createConnection() {
		Session session = (Session) emf.createEntityManager().getDelegate();
		Connection connection = (Connection) session.connection();
		saveReference(connection);
		return connection;
	}

	private EntityManager createEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		saveReference(em);
		return em;
	}

	private void saveReference(EntityManager em) {
		entityManagers.add(em);
	}

	private void saveReference(Connection connection) {
		connections.add(connection);
	}

	private void freeMemorySpace() {
		closeEntityManagers();
		closeConnections();
		clearEntityManagerFactory();
		System.gc();
	}

	private void clearEntityManagerFactory() {
		if (emf != null) {
			emf.close();
			emf = null;
		}
	}

	private void closeConnections() {
		for (Connection conn : connections) {
			try {
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}
		connections = new ArrayList<Connection>();
	}

	private void closeEntityManagers() {
		for (EntityManager em : entityManagers) {
			if (em.isOpen()) {
				em.close();
			}
		}
		entityManagers = new ArrayList<EntityManager>();
	}

}

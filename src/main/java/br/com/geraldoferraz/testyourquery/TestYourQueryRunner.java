package br.com.geraldoferraz.testyourquery;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.DefaultConfiguration;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.annotations.JPAEntityManager;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.HSQLBaseConfiguration;

public class TestYourQueryRunner extends BlockJUnit4ClassRunner {

	private List<Connection> connections = new ArrayList<Connection>();;
	private List<EntityManager> entityManagers = new ArrayList<EntityManager>();
	private EntityManagerFactory emf;
	private Configuration configuration;

	public TestYourQueryRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		super.runChild(method, notifier);
		freeMemorySpace();
	}

	@Override
	protected Object createTest() throws Exception {
		Object createdTest = getTestClass().getOnlyConstructor().newInstance();
		injectDependenciesOn(createdTest);

		return createdTest;
	}

	private void injectDependenciesOn(Object createdTest) throws Exception {
		emf = getEntityManagerFactory();
		injectEntityManagerOn(createdTest);
		injectConnectionOn(createdTest);
		injectEntityManagerOnDAOs(createdTest);
	}

	private EntityManagerFactory getEntityManagerFactory() throws Exception {
		Configuration config = findConfigurationInTest();
		if (config == null) {
			config = new HSQLBaseConfiguration();
		}
		return config.getEntityManagerFactory();
	}

	private Configuration findConfigurationInTest() throws Exception{
		if(configuration == null){
			DefaultConfiguration annotation = getTestClass().getAnnotation(DefaultConfiguration.class);
			if(annotation != null){
				configuration = annotation.value().newInstance();
			}else{
				configuration = new HSQLBaseConfiguration();
			}
		}
		return configuration;
	}

	private void injectEntityManagerOnDAOs(Object createdTest) throws Exception {
		List<FrameworkField> fields = getTestClass().getAnnotatedFields(Dao.class);
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
		List<FrameworkField> fields = getTestClass().getAnnotatedFields(JPAEntityManager.class);
		for (FrameworkField fField : fields) {
			Field field = fField.getField();
			field.setAccessible(true);
			field.set(createdTest, createEntityManager());
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<FrameworkField> fields = getTestClass().getAnnotatedFields(JDBCConnection.class);
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

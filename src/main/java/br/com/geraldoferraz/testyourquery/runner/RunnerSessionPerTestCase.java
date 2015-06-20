package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.util.database.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class RunnerSessionPerTestCase implements Runner {

	private ClassReflector clazzReflector;
	private EntityManager em;
	private Connection conn;

	public RunnerSessionPerTestCase(ClassReflector clazzReflector, Configuration configuration) {
		this.clazzReflector = clazzReflector;
		em = new ConnectionManager(configuration.getEntityManagerProvider()).getNewEntityManager();
		conn = ConnectionManager.getConnection(em);
	}

	public void beforeRunTest() {
		if(!em.getTransaction().isActive())
			em.getTransaction().begin();
	}

	public void afterRunTest() {
		em.getTransaction().commit();

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
				ClassReflector.injectOn(entityManagerField, daoObject, em);
			}
		}
	}

	private void injectEntityManagerOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(PersistenceContext.class);
		for (Field field : fields) {
			ClassReflector.injectOn(field, createdTest, em);
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JDBCConnection.class);
		for (Field field : fields) {
			ClassReflector.injectOn(field, createdTest, conn);
		}
	}

}

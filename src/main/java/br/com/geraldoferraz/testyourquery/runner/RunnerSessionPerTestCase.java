package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.annotations.JPAEntityManager;
import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.util.ConnectionManager;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassRelector;

public class RunnerSessionPerTestCase implements Runner {

	private ClassRelector clazzReflector;
	private EntityManager em;
	private Connection conn;

	public RunnerSessionPerTestCase(ClassRelector clazzReflector, Configuration configuration) {
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

			Object daoObject = ClassRelector.newInstanceOf(field);
			ClassRelector.injectOn(field, createdTest, daoObject);

			List<Field> entityManagers = ClassRelector.getFieldsByType(EntityManager.class, daoObject);
			for (Field entityManagerField : entityManagers) {
				ClassRelector.injectOn(entityManagerField, daoObject, em);
			}
		}
	}

	private void injectEntityManagerOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JPAEntityManager.class);
		for (Field field : fields) {
			ClassRelector.injectOn(field, createdTest, em);
		}
	}

	private void injectConnectionOn(Object createdTest) throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedFields(JDBCConnection.class);
		for (Field field : fields) {
			ClassRelector.injectOn(field, createdTest, conn);
		}
	}

}

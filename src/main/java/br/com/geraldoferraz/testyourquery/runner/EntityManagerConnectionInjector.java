package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.geraldoferraz.testyourquery.annotations.Dao;
import br.com.geraldoferraz.testyourquery.annotations.JDBCConnection;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class EntityManagerConnectionInjector {
	
	private ClassReflector clazzReflector;
	private ConnectionFactory connectionFactory;
	private Object createdTest;
	
	public EntityManagerConnectionInjector(ClassReflector clazzReflector,ConnectionFactory connectionFactory) throws Exception {
		this.clazzReflector = clazzReflector;
		this.connectionFactory = connectionFactory;
		injectEntityManagerOnStaticFields();
		injectConnectionOnStaticFields();
		injectEntityManagerOnStaticDAOs();
	}

	protected void injectOn(Object createdTest) throws Exception {
		this.createdTest = createdTest;
		injectEntityManagerOnInstanceFields();
		injectConnectionOnInstanceFields();
		injectEntityManagerOnInstanceDAOs();
	}

	private void injectEntityManagerOnInstanceDAOs() throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedInstanceFields(Dao.class);
		for (Field field : fields) {

			Object daoObject = ClassReflector.newInstanceOf(field);
			ClassReflector.injectOn(field, createdTest, daoObject);

			List<Field> entityManagers = ClassReflector.getFieldsByType(EntityManager.class, daoObject);
			for (Field entityManagerField : entityManagers) {
				ClassReflector.injectOn(entityManagerField, daoObject, connectionFactory.getEntityManager());
			}
		}
	}
	
	private void injectEntityManagerOnStaticDAOs() throws Exception{
		List<Field> fields = clazzReflector.getAnnotatedStaticFields(Dao.class);
		Class<?> javaClass = clazzReflector.getJavaClass();
		for (Field field : fields) {

			Object daoObject = ClassReflector.newInstanceOf(field);
			ClassReflector.injectOn(field, javaClass, daoObject);

			List<Field> entityManagers = ClassReflector.getFieldsByType(EntityManager.class, daoObject);
			for (Field entityManagerField : entityManagers) {
				ClassReflector.injectOn(entityManagerField, daoObject, connectionFactory.getEntityManager());
			}
		}
	}

	private void injectEntityManagerOnInstanceFields() throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedInstanceFields(PersistenceContext.class);
		for (Field field : fields) {
			ClassReflector.injectOn(field, createdTest, connectionFactory.getEntityManager());
		}
	}
	
	private void injectEntityManagerOnStaticFields() throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedStaticFields(PersistenceContext.class);
		Class<?> javaClass = clazzReflector.getJavaClass();
		for (Field field : fields) {
			ClassReflector.injectStaticOn(field, javaClass, connectionFactory.getEntityManager());
		}
	}


	private void injectConnectionOnInstanceFields() throws Exception {
		List<Field> fields = clazzReflector.getAnnotatedInstanceFields(JDBCConnection.class);
		for (Field field : fields) {
			ClassReflector.injectOn(field, createdTest, connectionFactory.getConnection());
		}
	}
	
	private void injectConnectionOnStaticFields() {
		List<Field> fields = clazzReflector.getAnnotatedStaticFields(JDBCConnection.class);
		Class<?> javaClass = clazzReflector.getJavaClass();
		for (Field field : fields) {
			ClassReflector.injectStaticOn(field, javaClass, connectionFactory.getConnection());
		}
	}

}

package br.com.geraldoferraz.testyourquery.config;

import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.Ejb3Configuration;

public abstract class JPAEntityManagerFactory implements EntityManagerProvider {

	@SuppressWarnings("deprecation")
	public EntityManagerFactory getEntityManagerFactory() {
		Ejb3Configuration config = new Ejb3Configuration();
		config.setProperties(getProperties());
		Set<Class<?>> annotedClasses = getAnnotedClasses();
		for (Class<?> annotedClass : annotedClasses) {
			config.addAnnotatedClass(annotedClass);
		}
		return config.createEntityManagerFactory();
	}

	protected abstract Properties getProperties();

	protected abstract Set<Class<?>> getAnnotedClasses();

}

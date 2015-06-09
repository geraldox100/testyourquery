package br.com.geraldoferraz.testyourquery.config;

import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.Ejb3Configuration;

public abstract class Configuration {

	private Ejb3Configuration config;

	public Configuration() {
		config = new Ejb3Configuration();
		config.setProperties(getProperties());
		Set<Class<?>> annotedClasses = getAnnotedClasses();
		for (Class<?> annotedClass : annotedClasses) {
			config.addAnnotatedClass(annotedClass);
		}
	}

	@SuppressWarnings("deprecation")
	public EntityManagerFactory getEntityManagerFactory() {
		return config.createEntityManagerFactory();
	}

	protected abstract Properties getProperties();

	protected abstract Set<Class<?>> getAnnotedClasses();

}

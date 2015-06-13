package br.com.geraldoferraz.testyourquery.config.provider;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.geraldoferraz.testyourquery.config.EntityManagerProvider;

public class PersistenceUnitProvider implements EntityManagerProvider {
	
	
	private String persistenceUnit;

	public PersistenceUnitProvider(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(persistenceUnit);
	}

}

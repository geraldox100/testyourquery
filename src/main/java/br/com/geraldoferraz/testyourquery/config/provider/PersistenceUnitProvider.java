package br.com.geraldoferraz.testyourquery.config.provider;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.geraldoferraz.testyourquery.config.EntityManagerProvider;

public class PersistenceUnitProvider implements EntityManagerProvider {
	
	
	private String persistenceUnit;
	private EntityManagerFactory createEntityManagerFactory;
	

	public PersistenceUnitProvider(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		if(createEntityManagerFactory == null){
			createEntityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
		}
		return createEntityManagerFactory;
	}

	public String getPersistenceUnit() {
		return persistenceUnit;
	}

}

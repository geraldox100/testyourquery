package br.com.geraldoferraz.testyourquery.config;

import javax.persistence.EntityManagerFactory;

public interface EntityManagerProvider {

	public EntityManagerFactory getEntityManagerFactory();
	public String getPersistenceUnit();

}

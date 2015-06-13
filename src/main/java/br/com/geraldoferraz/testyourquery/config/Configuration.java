package br.com.geraldoferraz.testyourquery.config;


public class Configuration {

	private SessionMode sessionMode;
	private EntityManagerProvider entityManagerProvider;

	public SessionMode getSessionMode() {
		return sessionMode;
	}

	public void setSessionMode(SessionMode sessionMode) {
		this.sessionMode = sessionMode;
	}

	public void setEntityManagerProvider(EntityManagerProvider entityManagerProvider) {
		this.entityManagerProvider = entityManagerProvider;
	}

	public EntityManagerProvider getEntityManagerProvider() {
		return entityManagerProvider;
	}

}

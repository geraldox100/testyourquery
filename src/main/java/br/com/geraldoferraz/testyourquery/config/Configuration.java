package br.com.geraldoferraz.testyourquery.config;


public class Configuration {

	private SessionMode sessionMode;
	private DefaultEntityManagerProvider entityManagerProvider;
	private String script;

	public SessionMode getSessionMode() {
		return sessionMode;
	}

	public void setSessionMode(SessionMode sessionMode) {
		this.sessionMode = sessionMode;
	}

	public void setEntityManagerProvider(EntityManagerProvider entityManagerProvider) {
		this.entityManagerProvider = new DefaultEntityManagerProvider(entityManagerProvider);
	}

	public DefaultEntityManagerProvider getEntityManagerProvider() {
		return entityManagerProvider;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return script;
	}

}

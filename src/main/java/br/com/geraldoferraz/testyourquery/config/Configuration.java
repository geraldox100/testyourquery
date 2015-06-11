package br.com.geraldoferraz.testyourquery.config;

import java.util.Set;

public class Configuration {

	private Set<Class<?>> entities;
	private String schema;
	private String showSQL;
	private SessionMode sessionMode;

	public Set<Class<?>> getEntities() {
		return entities;
	}

	public void setEntities(Set<Class<?>> entities) {
		this.entities = entities;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getShowSQL() {
		return showSQL;
	}

	public void setShowSQL(String showSQL) {
		this.showSQL = showSQL;
	}

	public SessionMode getSessionMode() {
		return sessionMode;
	}

	public void setSessionMode(SessionMode sessionMode) {
		this.sessionMode = sessionMode;
	}

	public boolean hasSchmea() {
		return schema != null && !schema.isEmpty();
	}

	public boolean hasEntities() {
		return this.entities != null && !this.entities.isEmpty();
	}


}

package br.com.geraldoferraz.testyourquery.config;

import static br.com.geraldoferraz.scanyourpath.searches.filters.arguments.SearchArguments.annotatedWith;
import static br.com.geraldoferraz.scanyourpath.searches.loaders.ClassPathLoaderTypes.full;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import br.com.geraldoferraz.scanyourpath.Scanner;
import br.com.geraldoferraz.testyourquery.config.provider.PersistenceUnitProvider;

public class ConfigurationFactory {

	private String schema;
	private String showSQL = "false";
	private SessionMode sessionMode = SessionMode.PER_TEST_CASE;
	private String basePackage;
	private Set<Class<?>> entities = new HashSet<Class<?>>();
	private EntityManagerProvider entityManagerProvider;
	private String persistenceUnit;
	private String script;

	public ConfigurationFactory withSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public ConfigurationFactory shouldShowSQL() {
		this.showSQL = "true";
		return this;
	}

	public ConfigurationFactory addEntity(Class<?>... entities) {
		this.entities.addAll(asList(entities));
		return this;
	}

	public ConfigurationFactory withSessionPerTestMode() {
		sessionMode = SessionMode.PER_TEST;
		return this;
	}

	public ConfigurationFactory withSessionPerTestCaseMode() {
		sessionMode = SessionMode.PER_TEST_CASE;
		return this;
	}

	public ConfigurationFactory searchEntitiesAt(String basePackage) {
		this.basePackage = basePackage;
		return this;
	}
	
	public ConfigurationFactory withProvider(EntityManagerProvider provider){
		this.entityManagerProvider = provider;
		return this;
	}
	
	public ConfigurationFactory persistenceUnit(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
		return this;
	}
	
	public ConfigurationFactory runScript(String script) {
		this.script = script;
		return this;
	}

	public Configuration build() {
		Configuration configuration = new Configuration();
		configuration.setSessionMode(sessionMode);
		configuration.setEntityManagerProvider(getProvider());
		configuration.setScript(script);
		return configuration;
	}

	private EntityManagerProvider getProvider() {
		if(entityManagerProvider == null){
			if(persistenceUnit != null && !(persistenceUnit.length() == 0)){
				entityManagerProvider = new PersistenceUnitProvider(persistenceUnit);
			}else{
				HSQLDBProvider hsqldbProvider = new HSQLDBProvider();
				hsqldbProvider.setSchema(schema);
				hsqldbProvider.setEntities(getEntities());
				hsqldbProvider.setShowSQL(showSQL);
				entityManagerProvider = hsqldbProvider;
			}
		}
		return entityManagerProvider;
	}

	private Set<Class<?>> getEntities() {
		Set<Class<?>> entities = new HashSet<Class<?>>();
		entities.addAll(this.entities);

		if (basePackage != null && !(basePackage.length() == 0)) {
			Scanner scan = new Scanner();
			scan.limitSearchingPathTo(full());
			Set<Class<?>> entitiesFound = scan.allClasses(annotatedWith(Entity.class)).startingIn(basePackage);
			entities.addAll(entitiesFound);
		}

		return entities;
	}

	

	

}

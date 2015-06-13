package br.com.geraldoferraz.testyourquery.config;

import static br.com.geraldoferraz.scanyourpath.searches.filters.arguments.SearchArguments.annotedWith;
import static br.com.geraldoferraz.scanyourpath.searches.loaders.ClassPathLoaderTypes.full;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import br.com.geraldoferraz.scanyourpath.Scanner;
import br.com.geraldoferraz.testyourquery.config.provider.PersistenceUnitProvider;

public class ConfigurationFactory {

	private String schema;
	private String showSQL = "true";
	private SessionMode sessionMode = SessionMode.PER_TEST;
	private String basePackage;
	private Set<Class<?>> entities = new HashSet<Class<?>>();
	private EntityManagerProvider entityManagerProvider;
	private String persistenceUnit;

	public ConfigurationFactory withSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public ConfigurationFactory shouldShowSQL(String showSQL) {
		this.showSQL = showSQL;
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

	public Configuration build() {
		Configuration configuration = new Configuration();
		configuration.setSessionMode(sessionMode);
		configuration.setEntityManagerProvider(getProvider());
		return configuration;
	}

	private EntityManagerProvider getProvider() {
		if(entityManagerProvider == null){
			if(persistenceUnit != null && !persistenceUnit.isEmpty()){
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

		if (basePackage != null && !basePackage.isEmpty()) {
			Scanner scan = new Scanner();
			scan.limitSearchingPathTo(full());
			Set<Class<?>> entitiesFound = scan.allClasses(annotedWith(Entity.class)).startingIn(basePackage);
			entities.addAll(entitiesFound);
		}

		return entities;
	}

	

}

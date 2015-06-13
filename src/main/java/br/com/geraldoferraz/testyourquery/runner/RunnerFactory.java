package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Method;

import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.config.SessionMode;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassRelector;

public class RunnerFactory {

	private ClassRelector classRelector;
	
	public RunnerFactory(Class<?> clazz) {
		classRelector = new ClassRelector(clazz);
	}

	public Runner createRunner() {
		Configuration configuration = resolveConfiguration();
		Runner runner = resolveRunner(configuration);
		return runner;
	}

	private Runner resolveRunner(Configuration configuration) {
		Runner runner;
		if (SessionMode.PER_TEST_CASE.equals(configuration.getSessionMode())) {
			runner = new RunnerSessionPerTestCase(classRelector,configuration);
		}else{
			runner = new RunnerSessionPerTest(classRelector, configuration);
		}
		return runner;
	}

	private Configuration resolveConfiguration() {
		Configuration configuration;
		
		Method method = classRelector.getConfiguratorMethod();
		
		if (method != null) {
			configuration = (Configuration) classRelector.invokeStatic(method);
		}else{
			configuration = new ConfigurationFactory().build();
		}
		return configuration;
	}

	

}

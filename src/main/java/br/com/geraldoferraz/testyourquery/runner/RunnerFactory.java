package br.com.geraldoferraz.testyourquery.runner;

import java.lang.reflect.Method;

import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.config.ConfigurationFactory;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public class RunnerFactory {

	private ClassReflector classReflector;
	
	public RunnerFactory(Class<?> clazz) {
		classReflector = new ClassReflector(clazz);
	}

	public Runner createRunner() {
		Configuration configuration = resolveConfiguration();
		Runner runner = resolveRunner(configuration);
		return runner;
	}

	private Runner resolveRunner(Configuration configuration) {
		
		Runner runner = configuration.getSessionMode().newInstance(configuration, classReflector);
		return runner;
	}

	private Configuration resolveConfiguration() {
		Configuration configuration;
		
		Method method = classReflector.getConfiguratorMethod();
		
		if (method != null) {
			configuration = (Configuration) classReflector.invokeStatic(method);
		}else{
			configuration = new ConfigurationFactory().build();
		}
		return configuration;
	}

	

}

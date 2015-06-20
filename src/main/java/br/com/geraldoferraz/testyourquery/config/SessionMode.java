package br.com.geraldoferraz.testyourquery.config;

import java.lang.reflect.Constructor;

import br.com.geraldoferraz.testyourquery.runner.Runner;
import br.com.geraldoferraz.testyourquery.runner.RunnerSessionPerTest;
import br.com.geraldoferraz.testyourquery.runner.RunnerSessionPerTestCase;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassReflector;

public enum SessionMode {

	PER_TEST(RunnerSessionPerTest.class), PER_TEST_CASE(RunnerSessionPerTestCase.class);

	private Class<? extends Runner> runnerClass;

	private SessionMode(Class<? extends Runner> runnerClass) {
		this.runnerClass = runnerClass;
	}

	public Runner newInstance(Configuration configuration, ClassReflector classReflector) {
		try {
			Constructor<? extends Runner> constructor = this.runnerClass.getConstructor(ClassReflector.class, Configuration.class);
			return constructor.newInstance(classReflector, configuration);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

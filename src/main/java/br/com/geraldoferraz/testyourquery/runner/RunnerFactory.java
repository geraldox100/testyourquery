package br.com.geraldoferraz.testyourquery.runner;

import org.junit.runners.model.TestClass;


public class RunnerFactory {


	private TestClass klass;

	public RunnerFactory(TestClass klass) {
		this.klass = klass;
	}

	public Runner createRunner() {
		return new RunnerSessionPerTest(klass);
	}

}

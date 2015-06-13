package br.com.geraldoferraz.testyourquery.runner;

import br.com.geraldoferraz.testyourquery.config.Configuration;
import br.com.geraldoferraz.testyourquery.util.reflection.ClassRelector;

public class RunnerSessionPerTestCase implements Runner{

	private ClassRelector classRelector;
	private Configuration configuration;

	public RunnerSessionPerTestCase(ClassRelector classRelector, Configuration configuration) {
		this.classRelector = classRelector;
		this.configuration = configuration;
	}

	public void beforeRunTest() {
		
	}

	public void afterRunTest() {
		
	}

	public void testObjectCreated(Object testObject) {
		
	}

}

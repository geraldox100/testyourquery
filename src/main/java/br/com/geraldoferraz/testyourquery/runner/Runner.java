package br.com.geraldoferraz.testyourquery.runner;

import org.junit.runners.model.FrameworkMethod;


public interface Runner {

	void beforeRunTest(FrameworkMethod method);

	void afterRunTest();

	Object createTestObject();


}

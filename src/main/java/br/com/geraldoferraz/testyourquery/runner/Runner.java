package br.com.geraldoferraz.testyourquery.runner;


public interface Runner {

	void beforeRunTest();

	void afterRunTest();

	void testObjectCreated(Object testObject);


}

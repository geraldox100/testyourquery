package br.com.geraldoferraz.testyourquery.util;

import javax.persistence.EntityManager;

public interface ScriptRunner {
	
	public void run(EntityManager em);

}

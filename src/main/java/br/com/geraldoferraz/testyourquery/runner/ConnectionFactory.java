package br.com.geraldoferraz.testyourquery.runner;

import java.sql.Connection;

import javax.persistence.EntityManager;

public interface ConnectionFactory {

	EntityManager getEntityManager();

	Connection getConnection();

}

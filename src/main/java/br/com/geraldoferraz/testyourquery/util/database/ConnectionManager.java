package br.com.geraldoferraz.testyourquery.util.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;

import br.com.geraldoferraz.testyourquery.config.EntityManagerProvider;

public class ConnectionManager {
	
	private List<Connection> connections = new ArrayList<Connection>();;
	private List<EntityManager> entityManagers = new ArrayList<EntityManager>();
	private EntityManagerProvider entityManagerProvider;
	
	public ConnectionManager(EntityManagerProvider entityManagerProvider) {
		this.entityManagerProvider = entityManagerProvider;
	}

	@SuppressWarnings("deprecation")
	public Connection getNewConnection() {
		Session session = (Session) getEMF().createEntityManager().getDelegate();
		Connection connection = (Connection) session.connection();
		saveReference(connection);
		return connection;
	}

	public EntityManager getNewEntityManager() {
		EntityManager em = getEMF().createEntityManager();
		em.getTransaction().begin();
		saveReference(em);
		return em;
	}

	private void saveReference(EntityManager em) {
		entityManagers.add(em);
	}

	private void saveReference(Connection connection) {
		connections.add(connection);
	}

	public void freeMemorySpace() {
		closeEntityManagers();
		closeConnections();
		clearEntityManagerFactory();
		System.gc();
	}

	private void clearEntityManagerFactory() {
		if (getEMF() != null) {
			getEMF().close();
		}
	}

	private void closeConnections() {
		for (Connection conn : connections) {
			try {
				conn.commit();
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}
		connections = new ArrayList<Connection>();
	}

	private void closeEntityManagers() {
		for (EntityManager em : entityManagers) {
			em.getTransaction().commit();
			if (em.isOpen()) {
				em.close();
			}
		}
		entityManagers = new ArrayList<EntityManager>();
	}

	private EntityManagerFactory getEMF() {
		return entityManagerProvider.getEntityManagerFactory();
	}

	public static Connection getConnection(EntityManager em) {
		Session session = (Session) em.getDelegate();
		@SuppressWarnings("deprecation")
		Connection connection = (Connection) session.connection();
		return connection;
	}

}

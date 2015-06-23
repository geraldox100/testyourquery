package br.com.geraldoferraz.testyourquery.util.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.hibernate.Session;

import br.com.geraldoferraz.testyourquery.config.EntityManagerProvider;

public class ConnectionManager {
	
	private List<Connection> connections = new ArrayList<Connection>();;
	private List<EntityManager> entityManagers = new ArrayList<EntityManager>();
	private EntityManagerFactory entityManagerFactory;
	
	public ConnectionManager(EntityManagerProvider entityManagerProvider) {
		entityManagerFactory = entityManagerProvider.getEntityManagerFactory();
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
		System.gc();
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
		return entityManagerFactory;
	}

	public static Connection getConnection(EntityManager em) {
		Session session = (Session) em.getDelegate();
		@SuppressWarnings("deprecation")
		Connection connection = (Connection) session.connection();
		return connection;
	}

	public void clearData() {
		Set<EntityType<?>> entities = getEMF().getMetamodel().getEntities();
		Map<String, Boolean> estadoTabela = new HashMap<String, Boolean>();
		for (EntityType<?> entityType : entities) {
			estadoTabela.put(entityType.getName(), false);
		}
		
		Set<String> keySet = estadoTabela.keySet();
		while(tabelaPossuiAlguemSemExcluir(estadoTabela)){
			for (String tabela : keySet) {
				try {
					if(estadoTabela.get(tabela) == false){
						getNewEntityManager().createQuery("delete from " + tabela).executeUpdate();
						estadoTabela.put(tabela, true);
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	private static boolean tabelaPossuiAlguemSemExcluir(Map<String, Boolean> estadoTabela) {
		Set<String> keySet = estadoTabela.keySet();
		for (String tabela : keySet) {
			Boolean excluido = estadoTabela.get(tabela);
			if(!excluido){
				return true;
			}
		}
		return false;
	}

}

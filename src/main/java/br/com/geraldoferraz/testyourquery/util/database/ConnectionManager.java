package br.com.geraldoferraz.testyourquery.util.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import br.com.geraldoferraz.testyourquery.config.DefaultEntityManagerProvider;

public class ConnectionManager {
	
	private List<Connection> connections = new ArrayList<Connection>();;
	private List<EntityManager> entityManagers = new ArrayList<EntityManager>();
	private EntityManagerFactory emf;
	private DefaultEntityManagerProvider entityManagerProvider;
	
	
	public ConnectionManager(DefaultEntityManagerProvider entityManagerProvider) {
		this.entityManagerProvider = entityManagerProvider;
		emf = entityManagerProvider.getEntityManagerFactory();
	}
	
	public void executeScript(List<String> scripts) throws SQLException{
		EntityManager em = getNewEntityManager();
		for (String script : scripts) {
			em.createNativeQuery(script).executeUpdate();
		}
	}

	public Connection getNewConnection() {
		Connection connection = createConnection();
		saveReference(connection);
		return connection;
	}

	private Connection createConnection() {
		return entityManagerProvider.getJDBCConnection();
	}

	public EntityManager getNewEntityManager() {
		EntityManager em = emf.createEntityManager();
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
			if(em.getTransaction().getRollbackOnly()){
				em.getTransaction().rollback();
			}else{
				em.getTransaction().commit();
			}
			if (em.isOpen()) {
				em.close();
			}
		}
		entityManagers = new ArrayList<EntityManager>();
	}

	public Connection getConnection() {
		return createConnection();  
	}

	public void clearData() {
		Set<Class<?>> entities = entityManagerProvider.getEntities();
		try {
			getConnection().createStatement().execute("SET REFERENTIAL_INTEGRITY FALSE;");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		ArrayList<Class<?>> lista = new ArrayList<Class<?>>(entities);
		while(lista.size() > 0){
			Iterator<Class<?>> it = lista.iterator();
			String tabela = "";
			while (it.hasNext()) {
				try {
					tabela = it.next().getSimpleName();
					getNewEntityManager().createQuery("delete from " + tabela).executeUpdate();
					it.remove();
				} catch (Exception e) {
				}
			}
		}
		try {
			getConnection().createStatement().execute("SET REFERENTIAL_INTEGRITY TRUE;");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}

package server.integration;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import common.FileDTO;
import server.model.File;
import server.model.User;

public class FileCatalogDAO {
	private final EntityManagerFactory emFactory;

	// The entity manager is owned by the thread itself
	private final ThreadLocal<EntityManager> threadLocalEntityManager = new ThreadLocal<>();

	public FileCatalogDAO() {
		emFactory = Persistence.createEntityManagerFactory("fileCatalogPersistenceUnit");
	}

	public User findUserByName(String username, boolean endTransactionAfterSearch) {
		if (username == null) {
			return null;
		}

		try {
			EntityManager em = beginTransaction();
			try {
				User result = em.createNamedQuery("findUserByName", User.class).setParameter("name", username)
						.getSingleResult();
				System.out.println(result.toString());
				return result;
			} catch (NoResultException noSuchUser) {
				System.out.println("No such user");
				return null;
			}
		} finally {
			if (endTransactionAfterSearch) {
				commitTransaction();
			}
		}
	}

	public File findFileByFileName(String fileName, boolean endTransactionAfterSearch) {
		if (fileName == null) {
			return null;
		}

		try {
			EntityManager em = beginTransaction();
			try {
				return em.createNamedQuery("findFileByFileName", File.class).setParameter("fileName", fileName)
						.getSingleResult();
			} catch (NoResultException noSuchUser) {
				return null;
			}
		} finally {
			if (endTransactionAfterSearch)
				commitTransaction();
		}
	}

	public void createUser(String username, String password) {
		try {
			EntityManager em = beginTransaction();
			em.persist(new User(username, password));
		} finally {
			commitTransaction();
		}
	}

	public void createFile(FileDTO file) {
		try {
			EntityManager em = beginTransaction();
			em.persist(file);
		} finally {
			commitTransaction();
		}
	}

	public void updateUser() {
		commitTransaction();
	}

	public void updateFile() {
		commitTransaction();
	}

	public void deleteUser(String name) {
		try {
			EntityManager em = beginTransaction();
			em.createNamedQuery("deleteUserByName", User.class).setParameter("name", name).executeUpdate();
		} finally {
			commitTransaction();
		}
	}

	public void deleteFile(String fileName) {
		try {
			EntityManager em = beginTransaction();
			em.createNamedQuery("deleteFileByFileName", File.class).setParameter("fileName", fileName).executeUpdate();
		} finally {
			commitTransaction();
		}
	}

	public List<User> findAllUsers() {
		try {
			EntityManager em = beginTransaction();
			try {
				return em.createNamedQuery("findAllUsers", User.class).getResultList();
			} catch (NoResultException noFiles) {
				return new ArrayList<>();
			}
		} finally {
			commitTransaction();
		}
	}

	public List<File> findAllFiles() {
		try {
			EntityManager em = beginTransaction();
			try {
				return em.createNamedQuery("findAllFiles", File.class).getResultList();
			} catch (NoResultException noFiles) {
				return new ArrayList<>();
			}
		} finally {
			commitTransaction();
		}
	}

	// Private Helpers -----------

	private EntityManager beginTransaction() {
		EntityManager em = emFactory.createEntityManager();
		threadLocalEntityManager.set(em);
		EntityTransaction transaction = em.getTransaction();
		// If a transaction is already active,
		// we just return it and continue working with it
		// rather than creating a new one
		if (!transaction.isActive()) {
			transaction.begin();
		}
		return em;
	}

	private void commitTransaction() {
		threadLocalEntityManager.get().getTransaction().commit();
	}

}

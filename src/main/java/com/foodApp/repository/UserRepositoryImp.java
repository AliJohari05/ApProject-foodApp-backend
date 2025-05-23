package com.foodApp.repository;

import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.foodApp.exception.DatabaseException;
import java.util.List;

public class UserRepositoryImp  implements UserRepository {
    @Override
    public void save(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    @Override
    public User findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        }
    }
    @Override
    public User findByPhone(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User where phone = :phone", User.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        }
    }
    @Override
    public List<User> findByRole(Role role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE role = :role", User.class);
            query.setParameter("role", role);
            return query.list();
        }
    }
    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User", User.class);
            return query.list();
        }
    }
    @Override
    public void deleteById(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }
            tx.commit();
        }catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Error deleting user", e);
        }
    }
}

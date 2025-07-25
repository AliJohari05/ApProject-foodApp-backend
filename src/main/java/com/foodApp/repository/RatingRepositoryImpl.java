package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Rating;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class RatingRepositoryImpl implements RatingRepository {

    @Override
    public Rating save(Rating rating) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(rating);
            tx.commit();
            return rating;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Failed to save rating", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


    @Override
    public Optional<Rating> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Rating.class, id));
        } catch (Exception e) {
            throw new DatabaseException("Failed to find rating by ID", e);
        }
    }

    @Override
    public List<Rating> findByMenuItemId(Integer menuItemId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery("FROM Rating WHERE menuItem.id = :menuItemId ORDER BY createdAt DESC", Rating.class);
            query.setParameter("menuItemId", menuItemId);
            return query.list();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find ratings by menu item ID", e);
        }
    }

    @Override
    public void delete(Rating rating) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.merge(rating)); // Reattach و حذف
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to delete rating", e);
        }
    }



    @Override
    public Optional<Rating> findByUserIdAndOrderIdAndMenuItemId(int userId, int orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery(
                    "FROM Rating WHERE user.userId = :userId AND order.id = :orderId", Rating.class);
            query.setParameter("userId", userId);
            query.setParameter("orderId", orderId);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new DatabaseException("Failed to check existing rating", e);
        }
    }

    @Override
    public Double findAverageRatingByMenuItemId(Integer menuItemId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                    "SELECT AVG(r.rating) FROM Rating r WHERE r.menuItem.id = :menuItemId", Double.class);
            query.setParameter("menuItemId", menuItemId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new DatabaseException("Failed to calculate average rating", e);
        }
    }
}
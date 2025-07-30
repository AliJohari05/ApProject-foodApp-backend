package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Restaurant;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class FavoriteRepositoryImpl implements FavoriteRepository {

    @Override
    public void addFavorite(int userId, int restaurantId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            String sql = "INSERT INTO user_favorite_restaurants (user_id, restaurant_id) VALUES (:userId, :restaurantId)";
            session.createNativeQuery(sql, Void.class)
                    .setParameter("userId", userId)
                    .setParameter("restaurantId", restaurantId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to add restaurant to favorites", e);
        }
    }

    @Override
    public void removeFavorite(int userId, int restaurantId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            String sql = "DELETE FROM user_favorite_restaurants WHERE user_id = :userId AND restaurant_id = :restaurantId";
            session.createNativeQuery(sql, Void.class)
                    .setParameter("userId", userId)
                    .setParameter("restaurantId", restaurantId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to remove restaurant from favorites", e);
        }
    }

    @Override
    public boolean isFavorite(int userId, int restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT COUNT(*) FROM user_favorite_restaurants WHERE user_id = :userId AND restaurant_id = :restaurantId";
            Long count = (Long) session.createNativeQuery(sql) // Native query for COUNT(*)
                    .setParameter("userId", userId)
                    .setParameter("restaurantId", restaurantId)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new DatabaseException("Failed to check if restaurant is favorite", e);
        }
    }

    @Override
    public List<Restaurant> findFavoriteRestaurantsByUserId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT r.* FROM restaurants r JOIN user_favorite_restaurants ufr ON r.id = ufr.restaurant_id WHERE ufr.user_id = :userId";
            Query<Restaurant> query = session.createNativeQuery(sql, Restaurant.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve favorite restaurants", e);
        }
    }
}
package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Order;
import com.foodApp.repository.OrderRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {

    @Override
    public void save(Order order) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to save order", e);
        }
    }

    @Override
    public Order findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Order.class, id);
        }
    }

    @Override
    public List<Order> findByCustomerId(int customerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE customer.id = :customerId", Order.class);
            query.setParameter("customerId", customerId);
            return query.list();
        }
    }

    @Override
    public List<Order> findByRestaurantId(int restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE restaurant.id = :restaurantId", Order.class);
            query.setParameter("restaurantId", restaurantId);
            return query.list();
        }
    }


    @Override
    public void deleteById(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if (order != null) session.remove(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to deleteById order", e);
        }
    }
}

package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.OrderItem;
import com.foodApp.repository.OrderItemRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class OrderItemRepositoryImpl implements OrderItemRepository {

    @Override
    public void save(OrderItem item) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(item);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to save order item", e);
        }
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<OrderItem> query = session.createQuery(
                    "FROM OrderItem WHERE order.id = :orderId", OrderItem.class);
            query.setParameter("orderId", orderId);
            return query.list();
        }
    }

    @Override
    public void deleteByOrderId(int orderId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery("DELETE FROM OrderItem WHERE order.id = :orderId");
            query.setParameter("orderId", orderId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to delete order items", e);
        }
    }
}

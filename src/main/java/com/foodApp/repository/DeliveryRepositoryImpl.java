package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Delivery;
import com.foodApp.model.DeliveryStatus;
import com.foodApp.repository.DeliveryRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class DeliveryRepositoryImpl implements DeliveryRepository {

    @Override
    public void save(Delivery delivery) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(delivery);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to save delivery", e);
        }
    }

    @Override
    public Delivery findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Delivery.class, id);
        }
    }

    @Override
    public List<Delivery> findByDeliveryPersonId(int deliveryPersonId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                    "FROM Delivery WHERE deliveryPerson.id = :personId", Delivery.class);
            query.setParameter("personId", deliveryPersonId);
            return query.list();
        }
    }

    @Override
    public Delivery findByOrderId(int orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                    "FROM Delivery WHERE order.id = :orderId", Delivery.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResult();
        }
    }

    @Override
    public void updateStatus(int deliveryId, DeliveryStatus status) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Delivery delivery = session.get(Delivery.class, deliveryId);
            if (delivery != null) {
                delivery.setStatus(status);
                delivery.setUpdatedAt(java.time.LocalDateTime.now());
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to update delivery status", e);
        }
    }
}

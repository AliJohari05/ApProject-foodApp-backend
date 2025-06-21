package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Order;
import com.foodApp.model.OrderStatus;
import com.foodApp.repository.OrderRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<Order> findOrdersByStatus(OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("from Order where status = :status");
            query.setParameter("status", status);
            return query.list();
        }catch (Exception e) {
            throw new DatabaseException("Failed to find orders by status", e);
        }
    }

    @Override
    public List<Order> findAllById(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE id IN (:orderIdsList)", Order.class);
            query.setParameterList("orderIdsList", orderIds);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("Failed to find orders by id", e);
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

    // New method for comprehensive filtering for admin
    @Override
    public List<Order> findOrdersWithFilters(String searchTerm, String vendorName, String courierName, String customerName, OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT o FROM Order o LEFT JOIN o.restaurant r LEFT JOIN o.customer c LEFT JOIN o.delivery d LEFT JOIN d.deliveryPerson dp");
            hql.append(" WHERE 1=1"); // Always true condition to easily append AND clauses

            Map<String, Object> parameters = new HashMap<>();

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(r.name) LIKE :searchTermParam OR LOWER(c.name) LIKE :searchTermParam OR CAST(o.id AS string) LIKE :searchTermParam OR LOWER(o.deliveryAddress) LIKE :searchTermParam)");
                parameters.put("searchTermParam", "%" + searchTerm.toLowerCase().trim() + "%");
            }

            if (vendorName != null && !vendorName.trim().isEmpty()) {
                hql.append(" AND LOWER(r.name) LIKE :vendorNameParam");
                parameters.put("vendorNameParam", "%" + vendorName.toLowerCase().trim() + "%");
            }

            if (customerName != null && !customerName.trim().isEmpty()) {
                hql.append(" AND LOWER(c.name) LIKE :customerNameParam");
                parameters.put("customerNameParam", "%" + customerName.toLowerCase().trim() + "%");
            }

            if (courierName != null && !courierName.trim().isEmpty()) {
                // Assuming deliveryPerson is accessible through the 'delivery' entity in Order
                hql.append(" AND LOWER(dp.name) LIKE :courierNameParam");
                parameters.put("courierNameParam", "%" + courierName.toLowerCase().trim() + "%");
            }

            if (status != null) {
                hql.append(" AND o.status = :statusParam");
                parameters.put("statusParam", status);
            }

            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("Failed to find orders with filters", e);
        }
    }

    @Override
    public List<Order> findAll() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("FROM Order", Order.class);
            return query.list();
        }
    }
}
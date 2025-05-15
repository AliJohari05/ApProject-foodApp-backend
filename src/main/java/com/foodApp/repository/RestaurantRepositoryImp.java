package com.foodApp.repository;

import com.foodApp.model.Restaurant;

import java.util.List;
import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Restaurant;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class RestaurantRepositoryImp implements RestaurantRepository {

    @Override
    public void save(Restaurant restaurant) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(restaurant);
            tx.commit();
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            throw new DatabaseException("Error saving restaurant", e);
        }
    }

    @Override
    public Restaurant findById(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Restaurant.class, id);
        }
    }

    @Override
    public List<Restaurant> findAllApproved() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("From Restaurant where approved = true", Restaurant.class);
            return query.list();
        }
    }

    @Override
    public List<Restaurant> findByOwnerId(int ownerId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query <Restaurant> query = session.createQuery("From Restaurant where owner = :ownerId", Restaurant.class);
            return query.list();
        }
    }

    @Override
    public void deleteById(int id) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, id);
            if(restaurant != null) {
                session.remove(restaurant);
            }
            tx.commit();
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            throw new DatabaseException("Error deleting restaurant", e);
        }

    }

}

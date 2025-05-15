package com.foodApp.repository;
import com.foodApp.exception.DatabaseException;
import com.foodApp.model.MenuItem;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MenuItemRepositoryImp implements MenuItemRepository {
    @Override
    public void save(MenuItem menuItem) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(menuItem);
            tx.commit();
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            throw new DatabaseException("Error saving menu item", e);
        }
    }

    @Override
    public MenuItem findById(String id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MenuItem.class, id);
        }
    }

    @Override
    public List<MenuItem> findAllByRestaurant(int restaurantId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MenuItem> query = session.createQuery("from MenuItem where restaurant.id = :restaurantId", MenuItem.class);
            query.setParameter("restaurantId", restaurantId);
            return query.list();
        }
    }

    @Override
    public List<MenuItem> findAllByCategory(int categoryId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MenuItem> query = session.createQuery("from MenuItem where category.id = :categoryId", MenuItem.class);
            query.setParameter("categoryId", categoryId);
            return query.list();
        }
    }

    @Override
    public void delete(int id) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MenuItem menuItem = session.get(MenuItem.class, id);
            session.remove(menuItem);
            tx.commit();
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            throw new DatabaseException("Error deleting menu item", e);
        }

    }
}

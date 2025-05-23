package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.MenuItem;
import com.foodApp.repository.MenuItemRepository;
import com.foodApp.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class MenuItemRepositoryImp implements MenuItemRepository {
    @Override
    public MenuItem save(MenuItem menuItem) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.saveOrUpdate(menuItem);
            tx.commit();
            return menuItem;
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            throw new DatabaseException("Error saving menu item", e);
        }
    }
    @Override
    public Optional<MenuItem> findById(Integer id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            MenuItem item= session.get(MenuItem.class,id);
            return Optional.ofNullable(item);
        }
    }
    @Override
    public List<MenuItem> findByRestaurantId (Integer restaurantId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MenuItem> query = session.createQuery("from MenuItem where restaurant.id = :restaurantId", MenuItem.class);
            query.setParameter("restaurantId", restaurantId);
            return query.list();
        }
    }
    @Override
    public List<MenuItem> findAllByCategory(Integer categoryId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MenuItem> query = session.createQuery(
                    "select m from MenuItem m join m.category c where c.id = :categoryId",
                    MenuItem.class
            );
            query.setParameter("categoryId", categoryId);
            List<MenuItem> items = query.list();
            return query.list();
        }
    }
    @Override
    public void delete(MenuItem item) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MenuItem menuItem = session.get(MenuItem.class, item.getId());
            if(menuItem != null){
            session.remove(menuItem);
            }
            tx.commit();
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            throw new DatabaseException("Error deleting menu item", e);
        }

    }
}

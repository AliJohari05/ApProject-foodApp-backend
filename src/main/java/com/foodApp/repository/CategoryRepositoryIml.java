package com.foodApp.repository;
import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Category;
import com.foodApp.repository.CategoryRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
public class CategoryRepositoryIml implements CategoryRepository {
    @Override
    public void save(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(category);
            tx.commit();
        }catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Error saving Category", e);
        }
    }

    @Override
    public Category findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Category.class, id);
        }
    }

    @Override
    public List<Category> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery("from Category",Category.class);
            return query.list();

        }
    }

    @Override
    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Category category = session.get(Category.class, id);
            if (category != null) session.remove(category);
            tx.commit();
        }catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Error deleting Category", e);
        }

    }
}

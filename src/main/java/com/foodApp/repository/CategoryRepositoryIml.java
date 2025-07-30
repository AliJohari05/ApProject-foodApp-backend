package com.foodApp.repository;
import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Category;
import com.foodApp.model.MenuItem; // ایمپورت جدید
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class CategoryRepositoryIml implements CategoryRepository {
    @Override
    public void save(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(category);
            tx.commit();
        }catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Error saving Category", e);
        }
    }

    @Override
    public Optional<Category> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Category.class, id));
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

    @Override
    public Optional<Category> findByRestaurantIdAndTitle(Integer restaurantId, String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery(
                    "SELECT c FROM Category c LEFT JOIN FETCH c.menuItems WHERE c.restaurant.id = :restaurantId AND c.title = :title",
                    Category.class
            );
            query.setParameter("restaurantId", restaurantId);
            query.setParameter("title", title);
            return query.uniqueResultOptional();
        }
    }



    @Override
    public Optional<Category> findByRestaurantIdAndMenuItemId(Integer restaurantId, Integer menuItemId) { // پیاده‌سازی متد جدید
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery(
                    "SELECT c FROM Category c JOIN c.menuItems mi WHERE c.restaurant.id = :restaurantId AND mi.id = :menuItemId", Category.class);
            query.setParameter("restaurantId", restaurantId);
            query.setParameter("menuItemId", menuItemId);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new DatabaseException("Failed to check category-menu item linkage for restaurant", e);
        }
    }


    @Override
    public void addMenuItemToCategory(Category category, MenuItem menuItem) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Category managedCategory = session.merge(category);
            MenuItem managedMenuItem = session.merge(menuItem);

            if (!managedCategory.getMenuItems().contains(managedMenuItem)) {
                managedCategory.getMenuItems().add(managedMenuItem);
            }
            if (!managedMenuItem.getCategory().contains(managedCategory)) {
                managedMenuItem.getCategory().add(managedCategory);
            }

            session.persist(managedCategory);
            session.persist(managedMenuItem);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to link menu item to category", e);
        }
    }


    @Override
    public void removeMenuItemFromCategory(Category category, MenuItem menuItem) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Attach entities to the session
            Category managedCategory = session.merge(category);
            MenuItem managedMenuItem = session.merge(menuItem);

            managedCategory.getMenuItems().remove(managedMenuItem);
            managedMenuItem.getCategory().remove(managedCategory);

            session.persist(managedCategory);
            session.persist(managedMenuItem);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to unlink menu item from category", e);
        }
    }


    @Override
    public List<Category> findCategoriesByRestaurantId(Integer restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery(
                    "FROM Category WHERE restaurant.id = :restaurantId ORDER BY displayOrder ASC", Category.class);
            query.setParameter("restaurantId", restaurantId);
            return query.list();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find categories by restaurant ID", e);
        }
    }


}
package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.MenuItem;
import com.foodApp.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

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
            return items; // اصلاح: قبلا دوبار query.list() برگردانده می‌شد
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

    @Override
    public List<MenuItem> findItemsWithFilters(String search, BigDecimal maxPrice, List<String> keywords) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT mi FROM MenuItem mi LEFT JOIN mi.category c WHERE 1=1");
            Map<String, Object> parameters = new HashMap<>();

            if (search != null && !search.trim().isEmpty()) {
                hql.append(" AND (LOWER(mi.name) LIKE :searchTerm OR LOWER(mi.description) LIKE :searchTerm)");
                parameters.put("searchTerm", "%" + search.toLowerCase().trim() + "%");
            }

            if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                hql.append(" AND mi.price <= :maxPrice");
                parameters.put("maxPrice", maxPrice);
            }

            if (keywords != null && !keywords.isEmpty()) {
                hql.append(" AND (");
                for (int i = 0; i < keywords.size(); i++) {
                    if (i > 0) {
                        hql.append(" OR ");
                    }
                    hql.append("LOWER(mi.keywords) LIKE :keyword").append(i);
                    hql.append(" OR LOWER(c.title) LIKE :keyword").append(i); // جستجو در عنوان دسته‌بندی
                    parameters.put("keyword" + i, "%" + keywords.get(i).toLowerCase().trim() + "%");
                }
                hql.append(")");
            }

            Query<MenuItem> query = session.createQuery(hql.toString(), MenuItem.class);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                // برای پارامترهای لیست (مانند keywords) از setParameterList استفاده کنید
                if (entry.getValue() instanceof List) {
                    query.setParameterList(entry.getKey(), (List<?>) entry.getValue());
                } else {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("Error finding menu items with filters", e);
        }
    }
}
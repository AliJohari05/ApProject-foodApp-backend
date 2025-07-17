package com.foodApp.repository;

import com.foodApp.model.Restaurant;

import java.util.List;
import com.foodApp.exception.DatabaseException;
import com.foodApp.repository.RestaurantRepository;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class RestaurantRepositoryImp implements RestaurantRepository {

    @Override
    public Restaurant save(Restaurant restaurant) {
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Restaurant mergedRestaurant = (Restaurant) session.merge(restaurant);
            tx.commit();
            return mergedRestaurant; 
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
    public List<Restaurant> findApprovedByFilters(String search, List<String> keywords) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT r FROM Restaurant r WHERE r.approved = true");

            boolean hasSearch = search != null && !search.isBlank();
            boolean hasKeywords = keywords != null && !keywords.isEmpty();

            if (hasSearch || hasKeywords) {
                hql.append(" AND ");
            }

            if (hasSearch) {
                hql.append("(LOWER(r.name) LIKE :searchTerm OR LOWER(r.address) LIKE :searchTerm OR LOWER(r.phone) LIKE :searchTerm)");
            }

            if (hasKeywords) {
                if (hasSearch) {
                    hql.append(" AND ");
                }
                hql.append("r.id IN (SELECT mi.restaurant.id FROM MenuItem mi JOIN mi.category mc WHERE ");
                for (int i = 0; i < keywords.size(); i++) {
                    if (i > 0) {
                        hql.append(" OR ");
                    }
                    hql.append("LOWER(mi.keywords) LIKE :keyword").append(i)
                            .append(" OR LOWER(mc.title) LIKE :keyword").append(i);
                }
                hql.append(")");
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);

            if (hasSearch) {
                query.setParameter("searchTerm", "%" + search.toLowerCase() + "%");
            }

            if (hasKeywords) {
                for (int i = 0; i < keywords.size(); i++) {
                    query.setParameter("keyword" + i, "%" + keywords.get(i).toLowerCase() + "%");
                }
            }

            return query.list();
        } catch (Exception e) {
            throw new DatabaseException("Error finding approved restaurants by filters", e);
        }
    }

    @Override
    public List<Restaurant> findByOwnerId(int ownerId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query <Restaurant> query = session.createQuery("From Restaurant where owner.userId = :ownerId", Restaurant.class);
            query.setParameter("ownerId", ownerId);
            return query.list();
        }
    }

    @Override
    public Restaurant findByIdSeller(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("From Restaurant where id = :id", Restaurant.class);
            query.setParameter("id", id);
            return query.uniqueResult();
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
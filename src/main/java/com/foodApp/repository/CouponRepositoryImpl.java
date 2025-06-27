package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Coupon;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class CouponRepositoryImpl implements CouponRepository {
    @Override
    public Optional<Coupon> findByCouponCode(String couponCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("FROM Coupon WHERE couponCode = :code", Coupon.class);
            query.setParameter("code", couponCode);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            throw new DatabaseException("Failed to find coupon by code", e);
        }
    }

    @Override
    public void save(Coupon coupon) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            // FIX: Use merge for existing entities, persist for new ones.
            // Hibernate's merge will reattach a detached entity or make a copy.
            // persist is only for new entities.
            // To handle both, you can check id or simply use merge which works for both new and detached
            if (coupon.getId() == null || coupon.getId() == 0) { // Assuming 0 for default int ID
                session.persist(coupon); // For new entities
            } else {
                session.merge(coupon); // For detached/existing entities
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to save or update coupon", e); // Changed message
        }
    }

    @Override
    public void updateUsageCount(Coupon coupon) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(coupon); // Ensure the detached coupon is managed
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to update coupon usage count", e);
        }
    }

    @Override
    public Optional<Coupon> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Coupon.class, id));
        } catch (Exception e) {
            throw new DatabaseException("Failed to find coupon by ID", e);
        }
    }

    @Override
    public List<Coupon> findAll() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("FROM Coupon", Coupon.class);
            return query.list();
        }catch (Exception e) {
            throw new DatabaseException("Failed to find coupons", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Coupon coupon = session.get(Coupon.class, id); // Get a managed instance
            if (coupon != null) {
                session.remove(coupon);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to delete coupon", e);
        }
    }
}
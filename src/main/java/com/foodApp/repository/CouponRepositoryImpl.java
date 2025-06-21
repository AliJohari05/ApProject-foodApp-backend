package com.foodApp.repository;

import com.foodApp.exception.DatabaseException;
import com.foodApp.model.Coupon;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
            session.persist(coupon);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new DatabaseException("Failed to save coupon", e);
        }
    }
}
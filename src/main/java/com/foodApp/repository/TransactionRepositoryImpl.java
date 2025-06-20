package com.foodApp.repository;

import com.foodApp.model.User;
import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.foodApp.model.TransactionModel;
import org.hibernate.query.Query;

import java.util.List;

public class TransactionRepositoryImpl implements TransactionRepository {

    @Override
    public void save(TransactionModel trx) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(trx);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public List<TransactionModel> findByUserId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM TransactionModel WHERE userId = :uid", TransactionModel.class)
                    .setParameter("uid", userId)
                    .list();
        }
    }

    @Override
    public List<TransactionModel> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TransactionModel> query = session.createQuery("from TransactionModel ", TransactionModel.class);
            return query.list();
        }
    }
}

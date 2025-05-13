package com.foodApp;

import com.foodApp.model.Role;
import com.foodApp.model.User;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;//Representing a request and response
import com.sun.net.httpserver.HttpHandler;//Interface for creating route handlers

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;//Specifying the server address and port

import com.foodApp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class Server {
    public static void main(String[] args) throws IOException {

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        User user = new User();
        user.setName("Ali Johari");
        user.setPhone("09121234988");
        user.setEmail("ali@example.com");
        user.setPassword("123456");
        user.setRole(Role.CUSTOMER);
        user.setAddress("Tehran-khiaban dolat-bolvar kave");

        session.persist(user);
        tx.commit();
        session.close();

        System.out.println("User saved successfully!");
    }


}

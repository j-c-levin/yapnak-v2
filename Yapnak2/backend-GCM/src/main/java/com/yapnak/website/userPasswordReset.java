package com.yapnak.website;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Joshua on 20/09/2015.
 */
public class userPasswordReset extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String hash = req.getParameter("response");
        PrintWriter out = resp.getWriter();
        Connection connection;
        try {
            if (hash != null) {
                if (SystemProperty.environment.value() ==
                        SystemProperty.Environment.Value.Production) {
                    // Load the class that provides the new "jdbc:google:mysql://" prefix.
                    Class.forName("com.mysql.jdbc.GoogleDriver");
                    connection = DriverManager.getConnection("jdbc:google:mysql://yapnak-app:yapnak-main/yapnak_main?user=root");
                } else {
                    // Local MySQL instance to use during development.
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://173.194.230.210/yapnak_main", "client", "g7lFVLRzYdJoWXc3");
                }
                try {
                    String sql = "SELECT COUNT(*) FROM forgot WHERE reset = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, hash);
                    ResultSet rs = stmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        //reset requested
                        Cookie part1 = new Cookie("com.yapnak.hash", hash);
                        resp.addCookie(part1);
                        resp.setHeader("Refresh", "0; url=/userReset");
                    } else {
                        //Incorrect hash
                        out.println("You don't have permission to view this page");
                        resp.setHeader("Refresh", "3; url=/");
                    }
                } finally {
                    connection.close();
                }
            } else {
                //Client hasn't requested reset
                out.println("You don't have permission to view this page");
                resp.setHeader("Refresh", "3; url=/");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

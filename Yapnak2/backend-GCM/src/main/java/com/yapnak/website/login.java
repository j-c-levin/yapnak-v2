package com.yapnak.website;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by Joshua on 08/06/2015.
 */
public class login extends HttpServlet {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String SALT = "Y3aQcfpTiUUdpSAY";

    // A password hashing method.
    public static String hashPassword(String in) {
        try {
            MessageDigest md = MessageDigest
                    .getInstance("SHA-256");
            md.update(SALT.getBytes());        // <-- Prepend SALT.
            md.update(in.getBytes());

            byte[] out = md.digest();
            return bytesToHex(out);            // <-- Return the Hex Hash.
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String nextSessionId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    private static final Logger logger = Logger.getLogger(login.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = null;
        Connection connection = null;
        PrintWriter out = resp.getWriter();
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            try {
                String email = req.getParameter("username");
                String password = req.getParameter("password");
                if (email == "" || password == "") {
                    out.println(
                            "<html><head></head><body>You are missing either a message or a name! Try again! " +
                                    "Redirecting in 3 seconds...</body></html>");
                } else {
                    String sql = "SELECT email, password FROM client WHERE email = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, email);
                    ResultSet rs = null;
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        if (hashPassword(password).equals(rs.getString("password"))) {
                            out.println("Login success! (get)");
                            Cookie part1 = new Cookie("com.yapnak.email", email);
                            int time = 60 * 60 * 24 * 30 * 12;
                            part1.setMaxAge(time);
                            resp.addCookie(part1);
                            out.println("cookies added");
                            //TODO: Add the client page
                            HttpSession session = req.getSession();
                            session.setAttribute("email", email);
                            resp.setHeader("Refresh", "0; url=/client.jsp");
                        } else {
                            out.println("Incorrect login.");
                            resp.setHeader("Refresh", "3; url=/index.jsp");
                        }
                    } else {
                        out.println("LOGIN FAILED!!!");
                        resp.setHeader("Refresh", "3; url=/index.jsp");
                    }
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Connection connection;
        PrintWriter out = resp.getWriter();
        try {
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
            queryBlock:
            try {
                String email = req.getParameter("username");
                String password = req.getParameter("password");
                if (email == "" || password == "") {
                    out.println(
                            "<html><head></head><body>You are missing either a message or a name! Try again! " +
                                    "Redirecting in 3 seconds...</body></html>");
                }
                logger.info("Beginning authentication for client " + email);
                String query = "SELECT clientID FROM client WHERE email = ? AND password = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, hashPassword(password));
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    logger.info("Authenticated client");
                    out.println("Login success!");
                    logger.info("ID " + rs.getInt("clientID"));
                    Cookie part1 = new Cookie("com.yapnak.email", email);
                    part1.setMaxAge(60 * 60 * 24 * 30 * 12);
                    resp.addCookie(part1);
                    resp.setHeader("Refresh", "0; url=/console");
                } else {
                    //Check for masterkey
                    query = "SELECT clientID FROM client WHERE email = ? AND masterkey = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, email);
                    statement.setString(2, password);
                    rs = statement.executeQuery();
                    if (rs.next()) {
                        //Masterkey found
                        logger.info("Authenticated client with masterkey");
                        logger.info("ID " + rs.getInt("clientID"));
                        Cookie part1 = new Cookie("com.yapnak.email", email);
                        part1.setMaxAge(60 * 60 * 24 * 30 * 12);
                        resp.addCookie(part1);
                        //Remove masterkey
                        query = "UPDATE client SET masterkey = ? where email = ?";
                        statement = connection.prepareStatement(query);
                        statement.setString(1, "");
                        statement.setString(2, email);
                        int success = statement.executeUpdate();
                        if (success == -1) {
                            logger.warning("masterkey remove failed for client");
                            out.println("masterkey remove failed");
                            resp.setHeader("Refresh", "2; url=/client");
                            break queryBlock;
                        }
                        logger.info("masterkey removed for client");
                        out.println("Login success");
                        resp.setHeader("Refresh", "0; url=/console");
                    } else {
                        logger.info("Incorrect client details");
                        out.println("Incorrect client details");
                        resp.setHeader("Refresh", "2; url=/client");
                    }
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
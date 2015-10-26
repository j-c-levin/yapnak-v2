package com.yapnak.website;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Joshua on 08/06/2015.
 */
public class signup extends HttpServlet {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    String url = null;
    Connection connection = null;
    String sql = null;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Finish signup
        String user = req.getParameter("user");
        PrintWriter out = resp.getWriter();
        if (user != null) {
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
                sql = "SELECT email, password, name FROM signup WHERE hash = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, user);
                ResultSet rs = stmt.executeQuery();
                //check if client has signed up
                if (rs.next()) {
                    sql = "INSERT INTO client (email, password, clientPhoto, clientName) VALUES (?,?,?,?)";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, rs.getString("email"));
                    stmt.setString(2, rs.getString("password"));
                    stmt.setString(3, "");
                    stmt.setString(4, rs.getString("name"));
                    int success = stmt.executeUpdate();
                    if (success != -1) {

                        sql = "INSERT INTO offers (clientID) VALUES (LAST_INSERT_ID()),(LAST_INSERT_ID()),(LAST_INSERT_ID())";
                        stmt = connection.prepareStatement(sql);
                        stmt.executeUpdate();
                        sql = "UPDATE client set offer1 = LAST_INSERT_ID(), offer2 = LAST_INSERT_ID() + 1, offer3 =  LAST_INSERT_ID() + 2 WHERE email = ?";
                        stmt = connection.prepareStatement(sql);
                        stmt.setString(1, rs.getString("email"));
                        stmt.executeUpdate();
                        out.println(rs.getString("name") + " has been signed up and informed.");
                        //inform of signup
                        Properties props = new Properties();
                        Session session = Session.getDefaultInstance(props, null);
                        String msgBody = "Your account on Yapnak has been activated, you can now sign in with your details here: http://yapnak.com/client, enjoy!";
                        try {
                            Message msg = new MimeMessage(session);
                            msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                            msg.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(rs.getString("email")));
                            msg.setSubject("Yapnak registration request received");
                            msg.setText(msgBody);
                            Transport.send(msg);
                        } catch (AddressException e) {
                            e.printStackTrace();
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        resp.setHeader("Refresh", "3; url=/client");
                    } else {
                        out.println("Huh, something went wrong.  We'll look into it.");
                        //TODO:Email us about the error
                        resp.setHeader("Refresh", "3; url=/register");
                    }
                }
                //redirect if user hasn't signed up
                else {
                    out.println("You don't appear to have signed up, go ahead and join");
                    resp.setHeader("Refresh", "3; url=/register");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //redirect if user hasn't signed up
        else {
            out.println("You don't appear to have signed up, go ahead and join");
            resp.setHeader("Refresh", "3; url=/register");
        }
    }

    public void sendEmail(String email, String name) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String link = "https://yapnak-app.appspot.com/signup?user=" + hashPassword(email);
        String msgBody = "Hey there,\n\nYou've asked to sign up to Yapnak, a platform connecting hungry people with quality deals.  We'll validate your account shortly, but if you didn't sign up then please reply and let us know!";
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(email));
            msg.setSubject("Yapnak registration request received");
            msg.setText(msgBody);
            Transport.send(msg);

            msgBody = name + " has signed up through the Yapnak registration portal.  To confirm their account, click: " + link + "\n\nTo ignore, do nothing.";
            msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("yapnak.uq@gmail.com"));
            msg.setSubject("Registration requested: " + name);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //Sign up user
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
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                String name = req.getParameter("businessname");
                sql = "SELECT email from signup where email = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                String resubmit = "";
                //Resend email
                if (rs.next()) {
                    resubmit = " (resubmit)";
                }
                //Send e-mail confirmation

                    sql = "REPLACE signup (email, hash, password, name) VALUES (?,?,?,?)";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, email);
                    stmt.setString(2, hashPassword(email));
                    System.out.println(hashPassword(password));
                    stmt.setString(3, hashPassword(password));
                    stmt.setString(4, name);
                    int success = stmt.executeUpdate();
                    if (success != -1) {
                        name = name + resubmit;
                        sendEmail(email, name);
                        out.println("Check your e-mail for confirmation");
                        resp.setHeader("Refresh", "3; url=/client");
                    } else {
                        out.println("Huh, something didn't work");
                        //TODO:Forward this somewhere for our attention
                        resp.setHeader("Refresh", "3; url=/client");
                    }

            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
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
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Joshua on 24/09/2015.
 */
public class mailinglist extends HttpServlet {
    private static final Logger logger = Logger.getLogger(photoUpload.class.getName());

    //*******Handles hashing********
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
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

    static String hashPassword(String in) {
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
    //********************************

    static void sendEmail(String email, String subject, String message) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            javax.mail.Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
            msg.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(email));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        logger.info("Inserting " + req.getParameter("email") + " into mailing list");
        Connection connection;
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
                String query = "INSERT INTO mailinglist (email,remove) VALUES (?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, req.getParameter("email"));
                statement.setString(2, hashPassword(req.getParameter("email")));
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Mailing list insert failed
                    logger.warning("Mailing list insert failed");
                    out.println("Mailing list insert failed");
                    resp.setHeader("Refresh", "3; url=/");
                    break queryBlock;
                }
                String subject = "Thanks for being interested in Yapnak!";
                String remove = "yapnak.com/mailingremove?r=" + hashPassword(req.getParameter("email"));
                String message = "Hello there!\n\nWe hope you're having an excellent day and that we can only make it better.\n\nWe hate spam as much as you do, so we promise that we won't bug you unless it's so very exciting that we can't contain ourselves.\n\nWe look forward to sharing our most exciting news with you.If you didn't sign up on our website (yapnak.com) then you can remove yourself from this list by clicking here: " + remove;
                sendEmail(req.getParameter("email"), subject, message);
                resp.setHeader("Refresh", "3; url=/");
                logger.info("Insert success");
                out.println("We've added " + req.getParameter("email") + " to our mailing list.  We'll send you an email confirmation of this.");
                resp.setHeader("Refresh", "3; url=/");
            } finally {
                connection.close();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.warning("ClassNotFoundException " + e);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warning("SQLException " + e);
        } finally {
        }

    }

}

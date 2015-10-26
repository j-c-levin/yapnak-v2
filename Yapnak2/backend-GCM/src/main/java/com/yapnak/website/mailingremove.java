package com.yapnak.website;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Joshua on 24/09/2015.
 */
public class mailingremove extends HttpServlet {
    private static final Logger logger = Logger.getLogger(photoUpload.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
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
                String query = "SELECT email FROM mailinglist WHERE remove = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, req.getParameter("r"));
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    //User not in mailing list
                    logger.warning("User not in mailing list");
                    out.println("User not in mailing list");
                    break queryBlock;
                }
                logger.info("Removing " + rs.getString("email") + " from the mailing list");
                query = "DELETE FROM mailinglist WHERE email = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, rs.getString("email"));
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Mailing list remove failed
                    logger.warning("Mailing list remove failed");
                    out.println("Mailing list remove failed");
                    resp.setHeader("Refresh", "3; url=/");
                    break queryBlock;
                }
                logger.info("mailing list remove success");
                out.println("You have been removed from our mailing list");
                resp.setHeader("Refresh", "3; url=/");
            } finally {
                connection.close();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        }
    }
}

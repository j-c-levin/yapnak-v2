package com.yapnak.website;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by Joshua on 14/06/2015.
 */
public class uploadPhoto2 extends HttpServlet {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private static final Logger logger = Logger.getLogger(login.class.getName());
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

            String url = null;
            Connection connection = null;
            PrintWriter out = resp.getWriter();
            HttpSession session = req.getSession();
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
                logger.info("Beginning photo update");
                Cookie[] cookies = req.getCookies();
                String email = "";
                for (Cookie cooky: cookies) {
                    if (cooky.getName().equals("com.yapnak.email")) {
                        email = cooky.getValue();
                        break;
                    }
                }
                if (!email.equals("")) {
                    logger.info("Found email cookie " + email);
                    String sql = "SELECT clientPhoto FROM client WHERE email = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();
                    String logo;
                    if (rs.next()) {
                        logo = rs.getString("clientPhoto");
                        logger.info("found clientPhoto string");
                    } else {
                        logger.warning("No clientPhoto string found");
                        logo = "";
                        out.print("failed to find previous");
                        resp.setHeader("Refresh", "1; url=/main");
                    }
                    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
                    List<BlobKey> blobKeys = blobs.get("image");
                    if (blobKeys == null || blobKeys.isEmpty()) {
                        //do nothing
                        logger.warning("Blob empty");
                        out.print("Blob empty");
                    } else {
                        if (!logo.equals("")) {
                            logger.info("Previous blob found, deleting");
                            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
                            blobstoreService.delete(new BlobKey(logo));
                        }
                        logo = blobKeys.get(0).getKeyString();
                    }
                    ImagesService services = ImagesServiceFactory.getImagesService();
                    ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(logo)).secureUrl(true);
                    try {
                        url = services.getServingUrl(serve);
                        url = url + "=s100";
                        logger.info("New client photo url: " + url);
                    } catch (IllegalArgumentException e) {
                        url = "https://yapnak.com/images/yapnakmonsterthumb.png";
                        e.printStackTrace();
                    } catch (ImagesServiceFailureException e1) {
                        url = "https://yapnak.com/images/yapnakmonsterthumb.png";
                        e1.printStackTrace();
                    }
                    sql = "UPDATE client SET clientPhoto = ?, clientPhotoUrl = ? WHERE email = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, logo);
                    stmt.setString(2, url);
                    stmt.setString(3, email);
                    int success = stmt.executeUpdate();
                    if (success != -1) {
                        //success
                        logger.info("successfully update photo");
                        out.print("successfully updated");
                        resp.setHeader("Refresh", "1; url=/console");
                    } else {
                        logger.warning("failed to update");
                        out.print("failed to update");
                        resp.setHeader("Refresh", "1; url=/console");
                    }
                } else {
                    logger.warning("No cookie found");
                    out.print("Couldn't find details, are you sure you've signed in?");
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

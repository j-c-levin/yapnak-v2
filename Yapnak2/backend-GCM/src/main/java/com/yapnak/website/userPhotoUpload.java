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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Joshua on 19/11/2015.
 */
public class userPhotoUpload  extends HttpServlet {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private static final Logger logger = Logger.getLogger(photoUpload.class.getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST");
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
                String userId = req.getParameter("userId");
                logger.info("found user to upload image for: " + userId);
                if (userId != null) {
                    String sql = "SELECT userImage FROM user WHERE userId = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    String userImage;
                    if (rs.next()) {
                        userImage = rs.getString("userImage");
                    } else {
                        userImage = "";
                        out.print("failed to update ");
                    }
                    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
                    List<BlobKey> blobKeys = blobs.get("image");
                    if (blobKeys == null || blobKeys.isEmpty()) {
                        //do nothing
                        logger.info("blob empty");
                    } else {
                        if (!userImage.equals("")) {
                            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
                            blobstoreService.delete(new BlobKey(userImage));
                            logger.info("deleted old blob");
                        }
                        userImage = blobKeys.get(0).getKeyString();
                    }
                    ImagesService services = ImagesServiceFactory.getImagesService();
                    ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(userImage));
                    try {
                        url = services.getServingUrl(serve);
                        url = url + "=s200";
                        logger.info("new url:" + url);
                    } catch (IllegalArgumentException e) {
                        url = "http://yapnak.com/images/yapnakmonsterthumb.png";
                        e.printStackTrace();
                    } catch (ImagesServiceFailureException e1) {
                        url = "http://yapnak.com/images/yapnakmonsterthumb.png";
                        e1.printStackTrace();
                    }
                    sql = "UPDATE user SET userImage = ?, userImageUrl = ? WHERE userId = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, userImage);
                    stmt.setString(2, url);
                    stmt.setString(3, userId);
                    int success = 2;
                    success = stmt.executeUpdate();
                    if (success != -1) {
                        //success
                        logger.info("success");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("text/plain");
                    } else {
                        logger.info("FAIL, success = " + success);
                        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        resp.setContentType("text/plain");
                    }
                } else {
                    logger.info("Something went wrong: userId = " + userId);
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

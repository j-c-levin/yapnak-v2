package com.yapnak.gcmbackend;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.ObjectifyService;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "sQLEntityApi",
        version = "v1",
        resource = "sQLEntity",
        namespace = @ApiNamespace(
                ownerDomain = "gcmbackend.yapnak.com",
                ownerName = "gcmbackend.yapnak.com",
                packagePath = ""
        ),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE})

public class SQLEntityEndpoint {

    private static final Logger logger = Logger.getLogger(SQLEntityEndpoint.class.getName());

    private static final String API_KEY = System.getProperty("gcm.api.key");

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(SQLEntity.class);
    }

    @ApiMethod(
            name = "getUser",
            path = "getUser",
            httpMethod = ApiMethod.HttpMethod.POST)
    public PointsEntity getUser(@Named("userID") String userID, @Named("clientEmail") String clientEmail) {
        Connection connection;
        PointsEntity points = new PointsEntity();
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
            try {
//                String statement = "SELECT userID, pushKey FROM user where userID = ?";
                String statement = "SELECT userID FROM user where userID = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, userID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    logger.info("found user " + rs.getString("userID"));

                    //push notification
/*                    String message = "You've gained points, nice one.";
                    Sender sender = new Sender(API_KEY);
                    Message msg = new Message.Builder().addData("message", message).build();
                    Result result = sender.send(msg, rs.getString("pushKey"), 5);*/

                    statement = "SELECT clientID from client where email = ?";
                    stmt = connection.prepareStatement(statement);
                    stmt.setString(1, clientEmail);
                    rs = stmt.executeQuery();
                    rs.next();
                    points.setClientID(rs.getInt("clientID"));
                    logger.info("at client: " + points.getClientID() + " " + clientEmail);
                    points.setUserID(userID);
                    statement = "SELECT points FROM points where userID = ? AND clientID = ?";
                    stmt = connection.prepareStatement(statement);
                    stmt.setString(1, userID);
                    stmt.setInt(2, rs.getInt("clientID"));
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        points.setPoints(rs.getInt("points") + 5);
                        //change the number here to adjust points given)
                        logger.info("points: " + points.getPoints());
                        statement = "UPDATE points SET points = ? where userID = ? AND clientID = ?";
                        stmt = connection.prepareStatement(statement);
                        stmt.setInt(1, points.getPoints());
                        stmt.setString(2, points.getUserID());
                        stmt.setInt(3, points.getClientID());
                        stmt.executeUpdate();
                        connection.close();
                        return points;
                    } else {
                        logger.info("creating " + points.getPoints() + " " + points.getUserID() + " " + points.getClientID());
                        statement = "INSERT INTO points (points,userID,clientID) VALUES (?,?,?)";
                        stmt = connection.prepareStatement(statement);
                        //number of points per visit
                        stmt.setInt(1, 5);
                        stmt.setString(2, points.getUserID());
                        stmt.setInt(3, points.getClientID());
                        stmt.executeUpdate();
                        points.setPoints(5);
                        connection.close();
                        return points;
                    }

                } else {
                    logger.info("couldn't find user");
                    points = null;
                }
            } finally {
                connection.close();
                return points;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return points;
        }
    }

    @ApiMethod(
            name = "forgotLogin",
            path = "forgotLogin",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity forgotLogin(@Named("email") String email) throws ClassNotFoundException, SQLException {
        VoidEntity voidEntity = new VoidEntity();
        Connection connection;
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
            String statement = "SELECT COUNT(email) AS count from client where email = ?";
            PreparedStatement stmt = connection.prepareStatement(statement);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                voidEntity.setStatus("True");
                //Gather hashes and send email
                String reset = hashPassword(String.valueOf(randInt()));
                String cancel = hashPassword(String.valueOf(randInt()));
                statement = "REPLACE forgot (email,reset,cancel,time) VALUES (?,?,?,CURDATE())";
                stmt = connection.prepareStatement(statement);
                stmt.setString(1, email);
                stmt.setString(2, reset);
                stmt.setString(3, cancel);
                stmt.executeUpdate();
                String subject = "Yapnak password reset";
                String message = "Hi,\n\nWe have received a request to reset the password on your Yapnak account.\n\nTo reset, click: www.yapnak.com/resetPassword?response=" + reset + "\n\nThis link will be active for one day.\n\nIf you didn't request this email, click here: www.yapnak.com/cancelReset?response=" + cancel + "\n\nKind regards,\nthe Yapnak team.";
                sendEmail(email, subject, message);
            } else {
                voidEntity.setStatus("False");
                voidEntity.setMessage("Email not found");
            }
        } finally {
            connection.close();
            return voidEntity;
        }
    }

    @ApiMethod(
            name = "resetPassword",
            path = "resetPassword",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity resetPassword(@Named("password") String password, @Named("hash") String hash) throws ClassNotFoundException, SQLException {
        VoidEntity voidEntity = new VoidEntity();
        Connection connection;

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

        String statement = "SELECT email FROM forgot WHERE reset = ?";
        PreparedStatement stmt = connection.prepareStatement(statement);
        stmt.setString(1, hash);
        ResultSet rs = stmt.executeQuery();
        rs.next();

        statement = "UPDATE client SET password = ? WHERE email = ?";
        stmt = connection.prepareStatement(statement);
        stmt.setString(1, hashPassword(password));
        stmt.setString(2, rs.getString("email"));
        stmt.executeUpdate();

        statement = "DELETE FROM forgot WHERE reset = ?";
        stmt = connection.prepareStatement(statement);
        stmt.setString(1, hash);
        stmt.executeUpdate();

        voidEntity.setStatus("True");
        voidEntity.setMessage("");

        connection.close();
        return voidEntity;


    }

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


    /**
     * Returns the {@link SQLEntity} with the corresponding ID.
     *
     * @param x is the x coordinate of the user
     * @param y is the y coordinate of the user
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code SQLEntity} with the provided ID.
     */
    @ApiMethod(
            name = "getClients",
            path = "getClients",
            httpMethod = ApiMethod.HttpMethod.GET)
    public SQLList getClients(@Named("longitude") double x, @Named("latitude") double y, @Named("userID") String userID) throws NotFoundException, OAuthRequestException {

        Connection connection;
        double distance = 0.1;
        List<SQLEntity> list = new ArrayList<SQLEntity>();
        SQLEntity sql = new SQLEntity();
        SQLList sqlList = new SQLList();
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
//            String statement = "SELECT clientName,clientX,clientY,clientOffer offer,clientFoodStyle,clientPhoto,rating,clientID FROM client WHERE clientX BETWEEN ? AND ? AND clientY BETWEEN ? AND ? AND showOffer = 1";
            String statement = "SELECT clientName,clientX,clientY,clientFoodStyle,clientPhoto,client.clientID,offers.offerText offer,offers.offerID FROM client JOIN offers ON client.clientID=offers.clientID AND offers.isActive = 1 AND offers.showOffer = 1 WHERE clientX BETWEEN ? AND ? AND clientY BETWEEN ? AND ?";
            PreparedStatement stmt = connection.prepareStatement(statement);
            double t = x - distance;
            stmt.setDouble(1, t);
            t = x + distance;
            stmt.setDouble(2, t);
            t = y - distance;
            stmt.setDouble(3, t);
            t = y + distance;
            stmt.setDouble(4, t);
            ResultSet rs = stmt.executeQuery();
            ResultSet rt;
            if (rs.next()) {
                rs.beforeFirst();
                while (rs.next()) {
                    logger.info("Retrieving client: " + rs.getString("clientName"));
                    sql = new SQLEntity();
                    sql.setId(rs.getInt("clientID"));
                    sql.setName(rs.getString("clientName"));
                    sql.setOffer(rs.getString("offer"));
                    sql.setX(rs.getDouble("clientX"));
                    sql.setY(rs.getDouble("clientY"));
                    sql.setRating(1);
                    sql.setFoodStyle(rs.getString("clientFoodStyle"));
                    sql.setShowOffer(1);
                    //get photo from blobstore
                    String url;
                    if (!rs.getString("clientPhoto").equals("")) {
                        if (SystemProperty.environment.value() ==
                                SystemProperty.Environment.Value.Production) {
                            logger.info("photo: " + rs.getString("clientPhoto"));
                            ImagesService services = ImagesServiceFactory.getImagesService();
                            ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(rs.getString("clientPhoto")));    // Blobkey of the image uploaded to BlobStore.
                            try {
                                url = services.getServingUrl(serve);
                                url = url + "=s100";
                                logger.info("got Photo: " + url);
                            } catch (IllegalArgumentException e) {
                                url = "http://yapnak.com/images/yapnakmonster.png";
                                e.printStackTrace();
                                logger.warning("IllegalArgumentException: " + e);
                            } catch (ImagesServiceFailureException e1) {
                                url = "http://yapnak.com/images/yapnakmonster.png";
                                e1.printStackTrace();
                                logger.warning("ImagesServiceFailureException: " + e1);
                            }
                        } else {
                            url = rs.getString("clientPhoto");
                        }
                    } else {
                        logger.info("No photo for client");
                        url = "http://yapnak.com/images/yapnakmonster.png";
                    }
                    sql.setPhoto(url);
                    statement = "SELECT points FROM points WHERE clientID = ? and userID = ?";
                    stmt = connection.prepareStatement(statement);
                    stmt.setInt(1, rs.getInt("clientID"));
                    //TODO:put in user name here
                    stmt.setString(2, userID);
                    rt = stmt.executeQuery();
                    if (rt.next()) {
                        sql.setPoints(rt.getInt("points"));
                    } else {
                        sql.setPoints(0);
                    }
                    list.add(sql);
                }
            } else {
                sql = new SQLEntity();
                sql.setOffer("Know anyone you'd like to see on here?");
                sql.setName("No clients found nearby :(");
                sql.setShowOffer(1);
                list.add(sql);
            }
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqlList.setList(list);
            return sqlList;
        }
    }

    /**
     * Returns the {@link SQLEntity} with the corresponding ID.
     *
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code SQLEntity} with the provided ID.
     */
    @ApiMethod(
            name = "getAllClients",
            path = "getAllClients",
            httpMethod = ApiMethod.HttpMethod.GET)
    public allList getAllClients() throws NotFoundException, OAuthRequestException {
/*        if (user == null) {
            throw new OAuthRequestException("User is not valid " + user);
        }*/
        Connection connection;
        List<all> list = new ArrayList<all>();
        ResultSet rs = null;
        all all = null;
        allList alllist = new allList();
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
            String statement = "SELECT * FROM client WHERE clientX BETWEEN -0.408929 AND -0.208929 AND clientY BETWEEN 51.58543 AND 51.78543";
            PreparedStatement stmt = connection.prepareStatement(statement);
            rs = stmt.executeQuery();
            while (rs.next()) {
                all = new all();
                all.setClientID(rs.getInt("clientID"));
                all.setEmail(rs.getString("email"));
                all.setPassword(rs.getString("password"));
                all.setAdmin(rs.getInt("admin"));
                all.setClientName(rs.getString("clientName"));
                all.setClientX(rs.getDouble("clientX"));
                all.setClientY(rs.getDouble("clientY"));
                all.setClientFoodStyle(rs.getString("clientFoodStyle"));
                all.setClientOffer(rs.getString("clientOffer"));
                all.setClientPhoto(rs.getString("clientPhoto"));
                all.setSalt(rs.getString("salt"));
                all.setRating(rs.getDouble("rating"));
                list.add(all);
            }
            connection.close();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        } finally {
            alllist.setList(list);
            return alllist;
        }
    }

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

    // A password hashing method.
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

    static String secureInt() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32).substring(5, 9);
    }

    public static int randInt() {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int max = 9998;
        int min = 1000;
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * Inserts a new {@code SQLEntity}.
     */
    @ApiMethod(
            name = "insertUser",
            path = "insertUser",
            httpMethod = ApiMethod.HttpMethod.POST)
    public UserEntity insert(@Named("email") String email, @Named("password") String password) {

        Connection connection;
        UserEntity user = new UserEntity();
        try {
            if (SystemProperty.environment.value() ==
                    SystemProperty.Environment.Value.Production) {
                // Load the class that provides the new "jdbc:google:mysql://" prefix.
                Class.forName("com.mysql.jdbc.GoogleDriver");
                connection = DriverManager.getConnection("jdbc:google:mysql://yapnak-app:yapnak-main/yapnak_main?user=root");
            } else {
                // Local MySQL instance kto use during development.
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://173.194.230.210/yapnak_main", "client", "g7lFVLRzYdJoWXc3");

            }
            int success = -1;
            try {
                String statement = "SELECT userID FROM user WHERE email = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    //user already exists with a google sign in
                    user.setUserID(rs.getString("userID"));
                } else {
                    statement = "INSERT INTO user (userID, email, password) VALUES(?,?,?)";
                    stmt = connection.prepareStatement(statement);

                    //Generate userID
                    String userID = "";
                    userID = email.substring(0, 4) + randInt();
                    user.setUserID(userID);
                    stmt.setString(1, userID);
                    stmt.setString(2, email);
                    stmt.setString(3, hashPassword(password));
                    success = stmt.executeUpdate();
                    if (success == -1) {
                        logger.warning("Inserting user failed");
                        user.setUserID("Failed");
                    } else {
                        logger.info("Successfully inserted the user");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.close();
                return user;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return user;
        }
    }

    //Dangerous API, depreciated
//    @ApiMethod(
//            name = "insertExternalUser",
//            path = "insertExternalUser",
//            httpMethod = ApiMethod.HttpMethod.POST)
//    public UserEntity insertExternal(@Named("email") String email) throws ClassNotFoundException, SQLException {
//        Connection connection;
//        UserEntity user = new UserEntity();
//        try {
//            if (SystemProperty.environment.value() ==
//                    SystemProperty.Environment.Value.Production) {
//                // Load the class that provides the new "jdbc:google:mysql://" prefix.
//                Class.forName("com.mysql.jdbc.GoogleDriver");
//                connection = DriverManager.getConnection("jdbc:google:mysql://yapnak-app:yapnak-main/yapnak_main?user=root");
//            } else {
//                // Local MySQL instance kto use during development.
//                Class.forName("com.mysql.jdbc.Driver");
//                connection = DriverManager.getConnection("jdbc:mysql://173.194.230.210/yapnak_main", "client", "g7lFVLRzYdJoWXc3");
//            }
//            int success = -1;
//            try {
//                String statement = "SELECT userID FROM user WHERE email = ?";
//                PreparedStatement stmt = connection.prepareStatement(statement);
//                stmt.setString(1, email);
//                ResultSet rs = stmt.executeQuery();
//                if (rs.next()) {
//                    //user already exists with a google sign in
//                    logger.info("found user " + rs.getString("userID"));
//                    user.setUserID(rs.getString("userID"));
//                } else {
//                    statement = "INSERT INTO user (userID, email) VALUES(?,?)";
//                    stmt = connection.prepareStatement(statement);
//                    //Generate userID
//                    String userID = "";
//                    userID = email.substring(0, 4) + randInt();
//                    user.setUserID(userID);
//                    stmt.setString(1, userID);
//                    stmt.setString(2, email);
//                    success = stmt.executeUpdate();
//                    if (success == -1) {
//                        logger.warning("Inserting user failed");
//                        user.setUserID("Failed");
//                    } else {
//                        logger.info("Successfully inserted the user " + user.getUserID());
//                    }
//                }
//            } finally {
//                connection.close();
//                return user;
//            }
//        } finally {
//            return user;
//        }
//    }

    @ApiMethod(
            name = "feedback",
            path = "feedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void feedback(@Named("Message") String message, @Named("type") int type, @Named("userID") String userID) throws UnsupportedEncodingException, MessagingException {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        javax.mail.Message msg = new MimeMessage(session);
        switch (type) {
            //1 = positive
            case 1:
                props = new Properties();
                session = Session.getDefaultInstance(props, null);
                msg = new MimeMessage(session);
                try {
                    msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                    msg.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                    msg.setSubject("Positive feedback");
                    msg.setText("From: " + userID + " - " + message);
                    Transport.send(msg);
                } finally {
                }
                break;
            //1 = negative, general
            case 2:
                props = new Properties();
                session = Session.getDefaultInstance(props, null);
                msg = new MimeMessage(session);
                try {
                    msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                    msg.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                    msg.setSubject("Negative feedback");
                    msg.setText("From: " + userID + " - " + message);
                    Transport.send(msg);
                } finally {

                }
                break;
            //3 = negative, client didn't accept user code
            case 3:
                props = new Properties();
                session = Session.getDefaultInstance(props, null);
                msg = new MimeMessage(session);
                try {
                    msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                    msg.addRecipient(javax.mail.Message.RecipientType.TO,
                            new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                    msg.setSubject("Negative feedback - client didn't accept code");
                    msg.setText("From: " + userID + " - " + message);
                    Transport.send(msg);
                } finally {

                }
                break;
        }
        return;
    }

    @ApiMethod(
            name = "setUserDetails",
            path = "setUserDetails",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void setUserDetails(@Named("number") String mobNo, @Named("fName") String fName, @Named("lName") String lName, @Named("userID") String userID) throws ClassNotFoundException, SQLException {
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
            int success = -1;
            try {
                String statement = "UPDATE user SET mobNo = ?, firstName = ?, lastName = ? WHERE userID = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, mobNo);
                stmt.setString(2, fName);
                stmt.setString(3, lName);
                stmt.setString(4, userID);
                success = stmt.executeUpdate();
            } finally {
                connection.close();
                return;
            }
        } finally {
            return;
        }
    }

    @ApiMethod(
            name = "getUserDetails",
            path = "getUserDetails",
            httpMethod = ApiMethod.HttpMethod.POST)
    public UserEntity getUserDetails(@Named("userID") String userID) throws ClassNotFoundException, SQLException {
        Connection connection;
        UserEntity user = new UserEntity();
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
            try {
                String statement = "SELECT firstName, lastName, mobNo, email FROM user WHERE userID = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, userID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getString("email") != null) {
                        user.setEmail(rs.getString("email"));
                    } else {
                        user.setEmail("_null");
                    }
                    if (rs.getString("firstName") != null) {
                        user.setFirstName(rs.getString("firstName"));
                    } else {
                        user.setFirstName("_null");
                    }
                    if (rs.getString("lastName") != null) {
                        user.setLastName(rs.getString("lastName"));
                    } else {
                        user.setLastName("_null");
                    }
                    if (rs.getString("mobNo") != null) {
                        user.setMobNo(rs.getString("mobNo"));
                    } else {
                        user.setMobNo("_null");
                    }
                } else {
                    logger.info("nothing found");
                }
            } finally {
                connection.close();
                return user;
            }
        } finally {
            return user;
        }
    }

    @ApiMethod(
            name = "recommend",
            path = "recommend",
            httpMethod = ApiMethod.HttpMethod.POST)
    private RecommendEntity recommend(@Named("user") String userID, @Named("clientID") int clientID, @Named("this user") String r_userID) throws ClassNotFoundException, SQLException {
        Connection connection;
        RecommendEntity recommendation = new RecommendEntity();
        String pushKey;
        String statement;
        PreparedStatement stmt;
        ResultSet rs;
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
            try {
                //check if userID is in system
                statement = "SELECT pushKey FROM user WHERE userID = ?";
                stmt = connection.prepareStatement(statement);
                stmt.setString(1, userID);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    //UserID exists in system, check if referred
                    pushKey = rs.getString("pushKey");
                    statement = "SELECT referrerID FROM points WHERE clientID = ? AND userID = ?";
                    stmt = connection.prepareStatement(statement);
                    stmt.setInt(1, clientID);
                    stmt.setString(2, userID);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        //user is also ready referred
                        recommendation.setResult(0);
                    } else {
                        //user hasn't been recommended, check the row exists
                        statement = "SELECT points FROM points where clientID = ? and userID = ?";
                        stmt = connection.prepareStatement(statement);
                        stmt.setInt(1, clientID);
                        stmt.setString(2, userID);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            //update the referrerID
                            statement = "UPDATE points SET referrerID = ? where clientID = ? and userID = ?";
                            stmt = connection.prepareStatement(statement);
                            stmt.setString(1, r_userID);
                            stmt.setInt(2, clientID);
                            stmt.setString(3, userID);
                            stmt.executeUpdate();
                            recommendation.setResult(1);
                            //post update
                            statement = "SELECT clientName from client where clientID = ?";
                            stmt = connection.prepareStatement(statement);
                            stmt.setInt(1, clientID);
                            rs = stmt.executeQuery();
                            rs.next();
                            //push notification
                            String message = "You have been recommended to eat at " + rs.getString("clientName");
                            Sender sender = new Sender(API_KEY);
                            Message msg = new Message.Builder().addData("message", message).build();
                            sender.send(msg, pushKey, 5);
                        } else {
                            //add a new row to the points table with referrerID
                            statement = "INSET INTO points (userID, clientID, referrerID) VALUES (?,?,?)";
                            stmt = connection.prepareStatement(statement);
                            stmt.setString(1, userID);
                            stmt.setInt(2, clientID);
                            stmt.setString(3, r_userID);
                            stmt.executeUpdate();
                            recommendation.setResult(1);
                            //post update
                            statement = "SELECT clientName from client where clientID = ?";
                            stmt = connection.prepareStatement(statement);
                            stmt.setInt(1, clientID);
                            rs = stmt.executeQuery();
                            rs.next();
                            //push notification
                            String message = "You have been recommended to eat at " + rs.getString("clientName");
                            Sender sender = new Sender(API_KEY);
                            Message msg = new Message.Builder().addData("message", message).build();
                            sender.send(msg, pushKey, 5);
                        }
                    }
                } else {
                    //check if it's a mobile number
                    statement = "SELECT pushKey FROM user WHERE mobNo = ?";
                    stmt = connection.prepareStatement(statement);
                    stmt.setString(1, userID);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        //mobNo is in system
                        pushKey = rs.getString("pushKey");
                        statement = "SELECT referrerID FROM points WHERE clientID = ? AND userID = ?";
                        stmt = connection.prepareStatement(statement);
                        stmt.setInt(1, clientID);
                        stmt.setString(2, userID);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            //user is also ready referred
                            recommendation.setResult(0);
                        } else {
                            //user hasn't been recommended, check the row exists
                            statement = "SELECT points FROM points where clientID = ? and userID = ?";
                            stmt = connection.prepareStatement(statement);
                            stmt.setInt(1, clientID);
                            stmt.setString(2, userID);
                            rs = stmt.executeQuery();
                            if (rs.next()) {
                                //update the referrerID
                                statement = "UPDATE points SET referrerID = ? where clientID = ? and userID = ?";
                                stmt = connection.prepareStatement(statement);
                                stmt.setString(1, r_userID);
                                stmt.setInt(2, clientID);
                                stmt.setString(3, userID);
                                stmt.executeUpdate();
                                recommendation.setResult(1);
                                //post update
                                statement = "SELECT clientName from client where clientID = ?";
                                stmt = connection.prepareStatement(statement);
                                stmt.setInt(1, clientID);
                                rs = stmt.executeQuery();
                                rs.next();
                                //push notification
                                String message = "You have been recommended to eat at " + rs.getString("clientName");
                                Sender sender = new Sender(API_KEY);
                                Message msg = new Message.Builder().addData("message", message).build();
                                sender.send(msg, pushKey, 5);
                            } else {
                                //add a new row to the points table with referrerID
                                statement = "INSET INTO points (userID, clientID, referrerID) VALUES (?,?,?)";
                                stmt = connection.prepareStatement(statement);
                                stmt.setString(1, userID);
                                stmt.setInt(2, clientID);
                                stmt.setString(3, r_userID);
                                stmt.executeUpdate();
                                recommendation.setResult(1);
                                //post update
                                statement = "SELECT clientName from client where clientID = ?";
                                stmt = connection.prepareStatement(statement);
                                stmt.setInt(1, clientID);
                                rs = stmt.executeQuery();
                                rs.next();
                                //push notification
                                String message = "You have been recommended to eat at " + rs.getString("clientName");
                                Sender sender = new Sender(API_KEY);
                                Message msg = new Message.Builder().addData("message", message).build();
                                sender.send(msg, pushKey, 5);
                            }
                        }
                    } else {
                        //user isn't in system, send a text
                        recommendation.setResult(2);
                    }
                }
            } finally {
                connection.close();
                return recommendation;
            }
        } finally {
            return recommendation;
        }
    }

    @ApiMethod(
            name = "getClientInfo",
            path = "getClientInfo",
            httpMethod = ApiMethod.HttpMethod.GET)
    public ClientEntity getClientInfo(@Named("email") String email) {
        ClientEntity client = new ClientEntity();
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
            try {
                String statement = "SELECT client.clientID, clientName, clientX, clientY, clientFoodStyle, clientPhoto, client.offer1,client.offer2,client.offer3, offers.offerID, offers.offerText offer, offers.showOffer showOffer FROM client JOIN offers ON client.clientID=offers.clientID WHERE client.email = ? AND isActive = 1";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    //client found
                    logger.info("retrieving client data: " + rs.getString("clientName"));
                    client.setStatus("True");
                    client.setId(rs.getInt("clientID"));
                    client.setName(rs.getString("clientName"));
                    client.setX(rs.getDouble("clientX"));
                    client.setY(rs.getDouble("clientY"));
                    client.setFoodStyle(rs.getString("clientFoodStyle"));
                    String url;
                    if (SystemProperty.environment.value() ==
                            SystemProperty.Environment.Value.Production) {
                        if (!rs.getString("clientPhoto").equals("")) {
                            if (SystemProperty.environment.value() ==
                                    SystemProperty.Environment.Value.Production) {
                                ImagesService services = ImagesServiceFactory.getImagesService();
                                ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(rs.getString("clientPhoto")));    // Blobkey of the image uploaded to BlobStore.
                                try {
                                    url = services.getServingUrl(serve);
                                    url = url + "=s100";
                                    logger.info("got Photo: " + url);
                                } catch (IllegalArgumentException e) {
                                    url = "http://yapnak.com/images/yapnakmonster.png";
                                    e.printStackTrace();
                                    logger.warning("IllegalArgumentException: " + e);
                                } catch (ImagesServiceFailureException e1) {
                                    url = "http://yapnak.com/images/yapnakmonster.png";
                                    e1.printStackTrace();
                                    logger.warning("ImagesServiceFailureException: " + e1);
                                }
                            } else {
                                url = "http://yapnak.com/images/yapnakmonster.png";
                            }
                        } else {
                            url = "http://yapnak.com/images/yapnakmonster.png";
                        }
                    } else {
                        url = "http://yapnak.com/images/yapnakmonster.png";
                    }
                    client.setPhoto(url);
                    do {

                        if (rs.getInt("offerID") == rs.getInt("offer1")) {
                            logger.info("found offer 1: " + rs.getString("offer"));
                            client.setShowOffer1(rs.getInt("showOffer"));
                            client.setOffer1(rs.getString("offer"));

                        } else if (rs.getInt("offerID") == rs.getInt("offer2")) {
                            logger.info("found offer 2: " + rs.getString("offer"));
                            client.setShowOffer2(rs.getInt("showOffer"));
                            client.setOffer2(rs.getString("offer"));

                        } else {
                            logger.info("found offer 3: " + rs.getString("offer"));
                            client.setShowOffer3(rs.getInt("showOffer"));
                            client.setOffer3(rs.getString("offer"));
                        }

                    } while (rs.next());

                } else {
                    //client not found
                    client.setStatus("False");
                    client.setMessage("No client found with that email");
                }
            } finally {
                connection.close();
                return client;
            }
        } finally {
            return client;
        }
    }

    @ApiMethod(
            name = "updateClientType",
            path = "updateClientType",
            httpMethod = ApiMethod.HttpMethod.POST)
    public ClientEntity updateClientType(@Named("type") String clientType, @Named("email") String email) {
        ClientEntity client = new ClientEntity();
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
            try {
                String statement = "UPDATE client set clientFoodStyle = ? where email = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, clientType);
                stmt.setString(2, email);
                int success = stmt.executeUpdate();
                if (success == 1) {
                    client.setStatus("True");
                    logger.info("successfully updated " + email + " food type to " + clientType);
                } else {
                    client.setMessage("False");
                    client.setMessage("Failed to update food type");
                    logger.info("failed to update " + email + " food type to " + clientType);
                }
            } finally {
                connection.close();
                return client;
            }
        } finally {
            return client;
        }
    }

    @ApiMethod(
            name = "updateClientName",
            path = "updateClientName",
            httpMethod = ApiMethod.HttpMethod.POST)
    public ClientEntity updateClientName(@Named("name") String clientName, @Named("email") String email) {
        ClientEntity client = new ClientEntity();
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
            try {
                String statement = "UPDATE client set clientName = ? where email = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, clientName);
                stmt.setString(2, email);
                int success = stmt.executeUpdate();
                if (success == 1) {
                    client.setStatus("True");
                    logger.info("successfully update client name to " + clientName);
                } else {
                    client.setMessage("False");
                    client.setMessage("Failed to update client name");
                    logger.info("Failed to update client name to " + clientName);
                }
            } finally {
                connection.close();
                return client;
            }
        } finally {
            return client;
        }
    }

    @ApiMethod(
            name = "updateClientLocation",
            path = "updateClientLocation",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity updateClientLocation(@Named("address") String address, @Named("email") String email) {
        VoidEntity voidEntity = new VoidEntity();
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
            try {
                String recv = "";
                String recvbuff = "";
                address = address.replaceAll(" ", "+");
                //need to check first char is valid and not a space
                URL jsonpage = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=AIzaSyDBM6kltuQ1mF_X9XYCseXR9x95uc9fyv4");
                URLConnection urlcon = jsonpage.openConnection();
                BufferedReader buffread = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));

                while ((recv = buffread.readLine()) != null)
                    recvbuff += recv;
                buffread.close();
                JSONParser x = new JSONParser();
                JSONObject j = null;
                try {
                    j = (JSONObject) x.parse(recvbuff);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                JSONArray array = (JSONArray) j.get("results");
                JSONObject details = (JSONObject) array.get(0);
                details = (JSONObject) details.get("geometry");
                details = (JSONObject) details.get("location");
                double Y = (double) details.get("lat");
                double X = (double) details.get("lng");
                String sql = "UPDATE client SET clientX = ?, clientY = ? WHERE email = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setDouble(1, X);
                stmt.setDouble(2, Y);
                stmt.setString(3, email);
                int success = stmt.executeUpdate();
                if (success == 1) {
                    voidEntity.setStatus("True");
                } else {
                    voidEntity.setStatus("False");
                    voidEntity.setMessage("Couldn't update the database, is the email correct?");
                }
            } finally {
                connection.close();
                return voidEntity;
            }
        } finally {
            return voidEntity;
        }
    }

    @ApiMethod(
            name = "toggleOffer",
            path = "toggleOffer",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity toggleOffer(@Named("email") String email, @Named("offer") int offer, @Named("value") int value) {
        VoidEntity voidEntity = new VoidEntity();
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
            try {
                String sql;
                if (offer == 1) {
                    sql = "SELECT offer1 offerID FROM client WHERE email = ?";
                } else if (offer == 2) {
                    sql = "SELECT offer2 offerID FROM client WHERE email = ?";
                } else {
                    sql = "SELECT offer3 offerID FROM client WHERE email = ?";
                }
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    sql = "UPDATE offers SET showOffer = ? WHERE offerID = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setInt(1, value);
                    stmt.setInt(2, rs.getInt("offerID"));
                    int success = stmt.executeUpdate();
                    if (success == 1) {
                        voidEntity.setStatus("True");
                    } else {
                        voidEntity.setStatus("False");
                        voidEntity.setMessage("Failed to update offers table");
                    }
                } else {
                    voidEntity.setStatus("False");
                    voidEntity.setMessage("Failed to find offerID");
                }
            } finally {
                connection.close();
                return voidEntity;
            }
        } finally {
            return voidEntity;
        }
    }

    @ApiMethod(
            name = "updateOffer",
            path = "updateOffer",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity updateOffer(@Named("email") String email, @Named("offer") int offer, @Named("text") String text) {
        VoidEntity voidEntity = new VoidEntity();
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
            try {
                String sql;
                if (offer == 1) {
                    sql = "SELECT offer1 offerID FROM client WHERE email = ?";
                } else if (offer == 2) {
                    sql = "SELECT offer2 offerID FROM client WHERE email = ?";
                } else {
                    sql = "SELECT offer3 offerID FROM client WHERE email = ?";
                }
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    sql = "UPDATE offers SET offerText = ? WHERE offerID = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, text);
                    stmt.setInt(2, rs.getInt("offerID"));
                    int success = stmt.executeUpdate();
                    if (success == 1) {
                        voidEntity.setStatus("True");
                    } else {
                        voidEntity.setStatus("False");
                        voidEntity.setMessage("Failed to update offers table");
                    }
                } else {
                    voidEntity.setStatus("False");
                    voidEntity.setMessage("Failed to find offerID");
                }
            } finally {
                connection.close();
                return voidEntity;
            }
        } finally {
            return voidEntity;
        }
    }

    @ApiMethod(
            name = "insertOffer",
            path = "insertOffer",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity insertOffer(@Named("email") String email, @Named("offer") int offer, @Named("text") String text) {
        VoidEntity voidEntity = new VoidEntity();
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
            try {
                String sql;
                sql = "SELECT clientID FROM client WHERE email = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    logger.info("Inserting new offer: " + text);
                    sql = "INSERT INTO offers (clientID, offerText) VALUES (?,?)";
                    stmt = connection.prepareStatement(sql);
                    stmt.setInt(1, rs.getInt("clientID"));
                    stmt.setString(2, text);
                    int success = stmt.executeUpdate();
                    if (success == 1) {
                        //deactivate old offer
                        if (offer == 1) {
                            sql = "SELECT offer1 offerID FROM client WHERE email = ?";
                        } else if (offer == 2) {
                            sql = "SELECT offer2 offerID FROM client WHERE email = ?";
                        } else {
                            sql = "SELECT offer3 offerID FROM client WHERE email = ?";
                        }
                        stmt = connection.prepareStatement(sql);
                        stmt.setString(1, email);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            sql = "UPDATE offers SET isActive = 0 WHERE offerID = ?";
                            stmt = connection.prepareStatement(sql);
                            stmt.setInt(1, rs.getInt("offerID"));
                            success = stmt.executeUpdate();
                            if (success == 1) {
                                //set new offer in clients tabs
                                if (offer == 1) {
                                    sql = "UPDATE client SET offer1 = LAST_INSERT_ID() WHERE email = ?";
                                } else if (offer == 2) {
                                    sql = "UPDATE client SET offer2 = LAST_INSERT_ID() WHERE email = ?";
                                } else {
                                    sql = "UPDATE client SET offer3 = LAST_INSERT_ID() WHERE email = ?";
                                }
                                stmt = connection.prepareStatement(sql);
                                stmt.setString(1, email);
                                success = stmt.executeUpdate();
                                if (success == 1) {
                                    voidEntity.setStatus("True");
                                } else {
                                    voidEntity.setStatus("False");
                                    voidEntity.setMessage("Failed to update client table after inserting into offers");
                                }
                            } else {
                                voidEntity.setStatus("False");
                                voidEntity.setMessage("Failed to set old offer inactive");
                            }
                        } else {
                            voidEntity.setStatus("False");
                            voidEntity.setMessage("Failed to find old offer");
                        }
                    } else {
                        voidEntity.setStatus("False");
                        voidEntity.setMessage("Failed to insert new offer");
                    }
                } else {
                    voidEntity.setStatus("False");
                    voidEntity.setMessage("Failed to find clientID");
                }
            } finally {
                connection.close();
                return voidEntity;
            }
        } finally {
            return voidEntity;
        }
    }

    @ApiMethod(
            name = "searchUsers",
            path = "searchUsers",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SearchUserEntity searchUsers(@Named("details") String[] details) {
        SearchUserEntity user = new SearchUserEntity();
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
            try {
                String statement = "SELECT COUNT(*),userID FROM user WHERE email = ? OR mobNO = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                ResultSet rs;
                List<String> isUser = new ArrayList<String>();
                for (int i = 0; i < details.length; i++) {
                    stmt.setString(1, details[i]);
                    stmt.setString(2, details[i]);
                    rs = stmt.executeQuery();
                    rs.next();
                    isUser.add(rs.getString("userID"));
                }
                user.setStatus("True");
                user.setIsUser(isUser);
            } finally {
                connection.close();
                return user;
            }
        } catch (ClassNotFoundException e) {
            user.setStatus("False");
            user.setMessage("ClassNotFoundException");
            e.printStackTrace();
        } catch (SQLException e) {
            user.setStatus("False");
            user.setMessage("SQLException");
            e.printStackTrace();
        } finally {
            return user;
        }
    }

    @ApiMethod(
            name = "userFeedback",
            path = "userFeedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public VoidEntity userFeedback(@Named("rating") double rating, @Named("isAccepted") int isAccepted, @Named("message") @Nullable String message, @Named("userID") String userID, @Named("offerID") int offerID) {
        VoidEntity voidEntity = new VoidEntity();
        Connection connection;
        logger.info(message);
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
            try {
                if (isAccepted == 0) {
                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props, null);
                    javax.mail.Message msg = new MimeMessage(session);
                    try {
                        msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                        msg.addRecipient(javax.mail.Message.RecipientType.TO,
                                new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                        msg.setSubject("Negative feedback - client didn't accept code");
                        msg.setText("From: " + userID + " regarding offerID: " + offerID + " - " + message);
                        Transport.send(msg);
                    } finally {

                    }
                }
            } finally {
                voidEntity.setStatus("True");
                connection.close();
                return voidEntity;
            }
        } catch (ClassNotFoundException e) {
            voidEntity.setStatus("False");
            voidEntity.setMessage("ClassNotFoundException");
            e.printStackTrace();
        } catch (SQLException e) {
            voidEntity.setStatus("False");
            voidEntity.setMessage("SQLException");
            e.printStackTrace();
        } finally {
            return voidEntity;
        }
    }

    @ApiMethod(
            name = "clientLogin",
            path = "clientLogin",
            httpMethod = ApiMethod.HttpMethod.POST)
    public ClientEntity clientLogin(@Named("email") String email, @Named("password") String password) {
        ClientEntity client = new ClientEntity();
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
            try {
                String statement = "SELECT COUNT(*) FROM client WHERE email = ? AND password = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    //client login
                    statement = "SELECT client.clientID, clientName, clientX, clientY, clientFoodStyle, clientPhoto, client.offer1,client.offer2,client.offer3, offers.offerID, offers.offerText offer, offers.showOffer showOffer FROM client JOIN offers ON client.clientID=offers.clientID WHERE client.email = ? AND isActive = 1";
                    stmt = connection.prepareStatement(statement);
                    stmt.setString(1, email);
                    rs = stmt.executeQuery();
                    rs.next();
                    client.setStatus("True");
                    client.setId(rs.getInt("clientID"));
                    client.setName(rs.getString("clientName"));
                    client.setX(rs.getDouble("clientX"));
                    client.setY(rs.getDouble("clientY"));
                    client.setFoodStyle(rs.getString("clientFoodStyle"));
                    String url;
                    if (SystemProperty.environment.value() ==
                            SystemProperty.Environment.Value.Production) {
                        if (!rs.getString("clientPhoto").equals("")) {
                            if (SystemProperty.environment.value() ==
                                    SystemProperty.Environment.Value.Production) {
                                ImagesService services = ImagesServiceFactory.getImagesService();
                                ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(rs.getString("clientPhoto")));    // Blobkey of the image uploaded to BlobStore.
                                try {
                                    url = services.getServingUrl(serve);
                                    url = url + "=s100";
                                    logger.info("got Photo: " + url);
                                } catch (IllegalArgumentException e) {
                                    url = "http://yapnak.com/images/yapnakmonster.png";
                                    e.printStackTrace();
                                    logger.warning("IllegalArgumentException: " + e);
                                } catch (ImagesServiceFailureException e1) {
                                    url = "http://yapnak.com/images/yapnakmonster.png";
                                    e1.printStackTrace();
                                    logger.warning("ImagesServiceFailureException: " + e1);
                                }
                            } else {
                                url = "http://yapnak.com/images/yapnakmonster.png";
                            }
                        } else {
                            url = "http://yapnak.com/images/yapnakmonster.png";
                        }
                    } else {
                        url = "http://yapnak.com/images/yapnakmonster.png";
                    }
                    client.setPhoto(url);
                    do {

                        if (rs.getInt("offerID") == rs.getInt("offer1")) {
                            logger.info("found offer 1: " + rs.getString("offer"));
                            client.setShowOffer1(rs.getInt("showOffer"));
                            client.setOffer1(rs.getString("offer"));

                        } else if (rs.getInt("offerID") == rs.getInt("offer2")) {
                            logger.info("found offer 2: " + rs.getString("offer"));
                            client.setShowOffer2(rs.getInt("showOffer"));
                            client.setOffer2(rs.getString("offer"));

                        } else {
                            logger.info("found offer 3: " + rs.getString("offer"));
                            client.setShowOffer3(rs.getInt("showOffer"));
                            client.setOffer3(rs.getString("offer"));
                        }

                    } while (rs.next());
                } else {
                    //login failed
                    client.setStatus("False");
                    client.setMessage("Client login details incorrect");
                }
            } finally {
                connection.close();
                return client;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return client;
        }
    }

    @ApiMethod(
            name = "qrSubmit",
            path = "qrSubmit",
            httpMethod = ApiMethod.HttpMethod.POST)

    public qrEntity qrSubmit(@Named("userID") String userID, @Named("clientID") int clientID, @Named("datetime") String date, @Named("hash") String hash) {
        qrEntity qr = new qrEntity();
        qr.setStatus("True");
        return qr;
    }
}
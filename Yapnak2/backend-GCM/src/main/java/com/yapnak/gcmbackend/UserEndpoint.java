package com.yapnak.gcmbackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
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
 * An endpoint class we are exposing
 */
@Api(
        name = "userEndpointApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "gcmbackend.yapnak.com",
                ownerName = "gcmbackend.yapnak.com",
                packagePath = ""
        )
)
public class UserEndpoint {
    private static final Logger logger = Logger.getLogger(UserEndpoint.class.getName());

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

    //Generates some random numbers
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

    @ApiMethod(
            name = "updatePhotoUrl",
            path = "updatePhotoUrl",
            httpMethod = ApiMethod.HttpMethod.POST)
    public OfferEntity updatePhotoUrl() {
        OfferEntity response = new OfferEntity();
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
                String query = "SELECT clientPhoto, clientID from client";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                query = "UPDATE client SET clientPhotoUrl = ? WHERE clientID = ?";
                statement = connection.prepareStatement(query);
                while (rs.next()) {
                    logger.info("found " + rs.getString("clientID"));
                    ImagesService services = ImagesServiceFactory.getImagesService();
                    ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(rs.getString("clientPhoto")));
                    String url;
                    try {
                        url = services.getServingUrl(serve);
                        url = url + "=s100";
                    } catch (IllegalArgumentException e) {
                        url = "http://yapnak.com/images/yapnakmonster.png";
                        logger.warning("IllegalArgumentException " + e);
                        e.printStackTrace();
                    } catch (ImagesServiceFailureException e1) {
                        url = "http://yapnak.com/images/yapnakmonster.png";
                        logger.warning("ImagesServiceFailureException " + e1);
                        e1.printStackTrace();
                    }
                    statement.setString(1, url);
                    statement.setInt(2, rs.getInt("clientID"));
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        logger.warning("Failed to update logo for " + rs.getInt("clientID"));
                    } else {
                        logger.info(rs.getInt("clientID") + " logo updated");
                    }
                }
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "registerUser",
            path = "registerUser",
            httpMethod = ApiMethod.HttpMethod.POST)
    public RegisterUserEntity registerUser(@Named("email") @Nullable String email, @Named("mobNo") @Nullable String mobNo, @Named("password") String password, @Named("firstName") @Nullable String firstName, @Named("lastName") @Nullable String lastName, @Named("promoCode") @Nullable String promoCode) {
        RegisterUserEntity response = new RegisterUserEntity();
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
                String query;
                PreparedStatement statement;
                String userId;
                if (email != null) {
                    query = "SELECT COUNT(*),userID FROM user WHERE email = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, email);
                    userId = email;
                    logger.info("searching for registered email: " + email);
                } else if (mobNo != null) {
                    query = "SELECT COUNT(*) FROM user WHERE mobNo = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, mobNo);
                    userId = mobNo;
                    logger.info("searching for registered number: " + mobNo);
                } else {
                    //user details are missing
                    response.setStatus("False");
                    response.setMessage("User email or mobile number missing");
                    logger.info("User email or mobile number missing");
                    break queryBlock;
                }
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    //User already exists in the system
                    response.setStatus("False");
                    response.setMessage("User already registered");
                    response.setUserId(rs.getString("userID"));
                    logger.warning("User already registered");
                    break queryBlock;
                }
                logger.info("no user found, registering");
                //User needs to be registered
                query = "INSERT INTO user (userID, firstName, lastName, mobNo, email, password) VALUES (?,?,?,?,?,?)";
                statement = connection.prepareStatement(query);
                String realUserId = "";
                if (!Character.isLetter(userId.charAt(0))) {
                    int start = randInt() / 1000;
                    String[] chars = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
                    userId = chars[(start % 9)] + userId.substring(1);
                    logger.info("Replacing first: " + userId);
                }
                try {
                    userId = userId.split("@")[0];
                    logger.info(userId);
                    if (userId.length() > 3) {
                        for (int i = 0; i < 3; i++) {
                            if (!Character.isLetterOrDigit((int) userId.charAt(i))) {
                                realUserId += String.valueOf(randInt()).substring(0, 1);
                            } else {
                                realUserId += userId.charAt(i);
                            }
                        }
                    } else {
                        for (int i = 0; i < userId.length(); i++) {
                            logger.info("Here be: " + userId.charAt(i));
                            if (!Character.isLetterOrDigit((int) userId.charAt(i))) {
                                realUserId += String.valueOf(randInt()).substring(0, 1);
                            } else {
                                realUserId += userId.charAt(i);
                            }
                        }
                        logger.info("realUserId not long enough, expanding");
                        while (realUserId.length() < 4) {
                            realUserId += String.valueOf(randInt()).substring(0, 1);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("EXCEPTION realUserId not long enough, expanding");
                    while (realUserId.length() < 4) {
                        realUserId += String.valueOf(randInt()).substring(0, 1);
                    }
                }
                realUserId += randInt();
                logger.info("Final: " + realUserId);
                statement.setString(1, realUserId);
                statement.setString(2, (firstName == null) ? "" : firstName);
                statement.setString(3, (lastName == null) ? "" : lastName);
                statement.setString(4, (mobNo == null) ? "" : mobNo);
                statement.setString(5, (email == null) ? "" : email);
                statement.setString(6, hashPassword(password));
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Insert failed
                    logger.warning("Registration insert FAILED");
                    response.setStatus("False");
                    response.setMessage("Registration insert failed");
                    break queryBlock;
                }
                response.setUserId(realUserId);
                if (promoCode == null) {
                    logger.info("Registration insert success");
                    response.setStatus("True");
                    break queryBlock;
                }
                //find promoId
                query = "SELECT promoId FROM promo WHERE promoCode = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, promoCode);
                rs = statement.executeQuery();
                if (!rs.next()) {
                    //ID not found
                    logger.info("No promoID found for " + promoCode);
                    response.setMessage("User inserted but promo code invalid");
                    response.setStatus("False");
                    break queryBlock;
                }
                logger.info("Found promoID " + rs.getInt("promoId"));
                //add to promo redeemed table
                query = "INSERT INTO promoRedeemed (promoId,userId) VALUES (?,?)";
                statement = connection.prepareStatement(query);
                statement.setInt(1, rs.getInt("promoId"));
                statement.setString(2, realUserId);
                success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Insert to promoRedeemed failed");
                    response.setStatus("False");
                    response.setStatus("Insert to promoRedeemed failed");
                    break queryBlock;
                }
                response.setMessage("Promo code accepted");
                //Update user points
                query = "UPDATE user SET loyaltyPoints = 10 where userID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, realUserId);
                success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Insert to update user loyalty points");
                    response.setStatus("False");
                    response.setStatus("Insert to update user loyalty points");
                }
                //Points update done
                logger.info("Registration insert and promo redeemed success");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "authenticateUser",
            path = "authenticateUser",
            httpMethod = ApiMethod.HttpMethod.POST)
    public AuthenticateEntity authenticateUser(@Named("email") @Nullable String
                                                       email, @Named("mobNo") @Nullable String mobNo, @Named("password") String password) {
        AuthenticateEntity response = new AuthenticateEntity();
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
                PreparedStatement statement;
                if (!email.equals("")) {
                    String query = "SELECT userID FROM user WHERE email = ? AND password = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, email);
                    logger.info("beginning user authentication for: " + email);
                } else if (!mobNo.equals("")) {
                    String query = "SELECT userID FROM user WHERE mobNo = ? AND password = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, mobNo);
                    logger.info("beginning user authentication for: " + mobNo);
                } else {
                    //user details are missing
                    response.setStatus("False");
                    response.setMessage("User email or mobile number missing");
                    logger.warning("User email or mobile number missing");
                    connection.close();
                    return response;
                }
                statement.setString(2, hashPassword(password));
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    //user valid
                    //do some authentication here
                    logger.info("successful authentication");
                    response.setStatus("True");
                    response.setUserId(rs.getString("userID"));
                } else {
                    //details incorrect
                    response.setStatus("False");
                    response.setMessage("User details incorrect");
                    logger.warning("User details incorrect");
                }
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "deauthenticateUser",
            path = "deauthenticateUser",
            httpMethod = ApiMethod.HttpMethod.GET)
    public DeauthenticateEntity deauthenticateUser(@Named("userId") String userId) {
        DeauthenticateEntity response = new DeauthenticateEntity();
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
                //deauthenticate the user here
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "getUserDetails",
            path = "getUserDetails",
            httpMethod = ApiMethod.HttpMethod.GET)
    public UserDetailsEntity getUserDetails(@Named("userId") @Nullable String
                                                    userId, @Named("email") @Nullable String email, @Named("mobNo") @Nullable String mobNo) {
        UserDetailsEntity response = new UserDetailsEntity();
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
            ObjectInputStream oin = null;
            queryBlock:
            try {
                String query = "SELECT userID, email, mobNo, dateOfBirth, firstName, lastName, loyaltyPoints FROM user WHERE userID = ? OR email = ? OR mobNo = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                String details;
                if (userId != null) {
                    details = userId;
                } else if (email != null) {
                    details = email;
                } else if (mobNo != null) {
                    details = mobNo;
                } else {
                    //no details sent
                    response.setStatus("False");
                    response.setMessage("No details provided");
                    logger.warning("No details provided");
                    break queryBlock;
                }
                statement.setString(1, details);
                statement.setString(2, details);
                statement.setString(3, details);
                logger.info("Searching for user details of: " + details);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    //User details found
                    logger.info("found user");
                    response.setDateOfBirth(rs.getDate("dateOfBirth"));
                    response.setEmail(rs.getString("email"));
                    response.setFirstName(rs.getString("firstName"));
                    response.setLastName(rs.getString("lastName"));
                    response.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
                    response.setUserId(rs.getString("userID"));
                    response.setMobNo(rs.getString("mobNo"));
                    response.setStatus("True");
                    GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
                    GcsFilename fileName = new GcsFilename("yapnak_main", (userId + "UserImage"));
                    GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, 1024 * 1024);
                    oin = new ObjectInputStream(Channels.newInputStream(readChannel));
                    response.setUserImage((String) oin.readObject());
                } else {
                    //No user found
                    response.setStatus("False");
                    response.setMessage("User not found");
                    logger.warning("User not found");
                    break queryBlock;
                }
            } finally {
                oin.close();
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "setUserDetails",
            path = "setUserDetails",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SetUserDetailsEntity setUserDetails(@Named("userId") String
                                                       userId, @Named("email") @Nullable String email, @Named("mobNo") @Nullable String
                                                       mobNo, @Named("password") @Nullable String password, @Named("dateOfBirth") @Nullable String
                                                       dateOfBirth, @Named("firstName") @Nullable String
                                                       firstName, @Named("lastName") @Nullable String lastName, @Nullable UserImageEntity
                                                       userImage) {
        SetUserDetailsEntity response = new SetUserDetailsEntity();
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
                String query = "SELECT COUNT(*) FROM user WHERE userID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                logger.info("Search for user: " + userId);
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    //userID not found
                    response.setStatus("False");
                    response.setMessage("userId not found");
                    logger.warning("userId not found");
                    break queryBlock;
                }
                //user exists, update details
                logger.info("user exists, updating details");
                //need users authenticated?
                if (email != null) {
                    //Update email
                    logger.info("updating email to: " + email);
                    query = "UPDATE user SET email = ? where userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, email);
                    statement.setString(2, userId);
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        response.setStatus("False");
                        response.setMessage("Failed to update email");
                        logger.warning("Failed to update email");
                        break queryBlock;
                    }
                }
                if (mobNo != null) {
                    //Update mobile number
                    logger.info("updating mobile number to: " + mobNo);
                    query = "UPDATE user SET mobNo = ? where userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, mobNo);
                    statement.setString(2, userId);
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        response.setStatus("False");
                        response.setMessage("Failed to update mobile number");
                        logger.warning("Failed to update mobile number");
                        break queryBlock;
                    }
                    logger.info("updated mobile number");
                }
                if (password != null) {
                    //Update password
                    logger.info("updating password to: " + hashPassword(password));
                    query = "UPDATE user SET password = ? where userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, hashPassword(password));
                    statement.setString(2, userId);
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        response.setStatus("False");
                        response.setMessage("Failed to update password");
                        logger.warning("Failed to update password");
                        break queryBlock;
                    }
                    logger.info("updated password");
                }
                if (dateOfBirth != null) {
                    try {
                        //Update date of birth
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                        Date parsed = format.parse(dateOfBirth);
                        java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
                        logger.info("updating date of birth to: " + sqlDate);
                        query = "UPDATE user SET dateOfBirth = ? where userID = ?";
                        statement = connection.prepareStatement(query);
                        statement.setDate(1, sqlDate);
                        statement.setString(2, userId);
                        int success = statement.executeUpdate();
                        if (success == -1) {
                            response.setStatus("False");
                            response.setMessage("Failed to update date of birth");
                            logger.warning("Failed to update date of birth");
                            break queryBlock;
                        }
                        logger.info("updated date of birth");
                    } catch (ParseException e) {
                        e.printStackTrace();
                        logger.warning("ParseException:" + e);
                        response.setStatus("False");
                        response.setMessage("Date of birth parsing exception");
                        break queryBlock;
                    }
                }
                if (firstName != null) {
                    //Update first name
                    logger.info("updating first name to: " + firstName);
                    query = "UPDATE user SET firstName = ? where userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, firstName);
                    statement.setString(2, userId);
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        response.setStatus("False");
                        response.setMessage("Failed to update first name");
                        logger.warning("Failed to update first name");
                        break queryBlock;
                    }
                    logger.info("updated first name");
                }
                if (lastName != null) {
                    //Update last name
                    logger.info("updating last name to: " + lastName);
                    query = "UPDATE user SET lastName = ? where userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, lastName);
                    statement.setString(2, userId);
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        response.setStatus("False");
                        response.setMessage("Failed to update last name");
                        logger.warning("Failed to update last name");
                        break queryBlock;
                    }
                    logger.info("updated last name");
                }
                if (userImage.getImageString() != null || userImage.getImageUrl() != null) {
                    try {
                        GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
                        GcsFilename fileName = new GcsFilename("yapnak_main", (userId + "UserImage"));
                        GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, GcsFileOptions.getDefaultInstance());
                        @SuppressWarnings("resource")
                        ObjectOutputStream oout =
                                new ObjectOutputStream(Channels.newOutputStream(outputChannel));
                        oout.writeObject(userImage.getImageString());
                        oout.close();
                        logger.info("updated user image");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                logger.info("Updates successful");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "getClients",
            path = "getClients",
            httpMethod = ApiMethod.HttpMethod.GET)
    public OfferListEntity getClients(@Named("longitude") double longitude,
                                      @Named("latitude") double latitude) {
        OfferListEntity response = new OfferListEntity();
        OfferEntity offer;
        List<OfferEntity> list = new ArrayList<>();
        Connection connection;
        double distance = 0.05;
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
                Date date = new Date();   // given date
                Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT")); // creates a new calendar instance
                calendar.setTime(date);   // assigns calendar to given date
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                hour = (hour + 1) % 24;
                if (hour >= 0 && hour <= 4) {
                    hour = 23 + (hour + 1);
                }
                logger.info("Hour is: " + hour);
                String statement = "SELECT clientName,clientX,clientY,clientFoodStyle,clientPhotoUrl,client.clientID,offers.offerText offer,offers.offerID, offerDays, offerPhotoUrl FROM client JOIN offers ON client.clientID=offers.clientID AND offers.isActive = 1 AND client.isActive = 1 AND offers.showOffer = 1 WHERE clientX BETWEEN ? AND ? AND clientY BETWEEN ? AND ? AND offerStart <= ? AND offerEnd >= ? LIMIT 21";
                PreparedStatement stmt = connection.prepareStatement(statement);
                double t = longitude - distance;
                stmt.setDouble(1, t);
                t = longitude + distance;
                stmt.setDouble(2, t);
                t = latitude - distance;
                stmt.setDouble(3, t);
                t = latitude + distance;
                stmt.setDouble(4, t);
                stmt.setInt(5, hour);
                stmt.setInt(6, hour);
                logger.info("search for clients at (lng/lat): " + longitude + " : " + latitude);
                ResultSet rs = stmt.executeQuery();
                //Needed to compensate for the fact that monday = 1
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                dayOfWeek = (dayOfWeek + 5) % 7;
                if (hour > 23) {
                    if (dayOfWeek == 0) {
                        dayOfWeek = 6;
                    } else {
                        dayOfWeek -= 1;
                    }
                }
                logger.info("day is " + dayOfWeek);
                JSONParser parse = new JSONParser();
                JSONArray days;
//                String[] images = {"https://yapnak-app.appspot.com/images/coffee_room_blt.jpg", "https://yapnak-app.appspot.com/images/2.jpg", "https://yapnak-app.appspot.com/images/3.jpg", "https://yapnak-app.appspot.com/images/4.jpg", "https://yapnak-app.appspot.com/images/5.jpg"};
                if (rs.next()) {
                    rs.beforeFirst();
                    int x = 0;
                    while (rs.next()) {
                        logger.info("Retrieving client: " + rs.getString("clientName"));
                        offer = new OfferEntity();
                        offer.setOfferId(rs.getInt("offerID"));
                        offer.setClientId(rs.getInt("clientID"));
                        offer.setClientName(rs.getString("clientName"));
                        offer.setOfferText(rs.getString("offer"));
                        offer.setLongitude(rs.getDouble("clientX"));
                        offer.setLatitude(rs.getDouble("clientY"));
                        offer.setFoodStyle(rs.getString("clientFoodStyle"));
                        offer.setClientPhoto(rs.getString("clientPhotoUrl"));
//                        x += 1;
//                        x %= 4;
//                        offer.setClientOfferPhoto(images[x]);
                        offer.setClientOfferPhoto(rs.getString("offerPhotoUrl"));
                        offer.setDistance(distance(longitude, latitude, rs.getDouble("clientX"), rs.getDouble("clientY")));
                        offer.setClientRating(4.0);
                        //Check if the offer is active on that day;
                        days = (JSONArray) parse.parse(rs.getString("offerDays"));
                        if ((boolean) days.get(dayOfWeek) == true) {
                            list.add(offer);
                        }
                    }
                    //sort list here by top three priority and then distance
                    logger.info(list.get(0).getClientName());
                    response.setOfferList(sort(list));
                    logger.info(list.get(0).getClientName());
                    response.setStatus("True");
                    response.setFoundOffers(true);
                } else {
                    //No clients found
                    response.setStatus("True");
                    logger.info("No clients found");
                    response.setFoundOffers(false);
                    break queryBlock;
                }
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "getOffers",
            path = "getOffers",
            httpMethod = ApiMethod.HttpMethod.GET)
    public OfferListEntity getOffers(@Named("longitude") double longitude,
                                     @Named("latitude") double latitude, @Named("userId") String userId) {
        OfferListEntity response = new OfferListEntity();
        OfferEntity offer;
        List<OfferEntity> list = new ArrayList<>();
        Connection connection;
        double distance = 0.05;
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
                String query = "SELECT COUNT(*) FROM user WHERE userID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                logger.info("getOffers, Search for user: " + userId);
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    //userID not found
                    response.setStatus("False");
                    response.setMessage("getOffers, userId " + userId + " not found");
                    logger.warning("getOffers, userId " + userId + " not found");
                    break queryBlock;
                }
                //user exists, update details
                logger.info("user exists, getting clients");
                Date date = new Date();   // given date
                Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT")); // creates a new calendar instance
                calendar.setTime(date);   // assigns calendar to given date
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
                hour = hour % 24;
                if (hour >= 0 && hour <= 4) {
                    hour = 23 + hour;
                }
                logger.info("Hour is: " + hour);
                query = "SELECT clientName,clientX,clientY,clientFoodStyle,clientPhotoUrl,client.clientID,offers.offerText offer,offers.offerID, offerDays, favourites.favouriteID, offerPhotoUrl FROM client JOIN offers ON client.clientID=offers.clientID AND offers.isActive = 1 AND client.isActive = 1 AND offers.showOffer = 1 LEFT JOIN favourites ON favourites.offerID = offers.offerID AND favourites.userID = ? WHERE clientX BETWEEN ? AND ? AND clientY BETWEEN ? AND ? AND offerStart <= ? AND offerEnd > ? LIMIT 21";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                double t = longitude - distance;
                statement.setDouble(2, t);
                t = longitude + distance;
                statement.setDouble(3, t);
                t = latitude - distance;
                statement.setDouble(4, t);
                t = latitude + distance;
                statement.setDouble(5, t);
                statement.setInt(6, hour);
                statement.setInt(7, hour);
                logger.info("search for clients at (lng/lat): " + longitude + " : " + latitude);
                rs = statement.executeQuery();
                //Needed to compensate for the fact that monday = 1
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                dayOfWeek = (dayOfWeek + 5) % 7;
                if (hour > 23) {
                    if (dayOfWeek == 0) {
                        dayOfWeek = 6;
                    } else {
                        dayOfWeek -= 1;
                    }
                }
                logger.info("day is " + dayOfWeek);
                JSONParser parse = new JSONParser();
                JSONArray days;
                String[] images = {"https://yapnak-app.appspot.com/images/coffee_room_blt.jpg", "https://yapnak-app.appspot.com/images/2.jpg", "https://yapnak-app.appspot.com/images/3.jpg", "https://yapnak-app.appspot.com/images/4.jpg", "https://yapnak-app.appspot.com/images/5.jpg"};
                if (rs.next()) {
                    rs.beforeFirst();
                    int x = 0;
                    while (rs.next()) {
                        logger.info("Retrieving client: " + rs.getString("clientName"));
                        offer = new OfferEntity();
                        offer.setOfferId(rs.getInt("offerID"));
                        offer.setClientId(rs.getInt("clientID"));
                        offer.setClientName(rs.getString("clientName"));
                        offer.setOfferText(rs.getString("offer"));
                        offer.setLongitude(rs.getDouble("clientX"));
                        offer.setLatitude(rs.getDouble("clientY"));
                        offer.setFoodStyle(rs.getString("clientFoodStyle"));
                        offer.setClientPhoto(rs.getString("clientPhotoUrl"));
//                        x += 1;
//                        x %= 4;
//                        offer.setClientOfferPhoto(images[x]);
                        offer.setClientOfferPhoto(rs.getString("offerPhotoUrl"));
                        offer.setDistance(distance(longitude, latitude, rs.getDouble("clientX"), rs.getDouble("clientY")));
                        offer.setClientRating(4.0);
                        offer.setFavourite(rs.getInt("favouriteID") != 0);
                        //Check if the offer is active on that day;
                        days = (JSONArray) parse.parse(rs.getString("offerDays"));
                        if ((boolean) days.get(dayOfWeek) == true) {
                            list.add(offer);
                        }
                    }
                    //sort list here by top three priority and then distance
                    logger.info(list.get(0).getClientName());
                    response.setOfferList(sort(list));
                    logger.info(list.get(0).getClientName());
                    response.setStatus("True");
                    response.setFoundOffers(true);
                } else {
                    //No clients found
                    response.setStatus("True");
                    logger.info("No clients found");
                    response.setFoundOffers(false);
                    break queryBlock;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    static List<OfferEntity> sort(List<OfferEntity> rawOffers) {
        List<OfferEntity> response = rawOffers;
        //set up checking variables
        boolean hasChanged = true;

        while (hasChanged) {
            hasChanged = false;
            //bubble sort
            for (int i = 0; i < response.size() - 1; i++) {
                if (Double.valueOf(response.get(i).getDistance()) > Double.valueOf(response.get(i + 1).getDistance())) {
                    hasChanged = true;
                    OfferEntity removedEntity = response.remove(i + 1);
                    response.add(i + 1, response.get(i));
                    response.remove(i);
                    response.add(i, removedEntity);
                }
            }
        }
        for (int i = 0; i < response.size(); i++) {
            try {
                if (Double.parseDouble(response.get(i).getDistance()) >= 30) {
                    response.get(i).setDistance("Really far away");
                } else {
                    response.get(i).setDistance(response.get(i).getDistance().split("\\.")[0] + " minutes");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    static String distance(double longitudeOrigin, double latitudeOrigin, double longitudeDest, double latitudeDest) {
        //a^2=b^2+c^2
        double b = (longitudeOrigin - longitudeDest);
        double c = (latitudeOrigin - latitudeDest);
        double a = Math.sqrt((b * b) + (c * c));
        //number of minutes
        double minutes = Math.floor(a / 0.00128489);
        String response;
        response = String.valueOf(minutes);
        return response;
    }

    @ApiMethod(
            name = "searchClients",
            path = "searchClients",
            httpMethod = ApiMethod.HttpMethod.GET)
    public OfferListEntity searchClients(@Named("searchString") String searchString) {
        OfferListEntity response = new OfferListEntity();
        OfferEntity offer;
        List<OfferEntity> list = new ArrayList<OfferEntity>();
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
                String query = "SELECT clientName,clientX,clientY,clientFoodStyle,clientPhotoUrl,client.clientID,offers.offerText offer,offers.offerID FROM client JOIN offers ON client.clientID=offers.clientID AND offers.isActive = 1 AND offers.showOffer = 1 WHERE clientName LIKE ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, "%" + searchString + "%");
                logger.info("Searching for clients similar to: " + searchString);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    rs.beforeFirst();
                    while (rs.next()) {
                        logger.info("Retrieving client: " + rs.getString("clientName"));
                        offer = new OfferEntity();
                        offer.setOfferId(rs.getInt("offerID"));
                        offer.setClientId(rs.getInt("clientID"));
                        offer.setClientName(rs.getString("clientName"));
                        offer.setOfferText(rs.getString("offer"));
                        offer.setLongitude(rs.getDouble("clientX"));
                        offer.setLatitude(rs.getDouble("clientY"));
                        offer.setFoodStyle(rs.getString("clientFoodStyle"));
                        offer.setClientPhoto(rs.getString("clientPhotoUrl"));
                        list.add(offer);
                    }
                    //sort list here by top three priority and then distance
                    response.setOfferList(list);
                    response.setStatus("True");
                    response.setFoundOffers(true);
                } else {
                    //No clients found
                    response.setStatus("True");
                    logger.info("No clients found");
                    response.setFoundOffers(false);
                    break queryBlock;
                }
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "favouriteOffer",
            path = "favouriteOffer",
            httpMethod = ApiMethod.HttpMethod.POST)
    public FavouriteOfferEntity favouriteOffer(@Named("userId") String userId, @Named("offerId") int offerId) {
        FavouriteOfferEntity response = new FavouriteOfferEntity();
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
                String query = "REPLACE favourites (userID,offerID) VALUES (?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, offerId);
                logger.info("Adding offer: " + offerId + " to favourites of: " + userId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    response.setStatus("False");
                    response.setMessage("Favourites insertion failed");
                    logger.warning("Favourites insertion failed");
                    break queryBlock;
                }
                logger.info("Favourites insertion success");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }

    }

    @ApiMethod(
            name = "removeFavouriteOffer",
            path = "removeFavouriteOffer",
            httpMethod = ApiMethod.HttpMethod.POST)
    public FavouriteOfferEntity removeFavouriteOffer(@Named("userId") String userId, @Named("offerId") int offerId) {
        FavouriteOfferEntity response = new FavouriteOfferEntity();
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
                String query = "DELETE FROM favourites WHERE userID = ? AND offerID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, offerId);
                logger.info("removing offer: " + offerId + " from the favourites of: " + userId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    response.setStatus("False");
                    response.setMessage("Favourites removal failed");
                    logger.warning("Favourites removal failed");
                    break queryBlock;
                }
                logger.info("Favourites removal success");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }

    }

    @ApiMethod(
            name = "feedback",
            path = "feedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public FeedbackEntity feedback(@Named("userId") String userId, @Named("clientId") int clientId, @Named("offerId") int offerId, @Named("rating") int rating, @Named("comment") @Nullable String comment, @Named("isAccepted") boolean isAccepted) {
        FeedbackEntity response = new FeedbackEntity();
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
                logger.info("feedback from: " + userId);
                if (isAccepted == false) {
                    //Offer not accepted, notify us
                    logger.info("Offer was not accepted!!!");
                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props, null);
                    javax.mail.Message msg = new MimeMessage(session);
                    String query = "SELECT email, clientName FROM client where clientID = ? ";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, clientId);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        //clientId found, send an email to us about the problem
                        logger.info("Retrieved client: " + rs.getString("clientName"));
                        msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                        msg.addRecipient(javax.mail.Message.RecipientType.TO,
                                new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
                        msg.setSubject("Negative feedback - client didn't accept code");
                        msg.setText("From: " + userId + " regarding client: " + rs.getString("clientName") + ", " + rs.getString("email") + " on offerId: " + offerId + " - " + comment);
                        Transport.send(msg);
                    } else {
                        //clientId not found
                        logger.info("client not found");
                        response.setStatus("False");
                        response.setMessage("ClientId not found");
                        break queryBlock;
                    }
                }
                if (rating > 5) {
                    response.setStatus("False");
                    response.setMessage("Rating value out of range");
                    break queryBlock;
                }
                String query = "REPLACE feedback (userID, clientID, offerID, feedback, rating) VALUES (?,?,?,?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, clientId);
                statement.setInt(3, offerId);
                statement.setString(4, (comment != null) ? comment : "");
                statement.setInt(5, rating);
                logger.info("inserting feedback " + statement.toString());
                int success = statement.executeUpdate();
                if (success == -1) {
                    //insert failed
                    response.setStatus("False");
                    response.setMessage("Feedback insert failed");
                    logger.warning("Feedback insert FAILED");
                    break queryBlock;
                }
                logger.info("Feedback inserted success");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "recommend",
            path = "recommend",
            httpMethod = ApiMethod.HttpMethod.POST)
    public RecommendEntity recommend(@Named("userId") String userId, @Named("otherUserId") @Nullable String otherUserId, @Named("otherMobNo") @Nullable String otherMobNo, @Named("clientId") int clientId) {
        RecommendEntity response = new RecommendEntity();
        response.setStatus("True");
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
                String details;
                String query;
                boolean isMob = false;
                if (otherUserId != null) {
                    details = otherUserId;
                    query = "SELECT COUNT(*) FROM user WHERE userID = ?";
                } else if (otherMobNo != null) {
                    details = otherMobNo;
                    isMob = true;
                    query = "SELECT COUNT(*) FROM user WHERE mobNo = ?";
                } else {
                    //Details not provided
                    logger.info("other user details have not been provided");
                    response.setStatus("False");
                    response.setMessage("other user details have not been provided");
                    break queryBlock;
                }
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, details);
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) < 1) {
                    //User is not registered
                    if (isMob) {
                        //send a recommendation sms
                        response.setStatus("True");
                        response.setResult(0);
                        logger.info("User not found, sending recommendation SMS");
                        break queryBlock;
                    }
                    //User is not in the system.
                    response.setStatus("False");
                    response.setResult(-1);
                    response.setMessage("User not found in the system");
                    logger.info("User not found in the system");
                    break queryBlock;
                }
                if (isMob) {
                    //Get userId from mobile number
                    query = "SELECT userID FROM user WHERE mobNo = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, details);
                    rs = statement.executeQuery();
                    rs.next();
                    details = rs.getString("userID");
                }
                //User found
                logger.info("User " + userId + " is recommending " + details + " to eat at " + clientId);
                query = "SELECT COUNT(*) FROM recommend WHERE userID = ? AND clientID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, details);
                statement.setInt(2, clientId);
                rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    //User has already been recommended to that client
                    response.setStatus("True");
                    response.setResult(2);
                    logger.info("Other user has already been recommended to that client");
                    break queryBlock;
                }
                //Insert recommendation details
                query = "REPLACE recommend (userID, clientID, referrerID) VALUES (?,?,?)";
                statement = connection.prepareStatement(query);
                statement.setString(1, details);
                statement.setInt(2, clientId);
                statement.setString(3, userId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Insert update failed
                    response.setStatus("False");
                    response.setMessage("Inserting recommendation failed");
                    response.setResult(-1);
                    logger.warning("Inserting recommendation failed");
                    break queryBlock;
                }
                response.setStatus("True");
                response.setResult(1);
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "searchUsers",
            path = "searchUsers",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SearchUserEntity searchUsers(@Named("details") String[] details) {
        SearchUserEntity response = new SearchUserEntity();
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
                String statement = "SELECT COUNT(*), userID FROM user WHERE email = ? OR mobNo = ?";
                PreparedStatement stmt = connection.prepareStatement(statement);
                ResultSet rs;
                List<String> isUser = new ArrayList<String>();
                for (int i = 0; i < details.length; i++) {
                    stmt.setString(1, details[i]);
                    stmt.setString(2, details[i]);
                    rs = stmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        isUser.add(rs.getString("userID"));
                    } else {
                        isUser.add("");
                    }
                }
                response.setStatus("True");
                response.setIsUser(isUser);
                logger.info("Successfully searched for users");
            } finally {
                connection.close();
                return response;
            }
        } catch (ClassNotFoundException e) {
            response.setStatus("False");
            response.setMessage("ClassNotFoundException");
            e.printStackTrace();
        } catch (SQLException e) {
            response.setStatus("False");
            response.setMessage("SQLException");
            e.printStackTrace();
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "getRedemptionForUser",
            path = "getRedemptionForUser",
            httpMethod = ApiMethod.HttpMethod.GET)
    public UserRedemptionEntity getRedemptionForUser(@Named("userId") String userId) {
        UserRedemptionEntity response = new UserRedemptionEntity();
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
                String query = "SELECT redemptionAvailable FROM user WHERE userID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    //User doesn't exist
                    logger.warning("User " + userId + " not found");
                    response.setStatus("False");
                    response.setMessage("User not found");
                    break queryBlock;
                }
                logger.info("Returning available redemption for " + userId);
                JSONParser parse = new JSONParser();
                JSONArray list = new JSONArray();
                list = (JSONArray) parse.parse(rs.getString("redemptionAvailable"));
                response.setAvailable(list);
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "forgotLogin",
            path = "forgotLogin",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity forgotLogin(@Named("email") String email) throws ClassNotFoundException, SQLException {
        SimpleEntity response = new SimpleEntity();
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
                String query = "SELECT COUNT(email) AS count from user where email = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                logger.info("user " + email + " is requesting a password reset");
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    //User is not in the system
                    logger.warning("User " + email + " does not exist");
                    response.setStatus("False");
                    response.setMessage("User " + email + " does not exist");
                    break queryBlock;
                }
                logger.info("Found user");
                //Gather hashes and send email
                String reset = hashPassword(String.valueOf(randInt()));
                String cancel = hashPassword(String.valueOf(randInt()));
                query = "REPLACE forgot (email,reset,cancel,time) VALUES (?,?,?,CURDATE())";
                statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, reset);
                statement.setString(3, cancel);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Replace failed
                    logger.warning("Update forgot table failed");
                    response.setStatus("False");
                    response.setMessage("Update forgot table failed");
                    break queryBlock;
                }
                logger.info("Updated forgot table, sending email");
                String subject = "Yapnak password reset";
                String message = "Hi,\n\nWe have received a request to reset the password on your Yapnak account.\n\nTo reset, click: www.yapnak.com/userPasswordReset?response=" + reset + "\n\nIf you didn't request this email, click here: www.yapnak.com/cancelReset?response=" + cancel + "\n\nKind regards,\nthe Yapnak team.";
                sendEmail(email, subject, message);
                logger.info("Email sent");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    static void sendEmail(String email, String subject, String message) throws AddressException, MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        javax.mail.Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("yapnak.uq@gmail.com", "Yapnak"));
        msg.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(email));
        msg.setSubject(subject);
        msg.setText(message);
        Transport.send(msg);
    }

    @ApiMethod(
            name = "resetPassword",
            path = "resetPassword",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity resetPassword(@Named("password") String password, @Named("hash") String hash) throws ClassNotFoundException, SQLException {
        SimpleEntity response = new SimpleEntity();
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
                String query = "SELECT email FROM forgot WHERE reset = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, hash);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    // User has not requested reset
                    response.setStatus("False");
                    response.setMessage("User has not requested reset");
                    logger.warning("User has not requested reset");
                    break queryBlock;
                }
                logger.info("Found email " + rs.getString("email") + " from forgot");
                query = "UPDATE user SET password = ? WHERE email = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, hashPassword(password));
                statement.setString(2, rs.getString("email"));
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Update failed
                    logger.warning("Updating user password failed");
                    response.setStatus("False");
                    response.setMessage("Updating user password failed");
                    break queryBlock;
                }
                logger.info("Updated password");
                query = "DELETE FROM forgot WHERE reset = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, hash);
                statement.executeUpdate();
                if (success == -1) {
                    //Delete failed
                    logger.warning("Deleting forgot row failed for " + hash);
                    response.setMessage("Deleting forgot row failed for " + hash);
                    break queryBlock;
                }
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "inAppPasswordReset",
            path = "inAppPasswordReset",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity inAppPasswordReset(@Named("password") String password, @Named("current") String current, @Named("userId") String userId) throws ClassNotFoundException, SQLException {
        SimpleEntity response = new SimpleEntity();
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
                String query = "SELECT password FROM user WHERE userID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    // User does not exist
                    response.setStatus("False");
                    response.setMessage("User not found: " + userId);
                    logger.warning("User not found: " + userId);
                    break queryBlock;
                }
                logger.info("Found user " + userId + ", checking current password");
                if (!rs.getString("password").equals(hashPassword(current))) {
                    //Passwords don't match
                    response.setStatus("False");
                    response.setMessage("Current password does not match");
                    logger.warning("Current password does not match");
                    break queryBlock;
                }
                logger.info("Passwords match");
                query = "UPDATE user SET password = ? WHERE userID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, hashPassword(password));
                statement.setString(2, userId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Update failed
                    logger.warning("Updating user password failed");
                    response.setStatus("False");
                    response.setMessage("Updating user password failed");
                    break queryBlock;
                }
                logger.info("Updated password");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "userLoginAnalytics",
            path = "userLoginAnalytics",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity userLoginAnalytics(@Named("userId") String userId, @Named("longitude") double longitude, @Named("latitude") double latitude) {
        SimpleEntity response = new SimpleEntity();
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
                String query = "INSERT INTO userlogin (userId, longitude, latitude) VALUES (?,?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setDouble(2, longitude);
                statement.setDouble(3, latitude);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Login analytics insert failed, user doesn't exist.
                    logger.warning("Login analytics insert failed for user " + userId);
                    response.setStatus("False");
                    response.setMessage("Login analytics insert failed for user " + userId);
                    break queryBlock;
                }
                //Login analytics insert success
                logger.info("Login analytics insert success for " + userId + " at " + longitude + " : " + latitude);
                response.setStatus("True");
            } catch (SQLException e) {
                logger.warning("Login analytics insert failed, user " + userId + " doesn't exist.");
                response.setStatus("False");
                response.setMessage("Login analytics insert failed, user " + userId + " doesn't exist.");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "offerRefreshAnalytics",
            path = "offerRefreshAnalytics",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity offerRefreshAnalytics(@Named("userId") String userId, @Named("longitude") double longitude, @Named("latitude") double latitude) {
        SimpleEntity response = new SimpleEntity();
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
                String query = "INSERT INTO offerRefresh (userId, longitude, latitude) VALUES (?,?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setDouble(2, longitude);
                statement.setDouble(3, latitude);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Login analytics insert failed, user doesn't exist.
                    logger.warning("Refresh analytics insert failed for user " + userId);
                    response.setStatus("False");
                    response.setMessage("Refresh analytics insert failed for user " + userId);
                    break queryBlock;
                }
                //Login analytics insert success
                logger.info("Refresh analytics insert success for " + userId + " at " + longitude + " : " + latitude);
                response.setStatus("True");
            } catch (SQLException e) {
                logger.warning("Refresh analytics insert failed, user " + userId + " doesn't exist.");
                response.setStatus("False");
                response.setMessage("Refresh analytics insert failed, user " + userId + " doesn't exist.");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "cardClickedAnalytics",
            path = "cardClickedAnalytics",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity cardClickedAnalytics(@Named("userId") String userId, @Named("position") int position, @Named("offerId") int offerId) {
        SimpleEntity response = new SimpleEntity();
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
                String query = "INSERT INTO cardClicked (userId, offerId, position) VALUES (?,?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, offerId);
                statement.setInt(3, position);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Login analytics insert failed, user doesn't exist.
                    logger.warning("card clicked analytics insert failed for user " + userId);
                    response.setStatus("False");
                    response.setMessage("card clicked analytics insert failed for user " + userId);
                    break queryBlock;
                }
                //Login analytics insert success
                logger.info("card clicked analytics insert success for " + userId + " for " + offerId + " at position " + position);
                response.setStatus("True");
            } catch (SQLException e) {
                logger.warning("card clicked analytics insert failed, user " + userId + " or offer " + offerId + " doesn't exist.");
                response.setStatus("False");
                response.setMessage("card clicked analytics insert failed, user " + userId + " or offer " + offerId + " doesn't exist.");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "sendFeedback",
            path = "sendFeedback",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity sendFeedback(@Named("name") String name, @Named("email") String email, @Named("message") String message) {
        SimpleEntity response = new SimpleEntity();
        String toEmail = "yapnak.uq@gmail.com";
        String subject = "Website Feedback from " + name + ", " + email;
        message += "\n\n" + email + "\n\nSent from the website";
        try {
            sendEmail(toEmail, subject, message);
            subject = "Yapnak feedback received";
            message = "Thank you for sharing your thoughts with us.\n\nWe have your email and will be giving it a look shortly.\n\nKind regards,\nThe Yapnak Team.";
            sendEmail(email, subject, message);
        } catch (AddressException e) {
            e.printStackTrace();
            response.setStatus("False");
            response.setMessage("AddressException: " + e);
            logger.warning("AddressException in sending feedback: " + e);
            return response;
        } catch (MessagingException e) {
            e.printStackTrace();
            response.setStatus("False");
            response.setMessage("MessagingException: " + e);
            logger.warning("MessagingException in sending feedback: " + e);
            return response;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            response.setStatus("False");
            response.setMessage("UnsupportedEncodingException: " + e);
            logger.warning("UnsupportedEncodingException in sending feedback: " + e);
            return response;
        }
        response.setStatus("True");
        return response;
    }

    @ApiMethod(
            name = "aboutUs",
            path = "aboutUs",
            httpMethod = ApiMethod.HttpMethod.GET)
    public AboutUsEntity aboutUs() {
        AboutUsEntity response = new AboutUsEntity();
        response.setStatus("True");
        String s = "At Yapnak we love good food but don't always have the time to find it at lunch. As food pioneers, we want to be shown where to get the best lunch, within walking distance, quickly and easily.  As a Yapnak user, you get this experience!\n\nEvery restaurant meal comes at the set price of a fiver - one less thing to worry about at lunchtime!\n\nFind what you fancy with 0 clicks, hit 'Get' to present your QR code in store and pick up your lunch.\n\nFavourite the best meals and we'll remind you as and when they return to Yapnak.\n\nRemember to recommend meals you particularly like to other pioneers!\n\nWelcome to the Yapnak club.";

        response.setAboutUs(s);
        return response;
    }

    @ApiMethod(
            name = "getFavourites",
            path = "getFavourites",
            httpMethod = ApiMethod.HttpMethod.GET)
    public OfferListEntity getFavourites(@Named("userId") String userId, @Named("longitude") double longitude, @Named("latitude") double latitude) {
        OfferListEntity response = new OfferListEntity();
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
                String query = "SELECT f.offerID, o.offerText, o.isActive, o.showOffer, c.clientName, c.clientX, c.clientY, c.clientID, o.offerPhotoUrl FROM favourites f, offers o, client c WHERE f.offerID = o.offerID AND o.clientID = c.clientID AND f.userID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    //What does it mean?
                    response.setStatus("False");
                    response.setMessage("Failed to retrieve favourites for " + userId);
                    logger.warning("Failed to retrieve favourites for " + userId);
                    break queryBlock;
                }
                //What does it mean?
                logger.info("Retrieved favourites for " + userId);
                response.setStatus("True");
                response.setFoundOffers(true);
                List<OfferEntity> list = new ArrayList<>();
                OfferEntity f;
                int x = 0;
                String[] images = {"https://yapnak-app.appspot.com/images/coffee_room_blt.jpg", "https://yapnak-app.appspot.com/images/2.jpg", "https://yapnak-app.appspot.com/images/3.jpg", "https://yapnak-app.appspot.com/images/4.jpg", "https://yapnak-app.appspot.com/images/5.jpg"};
                do {
                    f = new OfferEntity();
                    f.setClientRating(4.0);
                    f.setClientId(rs.getInt("clientID"));
                    f.setClientName(rs.getString("clientName"));
                    x += 1;
                    x %= 4;
                    f.setClientOfferPhoto(rs.getString("offerPhotoUrl"));
                    f.setDistance(distance(longitude, latitude, rs.getDouble("clientX"), rs.getDouble("clientY")));
                    f.setLatitude(rs.getDouble("clientY"));
                    f.setLongitude(rs.getDouble("clientX"));
                    f.setOfferId(rs.getInt("offerID"));
                    f.setOfferText(rs.getString("offerText"));
                    f.setFavourite(true);
                    list.add(f);
                } while (rs.next());
                list = sort(list);
                response.setOfferList(list);
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "photoUpload",
            path = "photoUpload",
            httpMethod = ApiMethod.HttpMethod.GET)
    public PhotoEntity photoUpload(@Named("userId") String userId) {
        PhotoEntity response = new PhotoEntity();
        logger.info("Creating photo upload for " + userId);
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        response.setUploadUrl(blobstoreService.createUploadUrl("/userPhotoUpload?userId=" + userId));
        response.setStatus("True");
        logger.info("Got user upload url: " + response.getUploadUrl());
        return response;
    }

    /*Braintree*/

//    private static BraintreeGateway gateway = new BraintreeGateway(
//            Environment.SANDBOX,
//            "cjg29hjqkkqj9gjm",
//            "f6yjq3c73bthgmtk",
//            "f2a40b8b4eb955a8ff1aa41f81499161", logger
//    );
//
//    @ApiMethod(
//            name = "client_token",
//            path = "client_token",
//            httpMethod = ApiMethod.HttpMethod.GET)
//    public ClientTokenEntity clientToken() {
//        ClientTokenEntity response = new ClientTokenEntity();
//
//        String token = gateway.clientToken().generate();
//        if (token.equals("")) {
//            response.setStatus("False");
//            response.setMessage("Token generation failed");
//            return response;
//        }
//        response.setClientToken(gateway.clientToken().generate());
//        response.setStatus("True");
//        return response;
//    }
//
//    @ApiMethod(
//            name = "checkout",
//            path = "checkout",
//            httpMethod = ApiMethod.HttpMethod.POST)
//    public CheckoutEntity checkout(@Named("payment_method_nonce") String nonce) {
//        CheckoutEntity response = new CheckoutEntity();
//
//        nonce = SandboxValues.PaymentMethodNonce.APPLE_PAY_VISA.nonce;
//
//        TransactionRequest request = new TransactionRequest()
//                .amount(new BigDecimal("100.00"))
//                .paymentMethodNonce(nonce)
//                .options()
//                .submitForSettlement(true)
//                .done();
//
//        Result<Transaction> result = gateway.transaction().sale(request);
//        logger.info((result.toString()));
//        response.setStatus("True");
//        return response;
//    }
}
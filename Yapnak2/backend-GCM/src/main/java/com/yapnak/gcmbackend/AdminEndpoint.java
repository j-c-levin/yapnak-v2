package com.yapnak.gcmbackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.utils.SystemProperty;

import java.math.BigInteger;
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
import java.util.logging.Logger;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "adminApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "gcmbackend.yapnak.com",
                ownerName = "gcmbackend.yapnak.com",
                packagePath = ""
        )
)
public class AdminEndpoint {

    private static final Logger logger = Logger.getLogger(AdminEndpoint.class.getName());

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

    //Generates secure numbers
    static String secureInt() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    @ApiMethod(
            name = "adminLogin",
            path = "adminLogin",
            httpMethod = ApiMethod.HttpMethod.POST)
    public AdminAuthEntity adminLogin(@Named("email") String email, @Named("password") String password) throws InternalServerErrorException, UnauthorizedException {
        AdminAuthEntity response = new AdminAuthEntity();
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
                String query = "SELECT COUNT(*) FROM admin WHERE email = ? AND password = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, hashPassword(password));
                logger.info(hashPassword(password));
                logger.info("Searching for admin: " + email);
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    //Details invalid
                    logger.warning("Admin details invalid");
                    response.setStatus("False");
                    response.setMessage("Admin details invalid");
                    break queryBlock;
                }
                //Details authenticated, generate token
                response.setSession(hashPassword(secureInt()));
                query = "UPDATE admin SET session = ? where email = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, response.getSession());
                statement.setString(2, email);
                int success = statement.executeUpdate();
                if (success == -1) {
                    logger.info("Session insert failed");
                    response.setSession("");
                    response.setSession("False");
                    response.setMessage("Session insert failed");
                    break queryBlock;
                }
                logger.info("Admin authorized");
                response.setStatus("True");
            } finally {
                connection.close();
                return response;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus("False");
            response.setMessage("ClassNotFoundException " + e);
        } catch (SQLException e1) {
            e1.printStackTrace();
            response.setStatus("False");
            response.setMessage("SQLException " + e1);
        } finally {
            return response;
        }

    }

    @ApiMethod(
            name = "hashGenerator",
            path = "hashGenerator",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void hashGenerator(@Named("password") String password) {
        logger.info(hashPassword(password));
    }

    @ApiMethod(
            name = "getAllClients",
            path = "getAllClients",
            httpMethod = ApiMethod.HttpMethod.GET)
    public ClientListEntity getAllClients() {
        ClientListEntity response = new ClientListEntity();
        List<OfferEntity> list = new ArrayList<>();
        OfferEntity offer;
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
                String query = "SELECT clientID,clientName from client";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    offer = new OfferEntity();
                    offer.setClientId(rs.getInt("clientID"));
                    offer.setClientName(rs.getString("clientName"));
                    list.add(offer);
                }
                response.setClientList(list);
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
            name = "getClientInfo",
            path = "getClientInfo",
            httpMethod = ApiMethod.HttpMethod.GET)
    public ClientEntity getClientInfo(@Named("clientId") String clientId) {
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
                String statement = "SELECT client.clientID, client.email, clientName, clientX, clientY, clientFoodStyle, clientPhotoUrl, client.offer1,client.offer2,client.offer3, client.isActive, offers.offerID, offers.offerText offer, offers.showOffer showOffer FROM client JOIN offers ON client.clientID=offers.clientID WHERE client.clientID = ? AND offers.isActive = 1";
                PreparedStatement stmt = connection.prepareStatement(statement);
                stmt.setString(1, clientId);
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
                    client.setPhoto(rs.getString("clientPhotoUrl"));
                    client.setIsActive(rs.getInt("isActive"));
                    client.setEmail(rs.getString("email"));
                    do {
                        if (rs.getInt("offerID") == rs.getInt("offer1")) {
                            logger.info("found offer 1: " + rs.getString("offer"));
                            client.setShowOffer1(rs.getInt("showOffer"));
                            client.setOffer1(rs.getString("offer"));
                            client.setOffer1Id(rs.getInt("offerID"));

                        } else if (rs.getInt("offerID") == rs.getInt("offer2")) {
                            logger.info("found offer 2: " + rs.getString("offer"));
                            client.setShowOffer2(rs.getInt("showOffer"));
                            client.setOffer2(rs.getString("offer"));
                            client.setOffer2Id(rs.getInt("offerID"));

                        } else {
                            logger.info("found offer 3: " + rs.getString("offer"));
                            client.setShowOffer3(rs.getInt("showOffer"));
                            client.setOffer3(rs.getString("offer"));
                            client.setOffer3Id(rs.getInt("offerID"));
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
            name = "getAllOffers",
            path = "getAllOffers",
            httpMethod = ApiMethod.HttpMethod.GET)
    public ClientOfferListEntity getAllOffers(@Named("clientId") int clientId) {
        ClientOfferListEntity response = new ClientOfferListEntity();
        ClientOfferEntity offer;
        List<ClientOfferEntity> list = new ArrayList<ClientOfferEntity>();
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
                String query = "SELECT offerID, offerText FROM offers WHERE clientID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, clientId);
                logger.info("Searching for offers from " + clientId);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    logger.info("Found offer " + rs.getInt("offerID"));
                    offer = new ClientOfferEntity();
                    offer.setOfferId(rs.getInt("offerID"));
                    offer.setOfferText(rs.getString("offerText"));
                    list.add(offer);
                }
                response.setOfferList(list);
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
            name = "insertPromo",
            path = "insertPromo",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void insertPromo(@Named("promoCode") String promoCode) {
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
                String query = "INSERT INTO promo (promoCode) VALUE (?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, promoCode);
                int success = statement.executeUpdate();
                if (success != -1) {
                    logger.info("Successfully added promo code " + promoCode);
                } else {
                    logger.warning("Promo code adding failed!");
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.warning("SQLException: " + e);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            logger.warning("ClassNotFoundException: " + e1);
        } finally {
        }

    }

    @ApiMethod(
            name = "toggleClient",
            path = "toggleClient",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity toggleClient(@Named("clientId") int clientId, @Named("session") String session, @Named("value") int value) {
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
                //Authorise admin
                String query = "SELECT adminID FROM admin WHERE session = ? ";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, session);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    //Admin auth failed
                    response.setStatus("False");
                    response.setMessage("Session auth failed");
                    logger.warning("Session auth failed");
                    break queryBlock;
                }
                int adminId = rs.getInt("adminID");
                logger.info("Found admin " + adminId);

                //Toggle client
                logger.info("Toggling client " + clientId + " to " + value);
                query = "UPDATE client SET isActive = ? WHERE clientID = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, value);
                statement.setInt(2, clientId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Update client toggle failed");
                    response.setStatus("False");
                    response.setMessage("Update client toggle failed");
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
            name = "generateMasterkey",
            path = "generateMasterkey",
            httpMethod = ApiMethod.HttpMethod.POST)
    public MasterkeyEntity generateMasterkey(@Named("clientId") int clientId) {
        MasterkeyEntity response = new MasterkeyEntity();
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
                String query = "SELECT COUNT(*) FROM client WHERE clientId = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, clientId);
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    logger.warning("Client " + clientId + " not found");
                    response.setStatus("False");
                    response.setMessage("Client " + clientId + " not found");
                    break queryBlock;
                }
                query = "UPDATE client SET masterkey = ? WHERE clientId = ?";
                statement = connection.prepareStatement(query);
                String masterkey = hashPassword(secureInt()).substring(0, 5);
                logger.info(masterkey);
                statement.setString(1, masterkey);
                statement.setInt(2, clientId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Master key update failed
                    logger.warning("Master key update failed for " + clientId);
                    response.setStatus("False");
                    response.setMessage("Master key update failed for " + clientId);
                    break queryBlock;
                }
                //Masterkey update success
                logger.info("Masterkey update success: " + masterkey);
                response.setStatus("True");
                response.setMasterkey(masterkey);
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "vitalReplace",
            path = "vitalReplace",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void vitalReplace() {
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
                String query = "SELECT offerID, offerPhoto, offerPhotoUrl FROM offers";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    if (!rs.getString("offerPhotoURl").equals("https://yapnak-app.appspot.com/images/yapnakDefaultOffer.png")) {
                        ImagesService services = ImagesServiceFactory.getImagesService();
                        ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(rs.getString("offerPhoto"))).secureUrl(true);
                        query = "UPDATE offers SET offerPhotoUrl = ? WHERE offerID = ?";
                        statement = connection.prepareStatement(query);
                        String url = services.getServingUrl(serve);
                        url += "=s600";
                        statement.setString(1, url);
                        statement.setInt(2, rs.getInt("offerID"));
                        int success = statement.executeUpdate();
                        if (success == -1 ){
                            //What does it mean?
                            logger.warning("error details");
                        }
                    }
                }
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
package com.yapnak.gcmbackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "clientEndpointApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "gcmbackend.yapnak.com",
                ownerName = "gcmbackend.yapnak.com",
                packagePath = ""
        )
)
public class ClientEndpoint {

    private static final Logger logger = Logger.getLogger(ClientEndpoint.class.getName());

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
                String query = "SELECT offerID, offerText, offerStart, offerEnd, offerDays, offerPhotoUrl FROM offers WHERE clientID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, clientId);
                logger.info("Searching for offers from " + clientId);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    logger.info("Found offer " + rs.getInt("offerID"));
                    offer = new ClientOfferEntity();
                    offer.setOfferId(rs.getInt("offerID"));
                    offer.setOfferText(rs.getString("offerText"));
                    offer.setOfferStart(rs.getInt("offerStart"));
                    offer.setOfferEnd(rs.getInt("offerEnd"));
                    JSONParser parse = new JSONParser();
                    JSONArray array = (JSONArray) parse.parse(rs.getString("offerDays"));
                    offer.setOfferDays(array);
                    offer.setOfferPhotoUrl(rs.getString("offerPhotoUrl"));
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
            name = "replaceActiveOffer",
            path = "replaceActiveOffer",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity replaceActiveOffer(@Named("email") String email, @Named("currentOfferId") int currentOfferId, @Named("newOfferId") int newOfferId, @Named("offerPosition") int offerPosition) {
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
                String query = "UPDATE client SET offer" + offerPosition + " = ? WHERE email = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, newOfferId);
                statement.setString(2, email);
                logger.info("Replacing offer " + currentOfferId + " with " + newOfferId + " at position " + offerPosition);
                int success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Replacing active offer failed");
                    response.setStatus("False");
                    response.setMessage("Replacing active offer failed");
                    break queryBlock;
                }
                logger.info("Replacing offer successful");
                query = "UPDATE offers SET isActive = ?, showOffer = ? WHERE offerID = ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, 0);
                statement.setInt(2, 0);
                statement.setInt(3, currentOfferId);
                success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Updating offers table failed on setting inactive");
                    response.setStatus("False");
                    response.setMessage("Replacing active offer failed on turning current offer inactive");
                    break queryBlock;
                }
                statement.setInt(1, 1);
                statement.setInt(2, 1);
                statement.setInt(3, newOfferId);
                success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Updating offers table failed on setting active");
                    response.setStatus("False");
                    response.setMessage("Replacing active offer failed on turning new offer active");
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
            name = "changeOfferHours",
            path = "changeOfferHours",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity changeOfferHours(@Named("email") String email, @Named("offerId") int offerId, @Named("offerStart") int offerStart, @Named("offerEnd") int offerEnd) {
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
                String query = "UPDATE offers SET offerStart = ?, offerEnd = ? WHERE offerID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, offerStart);
                statement.setInt(2, offerEnd);
                statement.setInt(3, offerId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Offer hours update failed
                    logger.warning("Offer hours update failed");
                    response.setStatus("False");
                    response.setMessage("Offer hours update failed");
                    break queryBlock;
                }
                response.setStatus("True");
                logger.info("Changed offer " + offerId + " to new hours: " + offerStart + " to " + offerEnd);
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "changeOfferDays",
            path = "changeOfferDays",
            httpMethod = ApiMethod.HttpMethod.POST)
    public SimpleEntity changeOfferDays(@Named("email") String email, @Named("offerId") int offerId, @Named("days") String days) {
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
                String query = "UPDATE offers SET offerDays = ? WHERE offerId = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, days);
                statement.setInt(2, offerId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //Offer days update failed
                    logger.warning("Offer days update failed for offerId " + offerId + " and email " + email);
                    response.setStatus("False");
                    response.setMessage("Offer days update failed for offerId " + offerId + " and email " + email);
                    break queryBlock;
                }
                //Offer update success
                logger.info("Offer update success for " + offerId);
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
                String statement = "SELECT client.clientID, clientName, clientX, clientY, clientFoodStyle, clientPhotoUrl, client.offer1,client.offer2,client.offer3, client.isActive, offers.offerID, offers.offerText offer, offers.showOffer showOffer FROM client JOIN offers ON client.clientID=offers.clientID WHERE client.email = ? AND offers.isActive = 1";
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
                    client.setPhoto(rs.getString("clientPhotoUrl"));
                    client.setIsActive(rs.getInt("isActive"));
                    do {
                        if (rs.getInt("offerID") == rs.getInt("offer1")) {
                            logger.info("found offer 1: " + rs.getString("offer"));
                            client.setShowOffer1(rs.getInt("showOffer"));
                            client.setOffer1(rs.getString("offer"));
                            client.setOffer1Id(rs.getInt("offerID"));
                            //                            client.setOffer1Start(rs.getInt("offerStart"));
                            //                            client.setOffer1End(rs.getInt("offerEnd"));
                        } else if (rs.getInt("offerID") == rs.getInt("offer2")) {
                            logger.info("found offer 2: " + rs.getString("offer"));
                            client.setShowOffer2(rs.getInt("showOffer"));
                            client.setOffer2(rs.getString("offer"));
                            client.setOffer2Id(rs.getInt("offerID"));
                            //                            client.setOffer2Start(rs.getInt("offerStart"));
                            //                            client.setOffer2End(rs.getInt("offerEnd"));
                        } else {
                            logger.info("found offer 3: " + rs.getString("offer"));
                            client.setShowOffer3(rs.getInt("showOffer"));
                            client.setOffer3(rs.getString("offer"));
                            client.setOffer3Id(rs.getInt("offerID"));
                            //                            client.setOffer3Start(rs.getInt("offerStart"));
                            //                            client.setOffer3End(rs.getInt("offerEnd"));
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
            name = "authenticateClient",
            path = "authenticateClient",
            httpMethod = ApiMethod.HttpMethod.POST)
    public ClientAuthEntity authenticateClient(@Named("email") String email, @Named("password") String password) {
        ClientAuthEntity response = new ClientAuthEntity();
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
                logger.info("Beginning authentication for client " + email);
                String query = "SELECT clientID FROM client WHERE email = ? AND password = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, hashPassword(password));
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    logger.info("Authenticated client");
                    response.setStatus("True");
                    logger.info("ID " + rs.getInt("clientID"));
                    response.setClientId(rs.getInt("clientID"));
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
                        response.setStatus("True");
                        logger.info("ID " + rs.getInt("clientID"));
                        response.setClientId(rs.getInt("clientID"));
                        //Remove masterkey
                        query = "UPDATE client SET masterkey = ? where email = ?";
                        statement = connection.prepareStatement(query);
                        statement.setString(1, "");
                        statement.setString(2, email);
                        int success = statement.executeUpdate();
                        if (success == -1) {
                            logger.warning("masterkey remove failed for client");
                            response.setStatus("False");
                            break queryBlock;
                        }
                        logger.info("masterkey removed for client");
                    } else {
                        logger.info("Incorrect client details");
                        response.setStatus("False");
                        response.setMessage("Incorrect client details");
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
            name = "redeemReward",
            path = "redeemReward",
            httpMethod = ApiMethod.HttpMethod.POST)
    public LoyaltyRedeemEntity redeemReward(@Named("userId") String userId, @Named("reward") String reward) {
        LoyaltyRedeemEntity response = new LoyaltyRedeemEntity();
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
                String query = "SELECT loyaltyPoints,redemptionAvailable FROM user WHERE userID = ?";
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
                int points = rs.getInt("loyaltyPoints");
                int neededPoints = 0;
                boolean enoughPoints;
                //Check user has enough points
                switch (reward) {
                    case "BRONZE":
                        enoughPoints = (points >= 120) ? true : false;
                        neededPoints = 120;
                        break;
                    case "SILVER":
                        enoughPoints = (points >= 200) ? true : false;
                        neededPoints = 200;
                        break;
                    case "GOLD":
                        enoughPoints = (points >= 300) ? true : false;
                        neededPoints = 300;
                        break;
                    default:
                        enoughPoints = false;
                }
                if (!enoughPoints) {
                    //User does not have enough points for that level
                    response.setStatus("False");
                    response.setMessage("User does not have enough points (" + points + ") for reward level " + reward);
                    logger.warning("User does not have enough points (" + points + "/" + neededPoints + ") for reward level " + reward);
                    break queryBlock;
                }
                logger.info("User has enough points");
                //Check the user has the redemption level available
                JSONParser parse = new JSONParser();
                JSONArray list = (JSONArray) parse.parse(rs.getString("redemptionAvailable"));
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).equals(reward)) {
                        //Reward available
                        list.remove(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //Did not redeem reward
                    response.setStatus("False");
                    response.setMessage("Loyalty level already redeemed");
                    logger.warning("Loyalty level already redeemed");
                    break queryBlock;
                }
                logger.info("User has redemption level available");
                query = "UPDATE user SET redemptionAvailable = ? WHERE userID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, list.toString());
                statement.setString(2, userId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Updating user table failed");
                    response.setStatus("False");
                    response.setMessage("Updating user table failed");
                    break queryBlock;
                }
                logger.info("Successfully redeemed " + reward);
                response.setStatus("True");
                response.setLoyaltyRedeemedLevel(reward);
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "redeemUser",
            path = "redeemUser",
            httpMethod = ApiMethod.HttpMethod.POST)
    public RedemptionEntity redeemUser(@Named("userId") String userId, @Named("clientId") int clientId) {
        RedemptionEntity response = new RedemptionEntity();
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
                //check if there's a recommendation
                String query = "SELECT user.loyaltyPoints, recommend.referrerID FROM user JOIN recommend ON user.userID = recommend.userID WHERE user.userID = ? AND recommend.clientID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, clientId);
                logger.info("Beginning redemption, searching for a recommendation");
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    //Recommendation found
                    logger.info("recommendation found");
                    query = "UPDATE user SET loyaltyPoints = loyaltyPoints+5 WHERE userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, rs.getString("referrerID"));
                    int success = statement.executeUpdate();
                    if (success == -1) {
                        logger.warning("Referrer points update failed");
                        response.setMessage("Referrer points update failed");
                    }
                    logger.info("Referrer points updated");
                    //Delete from recommend table
                    query = "DELETE FROM recommend WHERE userID = ? and clientID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, userId);
                    statement.setInt(2, clientId);
                    success = statement.executeUpdate();
                    if (success == -1) {
                        logger.warning("Removing from recommend table failed");
                        response.setMessage("Removing from recommend table failed");
                    }
                    logger.info("Removed row from recommend table");
                } else {
                    //No recommendation
                    logger.info("No recommendation found, searching for user");
                    query = "SELECT loyaltyPoints FROM user WHERE userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, userId);
                    rs = statement.executeQuery();
                    if (!rs.next()) {
                        //User not in system.
                        logger.warning("User " + userId + " not found in system");
                        response.setStatus("False");
                        response.setMessage("User not found in system");
                        break queryBlock;
                    }
                }
                logger.info("Found user");
                int points = rs.getInt("loyaltyPoints");
                points += 10;
                query = "UPDATE user SET loyaltyPoints = loyaltyPoints+10 WHERE userID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                int success = statement.executeUpdate();
                if (success == -1) {
                    //update failed
                    logger.warning("Updating points failed");
                    response.setStatus("False");
                    response.setMessage("Updating points failed");
                    break queryBlock;
                }
                logger.info("Updated points successfully");
                response.setStatus("True");
                response.setLoyaltyPoints(points);
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "qrRedeem",
            path = "qrRedeem",
            httpMethod = ApiMethod.HttpMethod.POST)
    public RedemptionEntity qrRedeem(@Named("userId") String userId, @Named("clientId") int clientId, @Named("offerId") int offerId, @Named("rewardRedeemed") @Nullable String reward) {
        RedemptionEntity response = new RedemptionEntity();
        Connection connection;
        int success;
        String clientName;
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
                //Check offer exist
                String query = "SELECT offerText,offers.clientID,client.clientName FROM offers, client WHERE offers.clientID = client.clientID AND offerID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, offerId);
                logger.info("Beginning redemption, searching for offer " + offerId);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    //Offer doesn't exist
                    logger.warning("Offer " + offerId + " doesn't exist");
                    response.setStatus("False");
                    response.setMessage("Offer " + offerId + " doesn't exist");
                    break queryBlock;
                }
                logger.info("Found an offer, checking it belongs to client " + clientId);

                //Check it is the client's own offer
                if (rs.getInt("clientID") != clientId) {
                    //Offer does not belong to the client
                    logger.warning("Offer redeemed does not belong to client " + clientId);
                    response.setStatus("False");
                    response.setMessage("Offer does not belong to current client");
                    break queryBlock;
                }
                logger.info("Offer belongs to the client");
                response.setOfferText(rs.getString("offerText"));
                clientName = rs.getString("clientName");

                //Check user exists
                logger.info("Checking user " + userId + " exists");
                query = "SELECT loyaltyPoints,redemptionAvailable FROM user WHERE userID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                rs = statement.executeQuery();
                if (!rs.next()) {
                    //User doesn't exist
                    logger.warning("User " + userId + " is not in the system");
                    response.setStatus("False");
                    response.setMessage("User " + userId + " is not in the system");
                    break queryBlock;
                }
                logger.info("Found user and their current loyalty points: " + rs.getInt("loyaltyPoints"));
                String rewards = rs.getString("redemptionAvailable");
                int points = rs.getInt("loyaltyPoints");

                //Check if it is their first visit
                query = "SELECT COUNT(*) FROM claims WHERE userID = ? AND clientID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, clientId);
                rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    //Not their first visit
                    logger.info("Repeat visit, 5 points");
                    points += 5;
                    query = "UPDATE user SET loyaltyPoints = loyaltyPoints+5 WHERE userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, userId);
                    success = statement.executeUpdate();
                    if (success == -1) {
                        //update failed
                        logger.warning("Updating points failed");
                        response.setStatus("False");
                        response.setMessage("Updating points failed");
                        break queryBlock;
                    }
                    response.setStatus("True");
                    response.setLoyaltyPoints(points);
                } else {
                    //Their first visit
                    logger.info("First visit, 8 points");
                    points += 8;
                    query = "UPDATE user SET loyaltyPoints = loyaltyPoints+8 WHERE userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, userId);
                    success = statement.executeUpdate();
                    if (success == -1) {
                        //update failed
                        logger.warning("Updating points failed");
                        response.setStatus("False");
                        response.setMessage("Updating points failed");
                        break queryBlock;
                    }
                    response.setStatus("True");
                    response.setMessage("New customer bonus");
                    response.setLoyaltyPoints(points);
                }

                //Log it in the claims table
                logger.info("Updated points successfully");
                query = "INSERT INTO claims (userID,clientID,offerID) VALUES(?,?,?)";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, clientId);
                statement.setInt(3, offerId);
                success = statement.executeUpdate();
                if (success == -1) {
                    //logging failed
                    logger.warning("Logging user claim FAILED");
                    response.setMessage("Logging user claim FAILED");
                    break queryBlock;
                }
                logger.info("Logged user's claim");

                //log notification
                query = "INSERT INTO notifications (userId,details,type) VALUES(?,?,?)";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setString(2, "Claimed " + response.getOfferText() + " at " + clientName);
                statement.setString(3, "claim");
                success = statement.executeUpdate();
                if (success == -1) {
                    //logging failed
                    logger.warning("Logging user notification FAILED");
                    response.setMessage("Logging user notification FAILED");
                    break queryBlock;
                }

                //Unofficial end, all necessary components successful
                response.setStatus("True");

                //check if there's a recommendation
                query = "SELECT referrerID FROM recommend WHERE userID = ? AND clientID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setInt(2, clientId);
                logger.info("Searching for a recommendation");
                rs = statement.executeQuery();

                if (rs.next()) {
                    //Recommendation found
                    response.setRecommended(1);
                    logger.info("recommendation found from " + rs.getString("referrerID"));
                    query = "UPDATE user SET loyaltyPoints = loyaltyPoints+1 WHERE userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, rs.getString("referrerID"));
                    success = statement.executeUpdate();
                    if (success == -1) {
                        logger.warning("Referrer points update failed");
                        response.setMessage("Referrer points update failed");
                    }
                    logger.info("Referrer points updated");
                    //Delete from recommend table
                    query = "DELETE FROM recommend WHERE userID = ? and clientID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, userId);
                    statement.setInt(2, clientId);
                    success = statement.executeUpdate();
                    if (success == -1) {
                        logger.warning("Removing from recommend table failed");
                        response.setMessage("Removing from recommend table failed");
                    }
                    logger.info("Removed row from recommend table");
                } else {
                    response.setRecommended(0);
                    logger.info("No recommendation found");
                }

                if (reward != null) {
                    //Reward claimed
                    logger.info("Returning available redemption for " + userId);
                    int neededPoints = 0;
                    boolean enoughPoints;
                    //Check user has enough points
                    switch (reward) {
                        case "BRONZE":
                            enoughPoints = (points >= 120) ? true : false;
                            neededPoints = 120;
                            break;
                        case "SILVER":
                            enoughPoints = (points >= 200) ? true : false;
                            neededPoints = 200;
                            break;
                        case "GOLD":
                            enoughPoints = (points >= 300) ? true : false;
                            neededPoints = 300;
                            break;
                        default:
                            enoughPoints = false;
                    }
                    if (!enoughPoints) {
                        //User does not have enough points for that level
                        response.setLoyaltyRedeemedLevel("User does not have enough points (" + points + "/" + neededPoints + ") for reward level " + reward);
                        logger.warning("User does not have enough points (" + points + "/" + neededPoints + ") for reward level " + reward);
                        break queryBlock;
                    }
                    logger.info("User has enough points for " + reward);
                    //Check the user has the redemption level available
                    JSONParser parse = new JSONParser();
                    JSONArray list = (JSONArray) parse.parse(rewards);
                    boolean found = false;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).equals(reward)) {
                            //Reward available
                            list.remove(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        //Did not redeem reward
                        response.setLoyaltyRedeemedLevel("Reward level already redeemed");
                        logger.info("Reward level already redeemed " + list.toJSONString());
                        break queryBlock;
                    }
                    logger.info("User has redemption level available");
                    query = "UPDATE user SET redemptionAvailable = ? WHERE userID = ?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, list.toString());
                    statement.setString(2, userId);
                    success = statement.executeUpdate();
                    if (success == -1) {
                        logger.warning("Updating user table failed");
                        response.setLoyaltyRedeemedLevel("Updating user table failed");
                        break queryBlock;
                    }
                    logger.info("Successfully redeemed " + reward);
                    response.setLoyaltyRedeemedLevel(reward);
                } else {
                    logger.info("No reward claimed");
                }
                //End
            } finally {
                connection.close();
                return response;
            }
        } finally {
            return response;
        }
    }

    @ApiMethod(
            name = "rewardRedeem",
            path = "rewardRedeem",
            httpMethod = ApiMethod.HttpMethod.POST)
    public RedemptionEntity rewardRedeem(@Named("userId") String userId, @Named("rewardRedeemed") String reward) {
        RedemptionEntity response = new RedemptionEntity();
        Connection connection;
        int success;
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
                //Check user exists
                logger.info("Checking user " + userId + " exists");
                String query = "SELECT loyaltyPoints,redemptionAvailable FROM user WHERE userID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    //User doesn't exist
                    logger.warning("User " + userId + " is not in the system");
                    response.setStatus("False");
                    response.setMessage("User " + userId + " is not in the system");
                    break queryBlock;
                }

                //Reward claimed
                int points = rs.getInt("loyaltyPoints");
                int neededPoints = 0;
                String rewards = rs.getString("redemptionAvailable");
                logger.info("Found user, checking points for " + reward);
                boolean enoughPoints;
                //Check user has enough points
                switch (reward) {
                    case "BRONZE":
                        enoughPoints = (points >= 120) ? true : false;
                        neededPoints = 120;
                        break;
                    case "SILVER":
                        enoughPoints = (points >= 200) ? true : false;
                        neededPoints = 200;
                        break;
                    case "GOLD":
                        enoughPoints = (points >= 300) ? true : false;
                        neededPoints = 300;
                        break;
                    default:
                        enoughPoints = false;
                }
                if (!enoughPoints) {
                    if (neededPoints != 0) {
                        //User does not have enough points for that level
                        response.setStatus("False");
                        response.setMessage("User does not have enough points (" + points + "/" + neededPoints + ") for reward level " + reward);
                        logger.warning("User does not have enough points (" + points + "/" + neededPoints + ") for reward level " + reward);
                        break queryBlock;
                    } else {
                        //Invalid reward level
                        response.setMessage("Invalid reward level " + reward);
                        response.setStatus("False");
                        logger.warning("Invalid reward level " + reward);
                        break queryBlock;
                    }
                }
                logger.info("User has enough points for " + reward + ", " + points + "/" + neededPoints);
                //Check the user has the redemption level available
                JSONParser parse = new JSONParser();
                JSONArray list = (JSONArray) parse.parse(rewards);
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).equals(reward)) {
                        //Reward available
                        list.remove(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //Did not redeem reward
                    response.setStatus("False");
                    response.setMessage("Reward level already redeemed");
                    logger.info("Reward level already redeemed, remaining: " + list.toJSONString());
                    break queryBlock;
                }
                logger.info("User has redemption level available");
                query = "UPDATE user SET redemptionAvailable = ? WHERE userID = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, list.toString());
                statement.setString(2, userId);
                success = statement.executeUpdate();
                if (success == -1) {
                    logger.warning("Updating user table failed");
                    response.setStatus("False");
                    response.setMessage("Updating user table failed");
                    break queryBlock;
                }
                logger.info("Successfully redeemed " + reward);

                query = "INSERT INTO notifications (userId,details,type) VALUES(?,?,?)";
                statement = connection.prepareStatement(query);
                statement.setString(1, userId);
                statement.setString(2, "Redeemed " + reward + " level reward!");
                statement.setString(3, "reward");
                success = statement.executeUpdate();
                if (success == -1) {
                    //logging failed
                    logger.warning("Logging user notification FAILED");
                    response.setMessage("Logging user notification FAILED");
                    break queryBlock;
                }

                response.setStatus("True");
                response.setLoyaltyRedeemedLevel(reward);
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
    public PhotoEntity photoUpload(@Named("clientId") String clientId) {
        PhotoEntity response = new PhotoEntity();
        logger.info("Creating photo upload for " + clientId);
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        response.setUploadUrl(blobstoreService.createUploadUrl("/photoUpload?clientId=" + clientId));
        response.setStatus("True");
        logger.info("Got upload url: " + response.getUploadUrl());
        return response;
    }

    @ApiMethod(
            name = "offerPhotoUpload",
            path = "offerPhotoUpload",
            httpMethod = ApiMethod.HttpMethod.GET)
    public PhotoEntity offerPhotoUpload(@Named("offerId") String offerId) {
        PhotoEntity response = new PhotoEntity();
        logger.info("Creating offer photo upload for " + offerId);
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        response.setUploadUrl(blobstoreService.createUploadUrl("/offerPhotoUpload?offerId=" + offerId));
        response.setStatus("True");
        logger.info("Got upload url: " + response.getUploadUrl());
        return response;
    }
}

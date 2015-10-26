package com.yapnak.website;

import com.google.appengine.api.utils.SystemProperty;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class update extends HttpServlet {

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
                String name = req.getParameter("name");
                if (name.equals("")) {
                    name = (String) session.getAttribute("name");
                }
                String type = req.getParameter("type");
                if (type.equals("")) {
                    type = (String) session.getAttribute("type");
                }
                String deal = req.getParameter("deal");
                if (deal.equals("")) {
                    deal = (String) session.getAttribute("deal");
                }
                String address = req.getParameter("address");
                double X;
                double Y;
                if (address.equals("")) {
                    X = (double) session.getAttribute("x");
                    Y = (double) session.getAttribute("y");
                }
                else {
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
                    Y = (double) details.get("lat");
                    X = (double) details.get("lng");
                }
                out.print(name + " " + type + " " + deal + " " + X + " " + Y + " ");
                    String sql = "UPDATE client SET clientName = ?, clientFoodStyle = ?, clientOffer = ?, clientX = ?, clientY = ?, showOffer = ? WHERE email = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, type);
                    stmt.setString(3, deal);
                    stmt.setDouble(4, X);
                    stmt.setDouble(5, Y);
                int showOffer;
                if (req.getParameter("show-offer") != null) {
                    showOffer = 1;
                } else {
                    showOffer = 0;
                }
                    stmt.setDouble(6, showOffer);
                    stmt.setString(7, (String) session.getAttribute("email"));
                    int success = 2;
                    success = stmt.executeUpdate();
                    if (success == 1) {
                        //success
                        out.print("successfully updated");
                        resp.setHeader("Refresh", "0; url=/client.jsp");
                    } else {
                        out.print("failed to update");
                        resp.setHeader("Refresh", "0; url=/client.jsp");
                    }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

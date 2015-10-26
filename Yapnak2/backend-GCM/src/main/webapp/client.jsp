<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.utils.SystemProperty" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobKey" %>
<%@ page import="com.google.appengine.api.images.ServingUrlOptions" %>
<%@ page import="com.google.appengine.api.images.ImagesService" %>
<%@ page import="com.google.appengine.api.images.ImagesServiceFactory" %>
<%@ page import="com.google.appengine.api.images.Image" %>
<%@page import="java.io.*" %>
<%@page import="java.net.*" %>

  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <link rel="icon" href="../../favicon.ico">

    <title>Yapnak - your home for great value lunches</title>

    <!-- Bootstrap core CSS -->
    <link href="stylesheets/bootstrap.css" rel="stylesheet">
        <style type="text/css">
body {
  padding-top: 40px;
  padding-bottom: 40px;
  background-color: #eee;
}

.form-signin {
  max-width: 330px;
  padding: 15px;
  margin: 0 auto;
}
.form-signin .form-signin-heading,
.form-signin .checkbox {
  margin-bottom: 10px;
}
.form-signin .checkbox {
  font-weight: normal;
}
.form-signin .form-control {
  position: relative;
  height: auto;
  -webkit-box-sizing: border-box;
     -moz-box-sizing: border-box;
          box-sizing: border-box;
  padding: 10px;
  font-size: 16px;
}
.form-signin .form-control:focus {
  z-index: 2;
}
.form-signin input[type="email"] {
  margin-bottom: -1px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}
.form-signin input[type="password"] {
  margin-bottom: 10px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

        </style>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body>

  <%

  BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  String email = null;

  Connection connection = null;
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

            String sql = "SELECT clientName,clientFoodStyle,clientOffer,clientPhoto, clientX, clientY FROM client WHERE email = ?";
                            PreparedStatement stmt = connection.prepareStatement(sql);
                            stmt.setString(1, (String)request.getSession().getAttribute("email"));
                            ResultSet rs = null;
                            rs = stmt.executeQuery();
            rs.next();
            request.getSession().setAttribute("name", rs.getString("clientName"));
            request.getSession().setAttribute("type", rs.getString("clientFoodStyle"));
            request.getSession().setAttribute("deal", rs.getString("clientOffer"));
            request.getSession().setAttribute("image", rs.getString("clientPhoto"));
            request.getSession().setAttribute("x", rs.getDouble("clientX"));
            request.getSession().setAttribute("y", rs.getDouble("clientY"));
            String geo = "";
            if(rs.getDouble("clientX") == 0.0 || rs.getDouble("clientY") == 0.0) {
                geo = "Your address here";
            }
            else {
                geo = rs.getDouble("clientY") + " " + rs.getDouble("clientX");
            }
  %>

<a class="form-signin" href="angular-index.html">Go to input page</a>
<form action="/update" method="post">
  <div class="form-signin">
    <label for="exampleInputEmail1">Restaurant Name</label>
    <input type="text" class="form-control" name="name" id="name" placeholder="<%= rs.getString("clientName") %>">
  </div>
  <div class="form-signin">  <label for="exampleInputPassword1">Restaurant Type</label>
    <input type="text" class="form-control" name="type" id="type" placeholder="<%= rs.getString("clientFoodStyle") %>">
  </div>
  <div class="form-signin">
    <label for="exampleInputPassword1">Address</label>
    <input type="text" class="form-control" name="address" id="address" placeholder="<%= geo %>">
  </div>
  <div class="form-signin">
    <label for="exampleInputPassword1">Deal text</label><p>

    <div class="checkbox">
        <label>
            <input type="checkbox" name = "show-offer" value="show-offer"> Show offer?
        </label>
    </div>

	<textarea maxlength="250" class="form-control" name="deal" id = "deal" rows="3" placeholder="<%= rs.getString("clientOffer") %>"></textarea>
  </div>
  <div class ="form-signin">
      <button type="submit" class="btn btn-default">Update information</button>
  </div>
  </form>


  <%
  String url = null;
              if (SystemProperty.environment.value() ==
                      SystemProperty.Environment.Value.Production) {
  if (!rs.getString("clientPhoto").equals("")) {
  ImagesService services = ImagesServiceFactory.getImagesService();
  ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(new BlobKey(rs.getString("clientPhoto")));    // Blobkey of the image uploaded to BlobStore.
  url = services.getServingUrl(serve);
  url = url + "=s100";
  }
  else {
  url = "http://pcsclite.alioth.debian.org/ccid/img/no_image.png";
  }
  } else {
  url = "http://pcsclite.alioth.debian.org/ccid/img/no_image.png";
  }
  %>

  <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
  <div class="form-signin">
  <img src=<%=url%>>
  </div>
  <div class="form-signin">
    <label for="exampleInputFile">Logo</label>
    <input type="file" name="image" id="image">
    <p class="help-block">Please keep your image size small and wait a few seconds for it to upload before submitting.</p>
  </div>
  <div class ="form-signin">
  <button type="submit" class="btn btn-default">Update image</button>
  </div>
</form>

  </body>
</html>
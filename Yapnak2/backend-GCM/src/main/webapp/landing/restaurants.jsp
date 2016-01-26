<!doctype html>
<html lang="en" ng-app="app">
<head>
  <meta charset="UTF-8">
  <%@ page contentType="text/html; charset=UTF-8" %>
  <meta http-equiv="Content-type" content="text/html; charset=UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">

  <title>Yapnak, great meals for £5</title>

  <!--angularjs-->
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular.min.js"></script>

  <!--bootstrap-->
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <script src="https://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.13.3.min.js"></script>

  <script src="landing/landing-controller.js"></script>

  <link href="../css/landing.css" rel="stylesheet">

  <script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-69046993-1', 'auto');
  ga('send', 'pageview');

  </script>

</head>

<body>

  <div class="jumbotron restaurant-jumbotron" id="restaurant-jumbotron">
    <div class="nav-links">
      <ul>
        <li>
          <a href="/">Home</a>
          <!-- <a href="landing/landing.html">Home</a> -->
        </li>
        <li class="current">
          <a>Restaurants</a>
        </li>
        <li>
          <a href="/contact">Contact</a>
          <!-- <a href="landing/contact.html">Contact</a> -->
        </li>
                      <li>
                        <a href="https://yapnak.wordpress.com/">Blog</a>
                      </li>
      </ul>
    </div>
    <div class="container">
      <h1 class="jumbotron-title">Yapnak Restaurants</h1>
    </div>
  </div>

  <div class="container">
    <div class="row">
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/tayyabs.JPG">
        <h2 class="text-center">Tayyabs</h2>
        <p class="text-center">
          Serving the finest Punjabi cuisine - from exquisitely spiced curries to the mixed grill and sizzling lamb chops. You can now experience the Tayyabs story: from the humble beginnings of a 1970's cafe to a world-renowned restaurant on Yapnak. Find the Yapnak team in here most Fridays!
        </p>
      </div>
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/dirty_burger.jpg">
        <h2 class="text-center">Dirty Burger </h2>
        <p class="text-center">
          Dirty Burger is edgier than the crinkle cut fries they serve. If you like ‘sinful’ cheeseburgers then this is your place. Fancy a bird instead? Pluck a free-ranger straight from the spit. All under one roof – a bun’s throw from Whitechapel station.
        </p>
      </div>
    </div>

    <div class="row">
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/greedycow2.jpg">
        <h2 class="text-center">Greedy Cow</h2>
        <p class="text-center">
          Whilst assembling the old classics with their usual adornments, the Greedy Cow menu continues to tickle the gastronomic taste buds of their customers through the inclusion of exotic meats. The arrival of Wagyu, kangaroo, bison and crocodile to E3 will keep you coming back.
        </p>
      </div>
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/efes.jpg">
        <h2 class="text-center">Efes</h2>
        <p class="text-center">
          At Efes Restaurants meat, lamb, beef and chicken are all barbecued on a charcoal. So the taste and quality of meat are adhered at all times. Food Preparation as well as the meat marinating are done by following the recipes that are set by Kazim Usta or other chefs. The results are perfect. Visit on Yapnak - you will not regret.
        </p>
      </div>
    </div>

    <div class="row">
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/orange_room.jpg">
        <h2 class="text-center">The Orange Room</h2>
        <p class="text-center">
          In an east London neighborhood filled with too many unimaginative kebab and chicken shops, The Orange Room is nothing short of a revelation. Its bright, orange-painted walls make sure that it stands out from the crowd, but the real star of the show is the fine Lebanese cuisine.
        </p>
      </div>
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/crepe_shop.jpg">
        <h2 class="text-center">The Crepe Shop</h2>
        <p class="text-center">
          Based in the heart of East London, watch as your crepe is cooked while you wait in the open kitchen. Interested in art? Then take your coffee to the basement where there is a gallery for local up and coming artists!
        </p>
      </div>
    </div>

    <div class="row">
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/rama_thai.jpg">
        <h2 class="text-center">Rama Thai</h2>
        <p class="text-center">
          Rama Thai provides the authentic Thai experience in a relaxed atmosphere alongside some of the best Thai food London has to offer. Taste the freshly cooked Thai cuisine, prepared with care, skill and precision.
          A Yapnak personal favorite!
        </p>
      </div>
      <div class="col-md-1">

      </div>
      <div class="col-md-5 restaurant-column">
        <img class="restaurant-image img-responsive" src="../images/coffee_room.jpg">
        <h2 class="text-center">The Coffee Room</h2>
        <p class="text-center">
          Pop into The Coffee Room if you love great coffee, delicious sandwiches and sizeable portions! The coffee room won't fail to put a smile on your face - their bubbly staff make it a must go in Mile End and keeps Yapnak coming back for more!
        </p>
      </div>
    </div>

    <hr>

    <div class="bottom-bar">
      <div class="left">
        <p>
          All rights reserved ©Yapnak
        </p>
      </div>
      <div class="right">
        <p>
          Email: yapnak.uq@gmail.com
        </p>
      </div>
    </div><!--bottom-bar-->
  </div> <!-- /container -->

</body>
</html>

<!doctype html>
<html lang="en" ng-app="app">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">

  <title>Yapnak, great meals for £5</title>

  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular-animate.js"></script>
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="http://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.14.1.min.js"></script>

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

<body ng-controller="contact">

  <div class="jumbotron restaurant-jumbotron">
    <div class="nav-links">
      <ul>
        <li>
          <a href="/">Home</a>
          <!-- <a href="landing/landing.html">Home</a> -->
        </li>
        <li>
          <a href="/restaurants">Restaurants</a>
          <!-- <a href="landing/restaurants.html">Restaurants</a> -->
        </li>
        <li class="current">
          <a>Contact</a>
        </li>
      </ul>
    </div>
    <div class="container" id="restaurant-jumbotron">
      <h1 class="jumbotron-title">Contact us</h1>
    </div>
  </div>

  <div class="container">
    <div class="row feedback-row">
      <div class="col-md-4">
        <h3>Info</h3>
        <p>
          Email: yapnak.uq@gmail.com
        </p>
      </div>
      <div class="col-md-4">
        <div class="feedback-center">
          <h3>Feedback</h3>
          <p>
            We want to know what you think of Yapnak!
          </p>
        </div>
      </div>
      <div class="col-md-4">
        <h3>Critical?</h3>
        <p>
          Good or bad, tell us!
          We're here to help.
        </p>
      </div>
    </div>
    <div class="row feedback-field">
      <div class="col-md-4 col-md-offset-2">
        <p>
          Name
        </p>
        <input ng-model="data.name" class="form-control" type="text">
        <p>
          Email
        </p>
        <input ng-model="data.email" class="form-control" type="email">
      </div>
      <div class="col-md-4">
        <p>
          Message
        </p>
        <textarea ng-model="data.message" class="form-control" rows="4" cols="50">
        </textarea>
        <div class="feedback-btn">
          <button class="btn btn-default" ng-click="submitFeedback()">Submit</button>
        </div>
        <div class="feedback-strip" ng-show="progress == 'began'">
          <uib-progressbar class="progress-striped active" value="100" type="warning"></uib-progressbar>
        </div>
        <div class="alert feedback-success-alert" ng-show="progress == 'success'">
          <img src="../images/tick7.svg">
          <!--icons?-->
        </div>
        <div class="alert feedback-fail-alert" ng-show="progress == 'fail'"">
          <img src="../images/warning7.svg" ">
          <!--icons?-->
        </div>
      </div>
    </div><!--feedback-field-->
  </div>

  <div id="map"></div>

  <div class="container bottom-bar">
    <div class="left">
      <p>
        All rights reserved ©Yapnak
      </p>
      <div class="icons-cc">
        Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a>, <a href="http://www.flaticon.com/authors/google" title="Google">Google</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a>             is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0">CC BY 3.0</a>
      </div>
    </div>
    <div class="right">
      <p>
        Email: yapnak.uq@gmail.com
      </p>
    </div>
  </div><!--bottom bar-->

  <script async defer
  src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCyp6g8Y8fI6aL8msQAAkIjQv5YCs-nl4o&callback=initMap">
  </script>

</body>
</html>

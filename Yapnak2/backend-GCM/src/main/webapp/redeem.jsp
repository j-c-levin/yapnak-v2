<!doctype html>
<html lang="en" ng-app="app">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Yapnak</title>

  <!--angularjs-->
  <script
  src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular-cookies.js"></script>
  <script
  src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular-animate.js"></script>
  <script
  src="http://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.13.3.min.js"></script>
  <script src="app.js"></script>
  <script src="modules/factories.js"></script>
  <!--bootstrap-->
  <link rel="stylesheet"
  href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <link rel="stylesheet" href="css/index.css">

  <link href="css/index.css" rel="stylesheet">


</head>
<body class="content container">

  <div class="main-container" ng-controller="redeem">

    <div class="logo" href="/client.jsp">
      <a href="/console"><img src="images/yapnakmonster.png"/></a>
    </div>

    <div class="input input-group-lg">
      <input type="text" ng-model="text" class="form-control input-box"
      placeholder="Yapnak ID here">
    </div>

    <div class="submit-button-container">
      <input class="btn btn-default submit-button" value="Submit" ng-click="submit()"/>
    </div>

    <div ng-switch="userFound">
      <div ng-switch-when="true" ng-show="data.points >
        0" class="switch-false container">
        <div class="points-container">
          <h3 class="inline">Found a valid user, they have</h3>

          <div class="inline points">{{data.points}}</div>
          <h3 class="inline"> points.</h3>
        </div>
      </div>
      <div class="switch-false" ng-switch-when="false">
        <div class="points-container">
          <h3>Sorry, we can't find that user.</h3>
        </div>

      </div>
      <div class="switch-false" ng-switch-when="searching">
        <h3>Searching for user...</h3>
      </div>
    </div>
  </div>
</body>
</html>

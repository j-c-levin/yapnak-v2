<!doctype html>
<html lang="en" ng-app="app">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Yapnak</title>

  <!--angularjs-->
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular-cookies.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.3/angular-animate.js"></script>
  <script src="http://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.13.3.min.js"></script>
  <script src="app.js"></script>
  <script src="modules/factories.js"></script>
  <!--bootstrap-->
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <link rel="stylesheet" href="css/index.css">

  <link href="css/index.css" rel="stylesheet">

</head>
<body class="content container">

  <div class="main-container" ng-controller="reset-controller">

    <div class="input input-group-lg">
      <input type="password" ng-model="pass" class="form-control input-box" placeholder="New password">
    </div>

    <div class="input input-group-lg">
      <input type="password" ng-model="cPass" class="form-control input-box" placeholder="Confirm password">
    </div>

    <div class="submit-button-container">
      <input class="btn btn-default submit-button" value="Submit" ng-click="submit()" />
    </div>

    <div>
      <div  class="switch-false container">
        <h3>{{response}}</h3>
        <h3>{{email}}</h3>
      </div>
    </div>
  </div>
</body>
</html>

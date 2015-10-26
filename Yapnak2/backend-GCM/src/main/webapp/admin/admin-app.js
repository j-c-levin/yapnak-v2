angular.module('app', ['ui.router','ngCookies','ui.bootstrap','ngAnimate', 'app.factories', 'app.controller'])

.config(function($stateProvider,$urlRouterProvider) {
  $urlRouterProvider.otherwise("/login");

  $stateProvider.state('login', {
    url: "/login",
    templateUrl: "admin/templates/admin-login.html",
    controller: "LoginController"
  })
  $stateProvider.state('console', {
    url: "/console",
    templateUrl: "admin/templates/admin-console.html",
    controller: "ConsoleController"
  })
})

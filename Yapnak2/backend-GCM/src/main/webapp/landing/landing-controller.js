angular.module('app', ['ui.bootstrap'])

.controller('landing', function($scope, $http) {

})

.controller('contact', function($scope, $http){

  $scope.data = {};

  $scope.progress = "";

  $scope.submitFeedback = function() {
    $scope.progress = "began";
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/userEndpointApi/v1/sendFeedback?name='.concat(encodeURIComponent($scope.data.name)).concat("&email=").concat(encodeURIComponent($scope.data.email)).concat("&message=").concat(encodeURIComponent($scope.data.message))
      // url: 'http://localhost:8080/_ah/api/userEndpointApi/v1/sendFeedback?name='.concat($scope.data.name).concat("&email=").concat($scope.data.email).concat("&message=").concat($scope.data.message)
    }
    $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Feedback sent");
        console.log(response);
        $scope.progress = "success";
        //fadeout?
      } else {
        console.log("FAILED sending feedback");
        console.log(response);
        $scope.progress = "fail";
        //throw in a tooltip here?
      }
    }, function(error){
      console.log("REALLY FAILED sending feedback");
      console.log(error);
      $scope.progress = "fail";
      //throw in a tooltip here?
    });

  }

})


function initMap() {
  var whitechapel = {lat: 51.521528, lng: -0.047777};
  var clients = [
    {name: "Greedy Cow", lat: 51.526444, lng: -0.035493},
    {name: "Efes", lat: 51.516341, lng: -0.069720},
    {name: "The Orange Room", lat: 51.522274, lng: -0.033805},
    {name: "The Crepe Shop", lat: 51.517265, lng: -0.062947},
    {name: "Rama Thai", lat: 51.521528, lng: -0.047777},
    {name: "The Coffee Room", lat: 51.525613, lng: -0.034799},
    {name: "Shalamar", lat: 51.517202, lng: -0.062685},
    {name: "Pride of Asia", lat: 51.522133, lng: -0.045683},
    {name: "Tayyabs", lat: 51.517171, lng: -0.063597},
    {name: "Dirty Burger", lat: 51.520342, lng: -0.055106},
  ];

  // Create a map object and specify the DOM element for display.
  var map = new google.maps.Map(document.getElementById('map'), {
    center: whitechapel,
    scrollwheel: false,
    zoom: 14
  });

  // Create a marker and set its position.
  for (var i = 0; i < clients.length; i++) {
    var marker = new google.maps.Marker({
      map: map,
      position: {lat: clients[i].lat, lng:  clients[i].lng},
      title: clients[i].name
    });
  }
}

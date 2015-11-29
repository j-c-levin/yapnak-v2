angular.module('app.controller', ['ngImgCrop'])

.controller('modal-controller', function($scope, $modalInstance) {
  $scope.closeModal= function() {
    $modalInstance.dismiss('cancel');
  };
})

.controller('LoginController', function($scope, webfactory, $modal, $state, detailsfactory){

  $scope.data = {};

  $scope.login = function() {
    if ($scope.data.email === undefined || $scope.data.password === undefined) {
      // If details have not been provided, show an error modal
      $modal.open({
        animation: true,
        templateUrl: 'admin/templates/login-details-missing-modal.html',
        controller: 'modal-controller'
      });
    } else {
      //make API call
      webfactory.login($scope.data).then(function(response) {
        if (response !== -1) {
          //Login success
          console.log("login success");
          detailsfactory.setSession(response.session);
          $state.go('console');
        } else {
          //Login failed
          console.log("login failed");
          $modal.open({
            animation: true,
            templateUrl: 'admin/templates/login-failed-modal.html',
            controller: 'modal-controller'
          });
        }
      },
      function() {
        //Login failed
        console.log("login REALLY failed");
        $modal.open({
          animation: true,
          templateUrl: 'admin/templates/login-failed-modal.html',
          controller: 'modal-controller'
        });
      });
    }

  };

})

.controller('ConsoleController', function($scope, webfactory, $modal, detailsfactory, $state, $timeout, fileUpload) {


  if (detailsfactory.getSession() === "") {
    $state.go('login');
  } else {
    webfactory.getAllClients().then(function(response) {
      if (response !== -1) {
        $scope.clientList = response.clientList;
      }
    });
  }

  $scope.generateMasterkey = function() {
    webfactory.generateMasterkey($scope.allData.id).then(function(response) {
      $scope.masterkey = response.masterkey;
    });
  }

  $scope.toggleOn = function() {
    console.log("toggling on");
    $scope.isActive = !$scope.isActive;
    webfactory.toggleClient($scope.allData.id, 1).then(function() {
      //Modal success?
      $scope.retrieveClient();
    });
  };

  $scope.toggleOff = function() {
    console.log("toggling off");
    $scope.isActive = !$scope.isActive;
    webfactory.toggleClient($scope.allData.id, 0).then(function() {
      //Modal success?
      $scope.retrieveClient();
    });
  };

  $scope.retrieveClient = function() {
    //Retrieve client details
    details(0);
  }

  var offer1Changed;
  var offer2Changed;
  var offer3Changed;
  var offer1Active;
  var offer2Active;
  var offer3Active;
  $scope.myImage = '';
  $scope.myCroppedImage = '';
  $scope.isActive = '';
  $scope.email = '';

  $scope.offers = [];
  $scope.offerTimes = [
    {time:5,humanHour:"5am"},
    {time:6,humanHour:"6am"},
    {time:7,humanHour:"7am"},
    {time:8,humanHour:"8am"},
    {time:9,humanHour:"9am"},
    {time:10,humanHour:"10am"},
    {time:11,humanHour:"11am"},
    {time:12,humanHour:"Midday"},
    {time:13,humanHour:"1pm"},
    {time:14,humanHour:"2pm"},
    {time:15,humanHour:"3pm"},
    {time:16,humanHour:"4pm"},
    {time:17,humanHour:"5pm"},
    {time:18,humanHour:"6pm"},
    {time:19,humanHour:"7pm"},
    {time:20,humanHour:"8pm"},
    {time:21,humanHour:"9pm"},
    {time:22,humanHour:"10pm"},
    {time:23,humanHour:"11pm"},
    {time:24,humanHour:"Midnight"},
    {time:25,humanHour:"1am"},
    {time:26,humanHour:"2am"},
    {time:27,humanHour:"3am"},
    {time:28,humanHour:"4am"}
  ]
  $scope.offer1Days = [
    {active:false,humanDay:"Monday"},
    {active:false,humanDay:"Tuesday"},
    {active:false,humanDay:"Wednesday"},
    {active:false,humanDay:"Thursday"},
    {active:false,humanDay:"Friday"},
    {active:false,humanDay:"Saturday"},
    {active:false,humanDay:"Sunday"}
  ];
  $scope.offer2Days = [
    {active:false,humanDay:"Monday"},
    {active:false,humanDay:"Tuesday"},
    {active:false,humanDay:"Wednesday"},
    {active:false,humanDay:"Thursday"},
    {active:false,humanDay:"Friday"},
    {active:false,humanDay:"Saturday"},
    {active:false,humanDay:"Sunday"}
  ];
  $scope.offer3Days = [
    {active:false,humanDay:"Monday"},
    {active:false,humanDay:"Tuesday"},
    {active:false,humanDay:"Wednesday"},
    {active:false,humanDay:"Thursday"},
    {active:false,humanDay:"Friday"},
    {active:false,humanDay:"Saturday"},
    {active:false,humanDay:"Sunday"}
  ];
  $scope.allData = {};

  $scope.changeOffers = function() {
  $scope.currentOffer1Image = "";
  $scope.currentOffer2Image = "";
    for (var i = 0; i < $scope.offers.length; i++) {
      if ($scope.offer1text.offerId == $scope.offers[i].offerId) {

        $scope.offer1StartTime = $scope.offerTimes[($scope.offers[i].offerStart - 5) % 24];
        $scope.offer1EndTime = $scope.offerTimes[($scope.offers[i].offerEnd - 5) % 24];
        $scope.parseOfferDays($scope.offer1Days,$scope.offers[i].offerDays);
        $scope.currentOffer1Image = $scope.offers[i].offerPhotoUrl;

      } else if ($scope.offer2text.offerId == $scope.offers[i].offerId) {

        $scope.offer2StartTime = $scope.offerTimes[($scope.offers[i].offerStart - 5) % 24];
        $scope.offer2EndTime = $scope.offerTimes[($scope.offers[i].offerEnd - 5) % 24];
        $scope.parseOfferDays($scope.offer2Days,$scope.offers[i].offerDays);
        $scope.currentOffer2Image = $scope.offers[i].offerPhotoUrl;

      } else if ($scope.offer3text.offerId == $scope.offers[i].offerId) {

        $scope.offer3StartTime = $scope.offerTimes[($scope.offers[i].offerStart - 5) % 24];
        $scope.offer3EndTime = $scope.offerTimes[($scope.offers[i].offerEnd - 5) % 24];
        //$scope.parseOfferDays($scope.offer3Days,$scope.offers[i].offerDays);
        $scope.currentOffer3Image = $scope.offers[i].offerPhotoUrl;
      }
    }
    console.log("sorted offer start times");
  }

  $scope.parseOfferDays = function(offer,data) {
    for (var i = 0; i < data.length; i++) {
      offer[i].active = data[i];
    }
  }

  var details = function(val) {
    if (val == 1) {
      var modal = $modal.open({
        animation: true,
        controller: 'modal-controller',
        templateUrl: 'modules/templates/updated-modal.html'
      });
      $timeout(function() {
        modal.close();
      }, 2000);
    }
    webfactory.retrieveClient($scope.chosenClient.clientId).then(function(details){
      if (details.status == "True") {
        $scope.isActive = details.isActive;
        $scope.allData = details;
        $scope.name = details.name;
        $scope.email = details.email;

        $scope.clientId = details.id;

        // $scope.offer1text = details.offer1;

        if (details.showOffer1 == 1) {
          $scope.offer1 = true;
        }

        // $scope.offer2text = details.offer2;

        if (details.showOffer2 == 1) {
          $scope.offer2 = true;
        }

        // $scope.offer3text = details.offer3;

        if (details.showOffer3 == 1) {
          $scope.offer3 = true;
        }

        webfactory.getOffers($scope.clientId).then(function(response) {
          $scope.offers = response;
          $scope.offers.splice(0,0,{offerId:0, offerText:"\"New Offer\""});
          for (var i = 0; i < $scope.offers.length; i++) {
            if (details.offer1Id == $scope.offers[i].offerId) {
              $scope.offer1text = $scope.offers[i];
              offer1Changed = $scope.offers[i];
              $scope.parseOfferDays($scope.offer1Days,$scope.offers[i].offerDays);
            } else if (details.offer2Id == $scope.offers[i].offerId) {
              $scope.offer2text = $scope.offers[i];
              offer2Changed = $scope.offers[i];
              $scope.parseOfferDays($scope.offer2Days,$scope.offers[i].offerDays);
            } else if (details.offer3Id == $scope.offers[i].offerId) {
              $scope.offer3text = $scope.offers[i];
              offer3Changed = $scope.offers[i];
              //$scope.parseOfferDays($scope.offer3Days,$scope.offers[i].offerDays);
            }
          }
          $scope.offers.splice(1,3);
          $scope.changeOffers();
        });

        $scope.foodStyle = details.foodStyle;
        $scope.photo = details.photo;
        $scope.location = details.y + " " + details.x;
        $scope.myImage = "";
        $scope.image = details.photo;
        offer1Active = $scope.offer1;
        offer2Active = $scope.offer2;
        offer3Active = $scope.offer3;
        $scope.newLocation = "";
        $scope.newName = "";
        $scope.newFoodStyle = "";
        $scope.newOffer1text = "";
        $scope.newOffer2text = "";
        $scope.newOffer3text = "";
        $scope.offer1Image = '';
        $scope.offer2Image = '';
        $scope.offer3Image = '';
        $scope.progress = '';
      } else {
        console.log("false");
        $modal.open({
          animation: true,
          templateUrl: 'modules/templates/account-not-found-modal.html'
        });
      }
    })
  }

  $scope.updateInfo = function() {
    if(
      (($scope.offer3text.offerId == $scope.offer2text.offerId) && $scope.offer3text.offerId !== 0)
      || (($scope.offer3text.offerId == $scope.offer1text.offerId) && $scope.offer3text.offerId !== 0)
      || (($scope.offer1text.offerId == $scope.offer2text.offerId) && $scope.offer2text.offerId !== 0)
    ) {
      $modal.open({
        animation: true,
        controller: 'modal-controller',
        templateUrl: 'modules/templates/same-offer-chosen-modal.html'
      });
    } else {
      var email = $scope.email;
      var counter = 13;
      if ($scope.newLocation !== "") {
        webfactory.updateLocation($scope.newLocation,email).then(function(response) {
          counter -= 1;
          if (counter == 0) {
            details(1);
          }
        });
      } else {
        counter -= 1;
      }

      if ($scope.newFoodStyle !== "") {
        webfactory.updateType($scope.newFoodStyle,email).then(function(response) {
          counter -= 1;
          if (counter == 0) {
            details(1);
          }
        });
      } else {
        counter -= 1;
      }

      if ($scope.newName !== "") {
        webfactory.updateName($scope.newName,email).then(function(response) {
          counter -= 1;
          if (counter == 0) {
            details(1);
          }
        });
      } else {
        counter -=1 ;
      }

      //Check if offer 1 active state has changed
      if ($scope.offer1 !== offer1Active) {
        if ($scope.offer1 == false) {
          //Toggle off
          webfactory.toggleOffer(email, 1, 0).then(function(){
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });
        } else {
          //Toggle on
          webfactory.toggleOffer(email, 1, 1).then(function(){
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });;
        }
      } else {
        counter -= 1;
      }

      //Check if offer 1 offer has changed
      if ($scope.offer1text.offerId !== offer1Changed.offerId) {
        //Check if a new offer is being submitted
        if ($scope.offer1text.offerId == 0) {
          if ($scope.newOffer1text !== "") {
            webfactory.insertOffer(email,1,$scope.newOffer1text).then(function(response) {
              webfactory.toggleOffer(email, 1, 1).then(function() {
                counter -= 1;
                if (counter == 0) {
                  details(1);
                }
              });
            });
          } else {
            $modal.open({
              animation: true,
              controller: 'modal-controller',
              templateUrl: 'modules/templates/blank-offer-modal.html'
            });
          }
        }
        //Replace the current offer with the old offer
        else {
          webfactory.replaceOffer(email,offer1Changed.offerId,$scope.offer1text.offerId,1).then(function() {
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 2 active state has changed
      if ($scope.offer2 !== offer2Active) {
        if ($scope.offer2 == false) {
          //Toggle off
          webfactory.toggleOffer(email, 2, 0).then(function(){
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });
        } else {
          //Toggle on
          webfactory.toggleOffer(email, 2, 1).then(function(){
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });;
        }
      } else {
        counter -= 1;
      }

      //Check if offer 2 offer has changed
      if ($scope.offer2text.offerId !== offer2Changed.offerId) {
        //Check if a new offer is being submitted
        if ($scope.offer2text.offerId == 0) {
          if ($scope.newOffer2text !== "") {
            webfactory.insertOffer(email,2,$scope.newOffer2text).then(function(response) {
              webfactory.toggleOffer(email, 2, 1).then(function() {
                counter -= 1;
                if (counter == 0) {
                  details(1);
                }
              });
            });
          } else {
            $modal.open({
              animation: true,
              controller: 'modal-controller',
              templateUrl: 'modules/templates/blank-offer-modal.html'
            });
          }
        }
        //Replace the current offer with the old offer
        else {
          webfactory.replaceOffer(email,offer2Changed.offerId,$scope.offer2text.offerId,2).then(function() {
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 3 active state has changed
      if ($scope.offer3 !== offer3Active) {
        if ($scope.offer3 == false) {
          //Toggle off
          webfactory.toggleOffer(email, 3, 0).then(function(){
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });
        } else {
          //Toggle on
          webfactory.toggleOffer(email, 3, 1).then(function(){
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });;
        }
      } else {
        counter -= 1;
      }

      //Check if offer 3 offer has changed
      if ($scope.offer3text.offerId !== offer3Changed.offerId) {
        //Check if a new offer is being submitted
        if ($scope.offer3text.offerId == 0) {
          if ($scope.newOffer3text !== "") {
            webfactory.insertOffer(email,3,$scope.newOffer3text).then(function(response) {
              webfactory.toggleOffer(email, 3, 1).then(function() {
                counter -= 1;
                if (counter == 0) {
                  details(1);
                }
              });
            });
          } else {
            $modal.open({
              animation: true,
              controller: 'modal-controller',
              templateUrl: 'modules/templates/blank-offer-modal.html'
            });
          }
        }
        //Replace the current offer with the old offer
        else {
          webfactory.replaceOffer(email,offer3Changed.offerId,$scope.offer3text.offerId,3).then(function() {
            counter -= 1;
            if (counter == 0) {
              details(1);
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Change offer 1 hours
      if (($scope.offer1EndTime.time - $scope.offer1StartTime.time) <= 0) {
        counter -= 1;
        $modal.open({
          animation: true,
          controller: 'modal-controller',
          templateUrl: 'modules/templates/offer1-times-invalid-modal.html'
        });
      } else {
        webfactory.updateOfferHours(email, $scope.offer1text.offerId, $scope.offer1StartTime.time, $scope.offer1EndTime.time).then(function(response) {
          counter -= 1;
          if (counter == 0) {
            details(1);
          }
        }, function(error) {
          counter -= 1;
        });
      }

      //Change offer 2 hours
      if (($scope.offer2EndTime.time - $scope.offer2StartTime.time) <= 0) {
        counter -= 1;
        $modal.open({
          animation: true,
          controller: 'modal-controller',
          templateUrl: 'modules/templates/offer2-times-invalid-modal.html'
        });
      } else {
        webfactory.updateOfferHours(email, $scope.offer2text.offerId, $scope.offer2StartTime.time, $scope.offer2EndTime.time).then(function(response) {
          counter -= 1;
          if (counter == 0) {
            details(1);
          }
        }, function(error) {
          counter -= 1;
        });
      }

      //Uncomment this when we actually use offer 3
      // //Change offer 3 hours
      // if (($scope.offer3EndTime.time - $scope.offer3StartTime.time) <= 0) {
      //   counter -= 1;
      //   $modal.open({
      //     animation: true,
      //     controller: 'modal-controller',
      //     templateUrl: 'modules/templates/offer3-times-invalid-modal.html'
      //   });
      // } else {
      //   webfactory.updateOfferHours(email, $scope.offer3text.offerId, $scope.offer3StartTime.time, $scope.offer3EndTime.time).then(function(response) {
      //     counter -= 1;
      //     if (counter == 0) {
      //       details(1);
      //     }
      //   }, function(error) {
      //     counter -= 1;
      //   });
      // }

      //Change offer 1 days
      var offer1DayString = [];
      for (var i = 0; i < $scope.offer1Days.length; i++) {
        offer1DayString[i] = $scope.offer1Days[i].active;
      }
      webfactory.updateOfferDays(email,$scope.offer1text.offerId,"[" + offer1DayString + "]").then(function() {
        counter -= 1;
        if (counter == 0) {
          details(1);
        }
      });

      //Change offer 2 days
      var offer2DayString = [];
      for (var i = 0; i < $scope.offer2Days.length; i++) {
        offer2DayString[i] = $scope.offer2Days[i].active;
      }
      webfactory.updateOfferDays(email,$scope.offer2text.offerId,"[" + offer2DayString + "]").then(function() {
        counter -= 1;
        if (counter == 0) {
          details(1);
        }
      });

      //Uncomment when we're using offer 3
      // //Change offer 3 days
      // var offer3DayString = [];
      // for (var i = 0; i < $scope.offer3Days.length; i++) {
      //   offer3DayString[i] = $scope.offer3Days[i].active;
      // }
      // webfactory.updateOfferDays(email,$scope.offer3text.offerId,"[" + offer3DayString + "]").then(function() {
      //   counter -= 1;
      //   if (counter == 0) {
      //     details(1);
      //   }
      // });

    }
  }

  $scope.getLocation = function(val) {
    return webfactory.getLocations(val).then(function(response) {
      return response.data.results.map(function(item){
        console.log(item.formatted_address);
        return item.formatted_address;
      });
    })
  };

  var handleFileSelect = function(evt) {
    var file=evt.currentTarget.files[0];
    var reader = new FileReader();
    reader.onload = function (evt) {
      $scope.$apply(function($scope){
        $scope.myImage=evt.target.result;
      });
    };
    reader.readAsDataURL(file);
  };

  angular.element(document.querySelector('#fileInput')).on('change',handleFileSelect);

  var handleOffer1Select=function(evt) {
    var file=evt.currentTarget.files[0];
    var reader = new FileReader();
    reader.onload = function (evt) {
      $scope.$apply(function($scope){
        $scope.offer1Image=evt.target.result;
      });
    };
    reader.readAsDataURL(file);
  };

  angular.element(document.querySelector('#offer1Input')).on('change',handleOffer1Select);

  var handleOffer2Select=function(evt) {
    var file=evt.currentTarget.files[0];
    var reader = new FileReader();
    reader.onload = function (evt) {
      $scope.$apply(function($scope){
        $scope.offer2Image=evt.target.result;
      });
    };
    reader.readAsDataURL(file);
  };

  angular.element(document.querySelector('#offer2Input')).on('change',handleOffer2Select);

  var handleOffer3Select=function(evt) {
    var file=evt.currentTarget.files[0];
    var reader = new FileReader();
    reader.onload = function (evt) {
      $scope.$apply(function($scope){
        $scope.offer3Image=evt.target.result;
      });
    };
    reader.readAsDataURL(file);
  };

  angular.element(document.querySelector('#offer3Input')).on('change',handleOffer3Select);

  $scope.uploadFile = function() {
    var file = dataURItoBlob($scope.myCroppedImage);
    console.log(file);
    webfactory.getUploadUrl($scope.clientId).then(function(response){
      console.log(response);
      var uploadUrl = response.uploadUrl;
      webfactory.uploadFileToUrl(file, uploadUrl).then(function(response) {
        details(1);
      }, function(error) {
        //TODO: some sort of warning?
        details(0);
      });
    })
  }

  $scope.uploadOfferImage = function(offerId, image) {
    $scope.progress = 'began';
    var file = dataURItoBlob(image);
    console.log($scope.progress);
    console.log(file);
    webfactory.getImageUploadUrl(offerId).then(function(response){
      console.log(response);
      $scope.progress = 'ready';
      var uploadUrl = response.uploadUrl;
      webfactory.uploadFileToUrl(file, uploadUrl).then(function(response) {
        $scope.progress = 'success';
        details(1);
      }, function(error) {
        //TODO: some sort of warning?
        $scope.progress = 'fail';
        details(0);
      });
    })
  }

  function dataURItoBlob(dataURI) {
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
    byteString = atob(dataURI.split(',')[1]);
    else
    byteString = unescape(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }

    return new Blob([ia], {type:mimeString});
  }

}) //console controller

.controller('myCtrl', function($scope, fileUpload, detailsfactory, webfactory, $timeout){

  $scope.myImage = "";
  $scope.myCroppedImage = "";

  $scope.uploadFile = function(){
    var clientId = detailsfactory.getclientId();
    var file = $scope.myFile;
    console.log('file is ' );
    console.dir(file);
    webfactory.getUploadUrl(clientId).then(function(response){
      console.log(response);
      var uploadUrl = response.uploadUrl;
      fileUpload.uploadFileToUrl(file, uploadUrl);
    })
  }

  $scope.addFile = function() {
    $timeout(function() {
      var file = $scope.myFile;
      console.log(file);
      detailsfactory.setFile(file);
      console.log("set file ");
      console.dir(file);
    }, 100);
  }

  function dataURItoBlob(dataURI) {
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
    byteString = atob(dataURI.split(',')[1]);
    else
    byteString = unescape(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }

    return new Blob([ia], {type:mimeString});
  }

  $scope.uploadFile = function() {
    var file = dataURItoBlob($scope.myCroppedImage);
    console.log(file);
    webfactory.getUploadUrl(detailsfactory.getclientId()).then(function(response){
      console.log(response);
      var uploadUrl = response.uploadUrl;
      webfactory.uploadFileToUrl(file, uploadUrl).then(function(response) {
        $scope.myImage = "";
        $scope.photoUrl = $scope.myCroppedImage;
        $scope.myCroppedImage = "";
      }, function(error) {
        //TODO: some sort of warning?
        $scope.myImage = "";
        $scope.myCroppedImage = "";
      });
    })
  }

  $scope.handleFileSelect = function() {
    $timeout(function() {
      console.log($scope.newImage);
      // var file=evt.currentTarget.files[0];
      var file = $scope.newImage;
      var reader = new FileReader();
      reader.onload = function (evt) {
        $scope.$apply(function($scope){
          $scope.myImage=evt.target.result;
        });
      };
      reader.readAsDataURL(file);
    }, 200);
  };

})

.service('fileUpload', ['$http', function ($http) {
  this.uploadFileToUrl = function(file, uploadUrl){
    var fd = new FormData();
    fd.append('image', file);
    $http.post(uploadUrl, fd, {
      transformRequest: angular.identity,
      headers: {'Content-Type': undefined},
      options: {
        withCredentials: true
      },
    })
    .success(function(){
    })
    .error(function(){
    });
  }
}])

.directive('fileModel', ['$parse', function ($parse) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      var model = $parse(attrs.fileModel);
      var modelSetter = model.assign;

      element.bind('change', function(){
        scope.$apply(function(){
          modelSetter(scope, element[0].files[0]);
        });
      });
    }
  };
}])

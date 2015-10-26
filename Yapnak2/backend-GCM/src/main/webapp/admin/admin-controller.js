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

  $scope.gotDetails = "";

  $scope.clientList = [];

  $scope.clientData = {};
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


  $scope.modal = "";

  $scope.photoUrl = "";

  var offer1Changed;
  var offer2Changed;
  var offer3Changed;
  var offer1Active;
  var offer2Active;
  var offer3Active;
  var email;
  $scope.clientData.myImage = "";
  $scope.clientData.myCroppedImage = "";

  $scope.masterkey = "";
  $scope.generateMasterkey = function() {
    webfactory.generateMasterkey($scope.clientData.id).then(function(response) {
      $scope.masterkey = response.masterkey;
    });
  }

  $scope.changeOffers = function() {

    for (var i = 0; i < $scope.clientData.offers.length; i++) {
      if ($scope.clientData.offer1text.offerId == $scope.clientData.offers[i].offerId) {

        $scope.clientData.offer1StartTime = $scope.offerTimes[($scope.clientData.offers[i].offerStart - 5) % 24];
        $scope.clientData.offer1EndTime = $scope.offerTimes[($scope.clientData.offers[i].offerEnd - 5) % 24];
        $scope.parseOfferDays($scope.offer1Days,$scope.clientData.offers[i].offerDays);

      } else if ($scope.clientData.offer2text.offerId == $scope.clientData.offers[i].offerId) {

        $scope.clientData.offer2StartTime = $scope.offerTimes[($scope.clientData.offers[i].offerStart - 5) % 24];
        $scope.clientData.offer2EndTime = $scope.offerTimes[($scope.clientData.offers[i].offerEnd - 5) % 24];
        $scope.parseOfferDays($scope.offer2Days,$scope.clientData.offers[i].offerDays);

      } else if ($scope.clientData.offer3text.offerId == $scope.clientData.offers[i].offerId) {

        $scope.clientData.offer3StartTime = $scope.offerTimes[($scope.clientData.offers[i].offerStart - 5) % 24];
        $scope.clientData.offer3EndTime = $scope.offerTimes[($scope.clientData.offers[i].offerEnd - 5) % 24];
        //$scope.parseOfferDays($scope.offer3Days,$scope.offers[i].offerDays);
      }
    }
  }

  $scope.parseOfferDays = function(offer,data) {
    for (var i = 0; i < data.length; i++) {
      offer[i].active = data[i];
    }
  }

  $scope.uploadFile = function(){
    var clientId = detailsfactory.getclientId();
    var file = detailsfactory.getFile();
    console.log('file is ' );
    console.dir(file);
    webfactory.getUploadUrl(clientId).then(function(response){
      console.log(response);
      var uploadUrl = response.uploadUrl;
      webfactory.uploadFileToUrl(file, uploadUrl).then(function(response) {
        $scope.retrieveClient();
      }, function(error) {
        $scope.retrieveClient();
      });
    })
  }

  $scope.closeModal= function() {
    $scope.modal.close();
  };

  $scope.retrieveClient = function() {
    //Retrieve client details
    webfactory.retrieveClient($scope.chosenClient.clientId).then(function(response) {
      if (response !== -1) {
        $scope.clientData = response;
        $scope.photoUrl = $scope.clientData.photo;
        detailsfactory.setClientId($scope.clientData.id);
        //Retrieve all client offers
        webfactory.getOffers($scope.chosenClient.clientId).then(function(response) {
          $scope.clientData.offers = response;
          $scope.clientData.offers.splice(0,0,{offerId:0, offerText:"\"New Offer\""});
          for (var i = 0; i < $scope.clientData.offers.length; i++) {
            if ($scope.clientData.offer1Id == $scope.clientData.offers[i].offerId) {
              $scope.clientData.offer1text = $scope.clientData.offers[i];
              offer1Changed = $scope.clientData.offers[i];
              $scope.parseOfferDays($scope.offer1Days,$scope.clientData.offers[i].offerDays);
            } else if ($scope.clientData.offer2Id == $scope.clientData.offers[i].offerId) {
              $scope.clientData.offer2text = $scope.clientData.offers[i];
              offer2Changed = $scope.clientData.offers[i];
              $scope.parseOfferDays($scope.offer2Days,$scope.clientData.offers[i].offerDays);
            } else if ($scope.clientData.offer3Id == $scope.clientData.offers[i].offerId) {
              $scope.clientData.offer3text = $scope.clientData.offers[i];
              offer3Changed = $scope.clientData.offers[i];
              //$scope.parseOfferDays($scope.offer3Days,$scope.clientData.offers[i].offerDays);
            }
          }
          $scope.clientData.offers.splice(1,3);
          $scope.changeOffers();
        });

        $scope.gotDetails = "client";
        $scope.clientData.locationText = $scope.clientData.x + " " + $scope.clientData.y;
        $scope.clientData.myImage = "";
        $scope.isActive = $scope.clientData.isActive == 1 ? true : false;
        $scope.clientData.offer1Shown = $scope.clientData.showOffer1 == 1 ? true : false;
        $scope.clientData.offer2Shown = $scope.clientData.showOffer2 == 1 ? true : false;
        $scope.clientData.offer3Shown = $scope.clientData.showOffer3 == 1 ? true : false;
        offer1Active = $scope.clientData.offer1Shown;
        offer2Active = $scope.clientData.offer2Shown;
        offer3Active = $scope.clientData.offer3Shown;
        $scope.clientData.newLocation = "";
        $scope.clientData.newName = "";
        $scope.clientData.newFoodStyle = "";
        $scope.clientData.newOffer1text = "";
        $scope.clientData.newOffer2text = "";
        $scope.clientData.newOffer3text = "";
        email = $scope.clientData.email;
      }
    });
  };

  var details = function() {
    var modal = $modal.open({
      animation: true,
      controller: 'modal-controller',
      templateUrl: 'admin/templates/updated-modal.html'
    });
    $timeout(function() {
      console.log("closed");
      modal.close();
    }, 2000);
    $scope.retrieveClient();
  };

  $scope.toggleOn = function() {
    console.log("toggling on");
    $scope.isActive = !$scope.isActive;
    webfactory.toggleClient($scope.clientData.id, 1).then(function() {
      //Modal success?
      $scope.retrieveClient();
    });
  };

  $scope.getEditList = function() {
    return ($scope.editList === "") ? "No Details Changed" : $scope.editList;
  };

  $scope.toggleOff = function() {
    console.log("toggling off");
    $scope.isActive = !$scope.isActive;
    webfactory.toggleClient($scope.clientData.id, 0).then(function() {
      //Modal success?
      $scope.retrieveClient();
    });
  };

  $scope.updateInfo = function() {
    $scope.editList = "";

    if ($scope.clientData.newLocation !== "") {
      $scope.editList += "Client Address to " + $scope.clientData.newLocation + " | ";
    }

    if ($scope.clientData.newFoodStyle !== "") {
      $scope.editList += "Food Style to " + $scope.clientData.newFoodStyle + " | ";
    }

    if ($scope.clientData.newName !== "") {
      $scope.editList += "Client Name to " + $scope.clientData.newName + " | ";
    }

    //Check if offer 1 active state has changed
    if ($scope.clientData.offer1Shown !== offer1Active) {
      if ($scope.clientData.offer1Shown === false) {
        //Toggle off
        $scope.editList += "Offer 1 OFF | ";
      } else {
        //Toggle on
        $scope.editList += "Offer 1 ON | ";
      }
    }

    //Check if offer 1 offer has changed
    if ($scope.clientData.offer1text.offerId !== offer1Changed.offerId) {
      //Check if a new offer is being submitted
      if ($scope.clientData.offer1text.offerId === 0) {
        if ($scope.clientData.newOffer1text !== "") {
          $scope.editList += "A new offer 1:" + $scope.newOffer1text + " | ";
        } else {
          $scope.editList += "A BLANK offer 1 (This will not be updated, please add text or revert back to original offer) | ";
        }
      }
      //Replace the current offer with the old offer
      else {
        $scope.editList += "Replacing offer 1 with: " + $scope.clientData.newOffer1text + " | ";
      }
    }

    //Check if offer 2 active state has changed
    if ($scope.clientData.offer2Shown !== offer2Active) {
      if ($scope.clientData.offer2Shown === false) {
        //Toggle off
        $scope.editList += "Offer 2 OFF | ";
      } else {
        //Toggle on
        $scope.editList += "Offer 2 ON | ";
      }
    }

    //Check if offer 2 offer has changed
    if ($scope.clientData.offer2text.offerId !== offer2Changed.offerId) {
      //Check if a new offer is being submitted
      if ($scope.clientData.offer2text.offerId === 0) {
        if ($scope.clientData.newOffer2text !== "") {
          $scope.editList += "A new offer 2:" + $scope.newOffer2text + " | ";
        } else {
          $scope.editList += "A BLANK offer 2 (This may not be updated, please add text or revert back to original offer) | ";
        }
      }
      //Replace the current offer with the old offer
      else {
        $scope.editList += "Replacing offer 2 with: " + $scope.clientData.newOffer2text + " | ";
      }
    }

    //Check if offer 3 active state has changed
    if ($scope.clientData.offer3Shown !== offer3Active) {
      if ($scope.clientData.offer3Shown === false) {
        //Toggle off
        $scope.editList += "Offer 3 OFF | ";
      } else {
        //Toggle on
        $scope.editList += "Offer 3 ON | ";
      }
    }

    //Check if offer 3 offer has changed
    if ($scope.clientData.offer3text.offerId !== offer3Changed.offerId) {
      //Check if a new offer is being submitted
      if ($scope.clientData.offer3text.offerId === 0) {
        if ($scope.clientData.newOffer3text !== "") {
          $scope.editList += "A new offer 3:" + $scope.newOffer3text + " | ";
        } else {
          $scope.editList += "A BLANK offer 3 (This may not be updated, please add text or revert back to original offer) | ";
        }
      }
      //Replace the current offer with the old offer
      else {
        $scope.editList += "Replacing offer 3 with: " + $scope.clientData.newOffer3text + " | ";
      }
    }
    ////Check for changed details
    $scope.modal = $modal.open({
      animation: true,
      templateUrl: 'admin/templates/confirm-edit-modal.html',
      scope: $scope
    });
  };

  $scope.getLocation = function(val) {
    return webfactory.getLocations(val).then(function(response) {
      return response.data.results.map(function(item){
        console.log(item.formatted_address);
        return item.formatted_address;
      });
    });
  };

  $scope.confirmUpdate = function() {
    $scope.modal.close();
    if((($scope.clientData.offer3text.offerId == $scope.clientData.offer2text.offerId) && $scope.clientData.offer3text.offerId !== 0) || (($scope.clientData.offer3text.offerId == $scope.clientData.offer1text.offerId) && $scope.clientData.offer3text.offerId !== 0) || (($scope.clientData.offer1text.offerId == $scope.clientData.offer2text.offerId) && $scope.clientData.offer2text.offerId !== 0)){
      $modal.open({
        animation: true,
        controller: 'modal-controller',
        templateUrl: 'admin/templates/same-offer-chosen-modal.html'
      });
    } else {
      var counter = 13;
      $scope.editList = "";

      if ($scope.clientData.newLocation !== "") {
        webfactory.updateLocation($scope.clientData.newLocation,email).then(function(response) {
          counter -= 1;
          if (counter === 0) {
            details();
          }
        });
      } else {
        counter -= 1;
      }

      if ($scope.clientData.newFoodStyle !== "") {
        webfactory.updateType($scope.clientData.newFoodStyle,email).then(function(response) {
          counter -= 1;
          if (counter === 0) {
            details();
          }
        });
      } else {
        counter -= 1;
      }

      if ($scope.clientData.newName !== "")  {
        webfactory.updateName($scope.clientData.newName,email).then(function(response) {
          counter -= 1;
          if (counter === 0) {
            details();
          }
        });
      } else {
        counter -=1 ;
      }

      //Check if offer 1 active state has changed
      if ($scope.clientData.offer1Shown !== offer1Active) {
        if ($scope.clientData.offer1Shown === false) {
          //Toggle off
          webfactory.toggleOffer(email, 1, 0).then(function(){
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        } else {
          //Toggle on
          webfactory.toggleOffer(email, 1, 1).then(function(){
            counter -= 1;
            if (counter === 0) {
              details(1);
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 1 offer has changed
      if ($scope.clientData.offer1text.offerId !== offer1Changed.offerId) {
        //Check if a new offer is being submitted
        if ($scope.clientData.offer1text.offerId === 0) {
          if ($scope.clientData.newOffer1text !== "") {
            webfactory.insertOffer(email,1,$scope.clientData.newOffer1text).then(function(response) {
              webfactory.toggleOffer(email, 1, 1).then(function() {
                counter -= 1;
                if (counter === 0) {
                  details();
                }
              });
            });
          } else {
            $modal.open({
              animation: true,
              controller: 'modal-controller',
              templateUrl: 'admin/templates/blank-offer-modal.html'
            });
          }
        }
        //Replace the current offer with the old offer
        else {
          webfactory.replaceOffer(email,offer1Changed.offerId,$scope.clientData.offer1text.offerId,1).then(function() {
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 2 active state has changed
      if ($scope.clientData.offer2Shown !== offer2Active)  {
        if ($scope.clientData.offer2Shown === false) {
          //Toggle off
          webfactory.toggleOffer(email, 2, 0).then(function(){
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        } else {
          //Toggle on
          webfactory.toggleOffer(email, 2, 1).then(function(){
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 2 offer has changed
      if ($scope.clientData.offer2text.offerId !== offer2Changed.offerId) {
        //Check if a new offer is being submitted
        if ($scope.clientData.offer2text.offerId === 0) {
          if ($scope.clientData.newOffer2text !== "") {
            webfactory.insertOffer(email,2,$scope.clientData.newOffer2text).then(function(response) {
              webfactory.toggleOffer(email, 2, 1).then(function() {
                counter -= 1;
                if (counter === 0) {
                  details();
                }
              });
            });
          } else {
            $modal.open({
              animation: true,
              controller: 'modal-controller',
              templateUrl: 'admin/templates/blank-offer-modal.html'
            });
          }
        }
        //Replace the current offer with the old offer
        else {
          webfactory.replaceOffer(email,offer2Changed.offerId,$scope.clientData.offer2text.offerId,2).then(function() {
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 3 active state has changed
      if ($scope.clientData.offer3Shown !== offer3Active) {
        if ($scope.clientData.offer3Shown === false) {
          //Toggle off
          webfactory.toggleOffer(email, 3, 0).then(function(){
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        } else {
          //Toggle on
          webfactory.toggleOffer(email, 3, 1).then(function(){
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Check if offer 3 offer has changed
      if ($scope.clientData.offer3text.offerId !== offer3Changed.offerId) {
        //Check if a new offer is being submitted
        if ($scope.clientData.offer3text.offerId === 0) {
          if ($scope.clientData.newOffer3text !== "") {
            webfactory.insertOffer(email,3,$scope.newOffer3text).then(function(response) {
              webfactory.toggleOffer(email, 3, 1).then(function() {
                counter -= 1;
                if (counter === 0) {
                  details();
                }
              });
            });
          } else {
            $modal.open({
              animation: true,
              controller: 'modal-controller',
              templateUrl: 'admin/templates/blank-offer-modal.html'
            });
          }
        }
        //Replace the current offer with the old offer
        else {
          webfactory.replaceOffer(email,offer3Changed.offerId,$scope.clientData.offer3text.offerId,3).then(function() {
            counter -= 1;
            if (counter === 0) {
              details();
            }
          });
        }
      } else {
        counter -= 1;
      }

      //Change offer 1 hours
      if (($scope.clientData.offer1EndTime.time - $scope.clientData.offer1StartTime.time) <= 0) {
        counter -= 1;
        $modal.open({
          animation: true,
          controller: 'modal-controller',
          templateUrl: 'admin/templates/offer1-times-invalid-modal.html'
        });
      } else {
        webfactory.updateOfferHours(email, $scope.clientData.offer1text.offerId, $scope.clientData.offer1StartTime.time, $scope.clientData.offer1EndTime.time).then(function(response) {
          counter -= 1;
          if (counter == 0) {
            details(1);
          }
        }, function(error) {
          counter -= 1;
        });
      }

      //Change offer 2 hours
      if (($scope.clientData.offer2EndTime.time - $scope.clientData.offer2StartTime.time) <= 0) {
        counter -= 1;
        $modal.open({
          animation: true,
          controller: 'modal-controller',
          templateUrl: 'modules/templates/offer2-times-invalid-modal.html'
        });
      } else {
        webfactory.updateOfferHours(email, $scope.clientData.offer2text.offerId, $scope.clientData.offer2StartTime.time, $scope.clientData.offer2EndTime.time).then(function(response) {
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
      // if (($scope.clientData.offer3EndTime.time - $scope.clientData.offer3StartTime.time) <= 0) {
      //   counter -= 1;
      //   $modal.open({
      //     animation: true,
      //     controller: 'modal-controller',
      //     templateUrl: 'modules/templates/offer3-times-invalid-modal.html'
      //   });
      // } else {
      //   webfactory.updateOfferHours(email, $scope.clientData.offer3text.offerId, $scope.clientData.offer3StartTime.time, $scope.clientData.offer3EndTime.time).then(function(response) {
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
      webfactory.updateOfferDays(email,$scope.clientData.offer1text.offerId,"[" + offer1DayString + "]").then(function() {
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
      webfactory.updateOfferDays(email,$scope.clientData.offer2text.offerId,"[" + offer2DayString + "]").then(function() {
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
      // webfactory.updateOfferDays(email,$scope.clientData.offer3text.offerId,"[" + offer3DayString + "]").then(function() {
      //   counter -= 1;
      //   if (counter == 0) {
      //     details(1);
      //   }
      // });

    } //if/else
  }; //confirm update

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

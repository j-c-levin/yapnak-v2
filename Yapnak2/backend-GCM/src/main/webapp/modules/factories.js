angular.module('app.factories', [])

.factory('webfactory', ['$http', function ($http) {
  var result = {};

  result.submit = function (userID, clientEmail) {

    var data = {
      userID: userID, clientEmail: clientEmail
    }

    return $http.post('https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/getUser?userID='.concat(data.userID).concat('&clientEmail=').concat(data.clientEmail)).then(function (response) {
      return response.data;
    }, function (error) {
      return error;
    })
  };

  result.forgot = function (email) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/forgotLogin',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      transformRequest: function (obj) {
        var str = [];
        for (var p in obj)
        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        return str.join("&");
      },
      data: {
        email: email
      }
    }
    return $http(req).then(function (response) {

      return response.data.status;

    }, function (error) {
      return error;
    })
  };

  result.reset = function (password, hash) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/resetPassword',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      transformRequest: function (obj) {
        var str = [];
        for (var p in obj)
        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        return str.join("&");
      },
      data: {
        password: password,
        hash: hash
      }
    }
    return $http(req).then(function (response) {
      return response.data.status;
    }, function (error) {
      console.log(error.data.message);
      return error.data.status;
    });
  };

  result.userReset = function(password,hash) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/userEndpointApi/v1/resetPassword',
      // url: 'http://localhost:8080/_ah/api/userEndpointApi/v1/resetPassword',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      transformRequest: function (obj) {
        var str = [];
        for (var p in obj)
        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        return str.join("&");
      },
      data: {
        password: password,
        hash: hash
      }
    }
    return $http(req).then(function (response) {
      return response.data.status;
    }, function (error) {
      console.log(error.data.message);
      return error.data.status;
    });
  };

  result.getInfo = function(email) {
    var req = {
      method: 'GET',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/getClientInfo?email='.concat(email)
      // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/getClientInfo?email='.concat(email)
    }
    return $http(req).then(function (response) {
      if (response.data.status == "True") {
        console.log("Got information");
        console.log(response.data);
        return response.data;
      } else {
        console.log("Failed");
        console.log(response);
        return response;
      }
    },function (error) {
      console.log("failed");
      console.log(error);
      return error;
    });
  }

  result.getOffers = function(clientId) {
    var req = {
      method: 'GET',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/getAllOffers?clientId='.concat(clientId)
      // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/getAllOffers?clientId='.concat(clientId)
    }
    return $http(req).then(function (response) {
      if (response.data.status == "True") {
        console.log("Got offers list");
        console.log(response.data);
        return response.data.offerList;
      } else {
        console.log("Failed");
        console.log(response);
        return response;
      }
    },function (error) {
      console.log("failed");
      console.log(error);
      return error;
    });
  }

  result.updateName = function(name,email) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateClientName?email='.concat(email).concat("&name=").concat(name)
      //          url: 'http://localhost:8080/_ah/api/sQLEntityApi/v1/updateClientName?email='.concat(email).concat("&name=").concat(name)
    }
    return $http(req).then(function (response) {
      if (response.data.status == "True") {
        console.log("Successfully updated Name");
      } else {
        console.log("Failed");
        console.log(response);
      }
    },function (error) {
      console.log("failed");
      console.log(error);
    });
  }

  result.updateType = function(type,email) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateClientType?email='.concat(email).concat("&type=").concat(type)
      //      url: 'http://localhost:8080/_ah/api/sQLEntityApi/v1/updateClientType?email='.concat(email).concat("&type=").concat(type)
    }
    return $http(req).then(function (response) {
      if (response.data.status == "True") {
        console.log("Successfully updated Type");
      } else {
        console.log("Failed");
        console.log(response);
      }
    },function (error) {
      console.log("failed");
      console.log(error);
    });
  };

  result.updateMainText = function(name,type,email) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateClientInfo?email='.concat(email).concat("&name=").concat(name).concat("&type=").concat(type)
    }
    return $http(req).then(function (response) {
      if (response.data.status == "True") {
        console.log("Successfully updated main information");
      } else {
        console.log("Failed");
        console.log(response);
      }
    },function (error) {
      console.log("failed");
      console.log(error);
    });
  };

  result.updateLocation = function(address,email) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateClientLocation?email='.concat(email).concat("&address=").concat(encodeURIComponent(address))
    }
    return $http(req).then(function(response){
      if (response.data.status == "True") {
        console.log("successfully updated location");
        console.log(response);
      } else {
        console.log("location update failed");
        console.log(response);
      }
    }, function(error) {
      console.log("Location update went wrong somewhere");
      console.log(error);
    })
  };

  result.toggleOffer = function(email,offer,value) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/toggleOffer?email='.concat(email).concat("&offer=").concat(offer).concat("&value=").concat(value)
    }
    return $http(req).then(function(response){
      if (response.data.status == "True") {
        console.log("successfully updated offer status");
        console.log(response);
      } else {
        console.log("failed to update offer status");
        console.log(response);
      }
    }, function(error) {
      console.log("Offer toggle update went wrong somewhere");
      console.log(error);
    })
  };

  result.updateOffer = function(email,offer,text) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateOffer?email='.concat(email).concat("&offer=").concat(offer).concat("&text=").concat(text)
    }
    return $http(req).then(function(response){
      if (response.data.status == "True") {
        console.log("successfully updated offer text");
        console.log(response);
      } else {
        console.log("failed to update offer text");
        console.log(response);
      }
    }, function(error) {
      console.log("Offer text update went wrong somewhere");
      console.log(error);
    })
  };

  result.replaceOffer = function(email,currentOffer,newOffer,position) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/replaceActiveOffer?email='.concat(email).concat('&currentOfferId=').concat(currentOffer).concat('&newOfferId=').concat(newOffer).concat('&offerPosition=').concat(position)
      // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/replaceActiveOffer?email='.concat(email).concat('&currentOfferId=').concat(currentOffer).concat('&newOfferId=').concat(newOffer).concat('&offerPosition=').concat(position)
    }
    return $http(req).then(function(response){
      if (response.data.status == "True") {
        console.log("successfully replaced offer");
        console.log(response);
      } else {
        console.log("failed to replace offer");
        console.log(response);
      }
    }, function(error) {
      console.log("offer replacement went wrong somewhere");
      console.log(error);
    })
  };

  result.insertOffer = function(email,offer,text) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/insertOffer?email='.concat(email).concat("&offer=").concat(offer).concat("&text=").concat(encodeURIComponent(text))
    }
    return $http(req).then(function(response){
      if (response.data.status == "True") {
        console.log("successfully inserted new offer");
        console.log(response);
      } else {
        console.log("failed to update offer text");
        console.log(response);
      }
    }, function(error) {
      console.log("Offer text update went wrong somewhere");
      console.log(error);
    })
  };

  result.updateOfferHours = function(email, offerId, offerStart, offerEnd) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/changeOfferHours?email='.concat(email).concat("&offerId=").concat(offerId).concat("&offerStart=").concat(offerStart).concat("&offerEnd=").concat(offerEnd)
      // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/changeOfferHours?email='.concat(email).concat("&offerId=").concat(offerId).concat("&offerStart=").concat(offerStart).concat("&offerEnd=").concat(offerEnd)
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Offer hours updated success");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED Offer hours update");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED Offer hours update");
      console.log(error);
      return -1
    });
  }

  result.updateOfferDays = function(email,offerId, days) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/changeOfferDays?email='.concat(email).concat("&offerId=").concat(offerId).concat("&days=").concat(days)
      // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/changeOfferDays?email='.concat(email).concat("&offerId=").concat(offerId).concat("&days=").concat(days)
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Offer days update success");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED Offer days update");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED Offer days update");
      console.log(error);
      return -1
    });

  }

  result.getLocations = function(val) {
    return $http.get('https://maps.googleapis.com/maps/api/geocode/json', {
      params: {
        address: val,
        sensor: false
      }
    }).then(function(response){
      return response;
    });
  };

  result.getUploadUrl = function(clientId) {
    var req = {
      method: 'GET',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/photoUpload?clientId='.concat(clientId)
      // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/photoUpload?clientId='.concat(clientId)
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Retrieved upload url success");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED Retrieved upload url");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED Retrieved upload url");
      console.log(error);
      return -1
    });

  }

  result.getImageUploadUrl = function(offerId) {
    var req = {
      method: 'GET',
      url: 'https://yapnak-app.appspot.com/_ah/api/clientEndpointApi/v1/offerPhotoUpload?offerId='.concat(offerId)
//       url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/offerPhotoUpload?offerId='.concat(offerId)
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Retrieved upload url success");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED Retrieved upload url");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED Retrieved upload url");
      console.log(error);
      return -1
    });

  }

  result.uploadFileToUrl = function(file, uploadUrl){
    var fd = new FormData();
    fd.append('image', file);
    return $http.post(uploadUrl, fd, {
      transformRequest: angular.identity,
      headers: {'Content-Type': undefined},
      options: {
        withCredentials: true
      },
    })
    .success(function(){
      console.log("success uploading image");
    })
    .error(function(){
      console.log("failure uploading image");
    });
  }

  return result;
}])

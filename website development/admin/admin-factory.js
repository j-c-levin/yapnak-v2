angular.module('app.factories', [])

.factory('detailsfactory', [function() {
  var result = {};

  var session = "";

  var clientId = "";

  var file = "";

  result.setSession = function(details) {
    session = details;
  }

  result.getSession = function() {
    return session;
  }

  result.setClientId = function(details) {
    clientId = details;
  }

  result.getclientId = function() {
    return clientId;
  }

  result.setFile = function(details) {
    file = details;
  }

  result.getFile = function() {
    return file;
  }

  return result;
}])

.factory('webfactory', ['$http','detailsfactory', function($http,detailsfactory){
  var result = {};

  result.login = function(data) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/adminApi/v1/adminLogin?email='.concat(data.email).concat("&password=").concat(data.password)
      // url: 'http://localhost:8080/_ah/api/adminApi/v1/adminLogin?email='.concat(data.email).concat("&password=").concat(data.password)
    }
    return $http(req).then(function(response){
      if (response.data.status == "True") {
        console.log("admin login success");
        console.log(response);
        return response.data;
      } else {
        console.log("admin login FAILED");
        console.log(response);
        return -1;
      }
    }, function(error){
      console.log("admin login FAILED");
      console.log(error);
      return -1;
    })
  };

  result.getAllClients = function() {
    var req = {
      method: 'GET',
      url: 'https://yapnak-app.appspot.com/_ah/api/adminApi/v1/getAllClients'
      // url: 'http://localhost:8080/_ah/api/adminApi/v1/getAllClients'
    }
    console.log(req);
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Retrieved clients");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED retrieving clients");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED retrieving clients");
      console.log(error);
      return -1
    })
  };

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

  result.retrieveClient = function(clientId) {
    var req = {
      method: 'GET',
      url: 'https://yapnak-app.appspot.com/_ah/api/adminApi/v1/getClientInfo?clientId='.concat(clientId)
      // url: 'http://localhost:8080/_ah/api/adminApi/v1/getClientInfo?clientId='.concat(clientId)
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Retrieved client data");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED retrieving client data");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED retrieving client data");
      console.log(error);
      return -1
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

  result.updateName = function(name,email) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateClientName?email='.concat(email).concat("&name=").concat(encodeURIComponent(name))
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
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/updateClientType?email='.concat(email).concat("&type=").concat(encodeURIComponent(type))
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

  result.toggleClient = function(clientId,value) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/adminApi/v1/toggleClient?clientId='.concat(clientId).concat("&value=").concat(value).concat("&session=").concat(detailsfactory.getSession())
      // url: 'http://localhost:8080/_ah/api/adminApi/v1/toggleClient?clientId='.concat(clientId).concat("&value=").concat(value).concat("&session=").concat(detailsfactory.getSession())
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Toggled client success");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED Toggled client");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED Toggled client");
      console.log(error);
      return -1
    });

  }

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

  result.toggleOffer = function(email,offer,value) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/sQLEntityApi/v1/toggleOffer?email='.concat(email).concat("&offer=").concat(offer).concat("&value=").concat(value)
    }
    return $http(req).then(function(response) {
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
        // url: 'http://localhost:8080/_ah/api/clientEndpointApi/v1/offerPhotoUpload?offerId='.concat(offerId)
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
    }).then(function(response) {
      console.log("success uploading image");
      console.log(response);
    }, function(error) {
      console.log("failure uploading image");
      console.log(error);
    })
  }

  result.generateMasterkey = function(clientId) {
    var req = {
      method: 'POST',
      url: 'https://yapnak-app.appspot.com/_ah/api/adminApi/v1/generateMasterkey?clientId='.concat(clientId)
      // url: 'http://localhost:8080/_ah/api/adminApi/v1/generateMasterkey?clientId='.concat(clientId)
    }
    return $http(req).then(function(response) {
      if (response.data.status == "True") {
        console.log("Generated masterkey");
        console.log(response);
        return response.data;
      } else {
        console.log("FAILED generating masterkey");
        console.log(response);
        return -1
      }
    }, function(error){
      console.log("REALLY FAILED generating masterkey");
      console.log(error);
      return -1
    });

  }

  return result;
}])

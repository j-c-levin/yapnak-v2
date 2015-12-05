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
  <script src="https://angular-ui.github.io/bootstrap/ui-bootstrap-tpls-0.13.3.min.js"></script>
  <script src="app.js"></script>
  <script src="modules/factories.js"></script>

  <!--bootstrap-->
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <link rel="stylesheet" href="css/index.css">

  <link href="css/index.css" rel="stylesheet">

  <!-- ngImgCrop -->
  <!-- <script src="lib/ngImgCrop/0.3.2/ng-img-crop.js"></script>
  <link rel="stylesheet" type="text/css" href="lib/ngImgCrop/0.3.2/ng-img-crop.css"> -->

  <!-- ngImgCropExtended -->
  <script src="lib/ngImgCropExt/b4f70e5/ng-img-crop.js"></script>
  <link rel="stylesheet" type="text/css" href="lib/ngImgCropExt/b4f70e5/ng-img-crop.css">

</head>

<body class="content container" ng-controller="client-controller">

  <div class="container crop-container">

    <div class="crop-area" ng-show="myImage !== ''">
      <img-crop image="myImage" result-image="myCroppedImage" area-type="square" result-image-size="{w: 200,h: 200}"></img-crop>
    </div>

    <img height="100" ng-src="{{image}}" ng-show="myImage == ''" class="current-image">

    <div class="crop-selector">
      <div>
        Select an image file:
        <input type="file" id="fileInput" />
      </div>
    </div>

    <button ng-show="myImage !== ''" class="btn btn-default image-upload" ng-click="uploadFile()">Update image</button>

  </div>

  <div class="form-signin">

    <label
    for="exampleInputEmail1">Restaurant Name</label>
    <input type="text"
    class="form-control" name="name" ng-model="newName" id="name" placeholder="{{name}}">
  </div>

  <div class="form-signin">
    <label for="exampleInputPassword1">Restaurant Type</label>
    <input type="text" class="form-control" ng-model="newFoodStyle" name="type" id="type"
    placeholder="{{foodStyle}}">
  </div>

  <div class="form-signin">
    <label
    for="exampleInputPassword1">Address</label>

    <input type="text" ng-model="newLocation" placeholder="{{location}}" typeahead="address for address in getLocation($viewValue)" typeahead-loading="loadingLocations" typeahead-no-results="noResults" typeahead-min-length="5" class="form-control">
    <i ng-show="loadingLocations" class="glyphicon glyphicon-refresh"></i>
    <div ng-show="noResults">
      <i class="glyphicon glyphicon-remove"></i> No Results Found
    </div>

  </div>

  <div class="form-signin">

    <label>
      <input type="checkbox" name = "show-offer"
      value="show-offer" ng-model="offer1" ng-click="showOffer()"> Offer one
    </label>

    <hr>
    <div collapse="!offer1">

      <select class="form-control" ng-model="offer1text" ng-options="offer.offerText for offer in offers" ng-change="changeOffers()"></select>

      <input ng-show="offer1text.offerId == 0" type="text"
      class="form-control" maxlength="40" ng-model="newOffer1text" name="deal" id="deal" placeholder="Type a new offer in here">

      <label>Days active</label>
      <ul>
        <li ng-repeat="day in offer1Days">
          <label><input ng-model="day.active" type="checkbox" value=""> {{day.humanDay}}</label>
        </li>
      </ul>

      <label>Offer start</label>
      <select class="form-control" ng-model="offer1StartTime" ng-options="time.humanHour for time in offerTimes"></select>

      <label>Offer end</label>
      <select class="form-control" ng-model="offer1EndTime" ng-options="time.humanHour for time in offerTimes"></select>

      <div class="">
        <div class="crop-area" ng-show="offer1Image !== ''">
          <img-crop image="offer1Image" result-image="offer1ImageCrop" area-type="rectangle" aspect-ratio="2" result-image-size="{w: 600,h: 300}" chargement="'Loading...'"></img-crop>
        </div>
        <img height="135" ng-src="{{currentOffer1Image}}" ng-show="offer1Image == ''" class="offer-photo">
        <div class="crop-selector">
          <div>
            Select an image for this offer:
            <input type="file" id="offer1Input" />
          </div>
        </div>

        <button ng-show="offer1Image !== ''" class="btn btn-default image-upload" ng-click=" uploadOfferImage(allData.offer1Id,offer1ImageCrop)">Update image</button>

        <div ng-show="progress == 'began'">
          Preparing upload
        </div>
        <div ng-show="progress == 'ready'">
          Upload in progress
        </div>
        <div ng-show="progress == 'success'">
          Upload success.
        </div>
        <div ng-show="progress == 'fail'">
          Upload failed.
        </div>

      </div>

    </div>
  </div>

  <div class="form-signin">

    <label>
      <input type="checkbox" name = "show-offer"
      value="show-offer" ng-model="offer2" ng-click="showOffer()"> Offer two
    </label>

    <hr>
    <div collapse="!offer2">

      <select class="form-control" ng-model="offer2text" ng-options="offer.offerText for offer in offers" ng-change="changeOffers()"></select>

      <input ng-show="offer2text.offerId == 0" type="text"
      class="form-control" maxlength="40" ng-model="newOffer2text" name="deal" id="deal" placeholder="Type a new offer in here">

      <label>Days active</label>
      <ul>
        <li ng-repeat="day in offer2Days">
          <label><input ng-model="day.active" type="checkbox" value=""> {{day.humanDay}}</label>
        </li>
      </ul>

      <label>Offer start</label>
      <select class="form-control" ng-model="offer2StartTime" ng-options="time.humanHour for time in offerTimes"></select>

      <label>Offer end</label>
      <select class="form-control" ng-model="offer2EndTime" ng-options="time.humanHour for time in offerTimes"></select>

      <div class="">
        <div class="crop-area" ng-show="offer2Image !== ''">
          <img-crop image="offer2Image" result-image="offer2ImageCrop" area-type="rectangle" aspect-ratio="2" result-image-size="{w: 600,h: 300}" chargement="'Loading...'"></img-crop>
        </div>
        <img height="135" ng-src="{{currentOffer2Image}}" ng-show="offer2Image == ''" class="offer-photo">
        <div class="crop-selector">
          <div>
            Select an image for this offer:
            <input type="file" id="offer2Input" />
          </div>
        </div>
        <button ng-show="offer2Image !== ''" class="btn btn-default image-upload" ng-click="uploadOfferImage(allData.offer2Id, offer2ImageCrop)">Update image</button>

        <div ng-show="progress == 'began'">
          Preparing upload
        </div>
        <div ng-show="progress == 'ready'">
          Upload in progress
        </div>
        <div ng-show="progress == 'success'">
          Upload success.
        </div>
        <div ng-show="progress == 'fail'">
          Upload failed.
        </div>

      </div>

    </div>
  </div>

  <div class="form-signin">

    <label>
      <input type="checkbox" name = "show-offer"
      value="show-offer" ng-model="offer3" ng-click="showOffer()" ng-disabled="!offer3Enabled"> Offer three
    </label>

    <hr>
    <div collapse="!offer3">

      <select class="form-control" ng-model="offer3text" ng-options="offer.offerText for offer in offers" ng-change="changeOffers()"></select>

      <input ng-show="offer3text.offerId == 0" type="text"
      class="form-control" maxlength="40" ng-model="newOffer3text" name="deal" id="deal" placeholder="Type a new offer in here">

      <label>Days active</label>
      <ul>
        <li ng-repeat="day in offer3Days">
          <label><input ng-model="day.active" type="checkbox" value=""> {{day.humanDay}}</label>
        </li>
      </ul>

      <label>Offer start</label>
      <select class="form-control" ng-model="offer3StartTime" ng-options="time.humanHour for time in offerTimes"></select>

      <label>Offer end</label>
      <select class="form-control" ng-model="offer3EndTime" ng-options="time.humanHour for time in offerTimes"></select>

      <div class="">
        <div class="crop-area" ng-show="offer3Image !== ''">
          <img-crop image="offer3Image" result-image="offer3ImageCrop" area-type="rectangle" aspect-ratio="2" result-image-size="{w: 600,h: 300}"></img-crop>
        </div>
        <img height="135" ng-src="{{currentOffer3Image}}" ng-show="offer3Image == ''" class="offer-photo">
        <div class="crop-selector">
          <div>
            Select an image for this offer:
            <input type="file" id="offer3Input" />
          </div>
        </div>
        <button ng-show="offer3Image !== ''" class="btn btn-default image-upload" ng-click="uploadOfferImage(allData.offer3Id,offer3ImageCrop)">Update image</button>
        <div ng-show="progress == 'began'">
          Preparing upload
        </div>
        <div ng-show="progress == 'ready'">
          Upload in progress
        </div>
        <div ng-show="progress == 'success'">
          Upload success.
        </div>
        <div ng-show="progress == 'fail'">
          Upload failed.
        </div>
      </div>

    </div>
  </div>


  <div class="form-signin">
    <button class="btn btn-default btn-block" ng-click="updateInfo()">Update
      information
    </button>
  </div>

</body>
</html>

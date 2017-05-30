$(function () {

  ajaxCall();

});

var ajaxCall = function () {
  var ajaxCallBack = {
    success: onSuccess,
    error: onError
  }

  jsRoutes.controllers.HomeController.ajaxCall().ajax(ajaxCallBack);
};

var onSuccess = function (data) {
  alert(data);
}

var onError = function (error) {
  alert(error);
}
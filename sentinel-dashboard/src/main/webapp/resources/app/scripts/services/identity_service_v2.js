var app = angular.module('sentinelDashboardApp');

app.service('IdentityServiceV2', ['$http', function ($http) {

  this.fetchIdentityOfMachine = function (ip, port, searchKey) {
    var param = {
      ip: ip,
      port: port,
      searchKey: searchKey
    };
    return $http({
      url: 'v2/resource/machineResource.json',
      params: param,
      method: 'GET'
    });
  };
  this.fetchClusterNodeOfMachine = function (ip, port, searchKey) {
    var param = {
      ip: ip,
      port: port,
      type: 'cluster',
      searchKey: searchKey
    };
    return $http({
      url: 'v2/resource/machineResource.json',
      params: param,
      method: 'GET'
    });
  };
  this.fetchIdentityOfApp = function (app, searchKey) {
    var param = {
      app: app,
      searchKey: searchKey
    };
    return $http({
      url: 'v2/resource/appResource.json',
      params: param,
      method: 'GET'
    });
  };
  this.fetchClusterNodeOfApp = function (app, searchKey) {
    var param = {
      app: app,
      type: 'cluster',
      searchKey: searchKey
    };
    return $http({
      url: 'v2/resource/appResource.json',
      params: param,
      method: 'GET'
    });
  };
}]);

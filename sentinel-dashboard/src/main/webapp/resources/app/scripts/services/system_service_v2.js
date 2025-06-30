var app = angular.module('sentinelDashboardApp');

app.service('SystemServiceV2', ['$http', function ($http) {
  this.queryMachineRules = function (app) {
    var param = {
      app: app
    };
    return $http({
      url: '/v2/system/rules',
      params: param,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    return $http({
      url: '/v2/system/rule',
      data: rule,
      method: 'POST'
    });
  };

  this.saveRule = function (rule) {
    return $http({
      url: '/v2/system/rule/' + rule.id,
      data: rule,
      method: 'PUT'
    });
  };

  this.deleteRule = function (rule) {
    return $http({
      url: '/v2/system/rule/' + rule.id,
      method: 'DELETE'
    });
  };
}]);

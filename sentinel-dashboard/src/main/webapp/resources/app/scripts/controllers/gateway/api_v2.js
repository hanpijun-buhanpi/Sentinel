var app = angular.module('sentinelDashboardApp');

app.controller('GatewayApiControllerV2', ['$scope', '$stateParams', 'GatewayApiServiceV2', 'ngDialog',
  function ($scope, $stateParams, GatewayApiService, ngDialog) {
    $scope.app = $stateParams.app;

    $scope.apisPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    getApis();
    function getApis() {
      GatewayApiService.queryApis($scope.app).success(
        function (data) {
          if (data.code == 0 && data.data) {
            // To merge rows for api who has more than one predicateItems, here we build data manually
            $scope.apis = [];

            data.data.forEach(function(api) {
              api["predicateItems"].forEach(function (item, index) {
                var newItem = {};
                newItem["id"] = api["id"];
                newItem["app"] = api["app"];
                newItem["ip"] = api["ip"];
                newItem["port"] = api["port"];
                newItem["apiName"] = api["apiName"];
                newItem["pattern"] = item["pattern"];
                newItem["matchStrategy"] = item["matchStrategy"];
                // The itemSize indicates how many rows to merge, by using rowspan="{{api.itemSize}}" in <td> tag
                newItem["itemSize"] = api["predicateItems"].length;
                // Mark the flag of first item to zero, indicates the start row to merge
                newItem["firstFlag"] = index == 0 ? 0 : 1;
                // Still hold the data of predicateItems, in order to bind data in edit dialog html
                newItem["predicateItems"] = api["predicateItems"];
                $scope.apis.push(newItem);
              });
            });

            $scope.apisPageConfig.totalCount = data.data.length;
          } else {
            $scope.apis = [];
            $scope.apisPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getApis = getApis;

    // 外部动态规则源一般存在数据更新延迟，所以这里延迟100ms获取API列表
    function delayGetApis() {
      setTimeout(function () {
        getApis();
      }, 100);
    };

    var gatewayApiDialog;
    $scope.editApi = function (api) {
      $scope.currentApi = angular.copy(api);
      $scope.gatewayApiDialog = {
        title: '编辑自定义 API',
        type: 'edit',
        confirmBtnText: '保存'
      };
      gatewayApiDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/api-dialog.html',
        width: 900,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewApi = function () {
      $scope.currentApi = {
        grade: 0,
        app: $scope.app,
        predicateItems: [{matchStrategy: 0, pattern: ''}]
      };
      $scope.gatewayApiDialog = {
        title: '新增自定义 API',
        type: 'add',
        confirmBtnText: '新增'
      };
      gatewayApiDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/api-dialog.html',
        width: 900,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveApi = function () {
      var apiNames = [];
      if ($scope.gatewayApiDialog.type === 'add') {
        apiNames = $scope.apis.map(function (item, index, array) {
          return item["apiName"];
        }).filter(function (item, index, array) {
          return array.indexOf(item) === index;
        });
      }

      if (!GatewayApiService.checkApiValid($scope.currentApi, apiNames)) {
        return;
      }

      if ($scope.gatewayApiDialog.type === 'add') {
        addNewApi($scope.currentApi);
      } else if ($scope.gatewayApiDialog.type === 'edit') {
        saveApi($scope.currentApi, true);
      }
    };

    function addNewApi(api) {
      GatewayApiService.newApi(api).success(function (data) {
        if (data.code == 0) {
          delayGetApis();
          gatewayApiDialog.close();
        } else {
          alert('新增自定义API失败!' + data.msg);
        }
      });
    };

    function saveApi(api, edit) {
      GatewayApiService.saveApi(api).success(function (data) {
        if (data.code == 0) {
          delayGetApis();
          if (edit) {
            gatewayApiDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('修改自定义API失败!' + data.msg);
        }
      });
    };

    var confirmDialog;
    $scope.deleteApi = function (api) {
      $scope.currentApi = api;
      $scope.confirmDialog = {
        title: '删除自定义API',
        type: 'delete_api',
        attentionTitle: '请确认是否删除如下自定义API',
        attention: 'API名称: ' + api.apiName,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_api') {
        deleteApi($scope.currentApi);
      } else {
        console.error('error');
      }
    };

    function deleteApi(api) {
      GatewayApiService.deleteApi(api).success(function (data) {
        if (data.code == 0) {
          delayGetApis();
          confirmDialog.close();
        } else {
          alert('删除自定义API失败!' + data.msg);
        }
      });
    };

    $scope.addNewMatchPattern = function() {
      var total;
      if ($scope.currentApi.predicateItems == null) {
        $scope.currentApi.predicateItems = [];
        total = 0;
      } else {
        total = $scope.currentApi.predicateItems.length;
      }
      $scope.currentApi.predicateItems.splice(total + 1, 0, {matchStrategy: 0, pattern: ''});
    };

    $scope.removeMatchPattern = function($index) {
      if ($scope.currentApi.predicateItems.length <= 1) {
        // Should never happen since no remove button will display when only one predicateItem.
        alert('至少有一个匹配规则');
        return;
      }
      $scope.currentApi.predicateItems.splice($index, 1);
    };
  }]
);

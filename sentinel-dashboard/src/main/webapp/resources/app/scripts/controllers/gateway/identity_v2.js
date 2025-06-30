var app = angular.module('sentinelDashboardApp');

app.controller('GatewayIdentityControllerV2', ['$scope', '$stateParams', 'IdentityServiceV2',
  'ngDialog', 'GatewayFlowServiceV2', 'GatewayApiServiceV2', 'DegradeServiceV2',
  '$interval', '$location', '$timeout',
  function ($scope, $stateParams, IdentityService, ngDialog,
    GatewayFlowService, GatewayApiService, DegradeService, $interval, $location, $timeout) {

    $scope.app = $stateParams.app;

    $scope.currentPage = 1;
    $scope.pageSize = 16;
    $scope.totalPage = 1;
    $scope.totalCount = 0;
    $scope.identities = [];

    $scope.searchKey = '';
    $scope.ofMachine = true;
    $scope.table = null;

    // 外部动态规则源一般存在数据更新延迟，所以这里延迟100ms进行跳转
    function delayLocationPath(url) {
      setTimeout(function () {
        $location.path(url);
      }, 100);
    };

    function getApiNames() {
      GatewayApiService.queryApis($scope.app).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.apiNames = [];

            data.data.forEach(function (api) {
              $scope.apiNames.push(api["apiName"]);
            });
          }
        });
    }

    var gatewayFlowRuleDialog;
    var gatewayFlowRuleDialogScope;
    $scope.addNewGatewayFlowRule = function (resource) {
      gatewayFlowRuleDialogScope = $scope.$new(true);

      gatewayFlowRuleDialogScope.apiNames = $scope.apiNames;

      gatewayFlowRuleDialogScope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];

      gatewayFlowRuleDialogScope.currentRule = {
        grade: 1,
        app: $scope.app,
        resourceMode: gatewayFlowRuleDialogScope.apiNames.indexOf(resource) == -1 ? 0 : 1,
        resource: resource,
        interval: 1,
        intervalUnit: 0,
        controlBehavior: 0,
        burst: 0,
        maxQueueingTimeoutMs: 0
      };

      gatewayFlowRuleDialogScope.gatewayFlowRuleDialog = {
        title: '新增网关流控规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加',
        showAdvanceButton: true
      };

      gatewayFlowRuleDialogScope.useRouteID = function() {
        gatewayFlowRuleDialogScope.currentRule.resource = '';
      };

      gatewayFlowRuleDialogScope.useCustormAPI = function() {
        gatewayFlowRuleDialogScope.currentRule.resource = '';
      };

      gatewayFlowRuleDialogScope.useParamItem = function () {
        gatewayFlowRuleDialogScope.currentRule.paramItem = {
          parseStrategy: 0,
          matchStrategy: 0
        };
      };

      gatewayFlowRuleDialogScope.notUseParamItem = function () {
        gatewayFlowRuleDialogScope.currentRule.paramItem = null;
      };

      gatewayFlowRuleDialogScope.useParamItemVal = function() {
        gatewayFlowRuleDialogScope.currentRule.paramItem.pattern = "";
      };

      gatewayFlowRuleDialogScope.notUseParamItemVal = function() {
        gatewayFlowRuleDialogScope.currentRule.paramItem.pattern = null;
      };

      gatewayFlowRuleDialogScope.saveRule = saveGatewayFlowRule;
      gatewayFlowRuleDialogScope.saveRuleAndContinue = saveGatewayFlowRuleAndContinue;
      gatewayFlowRuleDialogScope.onOpenAdvanceClick = function () {
        gatewayFlowRuleDialogScope.gatewayFlowRuleDialog.showAdvanceButton = false;
      };
      gatewayFlowRuleDialogScope.onCloseAdvanceClick = function () {
        gatewayFlowRuleDialogScope.gatewayFlowRuleDialog.showAdvanceButton = true;
      };

      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: gatewayFlowRuleDialogScope
      });
    };

    function saveGatewayFlowRule() {
      if (!GatewayFlowService.checkRuleValid(gatewayFlowRuleDialogScope.currentRule)) {
        return;
      }
      GatewayFlowService.newRule(gatewayFlowRuleDialogScope.currentRule).success(function (data) {
        if (data.code === 0) {
          gatewayFlowRuleDialog.close();
          let url = '/dashboard/v2/gateway/flow/' + $scope.app;
          delayLocationPath(url);
        } else {
          alert('失败!');
        }
      }).error((data, header, config, status) => {
          alert('未知错误');
      });
    }

    function saveGatewayFlowRuleAndContinue() {
        if (!GatewayFlowService.checkRuleValid(gatewayFlowRuleDialogScope.currentRule)) {
            return;
        }
      GatewayFlowService.newRule(gatewayFlowRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          gatewayFlowRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var degradeRuleDialog;
    $scope.addNewDegradeRule = function (resource) {
      degradeRuleDialogScope = $scope.$new(true);
      degradeRuleDialogScope.currentRule = {
        enable: false,
        grade: 0,
        strategy: 0,
        resource: resource,
        limitApp: 'default',
        minRequestAmount: 5,
        statIntervalMs: 1000,
        app: $scope.app
      };

      degradeRuleDialogScope.degradeRuleDialog = {
        title: '新增降级规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加'
      };
      degradeRuleDialogScope.saveRule = saveDegradeRule;
      degradeRuleDialogScope.saveRuleAndContinue = saveDegradeRuleAndContinue;

      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: degradeRuleDialogScope
      });
    };

    function saveDegradeRule() {
        if (!DegradeService.checkRuleValid(degradeRuleDialogScope.currentRule)) {
            return;
        }
      DegradeService.newRule(degradeRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          degradeRuleDialog.close();
          var url = '/dashboard/v2/degrade/' + $scope.app;
          delayLocationPath(url);
        } else {
          alert('失败!');
        }
      });
    }

    function saveDegradeRuleAndContinue() {
        if (!DegradeService.checkRuleValid(degradeRuleDialogScope.currentRule)) {
            return;
        }
      DegradeService.newRule(degradeRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          degradeRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var searchHandler;
    $scope.searchChange = function (searchKey) {
      $timeout.cancel(searchHandler);
      searchHandler = $timeout(function () {
        $scope.searchKey = searchKey;
        reInitIdentityDatas();
      }, 600);
    };

    $scope.machineView = function () {
      $scope.ofMachine = true;
      queryIdentities();
    };
    $scope.appView = function () {
      $scope.ofMachine = false;
      queryIdentitiesOfApp();
    };

    $scope.$on('$destroy', function () {
      $interval.cancel(intervalId);
    });

    reInitIdentityDatas();
    var intervalId;
    function reInitIdentityDatas() {
      getApiNames();
      if ($scope.ofMachine) {
        queryIdentities();
      } else {
        queryIdentitiesOfApp();
      }
    };
    $scope.reInitIdentityDatas = reInitIdentityDatas;

    // 旧版本方法，查找单台机器的
    function queryIdentities() {
      IdentityService.fetchClusterNodeOfMachine($scope.app, $scope.searchKey).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.identities = data.data;
            $scope.totalCount = $scope.identities.length;
          } else {
            $scope.identities = [];
            $scope.totalCount = 0;
          }
        }
      );
    };
    $scope.queryIdentities = queryIdentities;

    // 新方法，查找所有机器，因性能较低，所以只推荐手动调用
    function queryIdentitiesOfApp() {
      IdentityService.fetchClusterNodeOfApp($scope.app, $scope.searchKey).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.identities = data.data;
            $scope.totalCount = $scope.identities.length;
          } else {
            $scope.identities = [];
            $scope.totalCount = 0;
          }
        }
      );
    };
    $scope.queryIdentitiesOfApp = queryIdentitiesOfApp;
  }]);

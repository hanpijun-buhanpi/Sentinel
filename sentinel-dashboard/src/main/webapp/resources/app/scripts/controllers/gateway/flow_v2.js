var app = angular.module('sentinelDashboardApp');

app.controller('GatewayFlowControllerV2', ['$scope', '$stateParams', 'GatewayFlowServiceV2', 'GatewayApiServiceV2', 'ngDialog',
  function ($scope, $stateParams, GatewayFlowService, GatewayApiService, ngDialog) {
    $scope.app = $stateParams.app;

    $scope.rulesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    getMachineRules();
    function getMachineRules() {
      GatewayFlowService.queryRules($scope.app).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.rules = data.data;
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getMachineRules = getMachineRules;

    // 外部动态规则源一般存在数据更新延迟，所以这里延迟100ms获取规则
    function delayGetMachineRules() {
      setTimeout(function () {
        getMachineRules();
      }, 100);
    };

    getApiNames();
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

    $scope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];

    var gatewayFlowRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      $scope.gatewayFlowRuleDialog = {
        title: '编辑网关流控规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      $scope.currentRule = {
        grade: 1,
        app: $scope.app,
        resourceMode: 0,
        interval: 1,
        intervalUnit: 0,
        controlBehavior: 0,
        burst: 0,
        maxQueueingTimeoutMs: 0
      };

      $scope.gatewayFlowRuleDialog = {
        title: '新增网关流控规则',
        type: 'add',
        confirmBtnText: '新增'
      };

      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if (!GatewayFlowService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.gatewayFlowRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.gatewayFlowRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    $scope.useRouteID = function() {
      $scope.currentRule.resource = '';
    };

    $scope.useCustormAPI = function() {
      $scope.currentRule.resource = '';
    };

    $scope.useParamItem = function () {
      $scope.currentRule.paramItem = {
        parseStrategy: 0,
        matchStrategy: 0
      };
    };

    $scope.notUseParamItem = function () {
      $scope.currentRule.paramItem = null;
    };

    $scope.useParamItemVal = function() {
      $scope.currentRule.paramItem.pattern = "";
      $scope.currentRule.paramItem.matchStrategy = 0;
    };

    $scope.notUseParamItemVal = function() {
      $scope.currentRule.paramItem.pattern = null;
      $scope.currentRule.paramItem.matchStrategy = null;
    };

    function addNewRule(rule) {
      GatewayFlowService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          delayGetMachineRules();
          gatewayFlowRuleDialog.close();
        } else {
          alert('新增网关流控规则失败!' + data.msg);
        }
      });
    };

    function saveRule(rule, edit) {
      GatewayFlowService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          delayGetMachineRules();
          if (edit) {
            gatewayFlowRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('修改网关流控规则失败!' + data.msg);
        }
      });
    };

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除网关流控规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下规则',
        attention: 'API名称: ' + rule.resource + ', ' + (rule.grade == 1 ? 'QPS阈值' : '线程数') + ': ' + rule.count,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_rule') {
        deleteRule($scope.currentRule);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      GatewayFlowService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          delayGetMachineRules();
          confirmDialog.close();
        } else {
          alert('删除网关流控规则失败!' + data.msg);
        }
      });
    };
  }]
);

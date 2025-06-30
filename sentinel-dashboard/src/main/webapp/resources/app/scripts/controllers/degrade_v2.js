var app = angular.module('sentinelDashboardApp');

app.controller('DegradeControllerV2', ['$scope', '$stateParams', 'DegradeServiceV2', 'ngDialog',
  function ($scope, $stateParams, DegradeService, ngDialog) {
    //初始化
    $scope.app = $stateParams.app;
    $scope.rulesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };
    getMachineRules();
    function getMachineRules() {
      DegradeService.queryMachineRules($scope.app).success(
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

    var degradeRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      $scope.degradeRuleDialog = {
        title: '编辑熔断规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      $scope.currentRule = {
        grade: 0,
        app: $scope.app,
        limitApp: 'default',
        minRequestAmount: 5,
        statIntervalMs: 1000,
      };
      $scope.degradeRuleDialog = {
        title: '新增熔断规则',
        type: 'add',
        confirmBtnText: '新增'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if (!DegradeService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.degradeRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.degradeRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    function parseDegradeMode(grade) {
        switch (grade) {
            case 0:
              return '慢调用比例';
            case 1:
              return '异常比例';
            case 2:
              return '异常数';
            default:
              return '未知';
        }
    }

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除熔断规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下熔断规则',
        attention: '资源名: ' + rule.resource +
            ', 熔断策略: ' + parseDegradeMode(rule.grade) + ', 阈值: ' + rule.count,
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
      DegradeService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          delayGetMachineRules();
          confirmDialog.close();
        } else {
          alert('失败：' + data.msg);
        }
      });
    };

    function addNewRule(rule) {
      DegradeService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          delayGetMachineRules();
          degradeRuleDialog.close();
        } else {
          alert('失败：' + data.msg);
        }
      });
    };

    function saveRule(rule, edit) {
      DegradeService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          delayGetMachineRules();
          if (edit) {
            degradeRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('失败：' + data.msg);
        }
      });
    }
  }]);

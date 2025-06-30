var app = angular.module('sentinelDashboardApp');

app.controller('SystemControllerV2', ['$scope', '$stateParams', 'SystemServiceV2', 'ngDialog',
  function ($scope, $stateParams, SystemService, ngDialog) {
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
      SystemService.queryMachineRules($scope.app).success(
        function (data) {
          if (data.code === 0 && data.data) {
            $scope.rules = data.data;
            $.each($scope.rules, function (idx, rule) {
              if (rule.highestSystemLoad >= 0) {
                rule.grade = 0;
              } else if (rule.avgRt >= 0) {
                rule.grade = 1;
              } else if (rule.maxThread >= 0) {
                rule.grade = 2;
              } else if (rule.qps >= 0) {
                rule.grade = 3;
              } else if (rule.highestCpuUsage >= 0) {
                  rule.grade = 4;
              }
            });
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    }
    $scope.getMachineRules = getMachineRules;

    // 外部动态规则源一般存在数据更新延迟，所以这里延迟100ms获取规则
    function delayGetMachineRules() {
      setTimeout(function () {
        getMachineRules();
      }, 100);
    };

    var systemRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      $scope.systemRuleDialog = {
        title: '编辑系统保护规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      systemRuleDialog = ngDialog.open({
        template: '/app/views/dialog/system-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      $scope.currentRule = {
        grade: 0,
        app: $scope.app
      };
      $scope.systemRuleDialog = {
        title: '新增系统保护规则',
        type: 'add',
        confirmBtnText: '新增'
      };
      systemRuleDialog = ngDialog.open({
        template: '/app/views/dialog/system-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if ($scope.systemRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.systemRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      var ruleTypeDesc = '';
      var ruleTypeCount = null;
      if (rule.highestSystemLoad != -1) {
        ruleTypeDesc = 'LOAD';
        ruleTypeCount = rule.highestSystemLoad;
      } else if (rule.avgRt != -1) {
        ruleTypeDesc = 'RT';
        ruleTypeCount = rule.avgRt;
      } else if (rule.maxThread != -1) {
        ruleTypeDesc = '线程数';
        ruleTypeCount = rule.maxThread;
      } else if (rule.qps != -1) {
        ruleTypeDesc = 'QPS';
        ruleTypeCount = rule.qps;
      }else if (rule.highestCpuUsage != -1) {
          ruleTypeDesc = 'CPU 使用率';
          ruleTypeCount = rule.highestCpuUsage;
      }

      $scope.confirmDialog = {
        title: '删除系统保护规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下系统保护规则',
        attention: '阈值类型: ' + ruleTypeDesc + ', 阈值: ' + ruleTypeCount,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };


    $scope.confirm = function () {
      if ($scope.confirmDialog.type === 'delete_rule') {
        deleteRule($scope.currentRule);
        // } else if ($scope.confirmDialog.type == 'enable_rule') {
        //     $scope.currentRule.enable = true;
        //     saveRule($scope.currentRule);
        // } else if ($scope.confirmDialog.type == 'disable_rule') {
        //     $scope.currentRule.enable = false;
        //     saveRule($scope.currentRule);
        // } else if ($scope.confirmDialog.type == 'enable_all') {
        //     enableAll($scope.app);
        // } else if ($scope.confirmDialog.type == 'disable_all') {
        //     disableAll($scope.app);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      SystemService.deleteRule(rule).success(function (data) {
        if (data.code === 0) {
          delayGetMachineRules();
          confirmDialog.close();
        } else if (data.msg != null) {
            alert('失败：' + data.msg);
        } else {
            alert('失败：未知错误');
        }
      });
    }

    function addNewRule(rule) {
      if (rule.grade == 4 && (rule.highestCpuUsage < 0 || rule.highestCpuUsage > 1)) {
        alert('CPU 使用率模式的取值范围应为 [0.0, 1.0]，对应 0% - 100%');
        return;
      }
      SystemService.newRule(rule).success(function (data) {
        if (data.code === 0) {
          delayGetMachineRules();
          systemRuleDialog.close();
        } else if (data.msg != null) {
          alert('失败：' + data.msg);
        } else {
          alert('失败：未知错误');
        }
      });
    }

    function saveRule(rule, edit) {
      SystemService.saveRule(rule).success(function (data) {
        if (data.code === 0) {
          delayGetMachineRules();
          if (edit) {
            systemRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else if (data.msg != null) {
          alert('失败：' + data.msg);
        } else {
          alert('失败：未知错误');
        }
      });
    }
  }]);

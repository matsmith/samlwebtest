 'use strict';

angular.module('samlwebtestApp')
    .factory('notificationInterceptor', function ($q, AlertService) {
        return {
            response: function(response) {
                var alertKey = response.headers('X-samlwebtestApp-alert');
                if (angular.isString(alertKey)) {
                    AlertService.success(alertKey, { param : response.headers('X-samlwebtestApp-params')});
                }
                return response;
            },
        };
    });
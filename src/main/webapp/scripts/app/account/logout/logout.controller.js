'use strict';

angular.module('samlwebtestApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });

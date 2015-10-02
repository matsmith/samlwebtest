'use strict';

angular.module('samlwebtestApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });



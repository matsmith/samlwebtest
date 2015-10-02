'use strict';

angular.module('samlwebtestApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('postsaml', {
                parent: 'account',
                url: '/samltest',
                data: {
                    roles: [],
                    pageTitle: 'Saml Initiator'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/saml/saml.html',
                        controller: 'SamlController'
                    }
                },
                resolve: {

                }
            });
    });

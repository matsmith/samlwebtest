'use strict';

angular.module('samlwebtestApp')
    .factory('saml', function samlService($http) {
        return {
            create: function(credentials){
                var data = 'customerId=' + credentials.customerId +
                    '&resourceUrl=' + credentials.resourceUrl;
                return $http.post('/saml/createresponse?customerId='+credentials.customerId + '&resourceUrl=' + credentials.resourceUrl, data, {
                    headers: {
                        'Content-Type': 'text/html',
                        'Accept': 'text/html'
                    }
                }).success(function (response) {
                    return response;
                });
            },

            sendToFusion: function(data){
                var data = 'SAMLResponse=' + data.responseSaml; +
                    '&RelayState=' + data.relayState;
                return $http.post('https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching', data, {
                    //headers: {
                    //    'Content-Type': 'text/html',
                    //    'Accept': 'text/html'
                    //}
                }).success(function (response) {
                    return response;
                });
            },


            test: function(data){
                var data = 'SAMLResponse=' + data.responseSaml; +
                    '&RelayState=' + data.relayState;
                //return $http.post('/saml/test', data, {
                return $http.post('https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching', data, {
                    headers: {
                        'Content-Type': 'text/html',
                        'Accept': 'text/html'
                    }
                }).success(function (response) {
                    return response;
                });
            }
        };
    });

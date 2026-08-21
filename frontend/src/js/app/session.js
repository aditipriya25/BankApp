define([], function () {
  'use strict';

  var KEYS = {
    token:      'bankingAuthToken',
    role:       'bankingRole',
    email:      'bankingEmail',
    customerId: 'bankingCustomerId'
  };

  function decode(token) {
    try {
      return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    } catch (e) {
      return null;
    }
  }

  function isTokenExpired(token) {
    var payload = decode(token);
    if (!payload || !payload.exp) return true;
    return Date.now() >= payload.exp * 1000;
  }

  return {
    getToken:      function () { return localStorage.getItem(KEYS.token); },
    getRole:       function () { return localStorage.getItem(KEYS.role); },
    getEmail:      function () { return localStorage.getItem(KEYS.email); },
    getCustomerId: function () { return localStorage.getItem(KEYS.customerId); },

    setCustomerId: function (id) { localStorage.setItem(KEYS.customerId, id); },

    setSession: function (token) {
      var payload = decode(token);
      if (!payload) return;
      localStorage.setItem(KEYS.token, token);
      localStorage.setItem(KEYS.role,  payload.role);
      localStorage.setItem(KEYS.email, payload.sub);
    },

    /** Clear all session data including customerId */
    clear: function () {
      Object.values(KEYS).forEach(function (key) { localStorage.removeItem(key); });
    },

    /** True when token exists AND has not expired */
    isLoggedIn: function () {
      var token = localStorage.getItem(KEYS.token);
      return !!token && !isTokenExpired(token);
    },

    hasRole: function (role) {
      return this.isLoggedIn() && this.getRole() === role;
    }
  };
});

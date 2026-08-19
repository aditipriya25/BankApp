define([], function () {
  'use strict';
  var keys = ['bankingAuthToken', 'bankingRole', 'bankingEmail'];
  function decode(token) { return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))); }
  return { getToken: function () { return localStorage.getItem(keys[0]); }, getRole: function () { return localStorage.getItem(keys[1]); }, getEmail: function () { return localStorage.getItem(keys[2]); }, getCustomerId: function () { return localStorage.getItem('bankingCustomerId'); }, setCustomerId: function (id) { localStorage.setItem('bankingCustomerId', id); }, setSession: function (token) { var payload = decode(token); localStorage.setItem(keys[0], token); localStorage.setItem(keys[1], payload.role); localStorage.setItem(keys[2], payload.sub); }, clear: function () { keys.forEach(function (key) { localStorage.removeItem(key); }); }, hasRole: function (role) { return !!this.getToken() && this.getRole() === role; } };
});

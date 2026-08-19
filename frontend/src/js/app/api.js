define(['app/session'], function (session) {
  'use strict';
  var baseUrl = 'http://localhost:8080';
  async function request(path, options) { var config = options || {}, headers = Object.assign({ 'Content-Type': 'application/json' }, config.headers || {}), token = session.getToken(); if (token) headers.Authorization = 'Bearer ' + token; var response = await fetch(baseUrl + path, Object.assign({}, config, { headers: headers })); var data = await response.json().catch(function () { return null; }); if ((response.status === 401 || response.status === 403) && token) { session.clear(); location.hash = '#/login'; throw new Error('Your session has expired. Please sign in again.'); } if (!response.ok) throw new Error((data && (data.message || data.error)) || 'The request could not be completed.'); return data; }
  return { get: function (path) { return request(path); }, post: function (path, body) { return request(path, { method: 'POST', body: JSON.stringify(body) }); }, put: function (path, body) { return request(path, { method: 'PUT', body: JSON.stringify(body) }); } };
});

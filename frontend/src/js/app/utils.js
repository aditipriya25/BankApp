define([], function () {
  'use strict';
  function escape(value) { return String(value == null ? '' : value).replace(/[&<>"]/g, function (character) { return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[character]; }); }
  return { escape: escape, date: function (value) { return value ? new Date(value).toLocaleString() : '—'; }, money: function (value) { return value == null ? '—' : '₹' + Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2 }); }, badge: function (value) { return '<span class="badge badge-' + escape(value).toLowerCase() + '">' + escape(value) + '</span>'; }, go: function (route) { location.hash = '#/' + route; } };
});

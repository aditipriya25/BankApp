define(['app/pages'], function (pages) {
  'use strict';
  var routes = { home: pages.home, features: pages.home, login: pages.login, signup: pages.signup, 'customer-dashboard': pages.customerDashboard, 'customer-lockers': pages.customerLockers, 'customer-bookings': pages.customerBookings, 'customer-kyc': pages.customerKyc, 'employee-dashboard': pages.employeeDashboard, 'employee-lockers': pages.employeeLockers, 'employee-requests': pages.employeeRequests, 'employee-kyc': pages.employeeKyc, 'employee-visits': pages.employeeVisits };
  return { render: function () { var route = (location.hash || '#/home').replace(/^#\//, '').split('#')[0]; (routes[route] || pages.home)(); } };
});

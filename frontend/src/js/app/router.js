define(['app/pages'], function (pages) {
  'use strict';
  var routes = {
    home: pages.home,
    features: pages.home,
    login: pages.login,
    signup: pages.signup,
    // Customer pages
    'customer-dashboard':  pages.customerDashboard,
    'customer-lockers':    pages.customerLockers,
    'customer-bookings':   pages.customerBookings,
    'customer-kyc':        pages.customerKyc,
    'customer-nominee':    pages.customerNominee,
    'customer-agreement':  pages.customerAgreement,
    'customer-rent':       pages.customerRentPayment,
    'customer-closure':    pages.customerClosure,
    // Employee pages
    'employee-dashboard':  pages.employeeDashboard,
    'employee-lockers':    pages.employeeLockers,
    'employee-requests':   pages.employeeRequests,
    'employee-kyc':        pages.employeeKyc,
    'employee-visits':     pages.employeeVisits,
    'employee-nominee':    pages.employeeNominee,
    'employee-agreements': pages.employeeAgreements,
    'employee-rent-dues':  pages.employeeRentDues,
    'employee-closures':   pages.employeeClosures,
    // Shared
    chatbot: pages.chatbot
  };
  return { render: function () { var route = (location.hash || '#/home').replace(/^#\//, '').split('#')[0]; (routes[route] || pages.home)(); } };
});

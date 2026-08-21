define(['app/pages', 'app/session', 'app/utils'], function (pages, session, utils) {
  'use strict';

  var routes = {
    home:               pages.home,
    features:           pages.home,
    login:              pages.login,
    signup:             pages.signup,
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

  /** Public routes that should redirect to dashboard when already logged in */
  var publicOnlyRoutes = { home: true, features: true, login: true, signup: true };

  return {
    render: function () {
      var hash  = (location.hash || '#/home').replace(/^#\//, '').split('#')[0].split('?')[0];
      var route = hash || 'home';
      var loggedIn = session.isLoggedIn();
      var role     = session.getRole();

      // If already logged-in and visiting login/signup/home → redirect to dashboard
      if (loggedIn && publicOnlyRoutes[route]) {
        utils.go(role === 'EMPLOYEE' ? 'employee-dashboard' : 'customer-dashboard');
        return;
      }

      // If not logged-in and trying to access a protected route → send to login
      if (!loggedIn && !publicOnlyRoutes[route]) {
        utils.go('login');
        return;
      }

      // Render the matching page or fall back to home
      var page = routes[route] || pages.home;
      page();
    }
  };
});

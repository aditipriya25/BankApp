define(['app/session', 'app/utils'], function (session, utils) {
  'use strict';

  /* ── Inline SVG Icons ──────────────────────────────────── */
  var IC = {
    home:    '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9.5L12 3l9 6.5V20a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9.5z"/><polyline points="9 21 9 12 15 12 15 21"/></svg>',
    locker:  '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2"/><circle cx="12" cy="13" r="2"/><path d="M12 11V9"/></svg>',
    cal:     '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>',
    shield:  '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
    inbox:   '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/></svg>',
    doc:     '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
    clock:   '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
    users:   '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
    logout:  '<svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
    moon:    '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>',
    sun:     '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>',
    chat:    '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
    person:  '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
    money:   '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>',
    close2:  '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>',
    menu:    '<svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="3" y1="7" x2="21" y2="7"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="17" x2="21" y2="17"/></svg>'
  };

  /* ── Nav config ─────────────────────────────────────────── */
  var CUST_NAV = [
    ['customer-dashboard', 'Overview',          IC.home],
    ['customer-lockers',   'Find a Locker',     IC.locker],
    ['customer-bookings',  'Visit Bookings',    IC.cal],
    ['customer-kyc',       'KYC Verification',  IC.shield],
    ['customer-nominee',   'Nominees',          IC.person],
    ['customer-agreement', 'My Agreement',      IC.doc],
    ['customer-rent',      'Pay Rent',          IC.money],
    ['customer-closure',   'Close Locker',      IC.close2],
    ['chatbot',            'AI Assistant',       IC.chat]
  ];
  var EMP_NAV = [
    ['employee-dashboard',  'Overview',         IC.home],
    ['employee-lockers',    'Locker Inventory', IC.locker],
    ['employee-requests',   'Requests',         IC.inbox],
    ['employee-kyc',        'KYC Reviews',      IC.doc],
    ['employee-visits',     'Visit Logs',       IC.clock],
    ['employee-nominee',    'All Nominees',     IC.person],
    ['employee-agreements', 'Agreements',       IC.doc],
    ['employee-rent-dues',  'Rent Dues',        IC.money],
    ['employee-closures',   'Closures',         IC.close2],
    ['chatbot',             'AI Assistant',      IC.chat]
  ];

  /* ── Page title map ─────────────────────────────────────── */
  var TITLES = {
    'customer-dashboard':  'Overview',
    'customer-lockers':    'Find a Locker',
    'customer-bookings':   'Visit Bookings',
    'customer-kyc':        'KYC Verification',
    'customer-nominee':    'Nominees',
    'customer-agreement':  'My Agreement',
    'customer-rent':       'Pay Rent',
    'customer-closure':    'Close Locker',
    'employee-dashboard':  'Overview',
    'employee-lockers':    'Locker Inventory',
    'employee-requests':   'Requests',
    'employee-kyc':        'KYC Reviews',
    'employee-visits':     'Visit Logs',
    'employee-nominee':    'All Nominees',
    'employee-agreements': 'Agreements',
    'employee-rent-dues':  'Rent Dues',
    'employee-closures':   'Closures',
    'chatbot':             'AI Assistant'
  };

  /* ── Theme helpers ──────────────────────────────────────── */
  function getTheme() { return localStorage.getItem('vb-theme') || 'dark'; }

  function applyTheme(t) {
    localStorage.setItem('vb-theme', t);
    document.documentElement.setAttribute('data-theme', t);
    /* Refresh all toggle buttons */
    document.querySelectorAll('.theme-btn').forEach(function (b) {
      b.innerHTML  = t === 'dark' ? IC.sun : IC.moon;
      b.title = t === 'dark' ? 'Switch to light mode' : 'Switch to dark mode';
    });
  }

  function toggleTheme() { applyTheme(getTheme() === 'dark' ? 'light' : 'dark'); }

  /* ── Build sidebar HTML ─────────────────────────────────── */
  function buildSidebar(nav, active, isEmp, email) {
    var initial = email ? email.charAt(0).toUpperCase() : '?';
    var links = nav.map(function (n) {
      return '<a class="sb-link' + (active === n[0] ? ' active' : '') +
        '" href="#/' + n[0] + '">' + n[2] +
        '<span>' + n[1] + '</span></a>';
    }).join('');

    return (
      '<aside class="sidebar" id="vb-sb">' +
        '<div class="sb-logo">' +
          '<div class="sb-logo-icon">V</div>' +
          '<div class="sb-logo-text">Vault<b>Bank</b></div>' +
        '</div>' +
        '<div class="sb-chip' + (isEmp ? ' emp' : '') + '">' +
          (isEmp ? IC.users : IC.shield) +
          '<span>' + (isEmp ? 'Employee' : 'Customer') + '</span>' +
        '</div>' +
        '<div class="sb-section">Navigation</div>' +
        '<nav class="sb-nav">' + links + '</nav>' +
        '<div class="sb-footer">' +
          '<div class="sb-user">' +
            '<div class="sb-avatar">' + utils.escape(initial) + '</div>' +
            '<span class="sb-email">' + utils.escape(email || '') + '</span>' +
          '</div>' +
        '</div>' +
      '</aside>'
    );
  }

  /* ── Main layout function ───────────────────────────────── */
  function layout(content, active) {
    var signedIn = !!session.getToken();
    var role     = session.getRole();
    var email    = session.getEmail();
    var theme    = getTheme();
    var isEmp    = role === 'EMPLOYEE';

    if (signedIn) {
      /* ===== Authenticated shell: sidebar + main-wrap ===== */
      var nav   = isEmp ? EMP_NAV : CUST_NAV;
      var title = TITLES[active] || 'VaultBank';

      document.body.innerHTML =
        '<div class="app-shell">' +

          /* Sidebar */
          buildSidebar(nav, active, isEmp, email) +

          /* Overlay (mobile) */
          '<div class="sb-overlay" id="vb-overlay"></div>' +

          /* Main area: topbar + page body + footer */
          '<div class="main-wrap">' +

            '<header class="topbar">' +
              '<button class="hamburger" id="vb-hbg" title="Toggle menu">' + IC.menu + '</button>' +
              '<span class="topbar-title">' + utils.escape(title) + '</span>' +
              '<div class="topbar-actions">' +
                '<button class="theme-btn" id="vb-theme" title="' + (theme === 'dark' ? 'Switch to light' : 'Switch to dark') + '">' +
                  (theme === 'dark' ? IC.sun : IC.moon) +
                '</button>' +
                '<button class="btn btn-sm signout-btn" data-action="logout">' +
                  IC.logout + '<span>Sign out</span>' +
                '</button>' +
              '</div>' +
            '</header>' +

            '<main class="page-body">' +
              '<div id="page-message"></div>' +
              content +
            '</main>' +

            '<footer class="app-footer">© 2026 VaultBank · Secure locker banking</footer>' +

          '</div>' + /* /main-wrap */

        '</div>'; /* /app-shell */

      /* Wire hamburger */
      var hbg = document.getElementById('vb-hbg');
      var sb  = document.getElementById('vb-sb');
      var ov  = document.getElementById('vb-overlay');
      function closeSb() { sb.classList.remove('open'); ov.classList.remove('open'); }
      if (hbg) hbg.onclick = function () { sb.classList.toggle('open'); ov.classList.toggle('open'); };
      if (ov)  ov.onclick  = closeSb;

    } else {
      /* ===== Public shell: topbar + content ===== */
      document.body.innerHTML =
        '<div class="pub-shell">' +

          '<header class="pub-topbar">' +
            '<a class="pub-brand" href="#/home">' +
              '<div class="pub-brand-icon">V</div>' +
              'Vault<b>Bank</b>' +
            '</a>' +
            '<nav class="pub-nav">' +
              '<a href="#/home">Home</a>' +
              '<a href="#/login">Sign in</a>' +
            '</nav>' +
            '<div class="pub-actions">' +
              '<button class="theme-btn" title="' + (theme === 'dark' ? 'Switch to light' : 'Switch to dark') + '">' +
                (theme === 'dark' ? IC.sun : IC.moon) +
              '</button>' +
              '<a class="btn btn-outline" href="#/login">Sign in</a>' +
              '<a class="btn btn-solid"   href="#/signup">Open account</a>' +
            '</div>' +
          '</header>' +

          '<div id="pub-content">' +
            '<div id="page-message" style="max-width:1120px;margin:1rem auto 0;padding:0 1.5rem;"></div>' +
            content +
          '</div>' +

          '<footer class="pub-footer">© 2026 VaultBank · Secure locker banking</footer>' +

        '</div>';
    }

    /* Wire theme toggle(s) */
    document.querySelectorAll('.theme-btn').forEach(function (b) {
      b.onclick = toggleTheme;
    });

    /* Wire sign-out */
    var lo = document.querySelector('[data-action="logout"]');
    if (lo) lo.onclick = function () { session.clear(); utils.go('home'); };

    /* ── Page-enter animation ───────────────────── */
    var pb = document.querySelector('.page-body') || document.getElementById('pub-content');
    if (pb) { pb.classList.add('page-enter'); setTimeout(function () { pb.classList.add('page-enter-active'); }, 30); }

    /* ── Floating chatbot button (authenticated pages) ── */
    if (signedIn && active !== 'chatbot') {
      var fab = document.createElement('button');
      fab.id = 'chatbot-fab';
      fab.title = 'VaultBot — AI Assistant';
      fab.innerHTML = IC.chat + '<span class="fab-pulse"></span>';
      fab.onclick = function () { utils.go('chatbot'); };
      document.body.appendChild(fab);
    }
  }

  /* ── Flash message ──────────────────────────────────────── */
  function message(text, type) {
    var box = document.querySelector('#page-message');
    if (!box) return;
    box.innerHTML = text
      ? '<div class="notice ' + (type || 'info') + '">' + utils.escape(text) + '</div>'
      : '';
  }

  return { layout: layout, message: message };
});

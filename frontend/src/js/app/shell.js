define(['app/session', 'app/utils', 'app/api'], function (session, utils, api) {
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
    menu:    '<svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="3" y1="7" x2="21" y2="7"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="17" x2="21" y2="17"/></svg>',
    bell:    '<svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>'
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

  /* ── Notification type → colour ─────────────────────────── */
  var NOTIF_COLORS = {
    KYC_APPROVED:       '#22c55e',
    KYC_REJECTED:       '#ef4444',
    KYC_SUBMITTED:      '#f59e0b',
    RENT_PAID:          '#22c55e',
    RENT_DUE:           '#f59e0b',
    CLOSURE_REQUESTED:  '#f59e0b',
    CLOSURE_APPROVED:   '#22c55e',
    CLOSURE_REJECTED:   '#ef4444',
    AGREEMENT_READY:    '#3b82f6',
    AGREEMENT_SIGNED:   '#22c55e',
    GENERAL:            '#94a3b8'
  };

  /* ── Theme helpers ──────────────────────────────────────── */
  function getTheme() { return localStorage.getItem('vb-theme') || 'dark'; }

  function applyTheme(t) {
    localStorage.setItem('vb-theme', t);
    document.documentElement.setAttribute('data-theme', t);
    document.querySelectorAll('.theme-btn').forEach(function (b) {
      b.innerHTML  = t === 'dark' ? IC.sun : IC.moon;
      b.title = t === 'dark' ? 'Switch to light mode' : 'Switch to dark mode';
    });
  }

  function toggleTheme() { applyTheme(getTheme() === 'dark' ? 'light' : 'dark'); }

  /* ── Notification bell ──────────────────────────────────── */
  function loadNotifications() {
    var bell  = document.getElementById('vb-bell');
    var badge = document.getElementById('vb-bell-badge');
    var dropdown = document.getElementById('vb-notif-drop');
    if (!bell) return;

    api.get('/api/notifications/unread-count').then(function (res) {
      var count = res && res.count ? res.count : 0;
      if (badge) {
        badge.textContent = count > 9 ? '9+' : String(count);
        badge.style.display = count > 0 ? 'flex' : 'none';
      }
    }).catch(function () {});

    bell.onclick = function (e) {
      e.stopPropagation();
      if (!dropdown) return;
      var visible = dropdown.style.display !== 'none';
      if (visible) {
        dropdown.style.display = 'none';
        return;
      }
      dropdown.style.display = 'block';
      dropdown.innerHTML = '<div style="padding:.6rem 1rem;font-size:.78rem;color:var(--ink2);font-weight:600;letter-spacing:.06em;">NOTIFICATIONS</div>';

      api.get('/api/notifications').then(function (list) {
        if (!list || !list.length) {
          dropdown.innerHTML += '<div style="padding:.8rem 1rem;color:var(--ink2);font-size:.85rem;">No notifications yet.</div>';
          return;
        }
        var top = list.slice(0, 8);
        top.forEach(function (n) {
          var dot = '<span style="width:8px;height:8px;border-radius:50%;background:' +
            (NOTIF_COLORS[n.type] || '#94a3b8') +
            ';display:inline-block;flex-shrink:0;margin-top:3px;"></span>';
          var item = document.createElement('div');
          item.className = 'notif-item' + (n.read ? '' : ' notif-unread');
          item.innerHTML =
            '<div style="display:flex;gap:.6rem;align-items:flex-start;">' +
              dot +
              '<div style="flex:1;">' +
                '<div style="font-size:.83rem;font-weight:' + (n.read ? '400' : '600') + ';color:var(--ink1);">' +
                  utils.escape(n.title) +
                '</div>' +
                '<div style="font-size:.76rem;color:var(--ink2);margin-top:.15rem;">' +
                  utils.escape(n.message ? n.message.substring(0, 80) + (n.message.length > 80 ? '…' : '') : '') +
                '</div>' +
              '</div>' +
            '</div>';
          item.onclick = function () {
            api.put('/api/notifications/' + n.id + '/read', {}).catch(function () {});
            item.classList.remove('notif-unread');
            if (badge) {
              var cur = parseInt(badge.textContent, 10) || 0;
              var next = Math.max(0, cur - 1);
              badge.textContent = next > 9 ? '9+' : String(next);
              badge.style.display = next > 0 ? 'flex' : 'none';
            }
          };
          dropdown.appendChild(item);
        });
        // Mark all read button
        var markAll = document.createElement('div');
        markAll.style.cssText = 'padding:.5rem 1rem;text-align:center;border-top:1px solid var(--border);margin-top:.3rem;';
        markAll.innerHTML = '<button class="btn btn-ghost btn-sm" id="notif-mark-all">Mark all as read</button>';
        dropdown.appendChild(markAll);
        var btn = document.getElementById('notif-mark-all');
        if (btn) btn.onclick = function (e) {
          e.stopPropagation();
          api.put('/api/notifications/read-all', {}).then(function () {
            dropdown.style.display = 'none';
            if (badge) badge.style.display = 'none';
          }).catch(function () {});
        };
      }).catch(function () {
        dropdown.innerHTML += '<div style="padding:.8rem 1rem;color:var(--ink2);font-size:.85rem;">Could not load notifications.</div>';
      });
    };

    // Close dropdown when clicking outside
    document.addEventListener('click', function () {
      if (dropdown) dropdown.style.display = 'none';
    });
    if (dropdown) dropdown.onclick = function (e) { e.stopPropagation(); };
  }

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
                /* Notification bell */
                '<div class="notif-wrap" style="position:relative;">' +
                  '<button class="theme-btn" id="vb-bell" title="Notifications" style="position:relative;">' +
                    IC.bell +
                    '<span id="vb-bell-badge" style="position:absolute;top:1px;right:1px;min-width:16px;height:16px;border-radius:8px;background:#ef4444;color:#fff;font-size:.62rem;font-weight:700;display:none;align-items:center;justify-content:center;padding:0 3px;"></span>' +
                  '</button>' +
                  '<div id="vb-notif-drop" style="display:none;position:absolute;right:0;top:calc(100% + 8px);width:300px;max-height:380px;overflow-y:auto;background:var(--bg2);border:1px solid var(--border);border-radius:12px;box-shadow:0 8px 32px rgba(0,0,0,.25);z-index:9999;"></div>' +
                '</div>' +
                '<button class="theme-btn" id="vb-theme" title="' + (theme === 'dark' ? 'Switch to light' : 'Switch to dark') + '">' +
                  (theme === 'dark' ? IC.sun : IC.moon) +
                '</button>' +
                '<button class="btn btn-sm signout-btn" id="vb-signout">' +
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

      /* Load notifications */
      loadNotifications();

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
      if (b.id !== 'vb-bell') b.onclick = toggleTheme;
    });

    /* Wire sign-out — clear session and go home */
    var so = document.getElementById('vb-signout');
    if (so) so.onclick = function () {
      session.clear();
      location.hash = '#/home';
      location.reload();
    };

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

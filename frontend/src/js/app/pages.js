define(['app/api', 'app/session', 'app/shell', 'app/utils'], function (api, session, shell, utils) {
  'use strict';

  var e  = utils.escape;
  var b  = utils.badge;
  var d  = utils.date;
  var m  = utils.money;
  var rc = utils.renderChart;
  var tp = utils.tooltipPlugin;
  var cd = utils.chartDefaults;

  /* ── Guard & error helpers ──────────────────────────────── */
  function guarded(role) {
    if (!session.hasRole(role)) { utils.go('login'); return false; }
    return true;
  }

  function loadingHtml() {
    return '<div class="loading"><div class="spinner"></div><span>Loading…</span></div>';
  }

  function showError(id) {
    return function (err) {
      var node = document.getElementById(id);
      if (node) node.innerHTML =
        '<div class="empty"><div class="empty-icon">⚠️</div>' +
        '<h3>Something went wrong</h3><p>' + e(err.message) + '</p></div>';
    };
  }

  /* ============================================================
     HOME
     ============================================================ */
  function home() {
    shell.layout(
      '<section class="hero">' +
        '<div>' +
          '<p class="hero-kicker">SECURE LOCKER BANKING</p>' +
          '<h1>Your valuables deserve a <span>better vault.</span></h1>' +
          '<p class="hero-sub">Browse available lockers, manage requests, make payments and schedule a secure visit — all in one place.</p>' +
          '<div class="hero-actions">' +
            '<a class="btn btn-solid" href="#/signup">Open a customer account</a>' +
            '<a class="btn btn-outline" href="#/login">Employee sign in</a>' +
          '</div>' +
        '</div>' +
        '<div class="vault-card">' +
          '<div class="vault-icon">🔐</div>' +
          '<strong>Protected Access</strong>' +
          '<small>Every locker visit is recorded and verified by our secure OTP system.</small>' +
        '</div>' +
      '</section>' +

      '<section class="features-sec">' +
        '<p class="sec-kicker">ONE PLATFORM</p>' +
        '<h2 class="sec-title">Locker services without the paper trail.</h2>' +
        '<div class="feat-grid">' +
          '<article class="feat-card"><div class="feat-icon">⌘</div><h3>Choose your locker</h3><p>Review available locker sizes and prices before submitting a request.</p></article>' +
          '<article class="feat-card"><div class="feat-icon">✓</div><h3>Track every step</h3><p>See approval, payment status and assignment details in real time.</p></article>' +
          '<article class="feat-card"><div class="feat-icon">◷</div><h3>Book your visit</h3><p>Schedule a visit and receive an OTP for secure key collection.</p></article>' +
        '</div>' +
      '</section>',
      'home'
    );
  }

  /* ============================================================
     AUTH
     ============================================================ */
  function auth(signup) {
    shell.layout(
      '<section class="auth-wrap">' +
        '<div class="auth-copy">' +
          '<p class="hero-kicker">VAULTBANK ACCESS</p>' +
          '<h1>' + (signup ? 'Start your secure locker journey.' : 'Welcome back.') + '</h1>' +
          '<p>' + (signup
            ? 'Create a customer account to request and manage a locker.'
            : 'Customer and employee accounts use the same secure sign-in.') + '</p>' +
        '</div>' +
        '<form id="auth-form" class="auth-panel">' +
          '<h2>' + (signup ? 'Create account' : 'Sign in') + '</h2>' +
          (signup
            ? '<div class="field"><label>Full name</label><input class="form-input" name="fullName" required placeholder="Jane Smith"></div>' +
              '<div class="field"><label>Phone number</label><input class="form-input" name="phone" required placeholder="+91 98765 43210"></div>'
            : '<div class="field"><label>Account type</label><select class="form-select" name="expectedRole"><option value="CUSTOMER">Customer</option><option value="EMPLOYEE">Employee</option></select></div>'
          ) +
          '<div class="field"><label>Email address</label><input class="form-input" name="email" type="email" required placeholder="you@example.com"></div>' +
          '<div class="field"><label>Password</label><input class="form-input" name="password" type="password" minlength="8" required placeholder="Min. 8 characters"></div>' +
          (signup ? '<div class="field"><label>Confirm password</label><input class="form-input" name="confirmPassword" type="password" minlength="8" required placeholder="Repeat password"></div>' : '') +
          '<div class="field"><button class="btn btn-primary btn-wide" type="submit">' +
            (signup ? 'Create account' : 'Sign in securely') +
          '</button></div>' +
          '<p class="form-foot">' +
            (signup ? 'Already have an account? <a href="#/login">Sign in</a>' : 'New customer? <a href="#/signup">Open an account</a>') +
          '</p>' +
        '</form>' +
      '</section>',
      signup ? 'signup' : 'login'
    );

    document.getElementById('auth-form').onsubmit = async function (event) {
      event.preventDefault();
      var form = event.currentTarget, values = Object.fromEntries(new FormData(form));
      try {
        if (!form.reportValidity()) return;
        if (signup) {
          if (values.password !== values.confirmPassword) throw new Error('Passwords do not match.');
          delete values.confirmPassword;
          var cust = await api.post('/api/bank-customers/addCustomer', values);
          session.setCustomerId(cust.id);
          utils.go('login');
        } else {
          var expected = values.expectedRole; delete values.expectedRole;
          var result = await api.post('/auth/login', values);
          session.setSession(result.token);
          if (session.getRole() !== expected) { session.clear(); throw new Error('This account does not match the selected type.'); }
          /* For customers: fetch and cache their ID so KYC and other features work */
          if (expected === 'CUSTOMER') {
            try {
              var me = await api.get('/api/bank-customers/me');
              if (me && me.id) session.setCustomerId(me.id);
            } catch (e) { /* non-fatal — KYC page will fall back to /me endpoints */ }
          }
          utils.go(expected === 'CUSTOMER' ? 'customer-dashboard' : 'employee-dashboard');
        }
      } catch (err) { shell.message(err.message, 'error'); }
    };

  }

  /* ============================================================
     CUSTOMER DASHBOARD
     ============================================================ */
  function customerDashboard() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd">' +
        '<p class="kicker">CUSTOMER PORTAL</p>' +
        '<h1>Your Locker Services</h1>' +
        '<p>Manage your lockers, payments, and visits from one place.</p>' +
      '</div>' +
      '<div id="kyc-banner"></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-dashboard'
    );

    /* Load KYC banner — always uses JWT, no stored ID needed */
    api.get('/api/kyc/status/me').then(function (kyc) {
      var bannerEl = document.getElementById('kyc-banner');
      if (!bannerEl) return;
      if (!kyc || kyc.status === 'NOT_SUBMITTED') {
        bannerEl.innerHTML = '<div class="notice error" style="margin-bottom:1rem;">⚠️ KYC not submitted. <a href="#/customer-kyc" style="color:inherit;font-weight:600;text-decoration:underline;">Complete KYC</a> before requesting a locker.</div>';
      } else if (kyc.status === 'REJECTED') {
        bannerEl.innerHTML = '<div class="notice error" style="margin-bottom:1rem;">❌ Your KYC was rejected. <a href="#/customer-kyc" style="color:inherit;font-weight:600;text-decoration:underline;">Re-submit KYC</a> to request a locker.</div>';
      } else if (kyc.status === 'APPROVED') {
        bannerEl.innerHTML = '<div class="notice success" style="margin-bottom:1rem;">✅ KYC Approved — you can request lockers freely.</div>';
      } else {
        bannerEl.innerHTML = '<div class="notice info" style="margin-bottom:1rem;">🕐 KYC is pending review.</div>';
      }
    }).catch(function () {});

    /* Load all active assignments */
    api.get('/api/locker-assignments/my-assignments').then(function (list) {
      if (!list || !list.length) {
        document.getElementById('data').innerHTML =
          '<div class="empty"><div class="empty-icon">🔓</div>' +
          '<h3>No active locker requests</h3>' +
          '<p>Browse our available lockers and submit a request to get started.</p>' +
          '<a class="btn btn-primary" href="#/customer-lockers">Find a locker</a></div>';
        return;
      }

      /* Build status cards for each assignment — no payment form here (use Pay Rent page) */
      var cards = list.map(function (a) {
        var acReq = a.requestStatus === 'APPROVED' ? 'ac-green' : a.requestStatus === 'PAID' ? 'ac-blue' : 'ac-amber';
        var acPay = a.paymentStatus === 'PAID' ? 'ac-green' : 'ac-amber';
        var payBtn = (a.requestStatus === 'PAID')
          ? '<a class="btn btn-sm btn-primary" href="#/customer-rent" style="margin-top:.8rem;display:inline-block;">💳 Pay Annual Rent</a>'
          : '';
        return '<div class="panel" style="margin-bottom:1rem;">'
          + '<h3 style="margin-bottom:.8rem;">🔐 Locker ' + e(a.locker && a.locker.lockerNumber) +
            ' <span style="font-size:.82rem;font-weight:400;color:var(--ink2);">' + e(a.locker && a.locker.size) + ' · ₹' + (a.locker && a.locker.price ? Number(a.locker.price).toLocaleString('en-IN') : 'N/A') + '/year</span></h3>'
          + '<div class="stat-grid">'
            + '<div class="stat-card ' + acReq + '">'
              + '<div class="sc-label">📋 Request Status</div>'
              + '<div style="margin-top:.6rem;">' + b(a.requestStatus) + '</div>'
            + '</div>'
            + '<div class="stat-card ' + acPay + '">'
              + '<div class="sc-label">💳 Annual Rent Status</div>'
              + '<div style="margin-top:.6rem;">' + b(a.paymentStatus) + '</div>'
              + '<div class="sc-sub" style="margin-top:.4rem;">' + (a.nextRentDueDate ? 'Next due: ' + d(a.nextRentDueDate) : '') + '</div>'
            + '</div>'
          + '</div>'
          + payBtn
          + '</div>';
      }).join('');

      document.getElementById('data').innerHTML =
        cards +
        '<a class="btn btn-outline" href="#/customer-lockers" style="display:inline-block;margin-top:.5rem;">+ Request another locker</a>';

    }).catch(showError('data'));
  }


  /* ============================================================
     CUSTOMER LOCKERS
     ============================================================ */
  function customerLockers() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd">' +
        '<p class="kicker">LOCKER BOOKING</p>' +
        '<h1>Pick Your Locker</h1>' +
        '<p>🔒 Grey = Booked &nbsp; 🔓 Green = Available — click to request</p>' +
      '</div>' +
      '<div class="panel">' +
        '<form id="filter" class="inline-form">' +
          '<div class="field"><label>Filter by size</label>' +
            '<select id="szFilter" class="form-select" name="size">' +
              '<option value="">All sizes</option>' +
              '<option value="SMALL">SMALL — ₹6,000/year</option>' +
              '<option value="MEDIUM">MEDIUM — ₹12,000/year</option>' +
              '<option value="LARGE">LARGE — ₹24,000/year</option>' +
            '</select>' +
          '</div>' +
          '<div class="field" style="flex:none;display:flex;gap:.5rem;align-items:flex-end;">' +
            '<button class="btn btn-primary" type="submit">Apply</button>' +
            '<button class="btn btn-ghost" type="button" id="resetF">Reset</button>' +
          '</div>' +
        '</form>' +
      '</div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-lockers'
    );

    function renderGrid(list, kycApproved) {
      if (!list.length) {
        document.getElementById('data').innerHTML =
          '<div class="empty"><div class="empty-icon">🔍</div><h3>No lockers match your filter</h3></div>';
        return;
      }
      var grouped = {};
      list.forEach(function (x) { var s = x.size || 'OTHER'; if (!grouped[s]) grouped[s] = []; grouped[s].push(x); });

      /* KYC banner — shown when customer KYC is not yet approved */
      var banner = !kycApproved
        ? '<div class="notice error" style="margin-bottom:1rem;">🛡️ KYC verification required. ' +
          '<a href="#/customer-kyc" style="color:inherit;font-weight:600;text-decoration:underline;">Complete your KYC</a>' +
          ' before requesting a locker.</div>'
        : '';

      var html = banner + '<div class="locker-wrap">';
      ['SMALL','MEDIUM','LARGE','OTHER'].forEach(function (sz) {
        if (!grouped[sz] || !grouped[sz].length) return;
        var price = grouped[sz][0] ? grouped[sz][0].price : '';
        html += '<div class="sz-section">' +
          '<div class="sz-header">' +
            '<span class="sz-badge">' + e(sz) + '</span>' +
            '<span class="sz-price">₹' + (price ? Number(price).toLocaleString('en-IN') + ' / year' : 'N/A') + '</span>' +
            '<span class="sz-count">' + grouped[sz].length + ' lockers</span>' +
          '</div>' +
          '<div class="ticket-grid">';
        grouped[sz].forEach(function (x) {
          /* Clickable only when locker is AVAILABLE and customer KYC is APPROVED */
          var clickable = x.status === 'AVAILABLE' && kycApproved;
          html += '<div class="ticket ' + (clickable ? 'ticket-av' : 'ticket-bk') + '"' +
            (clickable ? ' data-id="' + e(x.id) + '" role="button" tabindex="0"' : '') + '>' +
            '<div class="t-icon">' + (x.status === 'AVAILABLE' ? '🔓' : '🔒') + '</div>' +
            '<div class="t-num">' + e(x.lockerNumber) + '</div>' +
            '<div class="t-status">' + (x.status === 'AVAILABLE' ? 'Available' : (x.status === 'RESERVED' ? 'Reserved' : 'Booked')) + '</div>' +
            '</div>';
        });
        html += '</div></div>';
      });
      html += '</div>';
      document.getElementById('data').innerHTML = html;

      /* Wire click handlers only when KYC is approved — multiple lockers allowed */
      if (kycApproved) {
        document.querySelectorAll('.ticket-av[data-id]').forEach(function (tile) {
          function doReq() {
            api.post('/api/locker-assignments/request', { lockerId: tile.dataset.id })
              .then(function () { utils.go('customer-dashboard'); })
              .catch(function (err) { shell.message(err.message, 'error'); });
          }
          tile.onclick = doReq;
          tile.onkeydown = function (ev) { if (ev.key === 'Enter' || ev.key === ' ') doReq(); };
        });
      }
    }

    function load(sz) {
      document.getElementById('data').innerHTML = loadingHtml();
      Promise.all([
        api.get('/api/lockers/all-public' + (sz ? '?size=' + encodeURIComponent(sz) : '')),
        api.get('/api/kyc/status/me').catch(function () { return null; })
      ]).then(function (results) {
        var list        = results[0];
        var kyc         = results[1];
        var kycApproved = !!(kyc && kyc.status === 'APPROVED');
        renderGrid(list, kycApproved);
      }).catch(showError('data'));
    }
    load('');
    document.getElementById('filter').onsubmit = function (ev) { ev.preventDefault(); load(document.getElementById('szFilter').value); };
    document.getElementById('resetF').onclick   = function () { document.getElementById('szFilter').value = ''; load(''); };
  }

  /* ============================================================
     CUSTOMER BOOKINGS
     ============================================================ */
  function customerBookings() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">VISIT BOOKINGS</p><h1>Plan a Secure Visit</h1><p>Schedule your visit to the branch to access your locker.</p></div>' +
      '<div class="panel">' +
        '<h2>Book a Visit</h2>' +
        '<form id="book" class="inline-form">' +
          '<div class="field"><label>Date and time</label><input class="form-input" name="scheduledAt" type="datetime-local" required></div>' +
          '<div class="field" style="flex:none;align-self:flex-end;"><button class="btn btn-primary" type="submit">Book visit</button></div>' +
        '</form>' +
      '</div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-bookings'
    );

    function load() {
      api.get('/api/slot-bookings/my-bookings').then(function (list) {
        if (!list.length) {
          document.getElementById('data').innerHTML =
            '<div class="empty"><div class="empty-icon">📅</div><h3>No visits booked yet</h3><p>Use the form above to schedule your first visit.</p></div>';
          return;
        }
        var rows = list.map(function (x) {
          return '<tr><td>' + d(x.scheduledAt) + '</td>' +
            '<td><code style="background:var(--bg3);padding:.15rem .5rem;border-radius:5px;font-size:.85rem;">' + e(x.otpCode) + '</code></td>' +
            '<td>' + b(x.status) + '</td></tr>';
        }).join('');
        document.getElementById('data').innerHTML =
          '<div class="panel"><h2>Your Bookings</h2>' +
          '<div class="table-wrap"><table class="dt-table">' +
            '<thead><tr><th>Scheduled</th><th>OTP Code</th><th>Status</th></tr></thead>' +
            '<tbody>' + rows + '</tbody>' +
          '</table></div></div>';
      }).catch(showError('data'));
    }
    load();
    document.getElementById('book').onsubmit = async function (ev) {
      ev.preventDefault();
      try {
        await api.post('/api/slot-bookings/book?scheduledAt=' + encodeURIComponent(new FormData(ev.currentTarget).get('scheduledAt')), {});
        load();
      } catch (err) { shell.message(err.message, 'error'); }
    };
  }

  /* ============================================================
     CUSTOMER KYC
     ============================================================ */
  function customerKyc() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">IDENTITY VERIFICATION</p><h1>Complete Your KYC</h1><p>Submit your Aadhaar and PAN details for verification.</p></div>' +
      '<div id="status"></div>' +
      '<div class="panel" style="max-width:780px;">' +
        '<h2>KYC Documents</h2>' +
        '<form id="kyc">' +
          '<div class="form-grid">' +
            '<div class="field"><label>Aadhaar number</label><input class="form-input" name="aadhaarNumber" required placeholder="XXXX XXXX XXXX"></div>' +
            '<div class="field"><label>Aadhaar name</label><input class="form-input" name="aadhaarName" required placeholder="As on Aadhaar card"></div>' +
            '<div class="field"><label>Aadhaar address</label><input class="form-input" name="aadhaarAddress" required placeholder="Address on Aadhaar"></div>' +
            '<div class="field"><label>Aadhaar photo URL</label><input class="form-input" name="aadhaarPhotoUrl" type="url" required placeholder="https://..."></div>' +
            '<div class="field"><label>PAN number</label><input class="form-input" name="panNumber" required placeholder="ABCDE1234F"></div>' +
            '<div class="field"><label>PAN name</label><input class="form-input" name="panName" required placeholder="As on PAN card"></div>' +
            '<div class="field"><label>PAN address</label><input class="form-input" name="panAddress" required placeholder="Address on PAN"></div>' +
            '<div class="field"><label>Live photo URL</label><input class="form-input" name="livePhotoUrl" type="url" required placeholder="https://..."></div>' +
          '</div>' +
          '<div class="notice info" style="margin-bottom:1rem;">ℹ️ Your submitted documents will be reviewed by a bank employee. You will be notified once your KYC is approved or rejected.</div>' +
          '<button class="btn btn-primary" type="submit">Submit KYC</button>' +
        '</form>' +
      '</div>',
      'customer-kyc'
    );

    function load() {
      api.get('/api/kyc/status/me').then(function (x) {
        var statusEl = document.getElementById('status');
        if (!statusEl) return;
        if (!x || x.status === 'NOT_SUBMITTED') {
          statusEl.innerHTML =
            '<div class="notice info" style="max-width:780px;margin-bottom:1rem;">📋 No KYC submitted yet. Fill in the form below to get started.</div>';
          return;
        }
        var ac = x.status === 'APPROVED' ? 'ac-green' : x.status === 'REJECTED' ? 'ac-red' : 'ac-amber';
        statusEl.innerHTML =
          '<div class="stat-card ' + ac + '" style="max-width:780px;margin-bottom:1.2rem;">' +
            '<div class="sc-label">KYC Status</div>' +
            '<div style="margin-top:.5rem;">' + b(x.status) + '</div>' +
            (x.remarks || x.message ? '<div class="sc-sub" style="margin-top:.4rem;">' + e(x.remarks || x.message) + '</div>' : '') +
          '</div>';
      }).catch(function () {});
    }
    load();

    document.getElementById('kyc').onsubmit = async function (ev) {
      ev.preventDefault();
      var form = ev.currentTarget;
      var values = Object.fromEntries(new FormData(form));
      // photoMatchFlag is set by employee during review, NOT by customer
      delete values.photoMatchFlag;
      try {
        await api.post('/api/kyc/submit/me', values);
        load();
        shell.message('KYC submitted! An employee will review your documents shortly.', 'success');
      } catch (err) { shell.message(err.message, 'error'); }
    };
  }

  /* ============================================================
     EMPLOYEE DASHBOARD
     ============================================================ */
  function employeeDashboard() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">EMPLOYEE PORTAL</p><h1>Locker Operations</h1><p>Monitor requests, KYC reviews, and active assignments.</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-dashboard'
    );

    Promise.all([
      api.get('/api/locker-assignments/pending'),
      api.get('/api/locker-assignments/awaiting-payment'),
      api.get('/api/kyc/pending'),
      api.get('/api/locker-assignments/approved').catch(function () { return []; }),
      api.get('/api/locker-assignments/rejected').catch(function () { return []; })
    ]).then(function (r) {
      var pend = r[0]||[], awPay = r[1]||[], kycP = r[2]||[], appr = r[3]||[], rej = r[4]||[];
      document.getElementById('data').innerHTML =
        '<div class="stat-grid">' +
          '<div class="stat-card ac-amber"><div class="sc-label">⏳ Pending Requests</div><div class="sc-value">' + pend.length + '</div><a class="sc-link" href="#/employee-requests">Review requests ›</a></div>' +
          '<div class="stat-card ac-blue"><div class="sc-label">💳 Awaiting Payment</div><div class="sc-value">' + awPay.length + '</div><a class="sc-link" href="#/employee-requests">View payments ›</a></div>' +
          '<div class="stat-card ac-purple"><div class="sc-label">🛡 Pending KYC</div><div class="sc-value">' + kycP.length + '</div><a class="sc-link" href="#/employee-kyc">Review KYC ›</a></div>' +
        '</div>' +
        '<div class="charts-row">' +
          '<div class="chart-card"><h3>Request Breakdown</h3><div class="chart-box"><canvas id="e-bar"></canvas></div></div>' +
          '<div class="chart-card"><h3>KYC Status</h3><div class="chart-box"><canvas id="e-donut"></canvas></div></div>' +
        '</div>';

      var d0 = cd();
      rc('e-bar', {
        type: 'bar',
        data: {
          labels: ['Pending','Approved','Rejected','Awaiting Pay'],
          datasets: [{
            label: 'Count',
            data: [pend.length, appr.length, rej.length, awPay.length],
            backgroundColor: ['rgba(245,158,11,.7)','rgba(34,197,94,.7)','rgba(239,68,68,.7)','rgba(59,130,246,.7)'],
            borderColor:     ['#f59e0b','#22c55e','#ef4444','#3b82f6'],
            borderWidth: 2, borderRadius: 6, borderSkipped: false
          }]
        },
        options: Object.assign({ responsive: true, maintainAspectRatio: false,
          scales: {
            x: { ticks: { color: d0.textColor }, grid: { color: d0.gridColor } },
            y: { ticks: { color: d0.textColor, stepSize: 1 }, grid: { color: d0.gridColor }, beginAtZero: true }
          }
        }, tp())
      });

      rc('e-donut', {
        type: 'doughnut',
        data: {
          labels: ['Pending KYC','Reviewed'],
          datasets: [{
            data: [kycP.length || 1, Math.max(0, appr.length + rej.length)],
            backgroundColor: ['rgba(245,158,11,.8)','rgba(34,197,94,.8)'],
            borderWidth: 0, hoverOffset: 6
          }]
        },
        options: Object.assign({ responsive: true, maintainAspectRatio: false, cutout: '60%' }, tp())
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     EMPLOYEE LOCKERS
     ============================================================ */
  function employeeLockers() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">LOCKER INVENTORY</p><h1>Manage Lockers</h1><p>Add new lockers and view the full inventory.</p></div>' +
      '<div class="panel">' +
        '<h2>Add New Locker</h2>' +
        '<form id="add" class="inline-form">' +
          '<div class="field"><label>Locker number</label><input class="form-input" name="lockerNumber" required placeholder="e.g. L-101"></div>' +
          '<div class="field"><label>Size</label><select id="addSz" class="form-select" name="size" required><option value="">Select size</option><option value="SMALL">SMALL</option><option value="MEDIUM">MEDIUM</option><option value="LARGE">LARGE</option></select></div>' +
          '<div class="field"><label>Price (₹/mo)</label><input id="addPr" class="form-input" name="price" type="number" min="1" required placeholder="Auto-filled"></div>' +
          '<div class="field" style="flex:none;align-self:flex-end;"><button class="btn btn-primary" type="submit">Add locker</button></div>' +
        '</form>' +
      '</div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-lockers'
    );

    document.getElementById('addSz').onchange = function () {
      document.getElementById('addPr').value = { SMALL: 6000, MEDIUM: 12000, LARGE: 24000 }[this.value] || '';
    };

    function load() {
      api.get('/api/lockers').then(function (list) {
        if (!list.length) {
          document.getElementById('data').innerHTML =
            '<div class="empty"><div class="empty-icon">📦</div><h3>No lockers yet</h3><p>Add your first locker using the form above.</p></div>';
          return;
        }
        var grouped = {};
        list.forEach(function (x) { var s = x.size||'OTHER'; if(!grouped[s]) grouped[s]=[]; grouped[s].push(x); });
        var html = '<div class="tb-bar"><input class="tb-search" id="inv-s" placeholder="🔍 Search locker number or status…"></div>';
        ['SMALL','MEDIUM','LARGE','OTHER'].forEach(function (sz) {
          if (!grouped[sz]||!grouped[sz].length) return;
          var rows = grouped[sz].map(function (x) {
            return '<tr class="irow" data-q="' + e((x.lockerNumber+' '+x.size+' '+x.status).toLowerCase()) + '">' +
              '<td><strong>' + e(x.lockerNumber) + '</strong></td>' +
              '<td>' + e(x.size) + '</td><td>' + m(x.price) + '</td><td>' + b(x.status) + '</td></tr>';
          }).join('');
          html += '<div class="panel" style="margin-bottom:.9rem;">' +
            '<h3>' + e(sz) + ' Lockers <span class="cbadge cbadge-blue">' + grouped[sz].length + '</span></h3>' +
            '<div class="table-wrap"><table class="dt-table">' +
              '<thead><tr><th>Locker #</th><th>Size</th><th>Price/mo</th><th>Status</th></tr></thead>' +
              '<tbody>' + rows + '</tbody>' +
            '</table></div></div>';
        });
        document.getElementById('data').innerHTML = html;
        document.getElementById('inv-s').oninput = function () {
          var q = this.value.toLowerCase();
          document.querySelectorAll('.irow').forEach(function (r) { r.style.display = r.dataset.q.includes(q) ? '' : 'none'; });
        };
      }).catch(showError('data'));
    }
    load();
    document.getElementById('add').onsubmit = async function (ev) {
      ev.preventDefault();
      var form = ev.currentTarget;
      var vals = Object.fromEntries(new FormData(form)); vals.price = Number(vals.price);
      try { await api.post('/api/lockers', vals); form.reset(); load(); shell.message('Locker added!', 'success'); }
      catch (err) { shell.message(err.message, 'error'); }
    };
  }

  /* ============================================================
     EMPLOYEE REQUESTS
     ============================================================ */
  function employeeRequests() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">CUSTOMER REQUESTS</p><h1>Review Locker Requests</h1><p>Approve or reject pending customer applications.</p></div>' +
      '<div class="tab-bar">' +
        '<button class="tab-btn active" data-tab="pending">⏳ Pending</button>' +
        '<button class="tab-btn" data-tab="approved">✓ Approved</button>' +
        '<button class="tab-btn" data-tab="rejected">✕ Rejected</button>' +
      '</div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-requests'
    );

    function renderPending(list) {
      if (!list.length) return '<div class="empty"><div class="empty-icon">✅</div><h3>All clear — no pending requests</h3></div>';
      return '<div class="panel"><h2>Pending Approval <span class="cbadge cbadge-amber">' + list.length + '</span></h2>' +
        list.map(function (x) {
          return '<div class="req-row">' +
            '<div class="req-info"><strong>' + e(x.customer&&x.customer.fullName) + '</strong>' +
            '<span>Locker ' + e(x.locker&&x.locker.lockerNumber) + ' · ' + e(x.locker&&x.locker.size) + ' · ' + m(x.locker&&x.locker.price) + '</span></div>' +
            '<div class="req-acts">' +
              '<button class="btn btn-sm btn-success" data-action="approve" data-id="' + e(x.id) + '">✓ Approve</button>' +
              '<button class="btn btn-sm btn-danger"  data-action="reject"  data-id="' + e(x.id) + '">✕ Reject</button>' +
            '</div></div>';
        }).join('') + '</div>';
    }

    function renderList(list, tab) {
      if (!list.length) return '<div class="empty"><div class="empty-icon">📋</div><h3>No ' + tab + ' requests</h3></div>';
      var isApp = tab === 'approved';
      var cbCls = isApp ? 'cbadge-green' : 'cbadge-red';
      var thExtra = isApp ? '<th>Payment</th>' : '<th>Reviewed By</th>';
      var rows = list.map(function (x) {
        var extra = isApp ? '<td>' + b(x.paymentStatus) + '</td>' : '<td>' + e(x.approvedByEmployee&&x.approvedByEmployee.fullName) + '</td>';
        var q = e((x.customer&&x.customer.fullName||'')+(x.locker&&x.locker.lockerNumber||'')).toLowerCase();
        return '<tr class="rrow" data-q="' + q + '"><td><strong>' + e(x.customer&&x.customer.fullName) + '</strong></td>' +
          '<td>' + e(x.locker&&x.locker.lockerNumber) + '</td>' +
          '<td>' + e(x.locker&&x.locker.size) + ' · ' + m(x.locker&&x.locker.price) + '</td>' + extra + '</tr>';
      }).join('');
      return '<div class="panel">' +
        '<h2>' + (isApp ? 'Approved' : 'Rejected') + ' Requests <span class="cbadge ' + cbCls + '">' + list.length + '</span></h2>' +
        '<div class="tb-bar"><input class="tb-search req-s" placeholder="🔍 Search by customer or locker…"></div>' +
        '<div class="table-wrap"><table class="dt-table"><thead><tr><th>Customer</th><th>Locker</th><th>Size · Price</th>' + thExtra + '</tr></thead><tbody>' + rows + '</tbody></table></div></div>';
    }

    function loadTab(tab) {
      document.getElementById('data').innerHTML = loadingHtml();
      document.querySelectorAll('.tab-btn').forEach(function (btn) { btn.classList.toggle('active', btn.dataset.tab === tab); });
      var ep = tab==='pending' ? '/api/locker-assignments/pending' : tab==='approved' ? '/api/locker-assignments/approved' : '/api/locker-assignments/rejected';
      api.get(ep).then(function (list) {
        document.getElementById('data').innerHTML = tab === 'pending' ? renderPending(list) : renderList(list, tab);
        if (tab === 'pending') {
          document.querySelectorAll('[data-action]').forEach(function (btn) {
            btn.onclick = async function () {
              try { await api.post('/api/locker-assignments/'+btn.dataset.id+'/'+btn.dataset.action, {}); loadTab('pending'); }
              catch (err) { shell.message(err.message, 'error'); }
            };
          });
        } else {
          var s = document.querySelector('.req-s');
          if (s) s.oninput = function () {
            var q = this.value.toLowerCase();
            document.querySelectorAll('.rrow').forEach(function (r) { r.style.display = r.dataset.q.includes(q) ? '' : 'none'; });
          };
        }
      }).catch(showError('data'));
    }
    document.querySelectorAll('.tab-btn').forEach(function (btn) { btn.onclick = function () { loadTab(btn.dataset.tab); }; });
    loadTab('pending');
  }

  /* ============================================================
     EMPLOYEE KYC
     ============================================================ */
  function employeeKyc() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">KYC REVIEWS</p><h1>Review Identity Documents</h1><p>Verify customer Aadhaar and PAN submissions.</p></div>' +
      '<div class="tb-bar" style="margin-bottom:1rem;"><input class="tb-search" id="kyc-s" placeholder="🔍 Search by customer name…"></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-kyc'
    );
    api.get('/api/kyc/all').then(function (list) {
      if (!list.length) {
        document.getElementById('data').innerHTML = '<div class="empty"><div class="empty-icon">📄</div><h3>No KYC records found</h3></div>';
        return;
      }
      document.getElementById('data').innerHTML = list.map(function (x) {
        return '<div class="kyc-rec" data-q="' + e(x.customer&&x.customer.fullName||'').toLowerCase() + '">' +
          '<div><h3 style="margin-bottom:.4rem;">' + e(x.customer&&x.customer.fullName) + '</h3>' +
          '<div style="margin-bottom:.4rem;">' + b(x.kycStatus) + '</div>' +
          (x.remarks ? '<p style="font-size:.82rem;color:var(--ink2);">' + e(x.remarks) + '</p>' : '') + '</div>' +
          '<form data-id="' + e(x.id) + '" class="kyc-form">' +
            '<div class="field"><label>Decision</label><select class="form-select" name="status"><option value="APPROVED">APPROVED</option><option value="REJECTED">REJECTED</option></select></div>' +
            '<div class="field"><label>Remarks</label><input class="form-input" name="remarks" required placeholder="Review notes…"></div>' +
            '<button class="btn btn-primary btn-sm" type="submit">Save review</button>' +
          '</form></div>';
      }).join('');

      document.getElementById('kyc-s').oninput = function () {
        var q = this.value.toLowerCase();
        document.querySelectorAll('.kyc-rec').forEach(function (c) { c.style.display = c.dataset.q.includes(q) ? '' : 'none'; });
      };
      document.querySelectorAll('[data-id]').forEach(function (form) {
        form.onsubmit = async function (ev) {
          ev.preventDefault();
          try { await api.put('/api/kyc/'+form.dataset.id+'/review', Object.fromEntries(new FormData(form))); shell.message('KYC reviewed!', 'success'); employeeKyc(); }
          catch (err) { shell.message(err.message, 'error'); }
        };
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     EMPLOYEE VISITS
     ============================================================ */
  function employeeVisits() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">SECURE VISITS</p><h1>Verify Entry & Return Keys</h1><p>Validate customer OTPs and manage key issuance.</p></div>' +
      '<div class="panel">' +
        '<h2>Verify OTP</h2>' +
        '<form id="otp" class="inline-form">' +
          '<div class="field"><label>Booking ID</label><input class="form-input" name="bookingId" required placeholder="Booking reference"></div>' +
          '<div class="field"><label>OTP code</label><input class="form-input" name="otpCode" required placeholder="6-digit OTP"></div>' +
          '<div class="field" style="flex:none;align-self:flex-end;"><button class="btn btn-primary" type="submit">Verify OTP</button></div>' +
        '</form>' +
      '</div>' +
      '<div class="tb-bar"><input class="tb-search" id="vis-s" placeholder="🔍 Search by booking ID…"></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-visits'
    );

    function load() {
      api.get('/api/visit-logs').then(function (list) {
        if (!list.length) {
          document.getElementById('data').innerHTML = '<div class="empty"><div class="empty-icon">🕐</div><h3>No visit logs yet</h3></div>';
          return;
        }
        var rows = list.map(function (x) {
          return '<tr class="vrow" data-q="' + e(x.bookingId||'').toLowerCase() + '">' +
            '<td><code style="font-size:.82rem;background:var(--bg3);padding:.15rem .4rem;border-radius:4px;">' + e(x.bookingId) + '</code></td>' +
            '<td>' + d(x.keyIssuedAt) + '</td>' +
            '<td>' + (x.keyReturnedAt ? d(x.keyReturnedAt) : '<span class="badge badge-pending">In branch</span>') + '</td>' +
            '<td>' + (!x.keyReturnedAt ? '<button class="btn btn-sm btn-ghost" data-id="' + e(x.id) + '">Return key</button>' : '<span class="badge badge-approved">Returned</span>') + '</td>' +
            '</tr>';
        }).join('');
        document.getElementById('data').innerHTML =
          '<div class="panel"><h2>Visit Logs</h2>' +
          '<div class="table-wrap"><table class="dt-table"><thead><tr><th>Booking ID</th><th>Key Issued</th><th>Key Returned</th><th>Action</th></tr></thead><tbody>' + rows + '</tbody></table></div></div>';

        document.getElementById('vis-s').oninput = function () {
          var q = this.value.toLowerCase();
          document.querySelectorAll('.vrow').forEach(function (r) { r.style.display = r.dataset.q.includes(q) ? '' : 'none'; });
        };
        document.querySelectorAll('[data-id]').forEach(function (btn) {
          btn.onclick = async function () {
            try { await api.post('/api/visit-logs/'+btn.dataset.id+'/return-key', {}); load(); }
            catch (err) { shell.message(err.message, 'error'); }
          };
        });
      }).catch(showError('data'));
    }
    load();
    document.getElementById('otp').onsubmit = async function (ev) {
      ev.preventDefault();
      var form = ev.currentTarget;
      try { await api.post('/api/visit-logs/verify-otp', Object.fromEntries(new FormData(form))); form.reset(); load(); shell.message('OTP verified! Key issued.', 'success'); }
      catch (err) { shell.message(err.message, 'error'); }
    };
  }


  /* ============================================================
     CUSTOMER — NOMINEE MANAGEMENT  (RBI para 5.1)
     ============================================================ */
  function customerNominee() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">RBI PARA 5.1</p><h1>Nominees</h1>' +
      '<p>Add, update or cancel nominations for your lockers (Forms SL1 / SL2 / SL3 — Banking Regulation Act 1949).</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-nominee'
    );

    api.get('/api/locker-assignments/my-assignments').then(function (list) {
      var paid = (list || []).filter(function (a) { return a.requestStatus === 'PAID'; });
      if (!paid.length) {
        document.getElementById('data').innerHTML =
          '<div class="empty"><div class="empty-icon">📋</div><h3>No active lockers</h3>' +
          '<p>Nominees can only be added after your locker request is approved and paid.</p>' +
          '<a class="btn btn-primary" href="#/customer-dashboard">Go to Dashboard</a></div>';
        return;
      }

      var html = paid.map(function (a) {
        var lid = e(a.id), ln = e(a.locker && a.locker.lockerNumber);
        return '<div class="panel" style="margin-bottom:1.2rem;" id="nom-panel-' + lid + '">' +
          '<h3>🔐 Locker ' + ln + ' Nominees</h3>' +
          '<input class="form-input" id="nom-search-' + lid + '" placeholder="🔍 Search nominees by name or relationship…" style="max-width:340px;margin-bottom:.6rem;">' +
          '<div id="noms-' + lid + '"></div>' +
          '<details style="margin-top:.8rem;" id="nom-details-' + lid + '">' +
          '<summary class="btn btn-outline btn-sm" style="cursor:pointer;display:inline-flex;align-items:center;gap:.4rem;">+ Add Nominee (Form SL1)</summary>' +
          '<form id="nf-' + lid + '" class="inline-form" style="margin-top:.8rem;flex-wrap:wrap;" data-aid="' + lid + '">' +
            '<div class="field"><label>Full name *</label><input class="form-input" name="name" required placeholder="Nominee full name"></div>' +
            '<div class="field"><label>Relationship *</label><input class="form-input" name="relationship" required placeholder="e.g. Spouse, Child"></div>' +
            '<div class="field"><label>Date of Birth</label><input class="form-input" name="dateOfBirth" type="date"></div>' +
            '<div class="field"><label>Phone</label><input class="form-input" name="phone" placeholder="+91..."></div>' +
            '<div class="field"><label>Email</label><input class="form-input" name="email" type="email"></div>' +
            '<div class="field" style="flex:1 0 100%"><label>Address</label><input class="form-input" name="address"></div>' +
            '<div class="field"><label>Photo URL</label><input class="form-input" name="photoUrl" type="url" placeholder="https://..."></div>' +
            '<div class="field"><label>Form Type</label><select class="form-select" name="formType"><option value="SL1">SL1 (Single Hirer)</option><option value="SL1A">SL1A (Joint)</option></select></div>' +
            '<div class="check-row"><input name="isMinor" type="checkbox"><span>Nominee is a minor</span></div>' +
            '<div class="field" id="grd-' + lid + '" style="display:none;flex:1 0 100%"><label>Guardian name (required for minor)</label><input class="form-input" name="guardianName"></div>' +
            '<div class="field" style="flex:none;align-self:flex-end"><button class="btn btn-primary" type="submit">Add Nominee</button></div>' +
          '</form></details></div>';
      }).join('');
      document.getElementById('data').innerHTML = html;

      paid.forEach(function (a) {
        var lid = a.id;

        /** Reload nominees for just this locker's panel — keeps other panels intact */
        function loadNoms() {
          api.get('/api/nominees/' + lid).then(function (noms) {
            var el = document.getElementById('noms-' + lid);
            if (!el) return;
            var searchVal = (document.getElementById('nom-search-' + lid) || {}).value || '';
            var filtered = (noms || []).filter(function (n) {
              if (!searchVal) return true;
              var q = searchVal.toLowerCase();
              return (n.name || '').toLowerCase().includes(q) ||
                     (n.relationship || '').toLowerCase().includes(q);
            });
            if (!filtered.length) {
              el.innerHTML = '<p style="color:var(--ink2);font-size:.88rem;">' +
                (noms && noms.length ? 'No nominees match your search.' : 'No nominees registered yet. Add one below.') +
                '</p>';
            } else {
              el.innerHTML = '<div class="table-wrap"><table class="dt-table"><thead><tr><th>Name</th><th>Relationship</th><th>Form</th><th>Minor</th><th>Action</th></tr></thead><tbody>' +
                filtered.map(function (n) {
                  return '<tr><td>' + e(n.name) + '</td><td>' + e(n.relationship) + '</td>' +
                    '<td><span class="cbadge cbadge-blue">' + e(n.formType) + '</span></td>' +
                    '<td>' + (n.minor ? '✅' : '—') + '</td>' +
                    '<td><button class="btn btn-sm btn-danger" data-del="' + e(n.id) + '" data-aid="' + e(lid) + '">Cancel (SL2)</button></td></tr>';
                }).join('') + '</tbody></table></div>';
              // Wire delete
              el.querySelectorAll('[data-del]').forEach(function (btn) {
                btn.onclick = async function () {
                  if (!confirm('Cancel this nomination (Form SL2)?')) return;
                  try { await api.delete('/api/nominees/nominee/' + btn.dataset.del); loadNoms(); shell.message('Nomination cancelled.', 'success'); }
                  catch (err) { shell.message(err.message, 'error'); }
                };
              });
            }
          }).catch(function () {});
        }

        loadNoms();

        // Wire search
        var searchEl = document.getElementById('nom-search-' + lid);
        if (searchEl) searchEl.oninput = loadNoms;

        // Wire minor toggle
        var form = document.getElementById('nf-' + lid);
        if (!form) return;
        form.querySelector('[name="isMinor"]').onchange = function () {
          document.getElementById('grd-' + lid).style.display = this.checked ? '' : 'none';
        };
        form.onsubmit = async function (ev) {
          ev.preventDefault();
          var vals = Object.fromEntries(new FormData(form));
          vals.isMinor = form.querySelector('[name="isMinor"]').checked;
          try {
            await api.post('/api/nominees/' + lid, vals);
            form.reset();
            loadNoms(); // refresh only this locker's nominees — keep the panel open
            shell.message('Nominee added! ✅', 'success');
          } catch (err) { shell.message(err.message, 'error'); }
        };
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     CUSTOMER — LOCKER AGREEMENT  (RBI para 2.1)
     ============================================================ */
  function customerAgreement() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">RBI PARA 2.1</p><h1>My Locker Agreement</h1>' +
      '<p>Review and digitally sign your Board-approved locker agreement. A copy is provided to you (RBI 2.1.2).</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-agreement'
    );

    api.get('/api/locker-assignments/my-assignments').then(function (list) {
      var paid = (list || []).filter(function (a) { return a.requestStatus === 'PAID'; });
      if (!paid.length) {
        document.getElementById('data').innerHTML =
          '<div class="empty"><div class="empty-icon">📄</div><h3>No active lockers</h3><a class="btn btn-primary" href="#/customer-dashboard">Go to Dashboard</a></div>';
        return;
      }

      Promise.all(paid.map(function (a) {
        return api.get('/api/agreements/' + a.id).catch(function () { return null; }).then(function (ag) {
          return { a: a, ag: ag };
        });
      })).then(function (results) {
        var html = results.map(function (r) {
          var a = r.a, ag = r.ag;
          var ln = e(a.locker && a.locker.lockerNumber);
          if (!ag) {
            return '<div class="panel" style="margin-bottom:1rem;"><h3>🔐 Locker ' + ln + '</h3>' +
              '<div class="notice info">⏳ Your locker agreement is being prepared by the bank. ' +
              'It will appear here once generated. You will receive a notification when it\'s ready to sign.</div></div>';
          }
          var signed = ag.signedByCustomer;
          return '<div class="panel" style="margin-bottom:1.5rem;">' +
            '<h3>🔐 Locker ' + ln + ' — Agreement ' +
              (signed ? '<span class="cbadge cbadge-green">✅ Signed</span>' : '<span class="cbadge cbadge-amber">⏳ Pending Your Signature</span>') + '</h3>' +
            '<div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:.6rem;margin:.8rem 0;">' +
              '<div class="stat-card ac-blue"><div class="sc-label">Agreement Date</div><div style="font-size:.9rem;">' + d(ag.agreementDate) + '</div></div>' +
              '<div class="stat-card ac-green"><div class="sc-label">Stamp Duty</div><div style="font-size:.9rem;">' + (ag.stampDutyPaid ? '✅ Paid — ₹' + (ag.stampDutyAmount || 100) : '—') + '</div></div>' +
              '<div class="stat-card ' + (signed ? 'ac-green' : 'ac-amber') + '"><div class="sc-label">Your Signature</div><div style="font-size:.9rem;">' + (signed ? '✅ ' + d(ag.customerSignedAt) : 'Pending') + '</div></div>' +
            '</div>' +
            '<details style="margin:.8rem 0;"><summary class="btn btn-ghost btn-sm" style="cursor:pointer;">📄 View Full Agreement Text</summary>' +
            '<pre style="background:var(--bg2);border-radius:8px;padding:1rem;font-size:.8rem;white-space:pre-wrap;max-height:320px;overflow:auto;margin-top:.6rem;">' + e(ag.agreementContent || '') + '</pre></details>' +
            (!signed ? '<div class="check-row" id="acc-' + e(a.id) + '"><input type="checkbox" id="ch-' + e(a.id) + '"><span>I have read and accept all terms of this agreement</span></div>' +
              '<button id="sign-' + e(a.id) + '" class="btn btn-primary" style="margin-top:.5rem;" disabled data-aid="' + e(a.id) + '">✍️ Sign Agreement</button>' : '') +
            '</div>';
        }).join('');
        document.getElementById('data').innerHTML = html;

        results.forEach(function (r) {
          if (!r.ag || r.ag.signedByCustomer) return;
          var ch = document.getElementById('ch-' + r.a.id);
          var btn = document.getElementById('sign-' + r.a.id);
          if (ch && btn) {
            ch.onchange = function () { btn.disabled = !this.checked; };
            btn.onclick = async function () {
              try { await api.post('/api/agreements/' + r.a.id + '/sign', {}); customerAgreement(); shell.message('Agreement signed! ✅', 'success'); }
              catch (err) { shell.message(err.message, 'error'); }
            };
          }
        });
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     CUSTOMER — RENT PAYMENT GATEWAY  (RBI para 2.2)
     ============================================================ */
  function customerRentPayment() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">ANNUAL RENT — RBI PARA 2.2</p><h1>Pay Locker Rent</h1>' +
      '<p>Pay your annual locker rent securely. Non-payment for 3 consecutive years may lead to forced closure.</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-rent'
    );

    api.get('/api/locker-assignments/my-assignments').then(function (list) {
      var paid = (list || []).filter(function (a) { return a.requestStatus === 'PAID'; });
      if (!paid.length) {
        document.getElementById('data').innerHTML = '<div class="empty"><div class="empty-icon">💳</div><h3>No active lockers</h3><a class="btn btn-primary" href="#/customer-dashboard">Dashboard</a></div>';
        return;
      }

      var html = paid.map(function (a, idx) {
        var aid = e(a.id), ln = e(a.locker && a.locker.lockerNumber);
        var annualRent = a.locker && a.locker.price ? parseFloat(a.locker.price).toLocaleString('en-IN') : 'N/A';
        var rentDue = a.nextRentDueDate ? d(a.nextRentDueDate) : 'Not set';
        var unpaid = a.consecutiveUnpaidYears || 0;
        return '<div class="panel" style="margin-bottom:1.5rem;">' +
          '<h3>🔐 Locker ' + ln + ' — Annual Rent: ₹' + annualRent + ' / year</h3>' +
          (unpaid > 0 ? '<div class="notice error">⚠️ ' + unpaid + ' consecutive year(s) of unpaid rent. Forced closure after 3 years (RBI 6.3.1).</div>' : '') +
          '<div style="display:grid;grid-template-columns:1fr 1fr;gap:.6rem;margin:.8rem 0;">' +
            '<div class="stat-card ac-blue"><div class="sc-label">Rent Paid Until</div><div>' + (a.rentPaidUntil ? d(a.rentPaidUntil) : '—') + '</div></div>' +
            '<div class="stat-card ' + (unpaid > 0 ? 'ac-amber' : 'ac-green') + '"><div class="sc-label">Next Due</div><div>' + rentDue + '</div></div>' +
          '</div>' +
          '<div id="gw-' + idx + '" class="gateway-tabs">' +
            '<div class="gw-tab-bar">' +
              '<button class="gw-tab active" data-tab="upi" data-idx="' + idx + '">📱 UPI</button>' +
              '<button class="gw-tab" data-tab="card" data-idx="' + idx + '">💳 Card</button>' +
              '<button class="gw-tab" data-tab="netbanking" data-idx="' + idx + '">🏦 Net Banking</button>' +
              '<button class="gw-tab" data-tab="offline" data-idx="' + idx + '">🏢 Offline</button>' +
            '</div>' +
            '<div class="gw-content">' +
              '<div class="gw-pane active" id="gp-upi-' + idx + '">' +
                '<div class="upi-qr">📱 Scan to pay ₹' + annualRent + '</div>' +
                '<input class="form-input" id="upi-id-' + idx + '" placeholder="Enter UPI ID (e.g. name@upi)" style="margin-top:.8rem;">' +
              '</div>' +
              '<div class="gw-pane" id="gp-card-' + idx + '">' +
                '<div class="card-3d" id="card3d-' + idx + '">' +
                  '<div class="card-front"><div class="card-chip">▤</div><div class="card-num">•••• •••• •••• ____</div><div class="card-name">CARD HOLDER</div></div>' +
                '</div>' +
                '<input class="form-input" id="cn-' + idx + '" placeholder="Card number" maxlength="19" style="margin-top:.8rem;">' +
                '<div style="display:flex;gap:.6rem;margin-top:.5rem;"><input class="form-input" id="ex-' + idx + '" placeholder="MM/YY" maxlength="5" style="flex:1"><input class="form-input" id="cv-' + idx + '" placeholder="CVV" maxlength="3" type="password" style="flex:1"></div>' +
              '</div>' +
              '<div class="gw-pane" id="gp-netbanking-' + idx + '">' +
                '<select class="form-select" id="bank-' + idx + '"><option value="">Select Bank</option>' +
                  ['SBI','HDFC','ICICI','Axis','Kotak','PNB','Bank of Baroda'].map(function (b) { return '<option>' + b + '</option>'; }).join('') +
                '</select>' +
                '<p style="color:var(--ink2);font-size:.83rem;margin-top:.5rem;">You will be redirected to your bank\'s secure portal.</p>' +
              '</div>' +
              '<div class="gw-pane" id="gp-offline-' + idx + '">' +
                '<div class="notice info">Pay at the branch with cash or cheque. Bring this receipt reference: <strong>OFFLINE-' + aid.substring(0,8).toUpperCase() + '</strong></div>' +
              '</div>' +
            '</div>' +
          '</div>' +
          '<button class="btn btn-primary btn-wide" id="pay-btn-' + idx + '" style="margin-top:.8rem;" data-aid="' + aid + '" data-idx="' + idx + '">💳 Pay ₹' + annualRent + ' Now</button>' +
          '<details style="margin-top:.8rem;"><summary class="btn btn-ghost btn-sm" style="cursor:pointer;">📜 View Payment History</summary><div id="hist-' + idx + '" style="margin-top:.6rem;"></div></details>' +
        '</div>';
      }).join('');
      document.getElementById('data').innerHTML = html;

      // Wire gateway tabs
      document.querySelectorAll('.gw-tab').forEach(function (btn) {
        btn.onclick = function () {
          var idx = btn.dataset.idx, tab = btn.dataset.tab;
          var gw = document.getElementById('gw-' + idx);
          gw.querySelectorAll('.gw-tab').forEach(function (t) { t.classList.remove('active'); });
          gw.querySelectorAll('.gw-pane').forEach(function (p) { p.classList.remove('active'); });
          btn.classList.add('active');
          var pane = document.getElementById('gp-' + tab + '-' + idx);
          if (pane) pane.classList.add('active');
        };
      });

      // Card flip animation
      paid.forEach(function (_, idx) {
        var cn = document.getElementById('cn-' + idx);
        var card = document.getElementById('card3d-' + idx);
        if (cn && card) {
          cn.oninput = function () {
            var v = this.value.replace(/\D/g,'').substring(0,16);
            this.value = v.replace(/(.{4})/g,'$1 ').trim();
            card.querySelector('.card-num').textContent =
              (v + '________________').substring(0,16).replace(/(.{4})/g,'$1 ').trim();
          };
        }
      });

      // Wire pay buttons
      paid.forEach(function (a, idx) {
        var btn = document.getElementById('pay-btn-' + idx);
        if (!btn) return;
        btn.onclick = async function () {
          var gw = document.getElementById('gw-' + idx);
          var activeTab = gw.querySelector('.gw-tab.active').dataset.tab;
          var dto = { paymentMethod: activeTab.toUpperCase() };
          if (activeTab === 'upi') dto.upiId = (document.getElementById('upi-id-' + idx) || {}).value || '';
          if (activeTab === 'card') dto.cardNumber = (document.getElementById('cn-' + idx) || {}).value || '';
          if (activeTab === 'netbanking') dto.bankName = (document.getElementById('bank-' + idx) || {}).value || '';
          btn.disabled = true; btn.textContent = '⏳ Processing…';
          try {
            var result = await api.post('/api/rent/' + a.id + '/pay', dto);
            btn.textContent = '✅ Paid!';
            btn.style.background = 'var(--success)';
            shell.message('Rent paid successfully! Transaction ID: ' + result.transactionId, 'success');
            // Load history
            var hist = document.getElementById('hist-' + idx);
            if (hist) loadRentHistory(a.id, hist);
          } catch (err) {
            btn.disabled = false; btn.textContent = '💳 Retry Payment';
            btn.style.background = '';
            shell.message(err.message, 'error');
          }
        };

        // Load history on open
        var histEl = document.getElementById('hist-' + idx);
        if (histEl) loadRentHistory(a.id, histEl);
      });

      function loadRentHistory(aid, el) {
        api.get('/api/rent/' + aid + '/history').then(function (items) {
          if (!items || !items.length) { el.innerHTML = '<p style="color:var(--ink2);font-size:.85rem;">No payments yet.</p>'; return; }
          el.innerHTML = '<table class="dt-table"><thead><tr><th>Year</th><th>Amount</th><th>Method</th><th>Date</th><th>Receipt</th></tr></thead><tbody>' +
            items.map(function (p) {
              return '<tr><td>' + e(String(p.paymentYear)) + '</td><td>₹' + e(String(p.amount)) + '</td>' +
                '<td>' + b(p.paymentMethod) + '</td><td>' + d(p.paidAt) + '</td>' +
                '<td><code style="font-size:.78rem;background:var(--bg3);padding:.1rem .4rem;border-radius:4px;">' + e(p.receiptNumber) + '</code></td></tr>';
            }).join('') + '</tbody></table>';
        }).catch(function () {});
      }
    }).catch(showError('data'));
  }

  /* ============================================================
     CUSTOMER — LOCKER CLOSURE  (RBI Part VI)
     ============================================================ */
  function customerClosure() {
    if (!guarded('CUSTOMER')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">RBI PART VI</p><h1>Close My Locker</h1>' +
      '<p>Initiate voluntary closure or file a death-claim for your locker. All closures follow RBI guidelines.</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'customer-closure'
    );

    api.get('/api/locker-assignments/my-assignments').then(function (list) {
      var active = (list || []).filter(function (a) {
        return a.requestStatus === 'PAID' && (!a.closureStatus || a.closureStatus === 'NONE');
      });
      var inClosure = (list || []).filter(function (a) {
        return a.closureStatus && a.closureStatus !== 'NONE';
      });

      var html = '';

      if (inClosure.length) {
        html += '<div class="panel" style="margin-bottom:1rem;"><h2>🔄 Pending Closures</h2>' +
          inClosure.map(function (a) {
            return '<div class="closure-track">' +
              '<h4>Locker ' + e(a.locker && a.locker.lockerNumber) + ' — ' + b(a.closureType) + '</h4>' +
              '<div class="timeline">' +
                closureStep('Requested', a.closureStatus, ['REQUESTED','NOTICE_ISSUED','IN_PROGRESS','COMPLETED']) +
                closureStep('Notice Issued', a.closureStatus, ['NOTICE_ISSUED','IN_PROGRESS','COMPLETED']) +
                closureStep('In Progress', a.closureStatus, ['IN_PROGRESS','COMPLETED']) +
                closureStep('Completed', a.closureStatus, ['COMPLETED']) +
              '</div></div>';
          }).join('') + '</div>';
      }

      if (!active.length) {
        html += '<div class="empty"><div class="empty-icon">✅</div><h3>No eligible lockers for closure</h3>' +
          '<p>You have no active paid lockers, or all are already in the closure process.</p></div>';
      } else {
        html += '<div class="panel"><h2>Initiate Closure</h2>' +
          '<div class="tab-bar" style="margin-bottom:1rem;">' +
            '<button class="tab-btn active" data-ctab="normal">🔓 Normal Closure</button>' +
            '<button class="tab-btn" data-ctab="death">⚰️ Death Claim</button>' +
          '</div>' +

          '<div id="ct-normal">' +
          active.map(function (a) {
            return '<div style="margin-bottom:.8rem;">' +
              '<h4>Locker ' + e(a.locker && a.locker.lockerNumber) + '</h4>' +
              '<form id="nclose-' + e(a.id) + '" data-aid="' + e(a.id) + '" class="inline-form">' +
                '<div class="field"><label>Reason</label><input class="form-input" name="reason" required placeholder="e.g. No longer needed / Lost key"></div>' +
                '<div class="field" style="flex:none;align-self:flex-end;"><button class="btn btn-danger" type="submit">Request Closure</button></div>' +
              '</form></div>';
          }).join('') + '</div>' +

          '<div id="ct-death" style="display:none;">' +
          active.map(function (a) {
            return '<div style="margin-bottom:.8rem;">' +
              '<h4>Locker ' + e(a.locker && a.locker.lockerNumber) + '</h4>' +
              '<p class="sc-sub">RBI para 5.2.4: Bank must settle death claims within <strong>15 days</strong> of proof of death.</p>' +
              '<form id="dclose-' + e(a.id) + '" data-aid="' + e(a.id) + '" class="inline-form" style="flex-wrap:wrap;">' +
                '<div class="field"><label>Death Certificate URL *</label><input class="form-input" name="deathCertificateUrl" type="url" required placeholder="https://..."></div>' +
                '<div class="field"><label>Claimant Name & Relation</label><input class="form-input" name="claimantDetails" required placeholder="e.g. Jane Smith, Spouse"></div>' +
                '<div class="field" style="flex:none;align-self:flex-end;"><button class="btn btn-danger" type="submit">File Death Claim</button></div>' +
              '</form></div>';
          }).join('') + '</div>' +
        '</div>';
      }

      document.getElementById('data').innerHTML = html;

      // Wire tab switching
      document.querySelectorAll('[data-ctab]').forEach(function (btn) {
        btn.onclick = function () {
          document.querySelectorAll('[data-ctab]').forEach(function (b) { b.classList.remove('active'); });
          btn.classList.add('active');
          document.getElementById('ct-normal').style.display = btn.dataset.ctab === 'normal' ? '' : 'none';
          document.getElementById('ct-death').style.display  = btn.dataset.ctab === 'death' ? '' : 'none';
        };
      });

      // Wire normal closure forms
      active.forEach(function (a) {
        var nf = document.getElementById('nclose-' + a.id);
        if (nf) nf.onsubmit = async function (ev) {
          ev.preventDefault();
          if (!confirm('Confirm normal closure for Locker ' + (a.locker && a.locker.lockerNumber) + '?')) return;
          try { await api.post('/api/closure/' + a.id + '/normal', Object.fromEntries(new FormData(nf))); customerClosure(); shell.message('Closure requested!', 'success'); }
          catch (err) { shell.message(err.message, 'error'); }
        };
        var df = document.getElementById('dclose-' + a.id);
        if (df) df.onsubmit = async function (ev) {
          ev.preventDefault();
          try { await api.post('/api/closure/' + a.id + '/death', Object.fromEntries(new FormData(df))); customerClosure(); shell.message('Death claim filed! Bank will process within 15 days (RBI 5.2.4).', 'success'); }
          catch (err) { shell.message(err.message, 'error'); }
        };
      });
    }).catch(showError('data'));

    function closureStep(label, icon, done, current) {
      var cls = done ? 'done' : current ? 'current' : '';
      return '<div class="tl-step ' + cls + '">' +
        '<div class="tl-dot">' + (done ? '✓' : (current ? icon : '')) + '</div>' +
        '<div class="tl-label">' + label + '</div>' +
      '</div>';
    }
  }

  /* ============================================================
     EMPLOYEE — ALL NOMINEES VIEW
     ============================================================ */
  function employeeNominee() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">NOMINATION RECORDS</p><h1>All Nominees</h1>' +
      '<p>View all registered nominees across all active locker assignments.</p></div>' +
      '<div class="tb-bar"><input class="tb-search" id="nom-s" placeholder="🔍 Search by name or assignment…"></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-nominee'
    );

    api.get('/api/locker-assignments/approved').then(function (appr) {
      return api.get('/api/locker-assignments/pending').then(function (pend) {
        // Get all paid assignments
        return api.get('/api/locker-assignments/approved');
      });
    }).catch(function () { return []; });

    // Load all paid assignments then get nominees for each
    var allAssignments = [];
    api.get('/api/locker-assignments/approved').catch(function () { return []; }).then(function (appr) {
      allAssignments = appr || [];
      if (!allAssignments.length) {
        document.getElementById('data').innerHTML = '<div class="empty"><div class="empty-icon">📋</div><h3>No assignments found</h3></div>';
        return;
      }
      Promise.all(allAssignments.map(function (a) {
        return api.get('/api/nominees/employee/' + a.id).catch(function () { return []; }).then(function (noms) {
          return { a: a, noms: noms || [] };
        });
      })).then(function (results) {
        var all = [];
        results.forEach(function (r) {
          r.noms.forEach(function (n) {
            all.push({ n: n, a: r.a });
          });
        });
        if (!all.length) {
          document.getElementById('data').innerHTML = '<div class="empty"><div class="empty-icon">📋</div><h3>No nominees registered yet</h3></div>';
          return;
        }
        var rows = all.map(function (item) {
          var n = item.n, a = item.a;
          return '<tr class="nr" data-q="' + e((n.name + ' ' + (a.locker && a.locker.lockerNumber || '') + ' ' + (a.customer && a.customer.fullName || '')).toLowerCase()) + '">' +
            '<td>' + e(n.name) + '</td><td>' + e(n.relationship) + '</td>' +
            '<td>' + e(a.customer && a.customer.fullName) + '</td>' +
            '<td>' + e(a.locker && a.locker.lockerNumber) + '</td>' +
            '<td><span class="cbadge cbadge-blue">' + e(n.formType) + '</span></td>' +
            '<td>' + (n.minor ? '👶 Yes' : '—') + '</td>' +
            '<td>' + d(n.createdAt) + '</td></tr>';
        }).join('');
        document.getElementById('data').innerHTML =
          '<div class="panel"><h2>Nominees <span class="cbadge cbadge-blue">' + all.length + '</span></h2>' +
          '<div class="table-wrap"><table class="dt-table"><thead><tr><th>Nominee</th><th>Relation</th><th>Customer</th><th>Locker</th><th>Form</th><th>Minor</th><th>Registered</th></tr></thead><tbody>' + rows + '</tbody></table></div></div>';
        document.getElementById('nom-s').oninput = function () {
          var q = this.value.toLowerCase();
          document.querySelectorAll('.nr').forEach(function (r) { r.style.display = r.dataset.q.includes(q) ? '' : 'none'; });
        };
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     EMPLOYEE — AGREEMENTS MANAGEMENT
     ============================================================ */
  function employeeAgreements() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">RBI PARA 2.1</p><h1>Locker Agreements</h1>' +
      '<p>Generate, view, and renew Board-approved locker agreements. Renewal due by Jan 1, 2023 (RBI 2.1.1).</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-agreements'
    );

    api.get('/api/locker-assignments/approved').catch(function () { return []; }).then(function (list) {
      if (!list || !list.length) {
        document.getElementById('data').innerHTML = '<div class="empty"><div class="empty-icon">📄</div><h3>No approved assignments</h3></div>';
        return;
      }
      Promise.all(list.map(function (a) {
        return api.get('/api/agreements/' + a.id).catch(function () { return null; }).then(function (ag) {
          return { a: a, ag: ag };
        });
      })).then(function (results) {
        var rows = results.map(function (r) {
          var a = r.a, ag = r.ag;
          return '<tr><td>' + e(a.customer && a.customer.fullName) + '</td>' +
            '<td>' + e(a.locker && a.locker.lockerNumber) + '</td>' +
            '<td>' + (ag ? '<span class="cbadge cbadge-green">Generated</span>' : '<span class="cbadge cbadge-amber">None</span>') + '</td>' +
            '<td>' + (ag && ag.signedByCustomer ? '<span class="cbadge cbadge-green">✅ Signed</span>' : '<span class="cbadge cbadge-amber">Pending</span>') + '</td>' +
            '<td>' + (ag ? d(ag.renewalDue) : '—') + '</td>' +
            '<td>' +
              (!ag ? '<button class="btn btn-sm btn-primary" data-gen="' + e(a.id) + '">Generate</button> ' : '') +
              (ag ? '<button class="btn btn-sm btn-ghost" data-renew="' + e(a.id) + '">Renew</button>' : '') +
            '</td></tr>';
        }).join('');
        document.getElementById('data').innerHTML =
          '<div class="panel"><h2>Agreements</h2>' +
          '<div class="table-wrap"><table class="dt-table"><thead><tr><th>Customer</th><th>Locker</th><th>Status</th><th>Customer Signed</th><th>Renewal Due</th><th>Action</th></tr></thead><tbody>' + rows + '</tbody></table></div></div>';

        document.querySelectorAll('[data-gen]').forEach(function (btn) {
          btn.onclick = async function () {
            try { await api.post('/api/agreements/' + btn.dataset.gen, {}); employeeAgreements(); shell.message('Agreement generated!', 'success'); }
            catch (err) { shell.message(err.message, 'error'); }
          };
        });
        document.querySelectorAll('[data-renew]').forEach(function (btn) {
          btn.onclick = async function () {
            try { await api.post('/api/agreements/' + btn.dataset.renew + '/renew', {}); employeeAgreements(); shell.message('Agreement renewed!', 'success'); }
            catch (err) { shell.message(err.message, 'error'); }
          };
        });
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     EMPLOYEE — RENT DUES DASHBOARD
     ============================================================ */
  function employeeRentDues() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">RBI PARA 6.3</p><h1>Rent Dues Tracker</h1>' +
      '<p>Monitor overdue rent payments. Force closure initiation after 3 consecutive unpaid years (RBI 6.3.1).</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-rent-dues'
    );

    api.get('/api/rent/overdue').catch(function () { return []; }).then(function (overdue) {
      overdue = overdue || [];
      var critical = overdue.filter(function (a) { return (a.consecutiveUnpaidYears || 0) >= 3; });
      var warning  = overdue.filter(function (a) { return (a.consecutiveUnpaidYears || 0) > 0 && (a.consecutiveUnpaidYears || 0) < 3; });

      document.getElementById('data').innerHTML =
        '<div class="stat-grid">' +
          '<div class="stat-card ac-red"><div class="sc-label">🚨 Closure Eligible (3+ yrs)</div><div class="sc-value">' + critical.length + '</div></div>' +
          '<div class="stat-card ac-amber"><div class="sc-label">⚠️ Overdue (1-2 yrs)</div><div class="sc-value">' + warning.length + '</div></div>' +
          '<div class="stat-card ac-blue"><div class="sc-label">📊 Total Overdue</div><div class="sc-value">' + overdue.length + '</div></div>' +
        '</div>' +
        '<div class="charts-row" style="margin:1rem 0;">' +
          '<div class="chart-card"><h3>Unpaid Years Distribution</h3><div class="chart-box"><canvas id="rent-bar"></canvas></div></div>' +
        '</div>' +
        '<div class="panel" style="margin-top:1rem;">' +
          '<h2>Overdue Rent List</h2>' +
          (!overdue.length ? '<div class="empty"><div class="empty-icon">✅</div><h3>No overdue rents!</h3></div>' :
            '<div class="table-wrap"><table class="dt-table"><thead><tr><th>Customer</th><th>Locker</th><th>Unpaid Years</th><th>Next Due</th><th>Action</th></tr></thead><tbody>' +
            overdue.map(function (a) {
              var yrs = a.consecutiveUnpaidYears || 0;
              return '<tr><td>' + e(a.customer && a.customer.fullName) + '</td>' +
                '<td>' + e(a.locker && a.locker.lockerNumber) + '</td>' +
                '<td><span class="cbadge ' + (yrs >= 3 ? 'cbadge-red' : 'cbadge-amber') + '">' + yrs + ' year(s)</span></td>' +
                '<td>' + (a.nextRentDueDate ? d(a.nextRentDueDate) : '—') + '</td>' +
                '<td>' + (yrs >= 3 ? '<button class="btn btn-sm btn-danger" data-force="' + e(a.id) + '">Force Close (RBI 6.3)</button>' : '—') + '</td></tr>';
            }).join('') + '</tbody></table></div>') +
        '</div>';

      // Render chart
      var yrCounts = [0, 0, 0, 0];
      overdue.forEach(function (a) {
        var y = Math.min(3, a.consecutiveUnpaidYears || 0);
        yrCounts[y]++;
      });
      var d0 = cd();
      rc('rent-bar', {
        type: 'bar',
        data: {
          labels: ['0 years', '1 year', '2 years', '3+ years'],
          datasets: [{ label: 'Lockers', data: yrCounts,
            backgroundColor: ['rgba(34,197,94,.7)','rgba(245,158,11,.7)','rgba(239,68,68,.6)','rgba(239,68,68,.9)'],
            borderRadius: 6, borderSkipped: false }]
        },
        options: Object.assign({ responsive: true, maintainAspectRatio: false,
          scales: { x: { ticks: { color: d0.textColor }, grid: { color: d0.gridColor } },
                    y: { ticks: { color: d0.textColor, stepSize: 1 }, grid: { color: d0.gridColor }, beginAtZero: true } }
        }, tp())
      });

      // Wire force-close buttons
      document.querySelectorAll('[data-force]').forEach(function (btn) {
        btn.onclick = async function () {
          if (!confirm('Initiate non-payment closure for this locker? (RBI 6.3.1 — 3+ unpaid years)')) return;
          try { await api.post('/api/closure/' + btn.dataset.force + '/non-payment', {}); employeeRentDues(); shell.message('Non-payment closure initiated. Notice period started.', 'success'); }
          catch (err) { shell.message(err.message, 'error'); }
        };
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     EMPLOYEE — LOCKER CLOSURES MANAGEMENT
     ============================================================ */
  function employeeClosures() {
    if (!guarded('EMPLOYEE')) return;
    shell.layout(
      '<div class="page-hd"><p class="kicker">RBI PART VI</p><h1>Locker Closures</h1>' +
      '<p>Review, process, and complete locker closure requests. All types: Normal, Death, Non-Payment, Law Enforcement.</p></div>' +
      '<div id="data">' + loadingHtml() + '</div>',
      'employee-closures'
    );

    api.get('/api/closure/all').catch(function () { return []; }).then(function (all) {
      all = all || [];
      var byType = {};
      all.forEach(function (c) {
        var t = c.closureType || 'OTHER';
        if (!byType[t]) byType[t] = [];
        byType[t].push(c);
      });

      var pending   = all.filter(function (c) { return c.status !== 'COMPLETED'; });
      var completed = all.filter(function (c) { return c.status === 'COMPLETED'; });

      document.getElementById('data').innerHTML =
        '<div class="stat-grid">' +
          '<div class="stat-card ac-amber"><div class="sc-label">⏳ Pending</div><div class="sc-value">' + pending.length + '</div></div>' +
          '<div class="stat-card ac-green"><div class="sc-label">✅ Completed</div><div class="sc-value">' + completed.length + '</div></div>' +
          '<div class="stat-card ac-blue"><div class="sc-label">📊 Total</div><div class="sc-value">' + all.length + '</div></div>' +
        '</div>' +
        '<div class="charts-row" style="margin:1rem 0;">' +
          '<div class="chart-card"><h3>Closure Types</h3><div class="chart-box"><canvas id="cl-donut"></canvas></div></div>' +
          '<div class="chart-card"><h3>Status Overview</h3><div class="chart-box"><canvas id="cl-bar"></canvas></div></div>' +
        '</div>' +
        '<div class="panel" style="margin-top:1rem;">' +
          '<h2>Pending Closures</h2>' +
          (!pending.length ? '<div class="empty"><div class="empty-icon">✅</div><h3>No pending closures</h3></div>' :
            /* ---- REQUESTED closures: Approve or Reject ---- */
            var requested = pending.filter(function (c) { return c.status === 'REQUESTED'; });
            var inProgress = pending.filter(function (c) { return c.status !== 'REQUESTED'; });

            (requested.length ? [
              '<div class="panel" style="margin-bottom:1.2rem;">',
              '<h3 style="color:var(--amber);">⏳ Pending Approval (' + requested.length + ')</h3>',
              '<p class="sc-sub" style="margin-bottom:.8rem;">These customers have requested closure. Approve to release locker as Available, or reject to keep it active.</p>',
              requested.map(function (c) {
                var a = c.assignment || {};
                return '<div class="kyc-rec" style="margin-bottom:.8rem;">' +
                  '<div>' +
                    '<h4>' + b(c.closureType) + ' — Locker ' + e((a.locker && a.locker.lockerNumber) || '—') + '</h4>' +
                    '<p style="font-size:.82rem;color:var(--ink2);">Customer: ' + e((a.customer && a.customer.fullName) || '—') +
                      ' · Reason: ' + e(c.reason || '—') + ' · Requested: ' + d(c.requestedAt) + '</p>' +
                  '</div>' +
                  '<div style="display:flex;gap:.6rem;flex-wrap:wrap;align-items:center;margin-top:.6rem;">' +
                    '<button class="btn btn-primary btn-sm approve-cl" data-cid="' + e(c.id) + '">✅ Approve Closure</button>' +
                    '<input class="form-input" style="max-width:200px;font-size:.82rem;" id="rej-reason-' + e(c.id) + '" placeholder="Rejection reason (optional)">' +
                    '<button class="btn btn-danger btn-sm reject-cl" data-cid="' + e(c.id) + '">❌ Reject</button>' +
                  '</div>' +
                '</div>';
              }).join(''),
              '</div>'
            ] : []).join('') +

            /* ---- IN_PROGRESS / NOTICE_ISSUED closures: Completion form ---- */
            (inProgress.length ?
              '<div class="panel">' +
              '<h3 style="color:var(--blue);">📋 In Progress / Notice Issued (' + inProgress.length + ')</h3>' +
              inProgress.map(function (c) {
                var a = c.assignment || {};
                return '<div class="kyc-rec" style="margin-bottom:.8rem;">' +
                  '<div>' +
                    '<h4>' + b(c.closureType) + ' — Locker ' + e((a.locker && a.locker.lockerNumber) || '—') + '</h4>' +
                    '<p style="font-size:.82rem;color:var(--ink2);">Customer: ' + e((a.customer && a.customer.fullName) || '—') +
                      ' · Status: ' + b(c.status) + ' · Requested: ' + d(c.requestedAt) + '</p>' +
                    (c.noticeDueDate ? '<p style="font-size:.8rem;color:var(--ink2);">Notice Due: ' + d(c.noticeDueDate) + '</p>' : '') +
                  '</div>' +
                  '<form data-close-id="' + e(c.id) + '" class="close-form inline-form" style="flex-wrap:wrap;">' +
                    '<div class="field" style="flex:1 0 100%"><label>Inventory Details *</label><textarea class="form-input" name="inventoryDetails" rows="2" required placeholder="List all items found in locker…"></textarea></div>' +
                    '<div class="field"><label>Witness 1</label><input class="form-input" name="witness1Name" required placeholder="First witness name"></div>' +
                    '<div class="field"><label>Witness 2</label><input class="form-input" name="witness2Name" required placeholder="Second witness name"></div>' +
                    '<div class="field"><label>Video URL</label><input class="form-input" name="videoUrl" type="url" placeholder="https://… (RBI 6.3.2)"></div>' +
                    (c.closureType === 'NON_PAYMENT' ? '<div class="field" style="flex:1 0 100%"><label>Newspaper Notice Details</label><input class="form-input" name="newspaperNoticeDetails" placeholder="Two newspaper dailies (RBI 6.3.2)"></div>' : '') +
                    '<div class="field" style="flex:none;align-self:flex-end;"><button class="btn btn-primary" type="submit">✅ Complete Closure</button></div>' +
                  '</form></div>';
              }).join('') +
              '</div>' : '') +
        '</div>';

      // Charts
      var types = ['NORMAL','DEATH','NON_PAYMENT','LAW_ENFORCEMENT','INOPERATIVE'];
      var typeLabels = ['Normal','Death','Non-Payment','Law Enforcement','Inoperative'];
      var d0 = cd();
      rc('cl-donut', {
        type: 'doughnut',
        data: {
          labels: typeLabels,
          datasets: [{ data: types.map(function (t) { return (byType[t] || []).length || 0; }),
            backgroundColor: ['rgba(34,197,94,.8)','rgba(239,68,68,.8)','rgba(245,158,11,.8)','rgba(168,85,247,.8)','rgba(59,130,246,.8)'],
            borderWidth: 0, hoverOffset: 6 }]
        },
        options: Object.assign({ responsive: true, maintainAspectRatio: false, cutout: '60%' }, tp())
      });
      rc('cl-bar', {
        type: 'bar',
        data: {
          labels: ['Requested','Notice','In Progress','Completed'],
          datasets: [{ label: 'Count',
            data: ['REQUESTED','NOTICE_ISSUED','IN_PROGRESS','COMPLETED'].map(function (s) {
              return all.filter(function (c) { return c.status === s; }).length;
            }),
            backgroundColor: ['rgba(245,158,11,.7)','rgba(168,85,247,.7)','rgba(59,130,246,.7)','rgba(34,197,94,.7)'],
            borderRadius: 6, borderSkipped: false }]
        },
        options: Object.assign({ responsive: true, maintainAspectRatio: false,
          scales: { x: { ticks: { color: d0.textColor }, grid: { color: d0.gridColor } },
                    y: { ticks: { color: d0.textColor, stepSize: 1 }, grid: { color: d0.gridColor }, beginAtZero: true } }
        }, tp())
      });

      // Wire approve buttons
      document.querySelectorAll('.approve-cl').forEach(function (btn) {
        btn.onclick = async function () {
          if (!confirm('Approve this closure? The locker will immediately become AVAILABLE.')) return;
          try {
            await api.put('/api/closure/' + btn.dataset.cid + '/approve', {});
            employeeClosures();
            shell.message('Closure approved. Locker is now Available. Customer notified.', 'success');
          } catch (err) { shell.message(err.message, 'error'); }
        };
      });

      // Wire reject buttons
      document.querySelectorAll('.reject-cl').forEach(function (btn) {
        btn.onclick = async function () {
          var reasonEl = document.getElementById('rej-reason-' + btn.dataset.cid);
          var reason = reasonEl ? reasonEl.value : '';
          if (!confirm('Reject this closure request?')) return;
          try {
            await api.put('/api/closure/' + btn.dataset.cid + '/reject', { reason: reason });
            employeeClosures();
            shell.message('Closure rejected. Customer has been notified.', 'success');
          } catch (err) { shell.message(err.message, 'error'); }
        };
      });

      // Wire completion forms (NON_PAYMENT / LAW_ENFORCEMENT)
      document.querySelectorAll('.close-form').forEach(function (form) {
        form.onsubmit = async function (ev) {
          ev.preventDefault();
          var dto = Object.fromEntries(new FormData(form));
          try { await api.put('/api/closure/' + form.dataset.closeId + '/complete', dto); employeeClosures(); shell.message('Closure completed. Locker released back to available.', 'success'); }
          catch (err) { shell.message(err.message, 'error'); }
        };
      });
    }).catch(showError('data'));
  }

  /* ============================================================
     CHATBOT — VAULTBOT (Grok AI)
     ============================================================ */
  function chatbot() {
    if (!guarded('CUSTOMER') && !guarded('EMPLOYEE')) { utils.go('login'); return; }
    shell.layout(
      '<div class="page-hd"><p class="kicker">AI ASSISTANT</p><h1>VaultBot</h1>' +
      '<p>Your intelligent locker assistant powered by Grok AI. Ask anything about lockers, RBI guidelines, nominations, rent, or closure.</p></div>' +
      '<div class="chat-wrap">' +
        '<div id="chat-messages" class="chat-messages">' +
          '<div class="chat-msg assistant">' +
            '<div class="chat-avatar">🤖</div>' +
            '<div class="chat-bubble">👋 Hi! I\'m <strong>VaultBot</strong>, your locker assistant.<br><br>' +
            'I can help with:<br>• 📋 Nominee registration (RBI Forms SL1/SL2/SL3)<br>• 🔒 Locker closure procedures<br>• 💳 Rent payment & overdue tracking<br>• 📄 Locker agreements<br>• ⚖️ Bank liability & compensation<br><br>What would you like to know?</div>' +
          '</div>' +
        '</div>' +
        '<div class="chat-input-row">' +
          '<div class="chat-suggestions">' +
            '<button class="chat-pill" data-q="How do I add a nominee?">Add nominee</button>' +
            '<button class="chat-pill" data-q="What happens if I don\'t pay rent for 3 years?">Non-payment</button>' +
            '<button class="chat-pill" data-q="How do I close my locker?">Close locker</button>' +
            '<button class="chat-pill" data-q="What is the bank\'s liability for theft?">Bank liability</button>' +
          '</div>' +
          '<div class="chat-form">' +
            '<textarea id="chat-in" class="chat-textarea" placeholder="Ask VaultBot anything about your locker…" rows="1"></textarea>' +
            '<button id="chat-send" class="btn btn-primary">Send ✈️</button>' +
          '</div>' +
        '</div>' +
      '</div>',
      'chatbot'
    );

    var history = [];

    function appendMsg(role, text) {
      var msgs = document.getElementById('chat-messages');
      if (!msgs) return;
      var div = document.createElement('div');
      div.className = 'chat-msg ' + role;
      div.innerHTML = '<div class="chat-avatar">' + (role === 'user' ? '👤' : '🤖') + '</div>' +
        '<div class="chat-bubble">' + (role === 'assistant' ? text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>') : e(text)) + '</div>';
      msgs.appendChild(div);
      msgs.scrollTop = msgs.scrollHeight;
    }

    function sendMessage(msg) {
      if (!msg || !msg.trim()) return;
      appendMsg('user', msg);
      history.push({ role: 'user', content: msg });
      var inp = document.getElementById('chat-in');
      if (inp) inp.value = '';
      var sendBtn = document.getElementById('chat-send');
      if (sendBtn) { sendBtn.disabled = true; sendBtn.textContent = '⏳'; }

      // Typing indicator
      var msgs = document.getElementById('chat-messages');
      var typing = document.createElement('div');
      typing.className = 'chat-msg assistant';
      typing.id = 'typing-indicator';
      typing.innerHTML = '<div class="chat-avatar">🤖</div><div class="chat-bubble"><span class="typing-dots"><span></span><span></span><span></span></span></div>';
      if (msgs) msgs.appendChild(typing);
      if (msgs) msgs.scrollTop = msgs.scrollHeight;

      api.post('/api/chatbot/message', { message: msg, history: history.slice(-10) })
        .then(function (res) {
          var tyInd = document.getElementById('typing-indicator');
          if (tyInd) tyInd.remove();
          var reply = res.reply || 'Sorry, I could not process your request.';
          appendMsg('assistant', reply);
          history.push({ role: 'assistant', content: reply });
        })
        .catch(function (err) {
          var tyInd = document.getElementById('typing-indicator');
          if (tyInd) tyInd.remove();
          appendMsg('assistant', '❌ Sorry, I encountered an error: ' + err.message);
        })
        .finally(function () {
          var sb = document.getElementById('chat-send');
          if (sb) { sb.disabled = false; sb.textContent = 'Send ✈️'; }
        });
    }

    var sendBtn = document.getElementById('chat-send');
    var inp = document.getElementById('chat-in');
    if (sendBtn) sendBtn.onclick = function () { sendMessage(inp.value.trim()); };
    if (inp) {
      inp.onkeydown = function (ev) {
        if (ev.key === 'Enter' && !ev.shiftKey) { ev.preventDefault(); sendMessage(inp.value.trim()); }
      };
      inp.oninput = function () { this.style.height = 'auto'; this.style.height = Math.min(this.scrollHeight, 120) + 'px'; };
    }
    document.querySelectorAll('.chat-pill').forEach(function (pill) {
      pill.onclick = function () { sendMessage(pill.dataset.q); };
    });
  }

  /* ── Exports ─────────────────────────────────────────────── */
  return {
    home: home, login: function () { auth(false); }, signup: function () { auth(true); },
    customerDashboard: customerDashboard, customerLockers: customerLockers,
    customerBookings: customerBookings, customerKyc: customerKyc,
    employeeDashboard: employeeDashboard, employeeLockers: employeeLockers,
    employeeRequests: employeeRequests, employeeKyc: employeeKyc, employeeVisits: employeeVisits,
    customerNominee: customerNominee, customerAgreement: customerAgreement,
    customerRentPayment: customerRentPayment, customerClosure: customerClosure,
    employeeNominee: employeeNominee, employeeAgreements: employeeAgreements,
    employeeRentDues: employeeRentDues, employeeClosures: employeeClosures,
    chatbot: chatbot
  };
});

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

      /* Build a card + optional pay form for each assignment */
      var cards = list.map(function (a, idx) {
        var acReq = a.requestStatus === 'APPROVED' ? 'ac-green' : a.requestStatus === 'PAID' ? 'ac-blue' : 'ac-amber';
        var acPay = a.paymentStatus === 'PAID' ? 'ac-green' : 'ac-amber';
        var payPanel = (a.requestStatus === 'APPROVED' && a.paymentStatus !== 'PAID')
          ? '<div class="panel" style="margin-top:.8rem;">' +
              '<h3>Pay for Locker ' + e(a.locker && a.locker.lockerNumber) + '</h3>' +
              '<form id="pay-' + idx + '" class="inline-form" data-id="' + e(a.id) + '">' +
                '<div class="field"><label>Payment method</label>' +
                  '<select class="form-select" name="paymentMethod">' +
                    '<option value="ONLINE">Online</option>' +
                    '<option value="OFFLINE">Offline (at branch)</option>' +
                  '</select>' +
                '</div>' +
                '<div class="field" style="flex:none;"><button class="btn btn-primary" type="submit">Pay Now</button></div>' +
              '</form>' +
            '</div>'
          : '';
        return '<div class="panel" style="margin-bottom:1rem;">' +
          '<h3 style="margin-bottom:.8rem;">🔐 Locker ' + e(a.locker && a.locker.lockerNumber) +
            ' <span style="font-size:.82rem;font-weight:400;color:var(--ink2);">' + e(a.locker && a.locker.size) + ' · ' + m(a.locker && a.locker.price) + '/mo</span></h3>' +
          '<div class="stat-grid">' +
            '<div class="stat-card ' + acReq + '">' +
              '<div class="sc-label">📋 Request Status</div>' +
              '<div style="margin-top:.6rem;">' + b(a.requestStatus) + '</div>' +
            '</div>' +
            '<div class="stat-card ' + acPay + '">' +
              '<div class="sc-label">💳 Payment</div>' +
              '<div style="margin-top:.6rem;">' + b(a.paymentStatus) + '</div>' +
              '<div class="sc-sub" style="margin-top:.4rem;">' + (a.paymentDueDate ? 'Due ' + d(a.paymentDueDate) : '') + '</div>' +
            '</div>' +
          '</div>' +
          payPanel +
          '</div>';
      }).join('');

      document.getElementById('data').innerHTML =
        cards +
        '<a class="btn btn-outline" href="#/customer-lockers" style="display:inline-block;margin-top:.5rem;">+ Request another locker</a>';

      /* Wire pay forms */
      list.forEach(function (a, idx) {
        var payForm = document.getElementById('pay-' + idx);
        if (payForm) {
          payForm.onsubmit = async function (ev) {
            ev.preventDefault();
            var form = ev.currentTarget;
            try {
              await api.post('/api/locker-assignments/' + form.dataset.id + '/pay', Object.fromEntries(new FormData(form)));
              customerDashboard();
            } catch (err) { shell.message(err.message, 'error'); }
          };
        }
      });
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
              '<option value="SMALL">SMALL — ₹500/mo</option>' +
              '<option value="MEDIUM">MEDIUM — ₹1,000/mo</option>' +
              '<option value="LARGE">LARGE — ₹2,000/mo</option>' +
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
            '<span class="sz-price">₹' + (price ? Number(price).toLocaleString('en-IN') + ' / mo' : 'N/A') + '</span>' +
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
          '<div class="check-row"><input name="photoMatchFlag" type="checkbox" required><span>Live photo matches Aadhaar photo</span></div>' +
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
      values.photoMatchFlag = form.photoMatchFlag.checked;
      try {
        await api.post('/api/kyc/submit/me', values);
        load();
        shell.message('KYC submitted successfully!', 'success');
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
      document.getElementById('addPr').value = { SMALL: 500, MEDIUM: 1000, LARGE: 2000 }[this.value] || '';
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

  /* ── Exports ─────────────────────────────────────────────── */
  return {
    home: home, login: function () { auth(false); }, signup: function () { auth(true); },
    customerDashboard: customerDashboard, customerLockers: customerLockers,
    customerBookings: customerBookings, customerKyc: customerKyc,
    employeeDashboard: employeeDashboard, employeeLockers: employeeLockers,
    employeeRequests: employeeRequests, employeeKyc: employeeKyc, employeeVisits: employeeVisits
  };
});

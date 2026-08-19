define([], function () {
  'use strict';

  function escape(value) {
    return String(value == null ? '' : value).replace(/[&<>"]/g, function (c) {
      return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c];
    });
  }

  function date(value) {
    return value ? new Date(value).toLocaleString() : '—';
  }

  function money(value) {
    return value == null ? '—' : '₹' + Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2 });
  }

  function badge(value) {
    return '<span class="badge badge-' + escape(value).toLowerCase() + '">' + escape(value) + '</span>';
  }

  function go(route) { location.hash = '#/' + route; }

  /* ── Chart helpers ──────────────────────────────────────── */
  var _chartInstances = {};

  /** Destroy any existing Chart on a canvas, then create a new one. */
  function renderChart(canvasId, config) {
    if (_chartInstances[canvasId]) {
      _chartInstances[canvasId].destroy();
      delete _chartInstances[canvasId];
    }
    var canvas = document.getElementById(canvasId);
    if (!canvas || !window.Chart) return null;
    var chart = new window.Chart(canvas, config);
    _chartInstances[canvasId] = chart;
    return chart;
  }

  /** Shared dark-aware Chart.js defaults */
  function chartDefaults() {
    var isDark = document.documentElement.getAttribute('data-theme') !== 'light';
    return {
      gridColor:  isDark ? 'rgba(255,255,255,.07)' : 'rgba(0,0,0,.07)',
      textColor:  isDark ? '#64748b' : '#94a3b8',
      tooltipBg:  isDark ? '#1e293b' : '#ffffff',
      tooltipText: isDark ? '#f1f5f9' : '#0f172a'
    };
  }

  /** Returns a Chart.js plugin config for the tooltip style */
  function tooltipPlugin() {
    var d = chartDefaults();
    return {
      plugins: {
        tooltip: {
          backgroundColor: d.tooltipBg,
          titleColor: d.tooltipText,
          bodyColor: d.textColor,
          borderColor: 'rgba(14,165,233,.2)',
          borderWidth: 1,
          cornerRadius: 8,
          padding: 10
        },
        legend: {
          labels: { color: d.textColor, font: { size: 11, weight: '600' }, padding: 16, boxWidth: 12, boxHeight: 12 }
        }
      }
    };
  }

  return {
    escape: escape,
    date: date,
    money: money,
    badge: badge,
    go: go,
    renderChart: renderChart,
    chartDefaults: chartDefaults,
    tooltipPlugin: tooltipPlugin
  };
});

/** Application bootstrap. Routing and page implementations live in app/. */
require(['ojs/ojbootstrap', 'ojs/ojcontext', 'app/router'], function (Bootstrap, Context, router) {
  Bootstrap.whenDocumentReady().then(function () {
    window.addEventListener('hashchange', router.render);
    router.render();
    Context.getPageContext().getBusyContext().applicationBootstrapComplete();
  });
});

// Register the service worker (needed for the app to be installable)
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function () {
    navigator.serviceWorker.register('/sw.js').catch(function (err) {
      console.warn('Service worker registration failed:', err);
    });
  });
}

// Show an "Install app" button when the browser allows installing
var deferredPrompt = null;
window.addEventListener('beforeinstallprompt', function (e) {
  e.preventDefault();
  deferredPrompt = e;
  var btn = document.getElementById('installBtn');
  if (btn) {
    btn.hidden = false;
    btn.addEventListener('click', function () {
      btn.hidden = true;
      deferredPrompt.prompt();
      deferredPrompt = null;
    });
  }
});

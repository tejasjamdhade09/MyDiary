// Service worker: caches only static files. Diary pages are never cached,
// so private data is not stored on the device by the worker.
var CACHE = 'diary-v1';
var ASSETS = ['/css/style.css', '/js/app.js', '/offline.html',
              '/icons/icon-192.png', '/icons/icon-512.png'];

self.addEventListener('install', function (event) {
  event.waitUntil(caches.open(CACHE).then(function (c) { return c.addAll(ASSETS); }));
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(
    caches.keys().then(function (keys) {
      return Promise.all(keys.filter(function (k) { return k !== CACHE; })
                             .map(function (k) { return caches.delete(k); }));
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', function (event) {
  var req = event.request;
  if (req.method !== 'GET') return;

  if (req.mode === 'navigate') {
    // Pages: always try the network; show the offline page if it fails
    event.respondWith(fetch(req).catch(function () { return caches.match('/offline.html'); }));
    return;
  }
  var url = new URL(req.url);
  if (url.origin === self.location.origin && ASSETS.indexOf(url.pathname) !== -1) {
    event.respondWith(caches.match(req).then(function (r) { return r || fetch(req); }));
  }
});

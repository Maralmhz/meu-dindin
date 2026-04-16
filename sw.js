const CACHE = 'meudindin-v7';
self.addEventListener('install', e => { self.skipWaiting(); });
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(keys => Promise.all(keys.map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', e => {
  const url = e.request.url;
  if (url.includes('index.html') || url.endsWith('/meu-dindin/') || url.endsWith('/meu-dindin')) {
    e.respondWith(
      fetch(e.request, {cache: 'no-store'}).then(res => {
        return res.text().then(html => {
          if (!html.includes('ai.js')) {
            html = html.replace('</body>', '<script src="/meu-dindin/ai.js"></script></body>');
          }
          return new Response(html, {headers: {'Content-Type': 'text/html; charset=utf-8'}});
        });
      })
    );
  } else {
    e.respondWith(fetch(e.request));
  }
});

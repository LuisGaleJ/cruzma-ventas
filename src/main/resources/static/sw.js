/*
 * Service worker de Cruzma Ventas (PWA).
 * - Permite instalar el sistema en el celular ("Agregar a pantalla de inicio").
 * - Guarda en caché SOLO archivos estáticos propios (CSS, JS, íconos) y la página "Sin conexión".
 * - Las páginas con datos (clientes, ventas, comprobantes) siempre se piden al servidor: nunca se guardan
 *   en el teléfono, así no quedan datos de la empresa en caché ni se muestran cifras desactualizadas.
 * CSS, JS e íconos se piden primero al servidor; la caché es solo el respaldo sin conexión.
 * Al cambiar la lista ESTATICOS, sube la VERSION para que los celulares renueven la caché.
 */
const VERSION = 'cruzma-v2';
const ESTATICOS = [
  '/offline.html',
  '/css/cruzma.css',
  '/js/app.js',
  '/js/tablas.js',
  '/img/icono-192.png',
  '/img/favicon-32.png'
];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(VERSION).then((c) => c.addAll(ESTATICOS)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys()
      .then((claves) => Promise.all(claves.filter((k) => k !== VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;                       // formularios (POST) van directo al servidor
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;        // CDN (Bootstrap, DataTables): el navegador los maneja

  // Páginas: siempre del servidor; si no hay internet, la página "Sin conexión"
  if (req.mode === 'navigate') {
    e.respondWith(fetch(req).catch(() => caches.match('/offline.html')));
    return;
  }

  // Estáticos propios: primero el servidor (así cada despliegue se ve al instante);
  // la copia en caché solo se usa si no hay internet
  if (/^\/(css|js|img)\//.test(url.pathname)) {
    e.respondWith(
      fetch(req).then((resp) => {
        if (resp.ok) {
          const copia = resp.clone();
          caches.open(VERSION).then((cache) => cache.put(req, copia));
        }
        return resp;
      }).catch(() => caches.match(req))
    );
  }
});

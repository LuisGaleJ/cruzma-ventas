/* Cruzma Ventas: comportamiento común de todas las pantallas.
   1) Menú hamburguesa en pantallas pequeñas.  2) Registro del service worker (PWA). */
document.addEventListener('DOMContentLoaded', function () {
  var menu = document.getElementById('menu');
  var boton = document.querySelector('.menu-boton');
  if (menu && boton) {
    var cerrar = function () {
      menu.classList.remove('abierto');
      boton.setAttribute('aria-expanded', 'false');
      boton.setAttribute('aria-label', 'Abrir menú');
    };
    boton.addEventListener('click', function () {
      var abierto = menu.classList.toggle('abierto');
      boton.setAttribute('aria-expanded', abierto ? 'true' : 'false');
      boton.setAttribute('aria-label', abierto ? 'Cerrar menú' : 'Abrir menú');
    });
    // Al elegir una opción o presionar Esc, el menú se cierra
    menu.querySelectorAll('.menu-enlaces a').forEach(function (a) { a.addEventListener('click', cerrar); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') cerrar(); });
  }
});

if ('serviceWorker' in navigator) {
  window.addEventListener('load', function () {
    navigator.serviceWorker.register('/sw.js').catch(function (err) {
      console.warn('No se pudo registrar el service worker', err);
    });
  });
}

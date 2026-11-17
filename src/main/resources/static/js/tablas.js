/*
 * Tablas con DataTables (búsqueda, orden por columna y paginación en el navegador).
 * Configuración común en español para todas las pantallas del sistema.
 * Uso: cruzmaTabla('#tablaClientes', { order: [[1, 'asc']] });
 * En pantallas angostas (celular) la extensión Responsive oculta las columnas que no entran y las muestra
 * al tocar la fila. Siempre quedan visibles la primera columna y la última (botones de acción).
 */
(function () {
    'use strict';

    var ESPANOL = {
        search: 'Buscar:',
        searchPlaceholder: 'Escribe para filtrar',
        lengthMenu: 'Mostrar _MENU_ registros',
        info: 'Mostrando _START_ a _END_ de _TOTAL_ registros',
        infoEmpty: 'Sin registros',
        infoFiltered: '(filtrado de _MAX_ en total)',
        zeroRecords: 'No se encontraron coincidencias',
        emptyTable: 'No hay registros',
        loadingRecords: 'Cargando...',
        paginate: { first: '«', previous: '‹', next: '›', last: '»' },
        aria: { orderable: 'Ordenar por esta columna', orderableReverse: 'Invertir el orden' }
    };

    /**
     * Convierte una tabla HTML en una DataTable.
     * Las columnas con la clase "sin-orden" (por ejemplo, la de botones) no se ordenan ni se buscan.
     * @param {string} selector selector CSS de la tabla
     * @param {object} [opciones] opciones de DataTables que reemplazan a las comunes
     * @returns {object|null} la instancia de DataTables, o null si la tabla no está en la página
     */
    window.cruzmaTabla = function (selector, opciones) {
        var tabla = document.querySelector(selector);
        if (!tabla) return null;                                       // la tabla no se muestra (lista vacía)
        var jq = window.jQuery;
        if (typeof window.DataTable === 'undefined' && !(jq && jq.fn && jq.fn.dataTable)) {
            console.warn('[Cruzma] DataTables no se cargó: revisa en la pestaña Network si el CDN está bloqueado.');
            return null;
        }
        var comunes = {
            language: ESPANOL,
            pageLength: 10,
            lengthMenu: [10, 25, 50, 100],
            responsive: true,
            columnDefs: [
                { targets: 'sin-orden', orderable: false, searchable: false },
                { targets: 0, responsivePriority: 1 },     // primera columna: siempre visible
                { targets: -1, responsivePriority: 2 }     // última columna (acciones): siempre visible
            ]
        };
        var config = Object.assign(comunes, opciones || {});
        return typeof window.DataTable !== 'undefined' ? new window.DataTable(tabla, config) : jq(tabla).DataTable(config);
    };
})();

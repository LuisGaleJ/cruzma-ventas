/*
 * Tablas con DataTables (búsqueda, orden por columna y paginación en el navegador).
 * Configuración común en español para todas las pantallas del sistema.
 * Uso: cruzmaTabla('#tablaClientes', { order: [[1, 'asc']] });
 * En pantallas angostas (celular) la extensión Responsive oculta las columnas que no entran y las muestra
 * al tocar la fila. Qué columnas se quedan lo decide cada tabla con data-priority en su <th>
 * (1 = la más importante, se oculta al último).
 * Con la extensión Buttons, cada tabla tiene botones Excel, CSV e Imprimir. Se exportan todas las filas que
 * pasan el filtro de búsqueda y todas las columnas, incluso las ocultas en el celular, menos la de botones
 * (clase "sin-orden").
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

    /** Texto visible de la celda, en una sola línea (ej. «MTR» y «METRO» en dos renglones → «MTR METRO»). */
    function textoCelda(data, fila, columna, nodo) {
        var texto = nodo ? (nodo.innerText || nodo.textContent || '') : String(data == null ? '' : data);
        return texto.replace(/\s+/g, ' ').trim();
    }

    /** Para CSV: un texto que empieza con = + - @ se marca con comilla para que Excel no lo ejecute como fórmula. */
    function textoCsvSeguro(data, fila, columna, nodo) {
        var texto = textoCelda(data, fila, columna, nodo);
        return /^[=+\-@]/.test(texto) ? "'" + texto : texto;
    }

    /** Título del archivo: el encabezado de la pantalla (Clientes, Productos y servicios, Usuarios). */
    function tituloPantalla() {
        var h1 = document.querySelector('.contenido h1');
        return h1 ? h1.textContent.trim() : document.title;
    }

    /** Botones de exportación; null si la extensión Buttons no se cargó (por ejemplo, CDN bloqueado). */
    function botonesExportar() {
        var dt = window.DataTable || (window.jQuery && window.jQuery.fn.dataTable);
        if (!dt || !dt.Buttons) return null;
        var hoy = new Date().toISOString().slice(0, 10);
        var archivo = function () { return 'Cruzma - ' + tituloPantalla() + ' - ' + hoy; };
        var columnas = ':not(.sin-orden)';
        return [
            { extend: 'excelHtml5', text: 'Excel', title: function () { return 'Grupo Cruzma - ' + tituloPantalla(); },
              filename: archivo, exportOptions: { columns: columnas, format: { body: textoCelda } } },
            { extend: 'csvHtml5', text: 'CSV', filename: archivo, bom: true,
              exportOptions: { columns: columnas, format: { body: textoCsvSeguro } } },
            { extend: 'print', text: 'Imprimir', title: function () { return 'Grupo Cruzma - ' + tituloPantalla(); },
              exportOptions: { columns: columnas, format: { body: textoCelda } } }
        ];
    }

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
            columnDefs: [{ targets: 'sin-orden', orderable: false, searchable: false }]
        };
        var botones = botonesExportar();
        if (botones) comunes.layout = { topStart: ['pageLength', { buttons: botones }] };
        var config = Object.assign(comunes, opciones || {});
        return typeof window.DataTable !== 'undefined' ? new window.DataTable(tabla, config) : jq(tabla).DataTable(config);
    };
})();

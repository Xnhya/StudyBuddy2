/* src/main/resources/static/js/api-consumer.js */

const API_BASE_URL = '/api';

/**
 * Función para consultar DNI
 * @param {string} dni - El número de DNI a consultar
 * @param {function} callback - Función a ejecutar con el resultado
 */
async function consultarDni(dni, callback) {
    if (!dni || dni.length !== 8) {
        alert("El DNI debe tener 8 dígitos.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/external/dni/${dni}`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token') // Si usas token
            }
        });

        const data = await response.json();
        
        if (data.success) {
            callback(data.data); // Llama a la función que actualiza la pantalla
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error en consulta DNI:', error);
        alert('Ocurrió un error al consultar el DNI.');
    }
}

/**
 * Función para consultar Cambio de Moneda
 */
async function consultarCambio(from, to, amount, resultElementId) {
    // Aquí podrías implementar la llamada a tu endpoint de moneda
    console.log(`Convirtiendo ${amount} ${from} a ${to}...`);
}
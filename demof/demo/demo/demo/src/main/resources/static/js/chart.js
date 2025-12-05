/* src/main/resources/static/js/chart.js */

// Función para renderizar un gráfico de actividad
function renderActivityChart(canvasId) {
    const ctx = document.getElementById(canvasId);
    
    if (!ctx) return;

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'],
            datasets: [{
                label: 'Horas de Estudio',
                data: [2, 4, 3, 5, 2, 6, 4],
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.2)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                }
            }
        }
    });
}
import { SENSORS, API } from './constants'

function mapUltimaMedicion(medicion) {
  const data = {}
  SENSORS.forEach(s => {
    data[s.key] = medicion[s.key] ?? null
  })
  return data
}

function formatTime(fechaHora) {
  return new Date(fechaHora).toLocaleTimeString('es-CL', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function buildHistoryFromMediciones(mediciones) {
  const ordenadas = [...mediciones].reverse() // antigua → nueva (izq → der en gráfico)
  const history = {}
  SENSORS.forEach(s => { history[s.key] = [] })
  ordenadas.forEach(m => {
    const t = formatTime(m.fechaHora)
    SENSORS.forEach(s => {
      history[s.key].push({ t, [s.key]: m[s.key] })
    })
  })
  return history
}

export async function downloadReportFromBackend() {
  const response = await fetch(API.report)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  return response.blob()
}

export async function fetchSensorData() {
  const response = await fetch(API.medicionesUltimas)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)

  const mediciones = await response.json()
  if (!mediciones.length) throw new Error('No hay mediciones disponibles')

  return {
    latest: mapUltimaMedicion(mediciones[0]),
    history: buildHistoryFromMediciones(mediciones),
    lastFechaHora: mediciones[0].fechaHora,
  }
}

// POST /api/chat — Formatea las alertas reales obtenidas de la Base de Datos
export async function sendChatMessage(message, sensorData, alerts, conditions) {
  await new Promise(r => setTimeout(r, 600 + Math.random() * 300))
  const low = message.toLowerCase()

  // Respuesta para "resumen", "estado" o "dia"
  if (low.includes('resumen') || low.includes('día') || low.includes('dia') || low.includes('estado')) {
    const summary = SENSORS.map(s => {
      const v = sensorData[s.key]
      const f = v !== undefined ? (s.key === 'lux' || s.key === 'ppm' ? Math.round(v) : Number(v).toFixed(1)) : '—'
      return `• ${s.label}: ${f} ${s.unit}`
    }).join('\n')
    
    return `📊 **Estado actual de mi-aula:**\n\n${summary}\n\nHistorial: El sistema cuenta con **${alerts.length}** alertas guardadas en la base de datos.`
  }

  // Respuesta para "alerta", "historial" o "registro"
  if (low.includes('alerta') || low.includes('historial') || low.includes('registro')) {
    if (!alerts || alerts.length === 0) {
      return '✅ **Registro Limpio:** No se registran alertas en la base de datos. Todos los parámetros ambientales están dentro del rango ideal.'
    }
    
    // Tomamos las últimas 5 alertas de la base de datos y las ordenamos hacia abajo con saltos de línea
    const listaAlertas = alerts.slice(0, 5).map((a, index) => {
      const comportamiento = a.high ? 'Por encima' : 'Por debajo'
      return `${index + 1}. 🚨 **${a.sensor}**: ${comportamiento} del rango (${a.value} ${a.unit}) — 🕒 ${a.time}`
    }).join('\n')

    return `🔔 **Historial de Alertas (Últimos registros en Base de Datos):**\n\n${listaAlertas}\n\n*Mostrando las 5 más recientes de un total de ${alerts.length}.*`
  }

  return `💬 Hola, soy Byte. Puedo ayudarte con el estado de la sala. Prueba haciendo clic en los botones de sugerencias o escribe:\n\n• **"resumen"**: Para ver cómo están los sensores ahora.\n• **"historial"**: Para listar los últimos incidentes de la BD.`
}

export function generateCSV(history) {
  const headers = ['timestamp', ...SENSORS.map(s => `${s.key}(${s.unit})`)]
  const rows = history.map(row =>
    [row.t, ...SENSORS.map(s => {
      const v = row[s.key]
      return v !== undefined ? (s.key === 'lux' || s.key === 'ppm' ? Math.round(v) : Number(v).toFixed(2)) : ''
    })].join(',')
  )
  return [headers.join(','), ...rows].join('\n')
}

export async function fetchSalaConditions() {
  const response = await fetch(API.salas)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  const salas = await response.json()
  if (!salas.length) throw new Error('No hay salas')
  const sala = salas[0]
  return {
    idSala: sala.idSala,
    nombre: sala.nombre,
    size: sala.m2,
    students: sala.cantidadEstudiantes,
    windows: sala.cantidadVentanas,
    ac: sala.aireAcondicionado,
    floor: sala.numeroPiso,
    ventilation: sala.tipoDeVentilacion,
  }
}

export async function updateSalaConditions(id, conditions) {
  const body = {
    cantidadEstudiantes: Number(conditions.students),
    m2: Number(conditions.size),
    cantidadVentanas: Number(conditions.windows),
    aireAcondicionado: conditions.ac,
    numeroPiso: Number(conditions.floor),
    tipoDeVentilacion: conditions.ventilation,
  }

  const response = await fetch(`${API.base}/api/salas/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  return response.json()
}

// Métodos de comunicación con la base de datos a través de Spring Boot
export async function fetchAlerts() {
  const response = await fetch('http://localhost:8080/api/alertas')
  if (!response.ok) {
    throw new Error('Error al obtener el historial de alertas')
  }
  return response.json()
}

export async function saveAlert(alerta) {
  const response = await fetch('http://localhost:8080/api/alertas', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(alerta)
  })
  if (!response.ok) {
    throw new Error('Error al guardar la alerta en el servidor')
  }
  return response.json()
}
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

// POST /api/chat — replace body with real fetch() for production
export async function sendChatMessage(message, sensorData, alerts, conditions) {
  await new Promise(r => setTimeout(r, 850 + Math.random() * 400))
  const low = message.toLowerCase()

  if (low.includes('resumen') || low.includes('día') || low.includes('dia')) {
    const summary = SENSORS.map(s => {
      const v = sensorData[s.key]
      const f = s.key === 'lux' || s.key === 'ppm' ? Math.round(v) : Number(v).toFixed(1)
      return `${s.label}: ${f}${s.unit}`
    }).join(' · ')
    return `📊 Resumen del día: ${summary}. Se registraron ${alerts.length} alertas. Condiciones ${alerts.length < 3 ? 'óptimas para el aprendizaje' : 'con variaciones que requieren atención'}.`
  }

  if (low.includes('alerta')) {
    if (!alerts.length) return '✅ Sin alertas registradas. Todas las condiciones están dentro del rango ideal.'
    const recent = alerts.slice(0, 3).map(a => `${a.sensor} a las ${a.time}`).join(', ')
    return `🚨 Se han registrado ${alerts.length} alertas. Las más recientes: ${recent}.`
  }

  return `💬 Los sensores muestran valores ${alerts.length > 2 ? 'con algunas variaciones' : 'estables'}. ¿Te explico algún parámetro en detalle?`
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
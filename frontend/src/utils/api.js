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

export async function fetchInformes() {
  const response = await fetch(API.informes)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  return response.json()
}

export async function downloadInformePdf(informeId) {
  const response = await fetch(`${API.informes}/${informeId}/pdf`)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  return response.blob()
}

export async function downloadMedicionesCsv() {
  const response = await fetch(`${API.report}`)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  return response.blob()
}

export async function fetchReportMetadata() {
  const response = await fetch(`${API.report}/info`)
  if (!response.ok) throw new Error(`Error HTTP ${response.status}`)
  return response.json()
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

// POST /api/chat — Asistente IA con contexto compacto (Gemini vía backend)
export async function sendChatMessage(message) {
  const response = await fetch(API.chat, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mensaje: message }),
  })

  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    throw new Error(data.error || `Error HTTP ${response.status}`)
  }

  return data.respuesta
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
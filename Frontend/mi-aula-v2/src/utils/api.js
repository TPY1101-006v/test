import { SENSORS } from './constants'

function rand(min, max) { return Math.random() * (max - min) + min }

function generateValue(sensor) {
  const { min, max } = sensor
  const range = max - min
  const center = (max + min) / 2
  if (Math.random() < 0.15)
    return Math.random() < 0.5 ? min - rand(2, range * 0.3) : max + rand(2, range * 0.3)
  return center + (Math.random() - 0.5) * range * 0.6
}

// GET /api/sensors — replace body with real fetch() for production
export async function fetchSensorData() {
  await new Promise(r => setTimeout(r, 300))
  const data = {}
  SENSORS.forEach(s => { data[s.key] = generateValue(s) })
  return data
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

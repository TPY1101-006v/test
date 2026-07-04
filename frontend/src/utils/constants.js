// Sensor definitions — ideal values based on learning environment research
export const SENSORS = [
  {
    key: 'temperatura',
    label: 'Temperatura',
    unit: '°C',
    icon: 'device_thermostat',
    ideal: 21,
    min: 20,
    max: 22,
    color: '#63b3ed',
    description: 'Confort térmico óptimo para concentración (20–22 °C)',
  },
  {
    key: 'humedad',
    label: 'Humedad',
    unit: '%',
    icon: 'water_drop',
    ideal: 50,
    min: 40,
    max: 60,
    color: '#68d391',
    description: 'Humedad relativa óptima para rendimiento cognitivo (40–60 %)',
  },
  {
    key: 'db',
    label: 'Decibeles',
    unit: 'dBA',
    icon: 'graphic_eq',
    ideal: 35,
    min: 35,
    max: 45,
    alertHighOnly: true,
    color: '#008ba3',
    description: 'Ruido de fondo — ≤35 dBA (aula vacía) hasta 45 dBA (actividades ligeras)',
  },
  {
    key: 'lux',
    label: 'Iluminación',
    unit: 'lx',
    icon: 'light_mode',
    ideal: 400,
    min: 300,
    max: 500,
    color: '#e7e300',
    description: 'Iluminación óptima para tareas de aula y estímulo circadiano (300–500 lx)',
  },
  {
    key: 'eco2',
    label: 'Dioxido de Carbono',
    unit: 'ppm',
    icon: 'air',
    ideal: 600,
    min: 400,
    max: 800,
    alertHighOnly: true,
    color: '#9145ed',
    description: 'CO₂ ideal 400–800 ppm; por encima reduce rendimiento cognitivo y ventilación',
  },
  {
    key: 'tvoc',
    label: 'Compuestos Orgánicos Volátiles',
    unit: 'ppb',
    icon: 'air',
    ideal: 110,
    min: 0,
    max: 220,
    alertHighOnly: true,
    color: '#fa3bc4',
    description: 'TVOC ideal 0–220 ppb; por encima puede causar síndrome del edificio enfermo',
  },
]

export const VENTILATION_TYPES = [
  { value: 'cruzada', label: 'Ventilación cruzada' },
  { value: 'unilateral', label: 'Ventilación unilateral' },
  { value: 'ninguna', label: 'Sin ventilación' },
]

export const VENTILATION_HELP = {
  cruzada: 'El aire entra por un lado y sale por el opuesto; renueva mejor el ambiente.',
  unilateral: 'Las ventanas están del mismo lado; la circulación de aire es limitada.',
  ninguna: 'No hay renovación natural del aire (sin ventanas útiles o cerradas de forma permanente).',
}

export const UPDATE_INTERVAL = 300

export const API = {
  base: 'http://localhost:8080',
  medicionesUltimas: 'http://localhost:8080/api/mediciones/ultimas',
  report: 'http://localhost:8080/api/report',
  informes: 'http://localhost:8080/api/informes',
  chat: 'http://localhost:8080/api/chat',
  salas: 'http://localhost:8080/api/salas',
}

export function getSensor(key) {
  return SENSORS.find(s => s.key === key)
}

// Detecta valores físicamente imposibles.
// Ejemplos:
// - CO₂ = -1 ppm → CALIBRAR
// - Lux = -5 lx → CALIBRAR
// - Humedad = -10 % → CALIBRAR
export function isCalibrationValue(key, value) {
  const sensoresEstrictos = ['eco2', 'tvoc', 'lux', 'humedad', 'db']
  const n = Number(value)

  return (
    value !== null &&
    value !== undefined &&
    !Number.isNaN(n) &&
    n < 0 &&
    sensoresEstrictos.includes(key)
  )
}

// Mismo redondeo que usa la UI.
// Evita alertas falsas en los bordes del rango.
export function roundSensorValue(key, value) {
  const n = Number(value)

  if (value === null || value === undefined || Number.isNaN(n)) {
    return null
  }

  if (key === 'lux' || key === 'eco2' || key === 'tvoc') {
    return Math.round(n)
  }

  return Math.round(n * 10) / 10
}

export function formatSensorValue(key, value) {
  const rounded = roundSensorValue(key, value)

  if (rounded === null) {
    return '—'
  }

  if (key === 'lux' || key === 'eco2' || key === 'tvoc') {
    return String(rounded)
  }

  return rounded.toFixed(1)
}

export function isOutOfRange(key, value) {
  const s = getSensor(key)
  const v = roundSensorValue(key, value)

  if (!s || v === null) {
    return false
  }

  // Si es un valor imposible, no lo tratamos como alerta ambiental.
  // Lo tratamos como problema de calibración.
  if (isCalibrationValue(key, value)) {
    return false
  }

  // Sensores donde solo importa cuando suben demasiado.
  // Ejemplo: CO₂, ruido, TVOC.
  if (s.alertHighOnly) {
    return v > s.max
  }

  return v < s.min || v > s.max
}

export function severity(key, value) {
  if (isCalibrationValue(key, value)) {
    return 'calibracion'
  }

  if (isOutOfRange(key, value)) {
    return 'alerta'
  }

  return 'ok'
}
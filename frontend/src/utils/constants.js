// Sensor definitions — ideal values based on learning environment research
export const SENSORS = [
  {
    key: 'temperatura',
    label: 'Temperatura',
    unit: '°C',
    icon: '🌡️',
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
    icon: '💧',
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
    icon: '🔊',
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
    icon: '💡',
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
    icon: '🌳',
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
    icon: '🌿',
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

export const UPDATE_INTERVAL = 300 // segundos

// API endpoints — reemplazar con la IP real del ESP8266
export const API = {
  base: 'http://localhost:8080',
  medicionesUltimas: 'http://localhost:8080/api/mediciones/ultimas',
  report: 'http://localhost:8080/api/report',
  informes: 'http://localhost:8080/api/informes',
  chat: 'http://localhost:8080/api/chat',
  salas: 'http://localhost:8080/api/salas',
}
// ====== FUNCIONES DE AYUDA (OBLIGATORIAS PARA EL PARCHE) ======

export function getSensor(key) {
  return SENSORS.find(s => s.key === key)
}

export function isOutOfRange(key, value) {
  const s = getSensor(key)
  if (!s || value === null || value === undefined || Number.isNaN(value)) return false
  if (s.alertHighOnly) return value > s.max
  return value < s.min || value > s.max
}

export function severity(key, value) {
  const s = getSensor(key)
  if (!s || !isOutOfRange(key, value)) return 'ok'
  if (key === 'eco2' && value > 1500) return 'critica'
  if (key === 'db' && value > 60) return 'critica'
  return 'advertencia'
}

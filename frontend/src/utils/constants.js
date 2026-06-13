// Sensor definitions — ideal values based on learning environment research
export const SENSORS = [
  {
    key: 'temperatura',
    label: 'Temperatura',
    unit: '°C',
    icon: '🌡️',
    ideal: 24,      // ~24°C maximiza rendimiento académico
    min: 23,
    max: 26,
    color: '#63b3ed',
    description: 'Confort térmico óptimo para concentración (23–26 °C)',
  },
  {
    key: 'humedad',
    label: 'Humedad',
    unit: '%',
    icon: '💧',
    ideal: 40,      // 40% reduce fatiga cognitiva
    min: 35,
    max: 50,
    color: '#68d391',
    description: 'Humedad relativa óptima para rendimiento cognitivo',
  },
  {
    key: 'db',
    label: 'Decibeles',
    unit: 'dB',
    icon: '🔊',
    ideal: 35,      // ≤35 dBA — por encima afecta atención y memoria
    min: 30,
    max: 50,
    color: '#008ba3',
    description: 'Ruido de fondo — máximo recomendado 35 dBA',
  },
  {
    key: 'lux',
    label: 'Iluminación',
    unit: 'lx',
    icon: '💡',
    ideal: 400,     // 300–500 lx mejora alerta y velocidad de lectura
    min: 300,
    max: 500,
    color: '#e7e300',
    description: 'Iluminación óptima para tareas de aula (300–500 lx)',
  },
  
  
  {
    key: 'eco2',
    label: 'Dioxido de Carbono',
    unit: 'ppm',
    icon: '🌳',
    ideal: 600, 
    min: 400,
    max: 800,
    color: '#9145ed',
    description: 'Concentraciones altas de CO2 reducen el rendimiento académico y puede favorecer el contagio de enfermedades',
  },
  
  
  {
    key: 'tvoc',
    label: 'Compuestos Orgánicos Volátiles',
    unit: 'ppb',
    icon: '🌿',
    ideal: 200,
    min: 0,
    max: 500,
    color: '#fa3bc4',
    description: 'Concentraciones altas de TVOC reducen la concentración y el rendimiento cognitivo',
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
  salas: 'http://localhost:8080/api/salas',
/*
  chat:       'http://ESP8266_IP/api/chat',        // POST { message, conditions } → { reply }
  conditions: 'http://ESP8266_IP/api/conditions',  // POST { students, size, ... } → { ok }
*/
}
// ====== FUNCIONES DE AYUDA (OBLIGATORIAS PARA EL PARCHE) ======

export function getSensor(key) {
  return SENSORS.find(s => s.key === key)
}

export function isOutOfRange(key, value) {
  const s = getSensor(key)
  if (!s || value === null || value === undefined || Number.isNaN(value)) return false
  return value < s.min || value > s.max
}

export function severity(key, value) {
  const s = getSensor(key)
  if (!s || !isOutOfRange(key, value)) return 'ok'
  // Si el CO2 pasa de 1500 o el ruido de 60, es una alerta crítica, si no, es advertencia
  if (key === 'eco2' && value > 1500) return 'critica'
  if (key === 'db' && value > 60) return 'critica'
  return 'advertencia'
}
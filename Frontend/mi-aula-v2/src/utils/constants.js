// Sensor definitions — ideal values based on learning environment research
export const SENSORS = [
  {
    key: 'temp',
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
    key: 'hum',
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
    color: '#f6ad55',
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
    color: '#fbd38d',
    description: 'Iluminación óptima para tareas de aula (300–500 lx)',
  },
  {
    key: 'ppm',
    label: 'CO₂',
    unit: 'ppm',
    icon: '🌿',
    ideal: 600,     // concentraciones altas reducen precisión cognitiva
    min: 400,
    max: 900,
    color: '#9ae6b4',
    description: 'Calidad del aire — altas concentraciones reducen rendimiento',
  },
]

export const DEFAULT_CONDITIONS = {
  students: 30,
  size: 50,
  windows: 'varias',
  ac: 'si',
  floor: 2,
}

export const UPDATE_INTERVAL = 300 // segundos

// API endpoints — reemplazar con la IP real del ESP8266
export const API = {
  sensors:    'http://ESP8266_IP/api/sensors',    // GET → { temp, hum, db, lux, ppm }
  chat:       'http://ESP8266_IP/api/chat',        // POST { message, conditions } → { reply }
  conditions: 'http://ESP8266_IP/api/conditions',  // POST { students, size, ... } → { ok }
}

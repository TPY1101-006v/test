import { useState, useEffect, useCallback, useRef } from 'react'
import { fetchSensorData } from '../utils/api'
import { SENSORS, UPDATE_INTERVAL, isOutOfRange, severity, formatSensorValue, roundSensorValue } from '../utils/constants'

const MAX_ALERTS = 50
const RETRY_SECONDS = 5

// Función para construir alertas reales cuando cambia la medición
function buildAlerts(latest, lastFechaHora, prevSnapshot) {
  const time = new Date(lastFechaHora).toLocaleTimeString('es-CL', {
    hour: '2-digit', minute: '2-digit',
  })
  const nuevas = []
  SENSORS.forEach(s => {
    const v = latest[s.key]
    if (v === null || v === undefined || Number.isNaN(v)) return
    if (!isOutOfRange(s.key, v)) return
    if (prevSnapshot && prevSnapshot[s.key] === v) return // Evita duplicados si es el mismo valor
    
    const rounded = roundSensorValue(s.key, v)
    nuevas.push({
      id:        `${lastFechaHora}-${s.key}`,
      sensor:    s.label,
      sensorKey: s.key,
      value:     formatSensorValue(s.key, v),
      unit:      s.unit,
      high:      rounded > s.max,
      severity:  severity(s.key, v),
      time,
      range:     `${s.min}–${s.max} ${s.unit}`,
    })
  })
  return nuevas
}

export function useSensors() {
  const [values, setValues]   = useState({})
  const [history, setHistory] = useState(() => { const h = {}; SENSORS.forEach(s => { h[s.key] = [] }); return h })
  const [alerts, setAlerts]   = useState([])
  const [countdown, setCountdown] = useState(UPDATE_INTERVAL)
  const [loading, setLoading] = useState(true)
  
  const nextUpdateAtRef = useRef(null)
  const lastFechaRef    = useRef(null)
  const prevValuesRef   = useRef({})
  const fetchingRef     = useRef(false)

  const refresh = useCallback(async () => {
    if (fetchingRef.current) return
    fetchingRef.current = true
    try {
      const { latest, history: apiHistory, lastFechaHora } = await fetchSensorData()
      const esNueva = lastFechaHora !== lastFechaRef.current

      setValues(latest || {})
      setHistory(apiHistory || {})

      if (esNueva) {
        const nuevas = buildAlerts(latest || {}, lastFechaHora, prevValuesRef.current)
        if (nuevas.length) {
          setAlerts(prev => [...nuevas, ...prev].slice(0, MAX_ALERTS))
        }
        prevValuesRef.current   = latest || {}
        lastFechaRef.current    = lastFechaHora
        nextUpdateAtRef.current = new Date(lastFechaHora).getTime() + UPDATE_INTERVAL * 1000
      } else {
        nextUpdateAtRef.current = Date.now() + RETRY_SECONDS * 1000
      }
    } catch (err) {
      console.error('Error fetching sensor data:', err)
      nextUpdateAtRef.current = Date.now() + RETRY_SECONDS * 1000
    } finally {
      fetchingRef.current = false
      setLoading(false)
    }
  }, [])

  useEffect(() => { refresh() }, [refresh])

  useEffect(() => {
    const iv = setInterval(() => {
      if (!nextUpdateAtRef.current) return
      const restante = Math.ceil((nextUpdateAtRef.current - Date.now()) / 1000)
      setCountdown(Math.min(Math.max(restante, 0), UPDATE_INTERVAL))
      if (restante <= 0) refresh()
    }, 1000)
    return () => clearInterval(iv)
  }, [refresh])

  // Alertas dinámicas "activas" en el momento exacto
  const activeAlerts = SENSORS
    .map(s => ({ sensor: s, value: values[s.key] }))
    .filter(({ sensor, value }) => isOutOfRange(sensor.key, value))
    .map(({ sensor, value }) => ({
      sensorKey: sensor.key,
      label:     sensor.label,
      value:     formatSensorValue(sensor.key, value),
      unit:      sensor.unit,
      range:     `${sensor.min}–${sensor.max} ${sensor.unit}`,
      severity:  severity(sensor.key, value),
      high:      roundSensorValue(sensor.key, value) > sensor.max,
    }))

  return { values, history, alerts, activeAlerts, countdown, loading, isOutOfRange }
}
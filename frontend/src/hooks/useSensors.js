import { useState, useEffect, useCallback, useRef } from 'react'
import { fetchSensorData } from '../utils/api'
import { SENSORS, UPDATE_INTERVAL } from '../utils/constants'

export function useSensors() {
  const [values, setValues]   = useState({})
  const [history, setHistory] = useState(() => { const h = {}; SENSORS.forEach(s => { h[s.key] = [] }); return h })
  const [alerts, setAlerts]   = useState([])
  const [countdown, setCountdown] = useState(UPDATE_INTERVAL)
  const [loading, setLoading] = useState(true)
  const nextUpdateAtRef = useRef(null)
  const lastFechaRef = useRef(null)
  const fetchingRef = useRef(false)

  const RETRY_SECONDS = 5

  const isOutOfRange = (key, val) => {
    const s = SENSORS.find(x => x.key === key)
    return s && (val < s.min || val > s.max)
  }

  const refresh = useCallback(async () => {
    if (fetchingRef.current) return
    fetchingRef.current = true
    try {
      const { latest, history: apiHistory, lastFechaHora } = await fetchSensorData()
      const esNueva = lastFechaHora !== lastFechaRef.current
  
      setValues(latest)
      setHistory(apiHistory)
      // ... (la lógica de alertas que ya tienes, sin cambios)
  
      lastFechaRef.current = lastFechaHora
  
      if (esNueva) {
        // próxima medición = la última + intervalo
        nextUpdateAtRef.current = new Date(lastFechaHora).getTime() + UPDATE_INTERVAL * 1000
      } else {
        // el backend todavía no inserta la nueva → reintentar pronto
        nextUpdateAtRef.current = Date.now() + RETRY_SECONDS * 1000
      }
    } catch (err) {
      console.error('Error fetching sensor data:', err)
      nextUpdateAtRef.current = Date.now() + RETRY_SECONDS * 1000  // no congelar la barra si falló la red
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

  return { values, history, alerts, countdown, loading, isOutOfRange }
}


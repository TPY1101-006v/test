import { useState, useEffect, useCallback, useRef } from 'react'
import { fetchSensorData } from '../utils/api'
import { SENSORS, UPDATE_INTERVAL } from '../utils/constants'

export function useSensors() {
  const [values, setValues]   = useState({})
  const [history, setHistory] = useState(() => { const h = {}; SENSORS.forEach(s => { h[s.key] = [] }); return h })
  const [alerts, setAlerts]   = useState([])
  const [countdown, setCountdown] = useState(UPDATE_INTERVAL)
  const [loading, setLoading] = useState(true)
  const cdRef = useRef(UPDATE_INTERVAL)

  const isOutOfRange = (key, val) => {
    const s = SENSORS.find(x => x.key === key)
    return s && val < s.min || val > s.max
  }

  const refresh = useCallback(async () => {
    try {
      const data = await fetchSensorData()
      const now = new Date().toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' })
      setValues(data)
      setHistory(prev => {
        const next = { ...prev }
        SENSORS.forEach(s => {
          next[s.key] = [...(prev[s.key] || []).slice(-11), { t: now, [s.key]: data[s.key] }]
        })
        return next
      })
      SENSORS.forEach(s => {
        const v = data[s.key]
        if (v < s.min || v > s.max) {
          setAlerts(prev => [{
            id: Date.now() + s.key, sensor: s.label, key: s.key,
            value: s.key === 'lux' || s.key === 'ppm' ? Math.round(v) : Number(v).toFixed(1),
            unit: s.unit, time: now, high: v > s.max,
          }, ...prev].slice(0, 30))
        }
      })
      cdRef.current = UPDATE_INTERVAL
    } catch (err) {
      console.error('Error fetching sensor data:', err)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { refresh() }, [refresh])

  useEffect(() => {
    const iv = setInterval(() => {
      cdRef.current -= 1
      setCountdown(cdRef.current)
      if (cdRef.current <= 0) { cdRef.current = UPDATE_INTERVAL; refresh() }
    }, 1000)
    return () => clearInterval(iv)
  }, [refresh])

  return { values, history, alerts, countdown, loading, isOutOfRange }
}

import React, { useState, useEffect, useRef } from 'react'
import Header from './components/Header'
import UpdateBar from './components/UpdateBar'
import AlertsBanner from './components/AlertsBanner'
import SensorCard from './components/SensorCard'
import SensorChartSection from './components/SensorChartSection'
import Chatbot from './components/Chatbot'
import Sidebar from './components/Sidebar'
import ThemeToggle from './components/ThemeToggle'
import { useSensors } from './hooks/useSensors'
import { SENSORS } from './utils/constants'
import { fetchAlerts, saveAlert } from './utils/api'
import styles from './App.module.css'

export default function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [dbAlerts, setDbAlerts] = useState([])
  const [soundEnabled, setSoundEnabled] = useState(false)

  const alertAudioRef = useRef(null)
  const lastAlertSignatureRef = useRef('')

  const {
    values,
    history,
    alerts,
    activeAlerts,
    countdown,
    loading,
    isOutOfRange,
  } = useSensors()

  useEffect(() => {
    alertAudioRef.current = new Audio('/alerta.mp3')
    alertAudioRef.current.preload = 'auto'
    alertAudioRef.current.volume = 0.45
  }, [])

  async function playAlertSound() {
    try {
      const audio = alertAudioRef.current || new Audio('/alerta.mp3')

      audio.pause()
      audio.currentTime = 0
      audio.volume = 0.45

      await audio.play()

      console.log('Sonido de alerta reproducido')
      return true
    } catch (err) {
      console.warn('No se pudo reproducir el sonido de alerta:', err)
      return false
    }
  }

  async function handleToggleSound() {
    const activando = !soundEnabled

    if (activando) {
      const ok = await playAlertSound()

      if (!ok) {
        alert('No se pudo activar el sonido. Revisa permisos del navegador o vuelve a hacer clic en Activar Alertas.')
      }
    }

    setSoundEnabled(prev => !prev)
  }

  useEffect(() => {
    fetchAlerts()
      .then(data => {
        setDbAlerts(data)
      })
      .catch(err => console.error('Error cargando alertas de la BD:', err))
  }, [])

  useEffect(() => {
    if (alerts && alerts.length > 0) {
      const ultimaAlerta = alerts[0]

      const yaExiste = dbAlerts.some(
        a => a.time === ultimaAlerta.time && a.sensor === ultimaAlerta.sensor
      )

      if (!yaExiste) {
        const nuevaAlertaBD = {
          sensor: ultimaAlerta.sensor,
          value: Number(ultimaAlerta.value),
          unit: ultimaAlerta.unit,
          high: ultimaAlerta.high || false,
        }

        saveAlert(nuevaAlertaBD)
          .then(alertaGuardada => {
            setDbAlerts(prev => [alertaGuardada, ...prev].slice(0, 15))
          })
          .catch(err => console.error('Error al persistir alerta en Spring Boot:', err))
      }
    }
  }, [alerts, dbAlerts])

  useEffect(() => {
    const listaAlertas = activeAlerts || []

    console.log('Estado sonido:', soundEnabled)
    console.log('Alertas activas:', listaAlertas.length, listaAlertas)

    if (listaAlertas.length === 0) {
      lastAlertSignatureRef.current = ''
      return
    }

    const currentSignature = listaAlertas
      .map(a => `${a.sensorKey || a.sensor || a.label}:${a.high ? 'high' : 'low'}`)
      .sort()
      .join('|')

    if (!soundEnabled) {
      return
    }

    if (lastAlertSignatureRef.current === currentSignature) {
      return
    }

    lastAlertSignatureRef.current = currentSignature

    playAlertSound()

    if (
      typeof Notification !== 'undefined' &&
      Notification.permission === 'granted'
    ) {
      new Notification('Alerta Monitoriza', {
        body: `${listaAlertas.length} ${listaAlertas.length === 1 ? 'sensor fuera de rango' : 'sensores fuera de rango'}.`,
        icon: '/logo.png',
      })
    }
  }, [activeAlerts, soundEnabled])

  return (
    <div className={styles.app}>
      <ThemeToggle />

      <Header onMenuOpen={() => setSidebarOpen(true)} />

      <UpdateBar countdown={countdown} />

      <AlertsBanner activeAlerts={activeAlerts} />

      <div className={styles.sectionLabel}>
        sensores activos
      </div>

      <div className={styles.cardsGrid}>
        {loading
          ? SENSORS.map(s => (
              <div key={s.key} className={styles.skeleton} />
            ))
          : SENSORS.map(s => (
              <SensorCard
                key={s.key}
                sensorKey={s.key}
                value={values[s.key]}
                outOfRange={isOutOfRange(s.key, values[s.key])}
              />
            ))}
      </div>

      <SensorChartSection history={history} values={values} />

      <Chatbot sensorData={values} alerts={dbAlerts} />

      <Sidebar
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        alerts={dbAlerts}
        activeAlerts={activeAlerts}
        sensorData={values}
        history={history}
        soundEnabled={soundEnabled}
        onToggleSound={handleToggleSound}
      />
    </div>
  )
}
import React, { useState, useEffect, useRef } from 'react' 
import Header from './components/Header'
import UpdateBar from './components/UpdateBar'
import AlertsBanner from './components/AlertsBanner'
import SensorCard from './components/SensorCard'
import SensorChartSection from './components/SensorChartSection'
import Chatbot from './components/Chatbot'
import Sidebar from './components/Sidebar'
import ThemeToggle from './components/ThemeToggle' // ✨ AÑADIDO: Importación del botón
import { useSensors } from './hooks/useSensors'
import { SENSORS } from './utils/constants'
import { fetchAlerts, saveAlert } from './utils/api'
import styles from './App.module.css'

export default function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [dbAlerts, setDbAlerts] = useState([]) 
  
  // <-- 2. Nuevo estado para controlar el sonido
  const [soundEnabled, setSoundEnabled] = useState(false)
  
  // <-- 3. Referencia para evitar spam de sonido por una misma alerta
  const hasNotifiedRef = useRef(false)

  const {
    values, history, alerts, activeAlerts,
    countdown, loading, isOutOfRange
  } = useSensors()

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
      const yaExiste = dbAlerts.some(a => a.time === ultimaAlerta.time && a.sensor === ultimaAlerta.sensor)

      if (!yaExiste) {
        const nuevaAlertaBD = {
          sensor: ultimaAlerta.sensor,
          value: Number(ultimaAlerta.value),
          unit: ultimaAlerta.unit,
          high: ultimaAlerta.high || false
        }

        saveAlert(nuevaAlertaBD)
          .then(alertaGuardada => {
            setDbAlerts(prev => [alertaGuardada, ...prev].slice(0, 15))
          })
          .catch(err => console.error('Error al persistir alerta en Spring Boot:', err))
      }
    }
  }, [alerts])

  // <-- 4. NUEVO BLOQUE: Lógica de Notificaciones y Sonido
  useEffect(() => {
    if (activeAlerts && activeAlerts.length > 0) {
      if (!hasNotifiedRef.current) {
        // Solo suena y notifica si el usuario activó el sonido desde el Sidebar
        if (soundEnabled) {
          const audio = new Audio('/alerta.mp3')
          audio.play().catch(err => console.warn('Autoplay bloqueado', err))

          if (Notification.permission === 'granted') {
            new Notification('¡Alerta Crítica Monitoriza!', {
              body: `Atención: ${activeAlerts.length} parámetro(s) fuera de rango.`,
              icon: '/vite.svg'
            })
          }
        }
        hasNotifiedRef.current = true
      }
    } else {
      hasNotifiedRef.current = false // Reseteamos si todo vuelve a la normalidad
    }
  }, [activeAlerts, soundEnabled]) // <-- Escucha cambios en alertas y en el estado del sonido

  return (
    <div className={styles.app}>
      <ThemeToggle /> {/* ✨ AÑADIDO: Renderizamos el botón del sol/luna */}
      
      <Header onMenuOpen={() => setSidebarOpen(true)} />
      <UpdateBar countdown={countdown} />
      
      <AlertsBanner activeAlerts={activeAlerts} />

      <div className={styles.sectionLabel}>sensores activos</div>
      <div className={styles.cardsGrid}>
        {loading
          ? SENSORS.map(s => <div key={s.key} className={styles.skeleton} />)
          : SENSORS.map(s => (
              <SensorCard
                key={s.key}
                sensorKey={s.key}
                value={values[s.key]}
                outOfRange={isOutOfRange(s.key, values[s.key])}
              />
            ))
        }
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
        // <-- 5. Pasamos las propiedades del sonido al Sidebar
        soundEnabled={soundEnabled}
        onToggleSound={() => setSoundEnabled(!soundEnabled)}
      />
    </div>
  )
}
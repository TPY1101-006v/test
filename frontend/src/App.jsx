import React, { useState, useEffect } from 'react'
import Header from './components/Header'
import UpdateBar from './components/UpdateBar'
import AlertsBanner from './components/AlertsBanner'
import SensorCard from './components/SensorCard'
import SensorChartSection from './components/SensorChartSection'
import Chatbot from './components/Chatbot'
import Sidebar from './components/Sidebar'
import { useSensors } from './hooks/useSensors'
import { SENSORS } from './utils/constants'
import { fetchAlerts, saveAlert } from './utils/api' // <-- Importamos saveAlert también
import styles from './App.module.css'

export default function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [dbAlerts, setDbAlerts] = useState([]) 
  
  // Traemos los datos y las alertas volátiles del hook
  const {
    values, history, alerts, activeAlerts,
    countdown, loading, isOutOfRange
  } = useSensors()

  // 1. Carga inicial: Trae el historial guardado en la Base de Datos al abrir mi-aula
  useEffect(() => {
    fetchAlerts()
      .then(data => {
        setDbAlerts(data) 
      })
      .catch(err => console.error('Error cargando alertas de la BD:', err))
  }, [])

  // 2. SINCRONIZACIÓN AUTOMÁTICA: Si el hook genera una alerta nueva, la subimos a Spring Boot
  useEffect(() => {
    if (alerts && alerts.length > 0) {
      // Tomamos la última alerta generada por el sistema web
      const ultimaAlerta = alerts[0]

      // Evitamos duplicar si ya la guardamos (comparamos con la última que tiene la BD)
      const yaExiste = dbAlerts.some(a => a.time === ultimaAlerta.time && a.sensor === ultimaAlerta.sensor)

      if (!yaExiste) {
        // Estructuramos el objeto idéntico a lo que espera nuestra Entidad Alerta.java
        const nuevaAlertaBD = {
          sensor: ultimaAlerta.sensor,
          value: Number(ultimaAlerta.value),
          unit: ultimaAlerta.unit,
          high: ultimaAlerta.high || false
        }

        // Hacemos el POST al Backend
        saveAlert(nuevaAlertaBD)
          .then(alertaGuardada => {
            // Insertamos la alerta guardada al principio de la lista del Chatbot y el Sidebar
            setDbAlerts(prev => [alertaGuardada, ...prev].slice(0, 15))
          })
          .catch(err => console.error('Error al persistir alerta en Spring Boot:', err))
      }
    }
  }, [alerts]) // <-- Cada vez que cambien las alertas del sensor, se ejecuta este bloque

  return (
    <div className={styles.app}>
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
      
      {/* Sincronizados con la misma base de datos real */}
      <Chatbot sensorData={values} alerts={dbAlerts} />
      
      <Sidebar
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        alerts={dbAlerts}            // <-- Muestra el historial persistente
        activeAlerts={activeAlerts}  
        sensorData={values}
        history={history}
      />
    </div>
  )
}
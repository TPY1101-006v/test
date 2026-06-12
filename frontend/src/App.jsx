import React, { useState } from 'react'
import Header from './components/Header'
import UpdateBar from './components/UpdateBar'
import SensorCard from './components/SensorCard'
import SensorChartSection from './components/SensorChartSection'
import Chatbot from './components/Chatbot'
import Sidebar from './components/Sidebar'
import { useSensors } from './hooks/useSensors'
import { SENSORS } from './utils/constants'
import styles from './App.module.css'

export default function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { values, history, alerts, countdown, loading, isOutOfRange } = useSensors()

  return (
    <div className={styles.app}>
      <Header onMenuOpen={() => setSidebarOpen(true)} />
      <UpdateBar countdown={countdown} />
      <div className={styles.sectionLabel}>sensores activos</div>
      <div className={styles.cardsGrid}>
        {loading
          ? SENSORS.map(s => <div key={s.key} className={styles.skeleton} />)
          : SENSORS.map(s => (
              <SensorCard key={s.key} sensorKey={s.key} value={values[s.key]} outOfRange={isOutOfRange(s.key, values[s.key])} />
            ))
        }
      </div>
      <SensorChartSection history={history} values={values} />
      <Chatbot sensorData={values} alerts={alerts} />
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} alerts={alerts} sensorData={values} history={history} />
    </div>
  )
}

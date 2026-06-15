import React, { useState, useEffect } from 'react'
import { fetchSalaConditions } from '../utils/api' 
import { getContextualRecommendation } from '../utils/recommendations' // <-- Aquí importamos el cerebro
import styles from './AlertsBanner.module.css'

export default function AlertsBanner({ activeAlerts }) {
  const [conditions, setConditions] = useState(null)

  // Cargamos los datos del formulario de la sala al montar el banner
  useEffect(() => {
    fetchSalaConditions()
      .then(data => setConditions(data))
      .catch(err => console.error('Error cargando condiciones para banner:', err))
  }, [])

  // Renderizado en estado normal (YAGNI)
  if (!activeAlerts || activeAlerts.length === 0) {
    return (
      <div className={`${styles.banner} ${styles.ok}`} role="status">
        <div className={styles.topRow}>
          <span className={styles.icon}>✓</span>
          <span className={styles.text}>ambiente óptimo — todos los sensores en rango</span>
        </div>
      </div>
    )
  }

  const hayCritica = activeAlerts.some(a => a.severity === 'critica')

  return (
    <div
      className={`${styles.banner} ${hayCritica ? styles.critica : styles.alerta}`}
      role="alert"
      aria-live="polite"
    >
      {/* FILA SUPERIOR: Tus chips originales */}
      <div className={styles.topRow}>
        <span className={styles.icon}>{hayCritica ? '🚨' : '⚠️'}</span>
        <div className={styles.list}>
          <div className={styles.title}>
            {activeAlerts.length} {activeAlerts.length === 1 ? 'sensor' : 'sensores'} fuera de rango
          </div>
          <div className={styles.chips}>
            {activeAlerts.map(a => (
              <span
                key={a.sensorKey}
                className={`${styles.chip} ${a.severity === 'critica' ? styles.chipCritica : ''}`}
                title={`Rango ideal: ${a.range}`}
              >
                {a.label}: <strong>{a.value} {a.unit}</strong>
                <span className={styles.arrow}>{a.high ? '↑' : '↓'}</span>
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* FILA INFERIOR: Sugerencias Inteligentes */}
      <div className={styles.recommendations}>
        {activeAlerts.map(a => (
          <div key={`rec-${a.sensorKey}`} className={styles.recItem}>
            <span className={styles.recIcon}>💡</span>
            <span className={styles.recText}>
              <strong>{a.label}:</strong> {getContextualRecommendation(a, conditions)}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}
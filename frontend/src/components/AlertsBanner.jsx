import React, { useState, useEffect } from 'react'
import { fetchSalaConditions } from '../utils/api'
import { getContextualRecommendation } from '../utils/recommendations'
import styles from './AlertsBanner.module.css'

export default function AlertsBanner({ activeAlerts }) {
  const [conditions, setConditions] = useState(null)
  const [expanded, setExpanded] = useState(false)

  useEffect(() => {
    fetchSalaConditions()
      .then(data => setConditions(data))
      .catch(err => console.error('Error cargando condiciones para banner:', err))
  }, [])

  if (!activeAlerts || activeAlerts.length === 0) {
    return (
      <div className={`${styles.banner} ${styles.ok}`} role="status">
        <div className={styles.compactRow}>
          <span className={`material-symbols-outlined ${styles.icon}`}>
            check_circle
          </span>

          <span className={styles.text}>
            ambiente óptimo — todos los sensores en rango
          </span>
        </div>
      </div>
    )
  }

  return (
    <div
      className={`${styles.banner} ${styles.alerta}`}
      role="alert"
      aria-live="polite"
    >
      <div className={styles.compactRow}>
        <span className={`material-symbols-outlined ${styles.icon}`}>
          warning
        </span>

        <div className={styles.title}>
          {activeAlerts.length} {activeAlerts.length === 1 ? 'sensor' : 'sensores'} fuera de rango
        </div>

        <button
          type="button"
          className={styles.toggleBtn}
          onClick={() => setExpanded(prev => !prev)}
          aria-expanded={expanded}
          title={expanded ? 'Compactar alerta' : 'Ver detalle de alerta'}
        >
          <span className="material-symbols-outlined">
            {expanded ? 'expand_less' : 'expand_more'}
          </span>

          {expanded ? 'Ocultar' : 'Ver detalle'}
        </button>
      </div>

      {expanded && (
        <>
          <div className={styles.chips}>
            {activeAlerts.map(a => (
              <span
                key={a.sensorKey}
                className={styles.chip}
                title={`Rango ideal: ${a.range}`}
              >
                {a.label}: <strong>{a.value} {a.unit}</strong>

                <span className={`material-symbols-outlined ${styles.arrow}`}>
                  {a.high ? 'arrow_upward' : 'arrow_downward'}
                </span>
              </span>
            ))}
          </div>

          <div className={styles.recommendations}>
            {activeAlerts.map(a => (
              <div key={`rec-${a.sensorKey}`} className={styles.recItem}>
                <span className={`material-symbols-outlined ${styles.recIcon}`}>
                  lightbulb
                </span>

                <span className={styles.recText}>
                  <strong>{a.label}:</strong> {getContextualRecommendation(a, conditions)}
                </span>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
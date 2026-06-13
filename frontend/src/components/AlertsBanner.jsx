import React from 'react'
import styles from './AlertsBanner.module.css'

// Banner superior con alertas ACTIVAS (sensores fuera de rango ahora mismo).
// Si no hay nada que mostrar, no renderiza nada (YAGNI).
export default function AlertsBanner({ activeAlerts }) {
  if (!activeAlerts || activeAlerts.length === 0) {
    return (
      <div className={`${styles.banner} ${styles.ok}`} role="status">
        <span className={styles.icon}></span>
        <span className={styles.text}>ambiente óptimo — todos los sensores en rango</span>
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
      <span className={styles.icon}>{hayCritica ? '' : ''}</span>
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
  )
}
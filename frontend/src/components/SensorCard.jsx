import React from 'react'
import styles from './SensorCard.module.css'
import { SENSORS } from '../utils/constants'

function formatVal(key, val) {
  if (val === undefined || val === null || isNaN(val)) return '—'
  if (key === 'lux' || key === 'ppm') return Math.round(val)
  return Number(val).toFixed(1)
}

export default function SensorCard({ sensorKey, value, outOfRange }) {
  const sensor = SENSORS.find(s => s.key === sensorKey)
  if (!sensor) return null

  const fmt = formatVal(sensorKey, value)
  const alert = outOfRange && value !== undefined

  return (
    <div className={`${styles.card} ${alert ? styles.alert : styles.ok}`}>
      <div className={styles.icon}>{sensor.icon}</div>
      <div className={styles.label}>{sensor.label}</div>
      <div
        className={styles.value}
        style={{ color: alert ? 'var(--warn)' : sensor.color }}
      >
        {fmt}
      </div>
      <div className={styles.unit}>{sensor.unit}</div>
      <div className={`${styles.status} ${alert ? styles.statusAlert : styles.statusOk}`}>
        {alert ? '⚠ ALERTA' : '✓ NORMAL'}
      </div>
      <div className={styles.range}>
        rango: {sensor.min}–{sensor.max} {sensor.unit}
      </div>
    </div>
  )
}

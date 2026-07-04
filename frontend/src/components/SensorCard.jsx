import React from 'react'
import styles from './SensorCard.module.css'
import {
  SENSORS,
  severity,
  formatSensorValue,
} from '../utils/constants'

export default function SensorCard({ sensorKey, value }) {
  const sensor = SENSORS.find(s => s.key === sensorKey)

  if (!sensor) {
    return null
  }

  const fmt = formatSensorValue(sensorKey, value)
  const estado = severity(sensorKey, value)

  let cardStyle = styles.ok
  let statusStyle = styles.statusOk
  let colorNumero = 'var(--text)'
  let textoEstado = 'NORMAL'
  let iconoEstado = 'check_circle'

  if (estado === 'calibracion') {
    cardStyle = styles.calibration
    statusStyle = styles.statusCalibration
    colorNumero = '#a855f7'
    textoEstado = 'CALIBRAR'
    iconoEstado = 'build_circle'
  }

  if (estado === 'alerta') {
    cardStyle = styles.warning
    statusStyle = styles.statusWarning
    colorNumero = 'var(--warn)'
    textoEstado = 'ALERTA'
    iconoEstado = 'warning'
  }

  return (
    <div className={`${styles.card} ${cardStyle}`}>
      <div
        className={styles.icon}
        style={{ color: sensor.color }}
      >
        <span className="material-symbols-outlined">
          {sensor.icon}
        </span>
      </div>

      <div className={styles.label}>
        {sensor.label}
      </div>

      <div
        className={styles.value}
        style={{ color: colorNumero }}
      >
        {fmt}
      </div>

      <div className={styles.unit}>
        {sensor.unit}
      </div>

      <div className={`${styles.status} ${statusStyle}`}>
        <span className={`material-symbols-outlined ${styles.statusIcon}`}>
          {iconoEstado}
        </span>

        {textoEstado}
      </div>

      <div className={styles.range}>
        rango: {sensor.min}–{sensor.max} {sensor.unit}
      </div>
    </div>
  )
}
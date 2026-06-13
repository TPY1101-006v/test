import React from 'react'
import styles from './SensorCard.module.css'
import { SENSORS, severity } from '../utils/constants'

function formatVal(key, val) {
  if (val === undefined || val === null || isNaN(val)) return '—'
  if (key === 'lux' || key === 'eco2' || key === 'tvoc') return Math.round(val)
  return Number(val).toFixed(1)
}

export default function SensorCard({ sensorKey, value }) {
  const sensor = SENSORS.find(s => s.key === sensorKey)
  if (!sensor) return null

  const fmt = formatVal(sensorKey, value)
  
  const estado = (value !== undefined && value !== null) 
    ? severity(sensorKey, value) 
    : 'ok'

  // Variables por defecto (Todo está normal)
  let cardStyle = styles.ok;
  let statusStyle = styles.statusOk;
  // ✨ CAMBIO AQUÍ: Usamos el color de texto normal de tu tema (o blanco) en lugar de sensor.color
  let colorNumero = 'var(--text, #ffffff)'; 
  let textoEstado = '✓ NORMAL';

  // Si supera el umbral crítico
  if (estado === 'critica') {
    cardStyle = styles.critical; 
    statusStyle = styles.statusCritical;
    colorNumero = '#ef4444'; // Rojo
    textoEstado = '🛑 CRÍTICO';
  } 
  // Si supera el umbral de advertencia
  else if (estado === 'advertencia') {
    cardStyle = styles.warning; 
    statusStyle = styles.statusWarning;
    colorNumero = 'var(--warn)'; // Naranja
    textoEstado = '⚠ ALERTA';
  }

  return (
    <div className={`${styles.card} ${cardStyle}`}>
      <div className={styles.icon}>{sensor.icon}</div>
      <div className={styles.label}>{sensor.label}</div>
      <div
        className={styles.value}
        style={{ color: colorNumero }}
      >
        {fmt}
      </div>
      <div className={styles.unit}>{sensor.unit}</div>
      <div className={`${styles.status} ${statusStyle}`}>
        {textoEstado}
      </div>
      <div className={styles.range}>
        rango: {sensor.min}–{sensor.max} {sensor.unit}
      </div>
    </div>
  )
}
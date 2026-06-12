import React from 'react'
import { UPDATE_INTERVAL } from '../utils/constants'
import styles from './UpdateBar.module.css'

export default function UpdateBar({ countdown }) {
  const m = Math.floor(countdown / 60)
  const s = countdown % 60
  const actualizado = countdown <= 0
  const label = actualizado ? 'Actualizando...' : `${m}:${String(s).padStart(2, '0')}`
  const pct = (countdown / UPDATE_INTERVAL) * 100

  return (
    <div className={styles.bar}>
      <span className={styles.text}>próxima actualización</span>
      <div className={styles.track}>
        <div className={styles.fill} style={{ width: `${pct}%` }} />
      </div>
      <span className={styles.text}>{label}</span>
    </div>
  )
}

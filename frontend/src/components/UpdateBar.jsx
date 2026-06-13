import React, { useEffect, useRef } from 'react'
import { UPDATE_INTERVAL } from '../utils/constants'
import styles from './UpdateBar.module.css'

export default function UpdateBar({ countdown }) {
  const fillRef = useRef(null)
  const prevRef = useRef(countdown)

  // El progreso va de 0% (inicio del ciclo) a 100% (momento de actualizar)
  const progreso = Math.max(0, Math.min(100, (1 - countdown / UPDATE_INTERVAL) * 100))

  useEffect(() => {
    const el = fillRef.current
    if (!el) return
    
    // Si el contador subió (significa que se reseteó tras actualizar),
    // quitamos la animación un milisegundo para que no se vea el rebobinado tosco hacia atrás.
    if (countdown > prevRef.current) {
      el.style.transition = 'none'
      void el.offsetWidth          // Forzar al navegador a renderizar el cambio inmediato
      el.style.transition = ''     // Devolver la animación normal
    }
    prevRef.current = countdown
  }, [countdown])

  const actualizando = countdown <= 0
  const m = Math.floor(countdown / 60)
  const s = countdown % 60
  const label = actualizando ? 'Actualizando…' : `${m}:${String(s).padStart(2, '0')}`

  return (
    <div className={styles.bar}>
      <span className={styles.text}>próxima actualización</span>
      <div className={styles.track}>
        <div
          ref={fillRef}
          className={`${styles.fill} ${actualizando ? styles.fillPulse : ''}`}
          style={{ width: `${progreso}%` }}
        />
      </div>
      <span className={styles.text}>{label}</span>
    </div>
  )
}
import React from 'react'
import styles from './Header.module.css'

export default function Header({ onMenuOpen }) {
  return (
    <header className={styles.header}>
      <div className={styles.logo}>
        <div className={styles.logoIcon}>🏫</div>
        <div>
          <div className={styles.logoText}>mi-aula</div>
          <div className={styles.logoSub}>monitor ambiental</div>
        </div>
      </div>

      <div className={styles.right}>
        <div className={styles.statusDot} />
        <span className={styles.statusLabel}>en vivo</span>
        <button className={styles.burgerBtn} onClick={onMenuOpen} aria-label="Abrir menú">
          <span /><span /><span />
        </button>
      </div>
    </header>
  )
}

import React from 'react'
import styles from './Header.module.css'

export default function Header({ onMenuOpen }) {
  return (
    <header className={styles.header}>
      <div className={styles.logo}>

        {/*
        ==========================================================
        LOGO TEMPORAL DE MONITORIZA

        El logo se carga desde:
            /public/logo.png

        Cuando el equipo tenga el logo definitivo,
        solamente reemplazar el archivo logo.png
        por el nuevo.

        NO será necesario modificar este componente.
        ==========================================================
        */}

        <div className={styles.logoIcon}>
          <img
            src="/logo.png"
            alt="Logo Monitoriza"
            className={styles.logoImage}
          />
        </div>

        <div>
          <div className={styles.logoText}>
            Monitoriza
          </div>

          <div className={styles.logoSub}>
            Sistema Inteligente de Monitoreo Ambiental
          </div>
        </div>

      </div>

      <div className={styles.right}>
        <div className={styles.statusDot}></div>

        <span className={styles.statusLabel}>
          EN VIVO
        </span>

        <button
          className={styles.burgerBtn}
          onClick={() => onMenuOpen()}
          aria-label="Abrir menú"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </header>
  )
}
import React, { useEffect, useState } from 'react';
import styles from './ThemeToggle.module.css';

export default function ThemeToggle() {
  // Inicializamos leyendo de localStorage, o por defecto 'dark'
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'dark');

  // Cada vez que 'theme' cambia, actualizamos el HTML y el localStorage
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  // Función para alternar el estado
  const toggleTheme = () => {
    setTheme((prevTheme) => (prevTheme === 'dark' ? 'light' : 'dark'));
  };

  return (
    <button 
      className={styles.toggleBtn} 
      onClick={toggleTheme} 
      title={theme === 'dark' ? "Cambiar a modo claro" : "Cambiar a modo oscuro"}
      aria-label="Alternar tema"
    >
      {theme === 'dark' ? '☀️' : '🌙'}
    </button>
  );
}
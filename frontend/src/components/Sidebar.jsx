import React, { useState, useEffect } from 'react'
import { fetchSalaConditions, updateSalaConditions, downloadReportFromBackend } from '../utils/api'
import { SENSORS,VENTILATION_TYPES,VENTILATION_HELP  } from '../utils/constants'
import styles from './Sidebar.module.css'




const PANELS = [
  { key: 'descarga',  emoji: '📥', cls: 'blue',  label: 'Descargar datos',      sub: 'Informe y CSV' },
  { key: 'sala',      emoji: '🏫', cls: 'green', label: 'Condiciones de sala',  sub: 'Configurar aula' },
  { key: 'historial', emoji: '🔔', cls: 'red',   label: 'Historial de alertas', sub: 'Eventos fuera de rango' },
  { key: 'nosotros',  emoji: '👥', cls: 'gray',  label: 'Sobre nosotros',       sub: 'El equipo Monitoriza' },
]

const CATS = [
  `  /\\_____/\\\n (  o   o  )\n (  =   =  )\n  (  ___  )\n   \\_____/`,
  `    /\\    /\\\n   (  oo  )\n   (=    =)\n    \\~~~~~/\n     \\_V_/`,
  `   /\\___/\\\n  ( ^ . ^ )\n  (       )\n   \\~~~~~/ `,
  `  /\\   /\\\n ( >.< )\n (  =  )\n  \\_U_/`,
  ` /\\_/\\  /\\_/\\\n( o.o )( o.o )\n > ^ <  > ^ <`,
]

function generateReport(sensorData, alerts) {
  const now = new Date().toLocaleString('es-CL')
  const rows = SENSORS.map(s => {
    const v = sensorData[s.key]
    const f = v !== undefined ? (s.key === 'lux' || s.key === 'ppm' ? Math.round(v) : Number(v).toFixed(1)) : '—'
    const out = v !== undefined && (v < s.min || v > s.max)
    return `  ${s.label}: ${f} ${s.unit}  ${out ? '⚠ FUERA DE RANGO' : '✓ OK'}`
  }).join('\n')
  const alertLines = alerts.length
    ? alerts.slice(0, 5).map(a => `  [${a.time}] ${a.sensor}: ${a.value}${a.unit}`).join('\n')
    : '  Sin alertas registradas'
  return `Monitoriza — INFORME AMBIENTAL\nGenerado: ${now}\n${'─'.repeat(40)}\n\nLECTURAS ACTUALES:\n${rows}\n\nALERTAS RECIENTES:\n${alertLines}\n\n${'─'.repeat(40)}\nMonitoriza v2.0.0`
}

  export default function Sidebar({ open, onClose, alerts, sensorData, history }) {
    const [conditions, setConditions] = useState(null)
    const [conditionsLoading, setConditionsLoading] = useState(true)
    const [conditionsError, setConditionsError] = useState(null)
    const [saved, setSaved] = useState(false)
    const [saving, setSaving] = useState(false)
    const [dlState, setDlState] = useState({ pdf: 'idle', csv: 'idle' })
    const [catOpen, setCatOpen] = useState(false)
    const [catArt, setCatArt] = useState('')
    const [activePanel, setActivePanel] = useState('descarga')
    useEffect(() => {
      setConditionsLoading(true)
      setConditionsError(null)
      fetchSalaConditions()
        .then(data => setConditions(data))
        .catch(err => {
          console.error('Error cargando condiciones de sala:', err)
          setConditionsError('No se pudieron cargar las condiciones de la sala')
          setConditions(null)
        })
        .finally(() => setConditionsLoading(false))
    }, [])

  function openCat() {
    setCatArt(CATS[Math.floor(Math.random() * CATS.length)])
    setCatOpen(true)
  }

  function downloadPDF() {
    setDlState(s => ({ ...s, pdf: 'loading' }))
    const blob = new Blob([generateReport(sensorData, alerts)], { type: 'text/plain;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = `Monitoriza-informe-${new Date().toISOString().slice(0,10)}.txt`; a.click()
    URL.revokeObjectURL(url)
    setDlState(s => ({ ...s, pdf: 'done' }))
    setTimeout(() => setDlState(s => ({ ...s, pdf: 'idle' })), 2500)
  }

  async function downloadCSV() {
    setDlState(s => ({ ...s, csv: 'loading' }))
    try {
      const blob = await downloadReportFromBackend()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `Monitoriza-${new Date().toISOString().slice(0, 10)}.csv`
      a.click()
      URL.revokeObjectURL(url)
      setDlState(s => ({ ...s, csv: 'done' }))
    } catch (err) {
      console.error('Error descargando CSV:', err)
      alert('No se pudo descargar el informe CSV.')
      setDlState(s => ({ ...s, csv: 'idle' }))
    }
    setTimeout(() => setDlState(s => ({ ...s, csv: 'idle' })), 2500)
  }
  async function saveConditions() {
    if (!conditions?.idSala) {
      alert('Aún no se cargaron los datos de la sala')
      return
    }
  
    setSaving(true)
    try {
      await updateSalaConditions(conditions.idSala, conditions)
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
    } catch (err) {
      console.error('Error guardando condiciones:', err)
      alert('No se pudieron guardar las condiciones')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <div className={`${styles.overlay} ${open ? styles.overlayOpen : ''}`} onClick={onClose} />
      <div className={`${styles.sidebar} ${open ? styles.sidebarOpen : ''}`}>
        <div className={styles.sbTop}>
          <div className={styles.header}>
            <span className={styles.title}>Menú</span>
            <button className={styles.closeBtn} onClick={onClose}>×</button>
          </div>

          {PANELS.map(p => (
            <div key={p.key} className={`${styles.menuItem} ${activePanel === p.key ? styles.menuActive : ''}`} onClick={() => setActivePanel(p.key)}>
              <div className={`${styles.menuIcon} ${styles[p.cls]}`}>{p.emoji}</div>
              <div><div className={styles.menuLabel}>{p.label}</div><div className={styles.menuSub}>{p.sub}</div></div>
            </div>
          ))}

          {activePanel === 'descarga' && (
            <div className={styles.panel}>
              <div className={styles.panelTitle}>exportar datos</div>
              <button className={`${styles.dlBtn} ${styles.dlPdf}`} onClick={downloadPDF} disabled={dlState.pdf === 'loading'}>
                📄 {dlState.pdf === 'loading' ? 'Generando...' : dlState.pdf === 'done' ? '✓ Descargado' : 'Descargar informe'}
              </button>
              <button className={`${styles.dlBtn} ${styles.dlCsv}`} onClick={downloadCSV} disabled={dlState.csv === 'loading'}>
                📊 {dlState.csv === 'loading' ? 'Exportando...' : dlState.csv === 'done' ? '✓ Descargado' : 'Descargar CSV'}
              </button>
            </div>
          )}

{activePanel === 'sala' && (
  <div className={styles.panel}>
    <div className={styles.panelTitle}>condiciones de sala</div>

    {conditionsLoading && (
      <div className={styles.noAlerts}>Cargando condiciones...</div>
    )}

    {conditionsError && (
      <div className={styles.noAlerts}>{conditionsError}</div>
    )}

    {!conditionsLoading && !conditionsError && conditions && (
      <>
        <div className={styles.salaName}>{conditions.nombre}</div>

        <label className={styles.formLabel}>Tamaño del aula (m²)</label>
        <input
          className={styles.formInput}
          type="number"
          min="10"
          value={conditions.size}
          onChange={e => setConditions(c => ({ ...c, size: e.target.value }))}
        />

        <label className={styles.formLabel}>Cantidad de estudiantes</label>
        <input
          className={styles.formInput}
          type="number"
          min="1"
          value={conditions.students}
          onChange={e => setConditions(c => ({ ...c, students: e.target.value }))}
        />

        <label className={styles.formLabel}>Cantidad de ventanas</label>
        <input
          className={styles.formInput}
          type="number"
          min="0"
          value={conditions.windows}
          onChange={e => setConditions(c => ({ ...c, windows: e.target.value }))}
        />

        <label className={styles.formLabel}>Aire acondicionado</label>
        <select
          className={styles.formInput}
          value={conditions.ac}
          onChange={e => setConditions(c => ({ ...c, ac: e.target.value }))}
        >
          <option value="no">Sin A/C</option>
          <option value="si">Con A/C</option>
        </select>

        <label className={styles.formLabel}>Piso del edificio</label>
        <input
          className={styles.formInput}
          type="number"
          min="1"
          value={conditions.floor}
          onChange={e => setConditions(c => ({ ...c, floor: e.target.value }))}
        />

        <div className={styles.labelRow}>
          <label className={styles.formLabel}>Tipo de ventilación</label>
          <span
            className={styles.helpIcon}
            title={`Ventilación cruzada: ${VENTILATION_HELP.cruzada}\nVentilación unilateral: ${VENTILATION_HELP.unilateral}\nSin ventilación: ${VENTILATION_HELP.ninguna}`}
          >
            ?
          </span>
        </div>
        <select
          className={styles.formInput}
          value={conditions.ventilation}
          onChange={e => setConditions(c => ({ ...c, ventilation: e.target.value }))}
        >
          {VENTILATION_TYPES.map(v => (
            <option key={v.value} value={v.value}>{v.label}</option>
          ))}
        </select>

        <button
          className={styles.saveBtn}
          onClick={saveConditions}
          disabled={saving}
        >
          {saving ? 'Guardando...' : saved ? '✓ Guardado' : 'Guardar condiciones'}
        </button>
      </>
    )}
  </div>
)}

          {activePanel === 'historial' && (
            <div className={styles.panel}>
              <div className={styles.panelTitle}>historial de alertas</div>
              {alerts.length === 0
                ? <div className={styles.noAlerts}>sin alertas registradas</div>
                : alerts.slice(0, 15).map(a => (
                  <div key={a.id} className={styles.alertItem}>
                    <span className={styles.alertIcon}>⚠️</span>
                    <div className={styles.alertInfo}>
                      <div className={styles.alertSensor}>{a.sensor}</div>
                      <div className={styles.alertMsg}>{a.high ? 'Por encima' : 'Por debajo'} del rango — {a.value} {a.unit}</div>
                    </div>
                    <div className={styles.alertTime}>{a.time}</div>
                  </div>
                ))
              }
            </div>
          )}

          {activePanel === 'nosotros' && (
            <div className={styles.panel}>
              <div className={styles.panelTitle}>sobre nosotros</div>
              <div className={styles.aboutCard}>
                <div className={styles.aboutName}>Proyecto Monitoriza</div>
                <div className={styles.aboutRole}>Monitoreo ambiental para el aprendizaje</div>
                <div className={styles.aboutDesc}>Sistema IoT basado en ESP8266 para optimizar las condiciones de aprendizaje en tiempo real.</div>
              </div>
              <div className={styles.aboutCard}>
                <div className={styles.aboutName}>Tecnología</div>
                <div className={styles.aboutRole}>ESP8266 · REST API · IA · React + Vite</div>
                <div className={styles.aboutDesc}>Frontend React + Vite. Backend ESP8266. IA para recomendaciones contextualizadas.</div>
              </div>
              <div className={styles.version}>versión 2.0.0 — Monitoriza</div>
            </div>
          )}
        </div>

        <div className={styles.sbDivider} />
        <div className={styles.catZone}>
          <button className={styles.catBtn} onClick={openCat}>
            <span className={styles.catPaw}>🐾</span>
            <span>¿quién anda por ahí?</span>
          </button>
        </div>
      </div>

      {catOpen && (
        <div className={styles.catModal} onClick={() => setCatOpen(false)}>
          <div className={styles.catCard} onClick={e => e.stopPropagation()}>
            <pre className={styles.catArt}>{catArt}</pre>
            <div className={styles.catName}>Byte</div>
            <div className={styles.catDesc}>Guardián oficial de Monitoriza.<br />Monitorea que todo esté bien desde su rincón favorito.</div>
            <button className={styles.catClose} onClick={() => setCatOpen(false)}>hasta luego, Byte 👋</button>
          </div>
        </div>
      )}
    </>
  )
}

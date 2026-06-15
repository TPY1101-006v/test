import React, { useState, useEffect } from 'react'
import {
  fetchSalaConditions,
  updateSalaConditions,
  fetchInformes,
  downloadInformePdf,
  downloadMedicionesCsv,
  fetchReportMetadata,
} from '../utils/api'
import { VENTILATION_TYPES, VENTILATION_HELP } from '../utils/constants'
import { getContextualRecommendation } from '../utils/recommendations'
import styles from './Sidebar.module.css'

const PANELS = [
  { key: 'descarga',  emoji: '📥', cls: 'blue',  label: 'Descargar datos',    sub: 'Informe PDF y CSV' },
  { key: 'sala',      emoji: '🏫', cls: 'green', label: 'Condiciones de sala', sub: 'Configurar aula' },
  { key: 'historial', emoji: '🔔', cls: 'red',   label: 'Historial de alertas', sub: 'Eventos fuera de rango' },
  { key: 'nosotros',  emoji: '👥', cls: 'gray',  label: 'Sobre nosotros',       sub: 'El equipo Monitoriza' },
]

// Gatos limpios, alineados y con espacios estándar (sin caracteres extraños)
const CATS = [
  "  /\\_/\\  \n (  . . ) \n =  > < = \n /      \\ \n(  || || )",
  "  /\\_/\\  \n (=^•^=) \n (     ) \n  |   |  \n(___\\__)",
  "  /\\_/\\  \n ( - . - ) zZ\n (  \" \"  ) \n (_______)"
]

function formatInformeFecha(fechaStr) {
  if (!fechaStr) return 'Sin fecha'
  const [y, m, d] = fechaStr.split('-').map(Number)
  const date = new Date(y, m - 1, d)
  const raw = date.toLocaleDateString('es-CL', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
  return raw.charAt(0).toUpperCase() + raw.slice(1)
}

export default function Sidebar({ open, onClose, alerts, activeAlerts, sensorData, history, soundEnabled, onToggleSound }) {
  const [conditions, setConditions] = useState(null)
  const [conditionsLoading, setConditionsLoading] = useState(true)
  const [conditionsError, setConditionsError] = useState(null)
  const [saved, setSaved] = useState(false)
  const [saving, setSaving] = useState(false)
  const [dlState, setDlState] = useState({ pdf: 'idle', csv: 'idle' })
  const [informes, setInformes] = useState([])
  const [selectedInformeId, setSelectedInformeId] = useState('')
  const [informesLoading, setInformesLoading] = useState(false)
  const [informesError, setInformesError] = useState(null)
  const [reportMeta, setReportMeta] = useState(null)
  const [reportMetaLoading, setReportMetaLoading] = useState(false)
  const [reportMetaError, setReportMetaError] = useState(null)
  const [catOpen, setCatOpen] = useState(false)
  const [catArt, setCatArt] = useState('')
  const [activePanel, setActivePanel] = useState('descarga')
  
  const [catClicks, setCatClicks] = useState(0)

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

  useEffect(() => {
    if (!open || activePanel !== 'descarga') return

    setInformesLoading(true)
    setInformesError(null)
    setReportMetaLoading(true)
    setReportMetaError(null)

    fetchInformes()
      .then(data => {
        const conPdf = (data || []).filter(i => i.tienePdf)
        setInformes(conPdf)
        setSelectedInformeId(conPdf.length ? String(conPdf[0].id) : '')
      })
      .catch(err => {
        console.error('Error cargando informes:', err)
        setInformesError('No se pudieron cargar los informes')
        setInformes([])
        setSelectedInformeId('')
      })
      .finally(() => setInformesLoading(false))

    fetchReportMetadata()
      .then(data => setReportMeta(data))
      .catch(err => {
        console.error('Error cargando metadata CSV:', err)
        setReportMetaError('No se pudo obtener el resumen del CSV')
        setReportMeta(null)
      })
      .finally(() => setReportMetaLoading(false))
  }, [open, activePanel])

  useEffect(() => {
    if (!open) setCatClicks(0)
  }, [open])

  function handleCatClick() {
    setCatClicks(prev => {
      const nuevoConteo = prev + 1
      if (nuevoConteo >= 10) {
        setCatArt(CATS[Math.floor(Math.random() * CATS.length)])
        setCatOpen(true)
        return 0 
      }
      return nuevoConteo
    })
  }

  async function downloadPDF() {
    if (!selectedInformeId) {
      alert('Selecciona un informe para descargar')
      return
    }

    setDlState(s => ({ ...s, pdf: 'loading' }))
    try {
      const blob = await downloadInformePdf(selectedInformeId)
      const informe = informes.find(i => String(i.id) === selectedInformeId)
      const fecha = informe?.fecha || new Date().toISOString().slice(0, 10)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `Monitoriza-informe-${fecha}.pdf`
      a.click()
      URL.revokeObjectURL(url)
      setDlState(s => ({ ...s, pdf: 'done' }))
    } catch (err) {
      console.error('Error descargando informe PDF:', err)
      alert('No se pudo descargar el informe PDF.')
      setDlState(s => ({ ...s, pdf: 'idle' }))
      return
    }
    setTimeout(() => setDlState(s => ({ ...s, pdf: 'idle' })), 2500)
  }

  async function downloadCSV() {
    setDlState(s => ({ ...s, csv: 'loading' }))
    try {
      const blob = await downloadMedicionesCsv()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `monitoriza-mediciones-${new Date().toISOString().slice(0, 10)}.csv`
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

  const handleSoundToggle = () => {
    if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
      Notification.requestPermission()
    }
    onToggleSound()
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

          <div style={{ padding: '0 1.5rem 1rem 1.5rem' }}>
             <button 
                onClick={handleSoundToggle}
                style={{
                  width: '100%', padding: '0.8rem', borderRadius: '8px', border: 'none',
                  backgroundColor: soundEnabled ? 'var(--warn)' : '#e2e8f0',
                  color: soundEnabled ? 'white' : '#475569', cursor: 'pointer',
                  fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem'
                }}
             >
                {soundEnabled ? '🔊 Desactivar Alertas' : '🔇 Activar Alertas'}
             </button>
          </div>

          {PANELS.map(p => (
            <div key={p.key} className={`${styles.menuItem} ${activePanel === p.key ? styles.menuActive : ''}`} onClick={() => setActivePanel(p.key)}>
              <div className={`${styles.menuIcon} ${styles[p.cls]}`}>{p.emoji}</div>
              <div><div className={styles.menuLabel}>{p.label}</div><div className={styles.menuSub}>{p.sub}</div></div>
            </div>
          ))}

          {activePanel === 'descarga' && (
            <div className={styles.panel}>
              <div className={styles.panelTitle}>Exportar datos</div>

              <div className={styles.exportCard}>
                <div className={styles.exportCardHeader}>
                  <span className={styles.exportIcon}>📄</span>
                  <div>
                    <div className={styles.exportCardTitle}>Informe diario (PDF)</div>
                    <div className={styles.exportCardSub}>
                      Análisis con IA, gráficos y recomendaciones de un día escolar.
                    </div>
                  </div>
                </div>

                {informesLoading && <div className={styles.exportHint}>Cargando informes...</div>}
                {informesError && <div className={styles.exportError}>{informesError}</div>}

                {!informesLoading && !informesError && informes.length === 0 && (
                  <div className={styles.exportHint}>No hay informes PDF disponibles aún.</div>
                )}

                {!informesLoading && !informesError && informes.length > 0 && (
                  <div className={styles.exportField}>
                    <label className={styles.formLabel} htmlFor="informe-select">
                      Selecciona el día del informe
                    </label>
                    <select
                      id="informe-select"
                      className={styles.formInput}
                      value={selectedInformeId}
                      onChange={e => setSelectedInformeId(e.target.value)}
                    >
                      {informes.map(informe => (
                        <option key={informe.id} value={informe.id}>
                          {formatInformeFecha(informe.fecha)}
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                <button
                  className={`${styles.dlBtn} ${styles.dlPdf}`}
                  onClick={downloadPDF}
                  disabled={dlState.pdf === 'loading' || informesLoading || !selectedInformeId}
                >
                  {dlState.pdf === 'loading' ? 'Descargando PDF...' : dlState.pdf === 'done' ? '✓ PDF descargado' : 'Descargar informe PDF'}
                </button>
              </div>

              <div className={styles.exportCard}>
                <div className={styles.exportCardHeader}>
                  <span className={styles.exportIcon}>📊</span>
                  <div>
                    <div className={styles.exportCardTitle}>Registro de mediciones (CSV)</div>
                    <div className={styles.exportCardSub}>
                      Todas las lecturas guardadas en la base de datos.
                    </div>
                  </div>
                </div>

                {reportMetaError && <div className={styles.exportError}>{reportMetaError}</div>}

                <button
                  className={`${styles.dlBtn} ${styles.dlCsv}`}
                  onClick={downloadCSV}
                  disabled={dlState.csv === 'loading' || reportMetaLoading || (reportMeta?.totalMediciones === 0)}
                >
                  {dlState.csv === 'loading' ? 'Generando CSV...' : dlState.csv === 'done' ? '✓ CSV descargado' : 'Descargar CSV completo'}
                </button>

                <div className={styles.exportMetaFooter}>
                  {reportMetaLoading && (
                    <span className={styles.exportMetaLine}>Calculando alcance del export...</span>
                  )}
                  {!reportMetaLoading && reportMeta && reportMeta.totalMediciones > 0 && (
                    <>
                      <span className={styles.exportMetaLine}>
                        Total de lecturas: {reportMeta.totalMediciones.toLocaleString('es-CL')}
                      </span>
                      {reportMeta.fechaDesde && reportMeta.fechaHasta && (
                        <span className={styles.exportMetaLine}>
                          Periodo: {reportMeta.fechaDesde} → {reportMeta.fechaHasta}
                        </span>
                      )}
                    </>
                  )}
                  {!reportMetaLoading && reportMeta && reportMeta.totalMediciones === 0 && (
                    <span className={styles.exportMetaLine}>Sin lecturas disponibles para exportar.</span>
                  )}
                </div>
              </div>
            </div>
          )}

          {activePanel === 'sala' && (
            <div className={styles.panel}>
              <div className={styles.panelTitle}>condiciones de sala</div>

              {conditionsLoading && <div className={styles.noAlerts}>Cargando condiciones...</div>}
              {conditionsError && <div className={styles.noAlerts}>{conditionsError}</div>}

              {!conditionsLoading && !conditionsError && conditions && (
                <>
                  <div className={styles.salaName}>{conditions.nombre}</div>

                  <label className={styles.formLabel}>Tamaño del aula (m²)</label>
                  <input className={styles.formInput} type="number" min="10" value={conditions.size} onChange={e => setConditions(c => ({ ...c, size: e.target.value }))} />

                  <label className={styles.formLabel}>Cantidad de estudiantes</label>
                  <input className={styles.formInput} type="number" min="1" value={conditions.students} onChange={e => setConditions(c => ({ ...c, students: e.target.value }))} />

                  <label className={styles.formLabel}>Cantidad de ventanas</label>
                  <input className={styles.formInput} type="number" min="0" value={conditions.windows !== undefined ? conditions.windows : ''} onChange={e => setConditions(c => ({ ...c, windows: e.target.value }))} />

                  <label className={styles.formLabel}>¿Cuenta con cortinas/persianas?</label>
                  <select className={styles.formInput} value={conditions.cortinas || 'no'} onChange={e => setConditions(c => ({ ...c, cortinas: e.target.value }))}>
                    <option value="no">Sin cortinas</option>
                    <option value="si">Con cortinas</option>
                  </select>

                  <label className={styles.formLabel}>Aire acondicionado</label>
                  <select className={styles.formInput} value={conditions.ac} onChange={e => setConditions(c => ({ ...c, ac: e.target.value }))}>
                    <option value="no">Sin A/C</option>
                    <option value="si">Con A/C</option>
                  </select>

                  <label className={styles.formLabel}>Piso del edificio</label>
                  <input className={styles.formInput} type="number" min="1" value={conditions.floor} onChange={e => setConditions(c => ({ ...c, floor: e.target.value }))} />

                  <div className={styles.labelRow}>
                    <label className={styles.formLabel}>Tipo de ventilación</label>
                    <span className={styles.helpIcon} title={`Ventilación cruzada: ${VENTILATION_HELP.cruzada}\nVentilación unilateral: ${VENTILATION_HELP.unilateral}\nSin ventilación: ${VENTILATION_HELP.ninguna}`}>
                      ?
                    </span>
                  </div>
                  <select className={styles.formInput} value={conditions.ventilation} onChange={e => setConditions(c => ({ ...c, ventilation: e.target.value }))}>
                    {VENTILATION_TYPES.map(v => (
                      <option key={v.value} value={v.value}>{v.label}</option>
                    ))}
                  </select>

                  <button className={styles.saveBtn} onClick={saveConditions} disabled={saving}>
                    {saving ? 'Guardando...' : saved ? '✓ Guardado' : 'Guardar condiciones'}
                  </button>
                </>
              )}
            </div>
          )}

          {activePanel === 'historial' && (
            <div className={styles.panel}>
              <div className={styles.panelTitle}>historial de alertas</div>
              
              {(!activeAlerts || activeAlerts.length === 0) && alerts.length === 0 ? (
                <div className={styles.noAlerts}>sin alertas registradas</div>
              ) : (
                <>
                  {/* ALERTAS EN VIVO CON RECOMENDACIÓN */}
                  {activeAlerts && activeAlerts.map(a => (
                    <div key={`live-${a.sensorKey}`} className={styles.alertItem} style={{ borderLeft: '3px solid var(--warn)', flexDirection: 'column', alignItems: 'flex-start' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                          <span className={styles.alertIcon}>🚨</span>
                          <div className={styles.alertInfo}>
                            <div className={styles.alertSensor}>{a.label}</div>
                            <div className={styles.alertMsg}>{a.high ? 'Por encima' : 'Por debajo'} del rango — <strong>{a.value} {a.unit}</strong></div>
                          </div>
                        </div>
                        <div className={styles.alertTime} style={{ color: 'var(--warn)', fontWeight: 'bold' }}>LIVE</div>
                      </div>
                      <div style={{ marginTop: '0.6rem', fontSize: '0.8rem', color: '#9b2c2c', backgroundColor: '#fff5f5', padding: '0.6rem', borderRadius: '4px', width: '100%' }}>
                        💡 {getContextualRecommendation(a, conditions)}
                      </div>
                    </div>
                  ))}

                  {/* HISTORIAL PASADO */}
                  {alerts.slice(0, 15).map(a => (
                    <div key={a.id} className={styles.alertItem}>
                      <span className={styles.alertIcon}>⚠️</span>
                      <div className={styles.alertInfo}>
                        <div className={styles.alertSensor}>{a.sensor || a.label}</div>
                        <div className={styles.alertMsg}>{a.high ? 'Por encima' : 'Por debajo'} del rango — {a.value} {a.unit}</div>
                      </div>
                      <div className={styles.alertTime}>{a.time}</div>
                    </div>
                  ))}
                </>
              )}
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
          <button className={styles.catBtn} onClick={handleCatClick}>
            <span className={styles.catPaw}>🐾</span>
            <span>
              {catClicks > 0 ? `¡Presiona ${10 - catClicks} veces más!` : '¿quién anda por ahí?'}
            </span>
          </button>
        </div>
      </div>

      {catOpen && (
        <div className={styles.catModal} onClick={() => setCatOpen(false)}>
          <div className={styles.catCard} onClick={e => e.stopPropagation()}>
            <pre className={styles.catArt}>{catArt}</pre>
            <div className={styles.catName}>Byte</div>
            <div className={styles.catDesc}>Guardián oficial de mi-aula.<br />Monitorea que todo esté bien desde su rincón favorito.</div>
            <button className={styles.catClose} onClick={() => setCatOpen(false)}>hasta luego, Byte 👋</button>
          </div>
        </div>
      )}
    </>
  )
}
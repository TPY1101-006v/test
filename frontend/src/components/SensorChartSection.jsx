import React, { useState, useRef, useEffect } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ReferenceLine, ResponsiveContainer,
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
} from 'recharts'
import { SENSORS } from '../utils/constants'
import styles from './SensorChartSection.module.css'

// ── helpers ──────────────────────────────────────────────────────────────────
function normalize(sensor, value) {
  if (value === undefined || isNaN(value)) return 50
  return Math.min(Math.max(Math.round((value / sensor.ideal) * 100), 0), 150)
}
function formatVal(key, v) {
  if (v === undefined || isNaN(v)) return '—'
  return key === 'lux' || key === 'ppm' ? Math.round(v) : Number(v).toFixed(1)
}

// ── Spider tooltip ────────────────────────────────────────────────────────────
const SpiderTooltip = ({ active, payload, values }) => {
  if (!active || !payload?.length) return null
  return (
    <div className={styles.tooltip}>
      {SENSORS.map(s => (
        <div key={s.key} className={styles.tooltipRow}>
          <span style={{ color: s.color }}>■</span>
          <span className={styles.tooltipLabel}>{s.label}</span>
          <span className={styles.tooltipVal}>{formatVal(s.key, values[s.key])} {s.unit}</span>
        </div>
      ))}
    </div>
  )
}

// ── Main component ────────────────────────────────────────────────────────────
export default function SensorChartSection({ history, values }) {
  const [activeGraph, setActiveGraph] = useState('historico')
  const [selectedSensor, setSelectedSensor] = useState('temperatura')

  // Line chart data
  const sensor = SENSORS.find(s => s.key === selectedSensor)
  const entries = history[selectedSensor] || []
  const lineData = entries.map(e => ({
    time: e.t,
    valor: e[selectedSensor] !== undefined
      ? parseFloat(Number(e[selectedSensor]).toFixed(2))
      : null,
    ideal: sensor.ideal,
  }))

  // Spider chart data
  const spiderData = SENSORS.map(s => ({
    sensor: s.label,
    actual: normalize(s, values[s.key]),
    ideal: 100,
  }))

  return (
    <div className={styles.section}>
      {/* Top bar */}
      <div className={styles.topBar}>
        <span className={styles.title}>datos del aula</span>
        <div className={styles.tabs}>
          <button
            className={`${styles.tab} ${activeGraph === 'historico' ? styles.tabActive : ''}`}
            onClick={() => setActiveGraph('historico')}
          >
            📈 Histórico
          </button>
          <button
            className={`${styles.tab} ${activeGraph === 'spider' ? styles.tabActive : ''}`}
            onClick={() => setActiveGraph('spider')}
          >
            🕸️ Vista General
          </button>
        </div>
      </div>

      {/* ── HISTÓRICO ── */}
      {activeGraph === 'historico' && (
        <>
          <div className={styles.sensorSelector}>
            {SENSORS.map(s => (
              <button
                key={s.key}
                className={`${styles.selBtn} ${selectedSensor === s.key ? styles.selActive : ''}`}
                onClick={() => setSelectedSensor(s.key)}
              >
                {s.label}
              </button>
            ))}
          </div>
          <div className={styles.legend}>
            <span className={styles.legItem}>
              <span className={styles.legLine} style={{ background: sensor.color }} />
              Medición
            </span>
            <span className={styles.legItem}>
              <span className={styles.legDash} />
              Valor ideal ({sensor.ideal} {sensor.unit})
            </span>
          </div>
          <div className={styles.chartWrap}>
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={lineData} margin={{ top: 8, right: 16, bottom: 0, left: -10 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                <XAxis dataKey="time" tick={{ fill: '#718096', fontSize: 11 }} tickLine={false} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} />
                <YAxis tick={{ fill: '#718096', fontSize: 11 }} tickLine={false} axisLine={{ stroke: 'rgba(255,255,255,0.1)' }} />
                <Tooltip
                  contentStyle={{ background: '#1e2333', border: '0.5px solid rgba(255,255,255,0.1)', borderRadius: 8, fontSize: 12, color: '#e2e8f0' }}
                  formatter={(val) => [`${val} ${sensor.unit}`, sensor.label]}
                />
                <ReferenceLine y={sensor.ideal} stroke="#f6ad55" strokeDasharray="6 4" strokeWidth={1.5} />
                <Line type="monotone" dataKey="valor" stroke={sensor.color} strokeWidth={2} dot={{ fill: sensor.color, r: 3 }} activeDot={{ r: 5 }} connectNulls />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </>
      )}

      {/* ── SPIDER ── */}
      {activeGraph === 'spider' && (
        <>
          <p className={styles.spiderSub}>
            Comparación normalizada de todos los sensores — 100% = valor ideal para el aprendizaje
          </p>
          <div className={styles.spiderWrap}>
            <ResponsiveContainer width="100%" height={280}>
              <RadarChart data={spiderData} margin={{ top: 10, right: 30, bottom: 10, left: 30 }}>
                <PolarGrid stroke="rgba(255,255,255,0.08)" />
                <PolarAngleAxis dataKey="sensor" tick={{ fill: '#a0aec0', fontSize: 12, fontFamily: 'DM Sans' }} />
                <PolarRadiusAxis angle={90} domain={[0, 150]} tick={false} axisLine={false} />
                <Radar name="Valor ideal" dataKey="ideal" stroke="#f6ad55" fill="#f6ad55" fillOpacity={0.08} strokeDasharray="5 4" strokeWidth={1.5} />
                <Radar name="Medición actual" dataKey="actual" stroke="#63b3ed" fill="#63b3ed" fillOpacity={0.18} strokeWidth={2} dot={{ r: 4, fill: '#63b3ed', strokeWidth: 0 }} />
                <Tooltip content={<SpiderTooltip values={values} />} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
          <div className={styles.spiderLegend}>
            <span className={styles.spLeg}><span className={styles.spDot} style={{ background: '#63b3ed' }} />Medición actual</span>
            <span className={styles.spLeg}><span className={styles.spDot} style={{ background: '#f6ad55', opacity: 0.7 }} />Valor ideal (100%)</span>
            <span className={styles.spNote}>Valores basados en investigación de entornos de aprendizaje óptimos</span>
          </div>
        </>
      )}
    </div>
  )
}

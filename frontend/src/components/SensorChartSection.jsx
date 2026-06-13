import React, { useState } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ReferenceLine, ResponsiveContainer,
} from 'recharts'
import { SENSORS } from '../utils/constants'
import styles from './SensorChartSection.module.css'

const ICON_MAP = {
  temperatura: '🌡️',
  humedad: '💧',
  db: '🔊',
  lux: '💡',
  eco2: '🍃',
  tvoc: '🧪'
}

export default function SensorChartSection({ history }) {
  const [selectedSensor, setSelectedSensor] = useState('temperatura')

  const sensor = SENSORS.find(s => s.key === selectedSensor)
  const entries = history[selectedSensor] || []
  
  const lineData = entries.map(e => ({
    time: e.t,
    valor: (e[selectedSensor] !== undefined && e[selectedSensor] !== null)
      ? parseFloat(Number(e[selectedSensor]).toFixed(2))
      : null,
  }))

  // Calculamos el máximo para asegurar que la línea ideal siempre se vea
  const dataValues = lineData.map(d => d.valor).filter(v => v !== null);
  const maxVal = dataValues.length > 0 ? Math.max(...dataValues) : sensor.ideal;
  const domainMax = Math.max(maxVal * 1.1, sensor.ideal * 1.2);

  return (
    <div className={styles.section}>
      <div className={styles.topBar}>
        <span className={styles.title}>Datos históricos del aula</span>
      </div>

      <div className={styles.sensorSelector}>
        {SENSORS.map(s => {
          const label = s.key === 'eco2' ? 'CO2' : s.key === 'tvoc' ? 'TVOC' : s.label;
          return (
            <button
              key={s.key}
              className={`${styles.selBtn} ${selectedSensor === s.key ? styles.selActive : ''}`}
              onClick={() => setSelectedSensor(s.key)}
            >
              <span className={styles.iconWrapper}>{ICON_MAP[s.key]}</span>
              {label}
            </button>
          )
        })}
      </div>
      
      <div style={{ marginTop: '30px' }}>
        <div className={styles.legend}>
          <span className={styles.legItem}>
            <span className={styles.legLine} style={{ background: sensor.color }} />
            Medición
          </span>
          <span className={styles.legItem}>
            <span className={styles.legDash} />
            Ideal ({sensor.ideal} {sensor.unit})
          </span>
        </div>
        
        <div className={styles.chartWrap}>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={lineData} margin={{ top: 10, right: 10, bottom: 0, left: -15 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis dataKey="time" tick={{ fill: '#718096', fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis 
                domain={[0, domainMax]} 
                tick={{ fill: '#718096', fontSize: 11 }} 
                tickLine={false} 
                axisLine={false} 
              />
              <Tooltip
                contentStyle={{ background: '#1e2333', border: '0.5px solid rgba(255,255,255,0.1)', borderRadius: 8 }}
                formatter={(val) => [`${val} ${sensor.unit}`, sensor.label]}
              />
              <ReferenceLine y={sensor.ideal} stroke="#f6ad55" strokeDasharray="6 4" strokeWidth={1.5} />
              <Line type="monotone" dataKey="valor" stroke={sensor.color} strokeWidth={2} dot={{ r: 2 }} connectNulls />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
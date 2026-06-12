import React, { useState } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ReferenceLine, ResponsiveContainer, Legend
} from 'recharts'
import { SENSORS } from '../utils/constants'
import styles from './SensorChart.module.css'

function buildChartData(history, sensorKey) {
  const entries = history[sensorKey] || []
  return entries.map(entry => ({
    time: entry.t,
    valor: entry[sensorKey] !== undefined
      ? parseFloat(
          sensorKey === 'lux' || sensorKey === 'ppm'
            ? Math.round(entry[sensorKey])
            : Number(entry[sensorKey]).toFixed(2)
        )
      : null,
  }))
}

export default function SensorChart({ history }) {
  const [selected, setSelected] = useState('temperatura')
  const sensor = SENSORS.find(s => s.key === selected)
  const data = buildChartData(history, selected)

  return (
    <div className={styles.section}>
      <div className={styles.header}>
        <span className={styles.title}>histórico — último período</span>
        <div className={styles.selector}>
          {SENSORS.map(s => (
            <button
              key={s.key}
              className={`${styles.selBtn} ${selected === s.key ? styles.active : ''}`}
              onClick={() => setSelected(s.key)}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      <div className={styles.legend}>
        <span className={styles.legendItem}>
          <span className={styles.legendLine} style={{ background: sensor.color }} />
          Medición
        </span>
        <span className={styles.legendItem}>
          <span className={styles.legendDash} />
          Valor ideal ({sensor.ideal} {sensor.unit})
        </span>
      </div>

      <div className={styles.chartWrap}>
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: -10 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
            <XAxis
              dataKey="time"
              tick={{ fill: '#718096', fontSize: 11 }}
              tickLine={false}
              axisLine={{ stroke: 'rgba(255,255,255,0.1)' }}
            />
            <YAxis
              tick={{ fill: '#718096', fontSize: 11 }}
              tickLine={false}
              axisLine={{ stroke: 'rgba(255,255,255,0.1)' }}
            />
            <Tooltip
              contentStyle={{
                background: '#1e2333',
                border: '0.5px solid rgba(255,255,255,0.1)',
                borderRadius: 8,
                fontSize: 12,
                color: '#e2e8f0',
              }}
              formatter={(val) => [`${val} ${sensor.unit}`, sensor.label]}
            />
            <ReferenceLine
              y={sensor.ideal}
              stroke="#f6ad55"
              strokeDasharray="6 4"
              strokeWidth={1.5}
            />
            <Line
              type="monotone"
              dataKey="valor"
              stroke={sensor.color}
              strokeWidth={2}
              dot={{ fill: sensor.color, r: 3 }}
              activeDot={{ r: 5 }}
              connectNulls
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

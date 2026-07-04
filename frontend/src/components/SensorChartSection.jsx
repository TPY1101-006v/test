import React, { useState } from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceArea,
  ResponsiveContainer,
} from 'recharts'
import { SENSORS } from '../utils/constants'
import styles from './SensorChartSection.module.css'

const STRICT_NEGATIVE_KEYS = ['eco2', 'tvoc', 'lux', 'humedad', 'db']

function formatSensorLabel(sensor) {
  if (!sensor) return ''

  if (sensor.key === 'eco2') return 'CO₂'
  if (sensor.key === 'tvoc') return 'TVOC'

  return sensor.label
}

function cleanSensorValue(rawValue, sensor) {
  if (rawValue === undefined || rawValue === null || rawValue === '') {
    return null
  }

  let value = Number(rawValue)

  if (Number.isNaN(value)) {
    return null
  }

  const limiteLogico = sensor?.max ? sensor.max * 3 : 10000

  const esNegativoImposible =
    STRICT_NEGATIVE_KEYS.includes(sensor.key) && value < 0

  if (esNegativoImposible || value > limiteLogico) {
    return null
  }

  if (sensor.key === 'lux' || sensor.key === 'eco2' || sensor.key === 'tvoc') {
    return Math.round(value)
  }

  return Number(value.toFixed(2))
}

function buildLineData(history, selectedSensor, sensor) {
  const entries = history[selectedSensor] || []

  return entries.map(entry => {
    const rawValue =
      entry[selectedSensor] !== undefined
        ? entry[selectedSensor]
        : entry.v !== undefined
          ? entry.v
          : entry.value

    return {
      time: entry.t,
      valor: cleanSensorValue(rawValue, sensor),
    }
  })
}

function getDomain(lineData, sensor) {
  const cleanValues = lineData
    .map(d => d.valor)
    .filter(v => v !== null && v !== undefined && !Number.isNaN(v))

  const baseValues = [
    ...cleanValues,
    sensor.min,
    sensor.max,
    sensor.ideal,
  ].filter(v => v !== null && v !== undefined && !Number.isNaN(Number(v)))

  const minBase = baseValues.length > 0 ? Math.min(...baseValues) : 0
  const maxBase = baseValues.length > 0 ? Math.max(...baseValues) : 1

  let domainMin

  if (minBase >= 0) {
    domainMin = Math.max(0, Math.floor(minBase * 0.85))
  } else {
    domainMin = Math.floor(minBase * 1.15)
  }

  const domainMax = Math.ceil(maxBase * 1.15)

  return [domainMin, domainMax <= domainMin ? domainMin + 1 : domainMax]
}

export default function SensorChartSection({ history }) {
  const [selectedSensor, setSelectedSensor] = useState('temperatura')

  const sensor = SENSORS.find(s => s.key === selectedSensor) || SENSORS[0]
  const lineData = buildLineData(history, selectedSensor, sensor)
  const [domainMin, domainMax] = getDomain(lineData, sensor)

  return (
    <div className={styles.section}>
      <div className={styles.topBar}>
        <span className={styles.title}>
          Datos históricos del aula
        </span>
      </div>

      <div className={styles.sensorSelector}>
        {SENSORS.map(s => {
          const label = formatSensorLabel(s)

          return (
            <button
              key={s.key}
              type="button"
              className={`${styles.selBtn} ${selectedSensor === s.key ? styles.selActive : ''}`}
              onClick={() => setSelectedSensor(s.key)}
              title={s.label}
            >
              <span
                className={`material-symbols-outlined ${styles.iconWrapper}`}
                style={{ color: selectedSensor === s.key ? s.color : undefined }}
              >
                {s.icon}
              </span>

              <span className={styles.sensorLabel}>
                {label}
              </span>
            </button>
          )
        })}
      </div>

      <div className={styles.chartBlock}>
        <div className={styles.legend}>
          <span className={styles.legItem}>
            <span
              className={styles.legLine}
              style={{ background: sensor.color }}
            />
            Medición
          </span>

          <span className={styles.legItem}>
            <span
              className={styles.legBand}
              style={{
                backgroundColor: `${sensor.color}22`,
                borderColor: `${sensor.color}55`,
              }}
            />
            Rango ideal ({sensor.min}–{sensor.max} {sensor.unit})
          </span>
        </div>

        <div className={styles.chartWrap}>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart
              data={lineData}
              margin={{ top: 10, right: 10, bottom: 0, left: -15 }}
            >
              <CartesianGrid
                strokeDasharray="3 3"
                stroke="var(--border)"
              />

              <XAxis
                dataKey="time"
                tick={{ fill: 'var(--muted)', fontSize: 11 }}
                tickLine={false}
                axisLine={false}
              />

              <YAxis
                domain={[domainMin, domainMax]}
                tick={{ fill: 'var(--muted)', fontSize: 11 }}
                tickLine={false}
                axisLine={false}
              />

              <Tooltip
                contentStyle={{
                  background: 'var(--card)',
                  border: '1px solid var(--border)',
                  borderRadius: 8,
                  color: 'var(--text)',
                  fontSize: 12,
                }}
                itemStyle={{ color: 'var(--text)' }}
                labelStyle={{ color: 'var(--muted)' }}
                formatter={(val) => [`${val} ${sensor.unit}`, sensor.label]}
              />

              <ReferenceArea
                y1={sensor.min}
                y2={sensor.max}
                fill={sensor.color}
                fillOpacity={0.12}
                strokeOpacity={0}
              />

              <Line
                type="monotone"
                dataKey="valor"
                stroke={sensor.color}
                strokeWidth={2}
                dot={{ r: 2, fill: sensor.color }}
                activeDot={{ r: 5 }}
                connectNulls
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
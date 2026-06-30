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
  
  const lineData = entries.map(e => {
    // 1. Buscamos el valor (soportamos distintas formas en las que puede llegar el JSON)
    let rawVal = e[selectedSensor] !== undefined ? e[selectedSensor] : (e.v !== undefined ? e.v : e.value);
    let parsedVal = (rawVal !== undefined && rawVal !== null) ? Number(rawVal) : null;

    // 2. ESCUDO ANTI-ANOMALÍAS (Outlier Filter)
    // Si el valor supera el máximo del sensor x 3 (o un límite general), es un error de hardware.
    // También filtramos lecturas negativas en sensores donde no tiene sentido físico.
    const limiteLogico = sensor?.max ? sensor.max * 3 : 10000;
    
    if (parsedVal !== null && (parsedVal > limiteLogico || parsedVal < 0)) {
      parsedVal = null; // Ignoramos el dato basura para no deformar la gráfica
    } else if (parsedVal !== null) {
      parsedVal = parseFloat(parsedVal.toFixed(2));
    }

    return {
      time: e.t,
      valor: parsedVal
    }
  })

  // 3. Calculamos el máximo real solo tomando en cuenta los datos limpios
  const dataValues = lineData.map(d => d.valor).filter(v => v !== null);
  const maxVal = dataValues.length > 0 ? Math.max(...dataValues) : sensor.ideal;
  
  // 4. Redondeamos el límite superior para que el eje Y se vea más limpio
  const domainMax = Math.ceil(Math.max(maxVal * 1.1, sensor.ideal * 1.2));

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
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="time" tick={{ fill: 'var(--muted)', fontSize: 11 }} tickLine={false} axisLine={false} />
              
              {/* Aplicamos el nuevo dominio limpio */}
              <YAxis 
                domain={[0, domainMax]} 
                tick={{ fill: 'var(--muted)', fontSize: 11 }} 
                tickLine={false} 
                axisLine={false} 
              />
              
              <Tooltip
                contentStyle={{ background: 'var(--card)', border: '1px solid var(--border)', borderRadius: 8 }}
                itemStyle={{ color: 'var(--text)' }}
                formatter={(val) => [`${val} ${sensor.unit}`, sensor.label]}
              />
              <ReferenceLine y={sensor.ideal} stroke="var(--accent3)" strokeDasharray="6 4" strokeWidth={1.5} />
              
              {/* connectNulls={true} hará que la línea ignore los cortes de los datos que acabamos de filtrar */}
              <Line type="monotone" dataKey="valor" stroke={sensor.color} strokeWidth={2} dot={{ r: 2 }} connectNulls />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
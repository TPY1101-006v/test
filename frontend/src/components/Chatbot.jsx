import React, { useState, useRef, useEffect } from 'react'
import { sendChatMessage } from '../utils/api'
import styles from './Chatbot.module.css'

const SUGGESTIONS = [
  {
    icon: 'school',
    label: 'CO₂ en el aula',
    text: '¿Qué consecuencias tiene el CO₂ por encima de 800 ppm en una sala escolar? Responde en 2 oraciones.',
  },
  {
    icon: 'monitoring',
    label: 'Sensores hoy',
    text: 'Según las mediciones de hoy, ¿qué sensores están alterados? Solo lista los nombres.',
  },
  {
    icon: 'trending_up',
    label: 'Patrones del mes',
    text: 'En los informes del mes, ¿qué sensor tuvo más alteraciones? Responde con el nombre y el total.',
  },
]

function Icon({ name, className = '' }) {
  return (
    <span className={`material-symbols-outlined ${className}`}>
      {name}
    </span>
  )
}

export default function Chatbot({ sensorData, alerts }) {
  const [messages, setMessages] = useState([
    {
      id: 0,
      type: 'bot',
      text: 'Hola, soy tu asistente ambiental de mi-aula. Puedo explicarte los sensores, consultar las mediciones de hoy o analizar patrones de los informes del mes.',
      icon: 'smart_toy',
    },
  ])

  const [input, setInput] = useState('')
  const [typing, setTyping] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, typing])

  async function send(text) {
    if (!text.trim()) return

    const userMsg = {
      id: Date.now(),
      type: 'user',
      text,
      icon: 'person',
    }

    setMessages(prev => [...prev, userMsg])
    setInput('')
    setTyping(true)

    try {
      const reply = await sendChatMessage(text)

      setMessages(prev => [
        ...prev,
        {
          id: Date.now() + 1,
          type: 'bot',
          text: reply,
          icon: 'smart_toy',
        },
      ])
    } catch (err) {
      setMessages(prev => [
        ...prev,
        {
          id: Date.now() + 1,
          type: 'bot',
          text: err.message || 'No pude conectarme al asistente. Intenta nuevamente.',
          icon: 'warning',
        },
      ])
    } finally {
      setTyping(false)
    }
  }

  return (
    <div className={styles.section}>
      <div className={styles.title}>
        <Icon name="smart_toy" className={styles.titleIcon} />

        <span>Asistente IA</span>

        <span className={styles.badge}>
          CONECTADO
        </span>
      </div>

      <div className={styles.messages}>
        {messages.map(msg => (
          <div
            key={msg.id}
            className={`${styles.msg} ${styles[msg.type]}`}
          >
            <Icon
              name={msg.icon || (msg.type === 'user' ? 'person' : 'smart_toy')}
              className={styles.msgIcon}
            />

            <span className={styles.msgText}>
              {msg.text}
            </span>
          </div>
        ))}

        {typing && (
          <div className={styles.typing}>
            <Icon name="more_horiz" className={styles.typingIcon} />
            escribiendo...
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      <div className={styles.suggestions}>
        {SUGGESTIONS.map(s => (
          <button
            key={s.text}
            className={styles.sugBtn}
            onClick={() => send(s.text)}
            type="button"
          >
            <Icon name={s.icon} className={styles.sugIcon} />
            {s.label}
          </button>
        ))}
      </div>

      <div className={styles.inputRow}>
        <input
          className={styles.input}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send(input)}
          placeholder="Escribe tu pregunta..."
          disabled={typing}
        />

        <button
          className={styles.sendBtn}
          onClick={() => send(input)}
          disabled={typing}
          type="button"
        >
          <Icon name="send" className={styles.sendIcon} />
          Enviar
        </button>
      </div>
    </div>
  )
}
import React, { useState, useRef, useEffect } from 'react'
import { sendChatMessage } from '../utils/api'
import styles from './Chatbot.module.css'

const SUGGESTIONS = [
  { label: '📋 Resumen del día', text: 'Dame el resumen del día' },
  { label: '🚨 Ver alertas', text: '¿Cuáles han sido las alertas?' },
]

export default function Chatbot({ sensorData, alerts }) {
  const [messages, setMessages] = useState([
    {
      id: 0,
      type: 'bot',
      text: '👋 Hola, soy tu asistente de Monitoriza. Puedo darte el resumen del día, informarte sobre alertas o responder tus preguntas sobre el ambiente del aula.',
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
    const userMsg = { id: Date.now(), type: 'user', text }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setTyping(true)

    try {
      const reply = await sendChatMessage(text, sensorData, alerts)
      setMessages(prev => [...prev, { id: Date.now() + 1, type: 'bot', text: reply }])
    } catch {
      setMessages(prev => [...prev, {
        id: Date.now() + 1, type: 'bot',
        text: '⚠️ No pude conectarme al asistente. Intenta nuevamente.',
      }])
    } finally {
      setTyping(false)
    }
  }

  return (
    <div className={styles.section}>
      <div className={styles.title}>
        asistente IA
        <span className={styles.badge}>CONECTADO</span>
      </div>

      <div className={styles.messages}>
        {messages.map(msg => (
          <div key={msg.id} className={`${styles.msg} ${styles[msg.type]}`}>
            {msg.text}
          </div>
        ))}
        {typing && <div className={styles.typing}>escribiendo...</div>}
        <div ref={bottomRef} />
      </div>

      <div className={styles.suggestions}>
        {SUGGESTIONS.map(s => (
          <button key={s.text} className={styles.sugBtn} onClick={() => send(s.text)}>
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
        <button className={styles.sendBtn} onClick={() => send(input)} disabled={typing}>
          Enviar
        </button>
      </div>
    </div>
  )
}

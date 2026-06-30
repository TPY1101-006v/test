export function getContextualRecommendation(alert, conditions) {
  const key = (alert.sensorKey || alert.label || alert.sensor || '').toLowerCase();

  // 1. FILTRO DE IMPOSIBLES (Errores de lectura)
  if (alert.value < 0 && (key.includes('co2') || key.includes('ppm') || key.includes('lux') || key.includes('tvoc') || key.includes('humedad'))) {
    return "⚠️ Lectura imposible (valor negativo). Verificar calibración o cableado del sensor.";
  }

  if (!conditions) return "Cargando contexto del aula para sugerencias...";

  // 2. EXTRACCIÓN DEL CONTEXTO DE LA SALA
  const studentsCount = Number(conditions.students) || 0;
  const hasAC = conditions.ac === 'si';
  const hasCortinas = conditions.cortinas === 'si';
  const windowCount = Number(conditions.windows) || 0;
  const ventType = conditions.ventilation || 'ninguna';

  // 3. MOTOR DE DECISIONES
  switch (true) {
    
    // --- TEMPERATURA ---
    case key.includes('temperatura'):
      if (alert.high) {
        let msg = "Exceso de calor. ";
        if (hasAC) {
          msg += "Activar A/C a 21°C. ";
        } else if (windowCount > 0) {
          msg += "Abrir ventanas y puerta para generar corriente de aire. ";
        } else {
          msg += "Sala sin ventanas: Abrir puerta principal de par en par y encender ventiladores. ";
        }
        if (studentsCount > 30) msg += "⚠️ Alta densidad biológica acelerando el calentamiento.";
        return msg.trim();
      } else {
        if (hasAC) return "Baja temperatura. Activar calefacción del A/C y cerrar puerta para reducir el estrés térmico.";
        if (windowCount > 0) return "Baja temperatura. Cerrar ventanas y puerta para retener calor y mejorar la concentración.";
        return "Baja temperatura. Mantener puerta cerrada; el frío desvía recursos metabólicos y reduce la satisfacción térmica.";
      }

    // --- HUMEDAD ---
    case key.includes('humedad'):
      if (alert.high) {
        if (hasAC) return "Alta humedad. Activar A/C en modo Deshumidificador (Dry) para evitar moho y carga térmica.";
        if (windowCount > 0) return "Alta humedad. Abrir ventanas y puerta para facilitar la evaporación del sudor.";
        return "Alta humedad. Abrir puerta principal y encender extractores de aire.";
      }
      return "Humedad muy baja. Cerrar puerta y ventanas para evitar resecar vías respiratorias y ojos.";

    // --- RUIDO ---
    case key.includes('db') || key.includes('ruido'):
      if (!alert.high) return "";
      if (windowCount > 0) return "Ruido por encima de 45 dBA. Cerrar ventanas y puerta para aislar el ruido externo.";
      return "Ruido por encima de 45 dBA. Solicitar silencio a la clase y mantener puerta cerrada hacia el pasillo.";

    // --- ILUMINACIÓN ---
    case key.includes('lux') || key.includes('iluminación'):
      if (alert.high) {
        if (windowCount > 0) {
          if (hasCortinas) return "Deslumbramiento. Cerrar cortinas o persianas inmediatamente.";
          return "Exceso de luz natural. Apagar luces artificiales.";
        }
        // Exceso de luz en sala sin ventanas:
        return "Exceso de luz artificial en sala ciega. Apagar parte de los interruptores de luminarias.";
      } else {
        if (windowCount > 0 && hasCortinas) return "Luz deficiente. Abrir cortinas para aprovechar el sol y encender luces.";
        return "Luz deficiente. Encender todas las luminarias del aula.";
      }

    // --- CO2 / CALIDAD DEL AIRE ---
    case key.includes('ppm') || key.includes('co2') || key.includes('carbono'):
      if (alert.high) {
        if (windowCount === 0) return "🚨 CRÍTICO: Sala sin ventanas. abrir puerta principal de par en par inmediatamente.";
        if (ventType === 'cruzada') return "Acción urgente: Abrir ventanas y puerta principal simultáneamente para lograr ventilación cruzada real.";
        if (ventType === 'unilateral') return "Acción urgente: Abrir todas las ventanas y abrir la puerta hacia el pasillo para apoyar el flujo.";
        return "Acción urgente: Abrir ventanas y puertas inmediatamente.";
      }
      return "";

    // --- TVOC / GASES ORGÁNICOS ---
    case key.includes('tvoc') || key.includes('orgánicos'):
      if (alert.high) {
        if (windowCount === 0) return "🚨 CRÍTICO: Gases/Olores detectados en sala sin ventanas. dejar puerta abierta para ventilar.";
        return "Gases, olores o químicos detectados. Abrir ventanas y puertas inmediatamente para diluir los contaminantes hacia el exterior.";
      }
      return "";

    default:
      return "Monitorear parámetro de cerca.";
  }
}
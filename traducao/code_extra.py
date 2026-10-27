# Extra code patches applied together with the translation (exec'd by patch_code.py; adds to PATCHES).
# They rewrite the few places where English word order is built in code, not just in single strings.

PATCHES.setdefault('Geo.kt', []).extend([
    # spoken distances
    ('''            return if (ft < 528) "${((ft / 50).roundToInt()) * 50} feet" else String.format("%.1f miles", m / 1609.344)
        }
        return if (m < 1000) "${((m / 50).roundToInt()) * 50} metres" else String.format("%.1f kilometres", m / 1000.0)''',
     '''            return if (ft < 528) "${((ft / 50).roundToInt()) * 50} pés" else String.format("%.1f milhas", m / 1609.344)
        }
        return if (m < 1000) "${((m / 50).roundToInt()) * 50} metros" else String.format(java.util.Locale("pt", "BR"), "%.1f quilômetros", m / 1000.0)'''),
    # ordinals: 1ª, 2ª … (the voice reads "primeira", "segunda")
    ('''    fun ordinal(n: Int): String = when {
        n % 100 in 11..13 -> "${n}th"
        n % 10 == 1 -> "${n}st"; n % 10 == 2 -> "${n}nd"; n % 10 == 3 -> "${n}rd"
        else -> "${n}th"
    }''',
     '''    fun ordinal(n: Int): String = "${n}ª"'''),
    # manoeuvre wording
    ('''            if (u == "ROUNDABOUT_BACK") return if (exit > 0) "take the ${ordinal(exit)} exit at the roundabout" else "go all the way round the roundabout"
            return if (exit > 0) "at the roundabout, take the ${ordinal(exit)} exit" else "take the roundabout"''',
     '''            if (u == "ROUNDABOUT_BACK") return if (exit > 0) "pegue a ${ordinal(exit)} saída na rotatória" else "dê a volta completa na rotatória"
            return if (exit > 0) "na rotatória, pegue a ${ordinal(exit)} saída" else "entre na rotatória"'''),
    ('''            "ARRIVE" to "arrive", "ARRIVE_LEFT" to "arrive, destination on the left", "ARRIVE_RIGHT" to "arrive, destination on the right",
            "DEPART" to "head out", "MAKE_UTURN" to "make a U-turn", "TRY_MAKE_UTURN" to "make a U-turn when possible",
            "STRAIGHT" to "continue straight", "FOLLOW" to "continue", "TURN_LEFT" to "turn left", "TURN_RIGHT" to "turn right",
            "SHARP_LEFT" to "turn sharp left", "SHARP_RIGHT" to "turn sharp right", "BEAR_LEFT" to "bear left", "BEAR_RIGHT" to "bear right",
            "KEEP_LEFT" to "keep left", "KEEP_RIGHT" to "keep right", "ENTER_MOTORWAY" to "join the motorway", "ENTER_FREEWAY" to "join the main road",
            "ENTER_HIGHWAY" to "join the main road", "TAKE_EXIT" to "take the exit", "MOTORWAY_EXIT_LEFT" to "take the exit on the left",
            "MOTORWAY_EXIT_RIGHT" to "take the exit on the right", "ENTRANCE_RAMP" to "take the slip road", "SWITCH_PARALLEL_ROAD" to "switch to the parallel road",
            "SWITCH_MAIN_ROAD" to "switch to the main road", "TAKE_FERRY" to "take the ferry",''',
     '''            "ARRIVE" to "chegue ao destino", "ARRIVE_LEFT" to "chegue ao destino, à esquerda", "ARRIVE_RIGHT" to "chegue ao destino, à direita",
            "DEPART" to "siga", "MAKE_UTURN" to "faça o retorno", "TRY_MAKE_UTURN" to "faça o retorno quando possível",
            "STRAIGHT" to "siga em frente", "FOLLOW" to "continue", "TURN_LEFT" to "vire à esquerda", "TURN_RIGHT" to "vire à direita",
            "SHARP_LEFT" to "vire bem à esquerda", "SHARP_RIGHT" to "vire bem à direita", "BEAR_LEFT" to "vire levemente à esquerda", "BEAR_RIGHT" to "vire levemente à direita",
            "KEEP_LEFT" to "mantenha-se à esquerda", "KEEP_RIGHT" to "mantenha-se à direita", "ENTER_MOTORWAY" to "entre na rodovia", "ENTER_FREEWAY" to "entre na via principal",
            "ENTER_HIGHWAY" to "entre na via principal", "TAKE_EXIT" to "pegue a saída", "MOTORWAY_EXIT_LEFT" to "pegue a saída à esquerda",
            "MOTORWAY_EXIT_RIGHT" to "pegue a saída à direita", "ENTRANCE_RAMP" to "pegue o acesso", "SWITCH_PARALLEL_ROAD" to "passe para a via paralela",
            "SWITCH_MAIN_ROAD" to "passe para a via principal", "TAKE_FERRY" to "pegue a balsa",'''),
    # lane advice
    ('''        val lanes = if (cnt == 1) "lane" else "${if (cnt == 2) "two" else if (cnt == 3) "three" else cnt.toString()} lanes"
        return when {
            act.first() == 0 -> "Use the left $lanes"
            act.last() == n - 1 -> "Use the right $lanes"
            cnt == 1 -> "Use lane ${act[0] + 1} from the left"
            else -> "Use the middle $lanes"
        }''',
     '''        val lanes = if (cnt == 1) "a faixa" else "as ${if (cnt == 2) "duas" else if (cnt == 3) "três" else cnt.toString()} faixas"
        return when {
            act.first() == 0 -> "Use $lanes da esquerda"
            act.last() == n - 1 -> "Use $lanes da direita"
            cnt == 1 -> "Use a ${act[0] + 1}ª faixa a partir da esquerda"
            else -> "Use $lanes do meio"
        }'''),
    # turn card caption
    ('''        s.maneuver.uppercase().startsWith("ROUNDABOUT") && s.roundExit > 0 -> "${ordinal(s.roundExit)} exit"
        s.exitNo.isNotBlank() -> "Exit ${s.exitNo}"''',
     '''        s.maneuver.uppercase().startsWith("ROUNDABOUT") && s.roundExit > 0 -> "${ordinal(s.roundExit)} saída"
        s.exitNo.isNotBlank() -> "Saída ${s.exitNo}"'''),
    # TomTom search results in Portuguese
    ('''limit=15&language=en-GB&typeahead=true''', '''limit=15&language=pt-BR&typeahead=true'''),
    ('''&radius=$radius&limit=25&language=en-GB"''', '''&radius=$radius&limit=25&language=pt-BR"'''),
])

# the turn card checks for the "Exit …" caption made above
PATCHES.setdefault('ui/ImmersiveNav.kt', []).extend([
    ('''val ukSign = engine.prefs.b("uksigns", true) && (t.sign.isNotBlank() && t.sign != t.street || t.tag.startsWith("Exit"))''',
     '''val ukSign = engine.prefs.b("uksigns", true) && (t.sign.isNotBlank() && t.sign != t.street || t.tag.startsWith("Saída"))'''),
    ('''if (t.tag.isNotBlank() && !(ukSign && t.tag.startsWith("Exit")))''',
     '''if (t.tag.isNotBlank() && !(ukSign && t.tag.startsWith("Saída")))'''),
    ('''if (engine.prefs.b("uksigns", true) && (t.sign.isNotBlank() && t.sign != t.street || t.tag.startsWith("Exit")))''',
     '''if (engine.prefs.b("uksigns", true) && (t.sign.isNotBlank() && t.sign != t.street || t.tag.startsWith("Saída")))'''),
    ('''UkSign(t.sign, t.tag.removePrefix("Exit ").takeIf { t.tag.startsWith("Exit") }.orEmpty(), t.street''',
     '''UkSign(t.sign, t.tag.removePrefix("Saída ").takeIf { t.tag.startsWith("Saída") }.orEmpty(), t.street'''),
])

PATCHES.setdefault('OfflineRouter.kt', []).extend([
    ('''.put("language", "en-GB")''', '''.put("language", "pt-BR")'''),
])

# Voice commands in Portuguese (the speech recogniser follows the phone's language)
VOICE_OLD_START = '''    fun handleVoice(raw: String): String? {'''
VOICE_OLD_END = '''        Regex("^(?:go to|take me to|navigate to|drive to|directions to|head to|find|search for|show me)\\\\s+(.+)$").find(t)?.let { return "search:" + it.groupValues[1] }
        return null
    }'''
_nav = open(os.path.join(root, 'NavEngine.kt'), encoding='utf-8').read()
_a = _nav.index(VOICE_OLD_START)
_b = _nav.index(VOICE_OLD_END, _a) + len(VOICE_OLD_END)
VOICE_NEW = r'''    fun handleVoice(raw: String): String? {
        val br = Locale("pt", "BR")
        val t0 = raw.lowercase(br).trim().removePrefix("ei horizon").removePrefix("hey horizon").trim(' ', ',', '.')
        // the same text without accents, so "café"/"cafe" and "pedágio"/"pedagio" match alike
        val t = java.text.Normalizer.normalize(t0, java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
        fun has(vararg w: String) = w.any { t.contains(it) }
        val f = SimpleDateFormat("HH:mm", br)
        fun say(s: String) { toast(s, 3500); speak(s, true) }
        val go = "(?:ir|va|vai|vamos|me leve|me leva|leve-me|navegar|navegue|rota|tracar rota|voltar|volta)"
        // places you have saved
        if (Regex("^$go(?: de volta)?(?: para| pra| pro| ao| a)?(?: minha)? ?casa$").matches(t) || t == "casa") {
            val h = places.slot("home"); if (h != null) { say("Indo para casa"); startRoute(h) } else say("A casa ainda não foi definida"); return ""
        }
        if (Regex("^$go(?: para| pra| pro| ao| a)?(?: o| meu)? ?(?:trabalho|servico)$").matches(t) || t == "trabalho") {
            val w = places.slot("work"); if (w != null) { say("Indo para o trabalho"); startRoute(w) } else say("O trabalho ainda não foi definido"); return ""
        }
        // reports
        if (t.startsWith("relatar") || t.startsWith("reportar") || t.startsWith("avisar") || t.startsWith("alerta")) {
            val k = when { has("policia", "blitz") -> "police"; has("acidente", "batida") -> "accident"; has("quebrado", "parado", "pane") -> "breakdown"; has("radar", "camera") -> "mobilecam"; else -> "hazard" }
            report(k); return ""
        }
        // finding things
        val kind = when {
            has("combustivel", "gasolina", "etanol", "alcool", "diesel", "posto", "abastecer") -> "fuel"
            has("carregador", "recarga", "recarregar", "eletroposto") -> "charging"
            has("banheiro", "sanitario") -> "toilets"
            has("cafe", "cafeteria", "padaria") -> "coffee"
            has("comida", "comer", "restaurante", "lanche", "almocar", "jantar", "fome") -> "food"
            has("ponto de parada", "area de descanso", "servicos") -> "services"
            has("estacionamento", "estacionar", "vaga") -> "parking"
            else -> null
        }
        if (kind != null && has("procur", "busca", "busque", "encontr", "achar", "ache", "proximo", "perto", "onde", "algum", "preciso", "mostr", "barato", "parada", "quero")) {
            nearbySort = if (has("barato")) "cheap" else "near"
            if (has("parada") && route != null) stopMode = true
            loadNearby(kind, has("destino", "quando chegar"))
            say("Procurando " + Overpass.kindLabel(kind).lowercase(br) + if (route != null) " ao longo da rota" else " por perto")
            return ""
        }
        // route preferences
        if (has("evitar pedagio", "sem pedagio")) { prefs.put("avoid_toll", true); say("Evitando pedágios"); dest?.let { startRoute(it, departMin, true, startNow = route != null && !previewing) }; return "" }
        if (has("evitar rodovia", "sem rodovia", "evitar estrada")) { prefs.put("avoid_motorway", true); say("Evitando rodovias"); dest?.let { startRoute(it, departMin, true, startNow = route != null && !previewing) }; return "" }
        if (has("evitar esta", "evitar essa", "desviar")) { if (route != null) { say("Procurando um desvio"); avoidRoadAhead() } else say("Não há rota para mudar"); return "" }
        // trip info
        if (has("que horas eu chego", "que horas chego", "previsao", "chegada", "quanto tempo", "quanto falta", "que distancia", "tempo restante", "quando eu chego", "quando vou chegar")) {
            val e = eta
            if (e == null) say("Não há rota no momento")
            else say("Você chega às ${e.arrive}, em ${Geo.fmtDur(e.secs)}. Faltam ${Geo.spoken(e.dist, imperial)}." + if (e.delayMin >= 1) " ${e.delayMin} minutos de trânsito." else "")
            return ""
        }
        if (has("compartilh")) return "share"
        // sound
        if (has("ativar som", "ligar som", "ligar o som", "ligar a voz", "ativar voz", "ativar a voz", "voz ligada", "som ligado")) { prefs.put("soundmode", 0); say("Orientação por voz ligada"); return "" }
        if (has("so alertas", "apenas alertas", "somente alertas")) { prefs.put("soundmode", 1); say("Só alertas"); return "" }
        if (has("mudo", "silencio", "silenciar", "calar", "cala a boca", "desligar voz", "desligar a voz", "voz desligada", "sem voz")) { say("Voz desligada"); prefs.put("soundmode", 2); return "" }
        // the map
        if (has("parar navegacao", "encerrar rota", "encerrar navegacao", "cancelar rota", "cancelar navegacao", "parar rota", "parar a rota", "terminar rota")) { endRoute(); say("Rota encerrada"); return "" }
        if (has("visao geral", "mostrar rota", "mostrar a rota", "rota inteira", "rota toda")) { overview(); return "" }
        if (has("aproximar", "mais zoom")) { zoom(1.0); return "" }
        if (has("afastar", "menos zoom")) { zoom(-1.0); return "" }
        if (has("norte para cima", "norte acima")) { if (!northUp) toggleNorthUp(); return "" }
        if (t == "3d" || t == "2d" || has("tres d", "dois d")) { if ((t.contains("3") || t.contains("tres")) != tilt3d) toggleTilt(); return "" }
        if (has("que horas sao", "que hora e")) { say("São ${f.format(Date())}"); return "" }
        // "me leve para …", "ir para …": the words after it are searched for, keeping their accents
        Regex("^((?:ir para|ir pra|ir ate|va para|va pra|va ate|vai para|vamos para|vamos pra|me leve para|me leve pra|me leve ate|me leva para|me leva pra|leve-me para|navegar para|navegar ate|navegue para|navegue ate|rota para|rota ate|tracar rota para|como chego em|como chego no|como chego na|como chego ao|como chego a|procurar|procure|buscar|busque|encontrar|encontre|mostrar|mostre)(?:\\s+(?:a|o|as|os))?)\\s+(.+)$").find(t)?.let {
            val skip = it.groupValues[1].split(Regex("\\s+")).size
            val rest = t0.split(Regex("\\s+")).drop(skip).joinToString(" ").ifBlank { it.groupValues[2] }
            return "search:" + rest
        }
        return null
    }'''
PATCHES.setdefault('NavEngine.kt', []).append((_nav[_a:_b], VOICE_NEW))

PATCHES['NavEngine.kt'].extend([
    # the early lane call on fast roads ("Em cerca de 1,5 km, use as duas faixas da esquerda")
    ('''speak(dist + ", " + laneAdv.replaceFirst("Use the", "move into the").replaceFirst("Use lane", "move into lane"))''',
     '''speak(dist + ", " + laneAdv.replaceFirstChar { it.lowercase() })'''),
    # the wrong-way and familiar-road lookups lowercase with the UK locale; harmless, but the date format for planned trips is shown
    ('''            val f = SimpleDateFormat("HH:mm", Locale.UK)
            toast(if (leave''', '''            val f = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
            toast(if (leave'''),
])

PATCHES.setdefault('Overpass.kt', []).extend([
    ('''        "fuel" -> "Fuel station"; "charging" -> "Charging point"; "food" -> "Food"; "coffee" -> "Coffee"
        "toilets" -> "Toilets"; "services" -> "Services"; else -> "Car park"''',
     '''        "fuel" -> "Posto de combustível"; "charging" -> "Ponto de recarga"; "food" -> "Restaurante"; "coffee" -> "Café"
        "toilets" -> "Banheiro"; "services" -> "Ponto de parada"; else -> "Estacionamento"'''),
])

# dates shown on screen in Portuguese (weekday and month names)
for _rel in ['ui/RouteUi.kt', 'ui/SettingsScreen.kt', 'ui/OfflineMapsPage.kt', 'ui/ImmersiveNav.kt']:
    _t = open(os.path.join(root, _rel), encoding='utf-8').read()
    for _fmt in ['"EEE HH:mm"', '"HH:mm"', '"EEE d MMM, HH:mm"', '"d MMM yyyy"']:
        _old = 'java.text.SimpleDateFormat(' + _fmt + ', java.util.Locale.UK)'
        if _t.count(_old) == 1:
            PATCHES.setdefault(_rel, []).append((_old, 'java.text.SimpleDateFormat(' + _fmt + ', java.util.Locale("pt", "BR"))'))
        elif _t.count(_old) > 1:
            PATCHES.setdefault(_rel, []).append(('@ALL@' + _old, 'java.text.SimpleDateFormat(' + _fmt + ', java.util.Locale("pt", "BR"))'))
# country names in Portuguese on the search setting
PATCHES['ui/SettingsScreen.kt'].append(('''java.util.Locale("", cc).getDisplayCountry(java.util.Locale.UK)''',
                                         '''java.util.Locale("", cc).getDisplayCountry(java.util.Locale("pt", "BR"))'''))

# Settings rows on a narrow (portrait phone) screen: when the controls on the right leave too little room for the title,
# they go under the title instead of squeezing it into a one-letter-wide column.
PATCHES.setdefault('ui/SettingsKit.kt', []).append((
'''    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).then(hlMod).then(if (onClick != null) Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick) else Modifier).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 14.dp)) {
            Text(title, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (!sub.isNullOrBlank()) Text(sub, color = c.sub, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { trailing() }
    }''',
'''    androidx.compose.ui.layout.Layout(
        content = {
            Column(Modifier.padding(end = 14.dp)) {
                Text(title, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (!sub.isNullOrBlank()) Text(sub, color = c.sub, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { trailing() }
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).then(hlMod).then(if (onClick != null) Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick) else Modifier).padding(vertical = 8.dp),
    ) { measurables, constraints ->
        val w = constraints.maxWidth
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val tr = measurables[1].measure(loose)
        // side by side while the title keeps at least ~180 dp, otherwise the controls go underneath
        if (tr.width == 0 || w - tr.width >= 180.dp.roundToPx()) {
            val tx = measurables[0].measure(loose.copy(maxWidth = (w - tr.width).coerceAtLeast(0)))
            val h = maxOf(tx.height, tr.height, constraints.minHeight)
            layout(w, h) {
                tx.placeRelative(0, (h - tx.height) / 2)
                tr.placeRelative(w - tr.width, (h - tr.height) / 2)
            }
        } else {
            val tx = measurables[0].measure(loose)
            val gap = 8.dp.roundToPx()
            val h = maxOf(tx.height + gap + tr.height, constraints.minHeight)
            layout(w, h) {
                tx.placeRelative(0, 0)
                tr.placeRelative(0, tx.height + gap)
            }
        }
    }'''))

# ---- Brazilian fuels and trip cost in reais (km/l, R$/litro) instead of UK miles per gallon, pence and pounds ----
_ss = open(os.path.join(root, 'ui/SettingsScreen.kt'), encoding='utf-8').read()
_fa = _ss.index('private fun FuelSection(prefs: Prefs) {')
_fb = _ss.index('    SGroup("Compartilhar sua chegada"', _fa)
FUEL_NEW = r'''private fun FuelSection(prefs: Prefs) {
    val fuel = prefs.s("fueltype", "GAS")
    fun num(s: String, n: Int) = s.replace(',', '.').filter { it.isDigit() || it == '.' }.take(n)
    SGroup("Combustível") {
        SChoice("Seu combustível", listOf("GAS" to "Gasolina", "GASAD" to "Aditivada", "ETANOL" to "Etanol", "B7" to "Diesel", "GNV" to "GNV", "EV" to "Elétrico"), fuel) { prefs.put("fueltype", it) }
        SToggle(prefs, "Sugerir parada para abastecer em viagens longas", "smartfuel", true,
            if (fuel == "EV") "Acima de uns 150 km, um ponto de recarga no caminho com o menor desvio" else "Acima de uns 150 km, um posto no caminho com o menor desvio")
    }
    if (fuel == "EV") SGroup("Custo da viagem nas opções de rota", footer = "Com os dois preenchidos, cada rota mostra mais ou menos quanto vai custar a recarga, em reais.") {
        SFieldItem("Consumo (km por kWh)", prefs.s("evkmkwh", ""), placeholder = "ex.: 6,5", number = true) { s -> prefs.put("evkmkwh", num(s, 5)) }
        SFieldItem("Preço da energia (R$ por kWh)", prefs.s("evrkwh", ""), placeholder = "ex.: 0,95", number = true) { s -> prefs.put("evrkwh", num(s, 6)) }
    } else {
        SGroup("Custo da viagem nas opções de rota", footer = "Com o consumo e o preço preenchidos, cada rota mostra mais ou menos quanto vai gastar de combustível, em reais. Se o preço ficar em branco, vale o preço da gasolina ou do etanol informado abaixo.") {
            SFieldItem("Consumo do carro (km/l)", prefs.s("brkml", ""), placeholder = "ex.: 12,5", number = true) { s -> prefs.put("brkml", num(s, 5)) }
            SFieldItem("Preço do combustível (R$/litro)", prefs.s("brppl", ""), placeholder = "ex.: 6,29", number = true) { s -> prefs.put("brppl", num(s, 6)) }
        }
        SGroup("Etanol ou gasolina?", footer = "Regra prática: o etanol compensa quando custa até 70% do preço da gasolina, porque rende cerca de 30% menos. Com o consumo do seu carro em cada um, a conta usa os números reais.") {
            SFieldItem("Preço da gasolina (R$/litro)", prefs.s("brgas", ""), placeholder = "ex.: 6,29", number = true) { s -> prefs.put("brgas", num(s, 6)) }
            SFieldItem("Preço do etanol (R$/litro)", prefs.s("bretanol", ""), placeholder = "ex.: 4,19", number = true) { s -> prefs.put("bretanol", num(s, 6)) }
            SFieldItem("Consumo com gasolina (km/l, opcional)", prefs.s("brkmlg", ""), placeholder = "ex.: 12,0", number = true) { s -> prefs.put("brkmlg", num(s, 5)) }
            SFieldItem("Consumo com etanol (km/l, opcional)", prefs.s("brkmle", ""), placeholder = "ex.: 8,5", number = true) { s -> prefs.put("brkmle", num(s, 5)) }
            etanolAdvice(prefs)?.let { SNote(it) }
        }
    }
'''
PATCHES['ui/SettingsScreen.kt'].append((_ss[_fa:_fb], FUEL_NEW))

PATCHES.setdefault('ui/Shared.kt', []).append((
'''fun isImperial(prefs: Prefs): Boolean =''',
'''/** The pump price to use for the trip cost: the one typed for it, or else the petrol or ethanol price from "Etanol ou gasolina?". */
internal fun brFuelPrice(prefs: Prefs): Double? {
    fun d(k: String) = prefs.s(k, "").replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
    val fuel = prefs.s("fueltype", "GAS")
    if (fuel == "EV") return d("evrkwh")
    return d("brppl") ?: when (fuel) { "ETANOL" -> d("bretanol"); "GAS", "GASAD" -> d("brgas"); else -> null }
}

/** Which pays off, ethanol or petrol: by cost per km when both consumptions are known, otherwise by the 70% rule. */
internal fun etanolAdvice(prefs: Prefs): String? {
    fun d(k: String) = prefs.s(k, "").replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
    val g = d("brgas") ?: return null
    val e = d("bretanol") ?: return null
    val kg = d("brkmlg"); val ke = d("brkmle")
    val br = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
    if (kg != null && ke != null) {
        val cg = g / kg * 100.0; val ce = e / ke * 100.0
        return (if (ce < cg) "Etanol compensa" else "Gasolina compensa") + ": a cada 100 km, gasolina sai " + br.format(cg) + " e etanol " + br.format(ce) + "."
    }
    val pct = Math.round(e / g * 100.0)
    return if (e / g <= 0.70) "Etanol compensa: custa $pct% do preço da gasolina (até 70% compensa)."
    else "Gasolina compensa: o etanol custa $pct% do preço dela (acima de 70%)."
}

fun isImperial(prefs: Prefs): Boolean ='''))

PATCHES.setdefault('ui/NavPage.kt', []).append((
'''    val ev = engine.prefs.s("fueltype", "E10") == "EV"
    val mpg = engine.prefs.s(if (ev) "evmpk" else "fuelmpg", "").toDoubleOrNull()?.takeIf { it > 0 }
    val ppl = engine.prefs.s(if (ev) "evppk" else "fuelppl", "").toDoubleOrNull()?.takeIf { it > 0 }
    val costText = if (mpg != null && ppl != null && selRoute != null)
        (if (ev) "Recarga aprox. " else "Combustível aprox. ") + java.text.NumberFormat.getCurrencyInstance(java.util.Locale.UK).format(
            if (ev) selRoute.len / 1609.344 / mpg * ppl / 100.0 else selRoute.len / 1609.344 / mpg * 4.54609 * ppl / 100.0) else null''',
'''    val ev = engine.prefs.s("fueltype", "GAS") == "EV"
    val kmPer = engine.prefs.s(if (ev) "evkmkwh" else "brkml", "").replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
    val price = brFuelPrice(engine.prefs)
    val costText = if (kmPer != null && price != null && selRoute != null)
        (if (ev) "Recarga aprox. " else "Combustível aprox. ") + java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR")).format(
            selRoute.len / 1000.0 / kmPer * price) else null'''))

PATCHES.setdefault('FuelFinder.kt', []).append((
'''    fun fuelName(fuel: String) = when (fuel) { "E5" -> "Gasolina aditivada"; "B7" -> "Diesel"; "EV" -> "Elétrico"; else -> "Gasolina comum" }''',
'''    fun fuelName(fuel: String) = when (fuel) { "GASAD", "E5" -> "Gasolina aditivada"; "ETANOL" -> "Etanol"; "B7" -> "Diesel"; "GNV" -> "GNV"; "EV" -> "Elétrico"; else -> "Gasolina" }'''))

# ---- UK-only features out of the way: clean air zones (ULEZ), British-style signs ----
PATCHES['ui/SettingsScreen.kt'].extend([
    ('''        SToggle(prefs, "Evitar zonas de ar limpo (Reino Unido)", "avoidzones", false, "Uma zona onde você começa ou termina não pode ser evitada")\n''', ''),
    ('''        SToggle(prefs, "Placas no estilo britânico no cartão de manobra", "uksigns", true)\n''', ''),
    ('''        SToggle(prefs, "Zonas de ar limpo (Reino Unido)", "zonewarn", true)\n''', ''),
])
PATCHES['ui/ImmersiveNav.kt'].append(('@ALL@engine.prefs.b("uksigns", true)', 'engine.prefs.b("uksigns", false)'))
PATCHES.setdefault('NavEngine.kt', []).append(('@ALL@prefs.b("zonewarn", true)', 'prefs.b("zonewarn", false)'))

PATCHES['ui/SettingsScreen.kt'].append(('''        SToggle(prefs, "Rotas econômicas", "route_eco", false, "Que gastam menos combustível em vez das mais rápidas")
        ZonesItem()
''', '''        SToggle(prefs, "Rotas econômicas", "route_eco", false, "Que gastam menos combustível em vez das mais rápidas")
'''))
# settings search: drop the removed UK entries, point the fuel ones at the new groups
PATCHES.setdefault('ui/SettingsIndex.kt', []).extend([
    ('''    SettingRef("Evitar zonas de ar limpo (Reino Unido)", "Uma zona onde você começa ou termina não pode ser evitada", "Rotas", 1),\n''', ''),
    ('''    SettingRef("Placas no estilo britânico no cartão de manobra", "", "Na tela", 1),\n''', ''),
    ('''    SettingRef("Zonas de ar limpo (Reino Unido)", "", "Na via", 2),\n''', ''),
    ('''    SettingRef("Seu combustível", "", "Preços de combustível", 4),
    SettingRef("Sugerir parada para abastecer em viagens longas", "Acima de uns 150 km, o posto com melhor custo-benefício no caminho", "Preços de combustível", 4),
    SettingRef("Client ID", "", "Preços de combustível", 4),
    SettingRef("Client secret", "", "Preços de combustível", 4),
    SettingRef("Preços", "", "Preços de combustível", 4),
    SettingRef("Viu um preço errado?", "", "Preços de combustível", 4),
    SettingRef("Consumo (milhas por galão)", "", "Custo de combustível nas opções de rota", 4),
    SettingRef("Preço do combustível (pence por litro)", "", "Custo de combustível nas opções de rota", 4),
''', '''    SettingRef("Seu combustível", "Gasolina, aditivada, etanol, diesel, GNV ou elétrico", "Combustível", 4),
    SettingRef("Sugerir parada para abastecer em viagens longas", "Acima de uns 150 km, um posto no caminho com o menor desvio", "Combustível", 4),
    SettingRef("Consumo do carro (km/l)", "Para o custo da viagem em reais", "Custo da viagem nas opções de rota", 4),
    SettingRef("Preço do combustível (R$/litro)", "Para o custo da viagem em reais", "Custo da viagem nas opções de rota", 4),
    SettingRef("Preço da gasolina (R$/litro)", "Etanol ou gasolina: qual compensa", "Etanol ou gasolina?", 4),
    SettingRef("Preço do etanol (R$/litro)", "Regra dos 70%", "Etanol ou gasolina?", 4),
'''),
])

# ---- offline packs copied to our own repo (pacotes-espelho.txt) are downloaded from there ----
PATCHES.setdefault('PackRegions.kt', []).extend([
    ('''    val url: String get() = if (id == "ew" || id == "gb") "$RELEASES/pack-$id" else "$COUNTRY_RELEASES/pack-$id"''',
     '''    val url: String get() = if (mirrored) "$OWN_RELEASES/pack-$id" else if (id == "ew" || id == "gb") "$RELEASES/pack-$id" else "$COUNTRY_RELEASES/pack-$id"
    /** Packs listed in pacotes-espelho.txt are copied to our own repo's releases and downloaded from there. */
    val mirrored: Boolean get() = id in MIRRORED_PACKS && BuildConfig.UPDATE_REPO.isNotBlank()'''),
    ('''    val extrasUrl: String get() = "$COUNTRY_RELEASES/pack-$id"''',
     '''    val extrasUrl: String get() = if (mirrored) "$OWN_RELEASES/pack-$id" else "$COUNTRY_RELEASES/pack-$id"'''),
    ('''const val COUNTRY_RELEASES = "https://github.com/benbishop028-arch/Horizon-Maps/releases/download"''',
     '''const val COUNTRY_RELEASES = "https://github.com/benbishop028-arch/Horizon-Maps/releases/download"
val OWN_RELEASES: String get() = "https://github.com/${BuildConfig.UPDATE_REPO}/releases/download"
val MIRRORED_PACKS: Set<String> = BuildConfig.MIRRORED_PACKS.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()'''),
])

# ---- security / privacy hardening ----
# Android Auto: only the real car hosts (Android Auto, Android Automotive) may connect, not any app on the phone
PATCHES.setdefault('car/CarNav.kt', []).append((
'''    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR''',
'''    // only Google's car hosts (Android Auto and Android Automotive) may connect, so no other app on the phone can drive it or read its screen
    override fun createHostValidator(): HostValidator =
        HostValidator.Builder(applicationContext).addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample).build()'''))
# Mapbox usage statistics ("de-identified location and usage data") opted out by default
PATCHES.setdefault('MainActivity.kt', []).append((
'''        if (tok.isNotEmpty()) try { com.mapbox.common.MapboxOptions.accessToken = tok } catch (e: Throwable) { }''',
'''        if (tok.isNotEmpty()) try { com.mapbox.common.MapboxOptions.accessToken = tok } catch (e: Throwable) { }
        // no usage statistics to Mapbox (the map's own telemetry), unless turned back on with the (i) button on the map
        if (!prefs.b("mbtelemetryset", false)) try {
            com.mapbox.maps.module.telemetry.MapTelemetryImpl(applicationContext).setUserTelemetryRequestState(false)
            prefs.put("mbtelemetryset", true)
        } catch (e: Throwable) { }'''))
# location-based queries only to the community OpenStreetMap servers in Europe (not the mail.ru mirror)
PATCHES.setdefault('Overpass.kt', []).append((
'''        "https://maps.mail.ru/osm/tools/overpass/api/interpreter",\n''', ''))

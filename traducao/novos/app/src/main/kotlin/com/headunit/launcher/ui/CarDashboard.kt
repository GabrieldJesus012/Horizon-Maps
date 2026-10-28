package com.headunit.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headunit.launcher.LL
import com.headunit.launcher.NavEngine
import com.headunit.launcher.NowPlaying
import com.headunit.launcher.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Text sized from the card, the same whatever the phone's own font size setting is (so it always fits its card). */
@Composable
private fun Float.tsp(): androidx.compose.ui.unit.TextUnit = (this / androidx.compose.ui.platform.LocalDensity.current.fontScale).sp

// the dashboard's own look: deep navy with glowing blue edges (always, day or night)
private val DBg = Brush.verticalGradient(listOf(Color(0xFF0B1636), Color(0xFF07102A)))
private val DCard = Brush.verticalGradient(listOf(Color(0xFF15275A), Color(0xFF0D1A3E)))
private val DEdge = Color(0xFF2F5BD8)
private val DText = Color(0xFFF2F6FF)
private val DSub = Color(0xFF9DB2E6)
private val DBlue = Color(0xFF3D8BFF)
private val DTrack = Color(0xFF223463)
private val DFont = FontFamily.SansSerif

/**
 * The car screen's home dashboard (shown when the app opens on Android Auto and whenever no route is running): the time and
 * date, your speed, the weather where you are, the distance driven today, the car's fuel and range, the music playing with its
 * controls, a window onto the live map, and the main actions. The car in the middle, or "Mapa", opens the full map.
 */
@Composable
internal fun CarDashboard(
    engine: NavEngine, actions: CarActions,
    left: Dp, top: Dp, right: Dp, bottom: Dp,
    vL: Float, vT: Float, vW: Float, vH: Float,
) {
    val ctx = LocalContext.current.applicationContext
    var win by remember { mutableStateOf<Rect?>(null) }
    val corner = 20.dp

    // a smaller arrow in the small map window, back to its size when the dashboard closes
    DisposableEffect(Unit) {
        val full = engine.prefs.i("cararrowpct", 160).coerceIn(60, 300) / 100f
        try { engine.map.setArrowScale(full * 0.45f) } catch (e: Exception) { }
        onDispose { try { engine.map.setArrowScale(full) } catch (e: Exception) { } }
    }
    // the weather where you are (refreshed every 20 minutes)
    var wx by remember { mutableStateOf<Weather.Now?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            val p: LL? = engine.fix?.pos
            if (p != null) {
                val w = withContext(Dispatchers.IO) { try { Weather.fetch(p) } catch (e: Exception) { null } }
                if (w != null) { wx = w; delay(20 * 60_000L) } else delay(60_000L)
            } else delay(5_000L)
        }
    }
    // the clock, and the song playing
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            try { NowPlaying.refresh(ctx) } catch (e: Exception) { }
            delay(1000L)
        }
    }

    // everything but the map window is covered, so the live map shows only through it
    Box(
        Modifier.fillMaxSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawBehind {
            drawRect(DBg)
            win?.let { r -> drawRoundRect(Color.Transparent, r.topLeft, r.size, CornerRadius(corner.toPx()), blendMode = BlendMode.Clear) }
        },
    ) {
        Row(
            Modifier.fillMaxSize().padding(start = left, top = top, end = right, bottom = bottom),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ---- left: clock, gauges with the car in the middle, buttons ----
            Column(Modifier.weight(0.63f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClockCard(now, Modifier.fillMaxWidth().weight(0.20f))
                Row(Modifier.fillMaxWidth().weight(0.62f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpeedGauge(engine, Modifier.weight(1f).fillMaxWidth())
                        WeatherGauge(wx, Modifier.weight(1f).fillMaxWidth())
                    }
                    CarTile(Modifier.weight(0.78f).fillMaxHeight().carTap("dash-car") { engine.carDash = false })
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TodayGauge(engine, Modifier.weight(1f).fillMaxWidth())
                        FuelGauge(engine, Modifier.weight(1f).fillMaxWidth())
                    }
                }
                Row(Modifier.fillMaxWidth().weight(0.18f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashButton(Icons.Rounded.Search, "Buscar", "dash-search", Modifier.weight(1f)) { actions.search() }
                    DashButton(Icons.Rounded.Star, "Locais", "dash-places", Modifier.weight(1f)) { actions.places() }
                    DashButton(Icons.Rounded.Map, "Mapa", "dash-map", Modifier.weight(1f)) { engine.carDash = false }
                    DashButton(Icons.Rounded.LocalGasStation, "Postos", "dash-fuel", Modifier.weight(1f)) { actions.nearby("fuel") }
                    DashButton(Icons.Rounded.Settings, "Ajustes", "dash-settings", Modifier.weight(1f)) { actions.settings() }
                }
            }
            // ---- right: the music (top), the live map window (bottom) ----
            Column(Modifier.weight(0.37f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MusicCard(ctx, Modifier.fillMaxWidth().weight(0.46f))
                Box(
                    Modifier.fillMaxWidth().weight(0.54f).border(2.dp, DEdge, RoundedCornerShape(corner))
                        .onGloballyPositioned { lc ->
                            val r = lc.boundsInRoot()
                            win = r
                            // the camera keeps your arrow in the middle of this window
                            engine.dashLeft = (2.0 * ((r.center.x - vL) / vW) - 1.0).coerceIn(0.0, 0.9)
                            engine.dashPad = (2.0 * ((r.center.y - vT) / vH) - 1.0).coerceIn(0.0, 0.9)
                        }
                        .carTap("dash-window") { engine.carDash = false },
                )
            }
        }
    }
}

@Composable
private fun DashCard(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(modifier.clip(shape).background(DCard).border(1.5.dp, DEdge.copy(alpha = 0.85f), shape)) { content() }
}

@Composable
private fun ClockCard(now: Long, modifier: Modifier) {
    val br = java.util.Locale("pt", "BR")
    val time = java.text.SimpleDateFormat("HH:mm", br).format(java.util.Date(now))
    val day = java.text.SimpleDateFormat("EEEE", br).format(java.util.Date(now)).uppercase(br)
    val date = java.text.SimpleDateFormat("d 'de' MMMM 'de' yyyy", br).format(java.util.Date(now))
    DashCard(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val mh = maxHeight.value
            val big = (mh * 0.58f).coerceIn(18f, 44f).tsp()
            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(time, color = DText, fontSize = big, fontWeight = FontWeight.Bold, fontFamily = DFont, maxLines = 1)
                Box(Modifier.padding(horizontal = 14.dp).width(2.dp).fillMaxHeight(0.6f).background(DBlue))
                Column(Modifier.weight(1f)) {
                    Text(day, color = DBlue, fontSize = (mh * 0.2f).coerceIn(9f, 14f).tsp(), fontWeight = FontWeight.Bold, fontFamily = DFont, letterSpacing = 1.sp, maxLines = 1)
                    Text(date, color = DText, fontSize = (mh * 0.25f).coerceIn(10f, 17f).tsp(), fontFamily = DFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * A round gauge sized to its tile: the label on top, a 240° track with the value's arc in [colors] (a gradient), the figure in
 * the middle of the ring and a note under it. All sizes follow the tile, so it reads the same on any car screen.
 */
@Composable
private fun Gauge(label: String, value: String, unit: String, frac: Float, colors: List<Color>, foot: String?, modifier: Modifier) {
    DashCard(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val h = maxHeight.value; val w = maxWidth.value
            val labelSp = (h * 0.11f).coerceIn(7f, 12f)
            val footSp = (h * 0.10f).coerceIn(7f, 11f)
            // the ring: as big as fits under the label and over the note
            val ringTop = h * 0.17f
            val ringBottom = h * (if (foot.isNullOrEmpty()) 0.96f else 0.86f)
            val d = minOf(w * 0.86f, (ringBottom - ringTop) * 1.12f)
            val cx = w / 2f; val cy = ringTop + d / 2f
            Canvas(Modifier.fillMaxSize()) {
                val px = density
                val r = d * px / 2f
                val stroke = (d * px * 0.085f).coerceAtLeast(3f)
                val tl = Offset(cx * px - r + stroke / 2f, cy * px - r + stroke / 2f)
                val sz = Size(2f * r - stroke, 2f * r - stroke)
                drawArc(DTrack, 150f, 240f, false, tl, sz, style = Stroke(stroke, cap = StrokeCap.Round))
                val f = frac.coerceIn(0f, 1f)
                if (f > 0.005f) drawArc(Brush.horizontalGradient(colors, startX = tl.x, endX = tl.x + sz.width), 150f, 240f * f, false, tl, sz, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Text(label, color = DSub, fontSize = labelSp.tsp(), fontWeight = FontWeight.Bold, fontFamily = DFont, letterSpacing = 1.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = (h * 0.04f).dp, start = 6.dp, end = 6.dp))
            Column(Modifier.align(Alignment.TopCenter).padding(top = (cy - d * 0.2f).coerceAtLeast(0f).dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, color = DText, fontSize = (d * 0.26f).coerceIn(10f, 34f).tsp(), fontWeight = FontWeight.Bold, fontFamily = DFont, maxLines = 1)
                if (unit.isNotEmpty()) Text(unit, color = DSub, fontSize = (d * 0.1f).coerceIn(7f, 12f).tsp(), fontFamily = DFont, maxLines = 1)
            }
            if (!foot.isNullOrEmpty()) Text(foot, color = DSub, fontSize = footSp.tsp(), fontFamily = DFont, maxLines = 1, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = (h * 0.04f).dp, start = 6.dp, end = 6.dp))
        }
    }
}

private val GBlue = listOf(Color(0xFF1E6BFF), Color(0xFF5CC8FF))
private val GOrange = listOf(Color(0xFFFFC14D), Color(0xFFFF7A1A))
private val GGreen = listOf(Color(0xFF22C55E), Color(0xFFB6F35A))
private val GRed = listOf(Color(0xFFFF8A4D), Color(0xFFFF3B3B))

@Composable
private fun SpeedGauge(engine: NavEngine, modifier: Modifier) {
    val kmh = ((engine.fix?.speed ?: 0.0) * 3.6).coerceAtLeast(0.0)
    val lim = engine.speedLimit?.let { Math.round(it * 3.6).toInt() }
    val over = lim != null && kmh > lim + 3
    Gauge("VELOCIDADE", "${Math.round(kmh)}", "km/h", (kmh / 160.0).toFloat(), if (over) GRed else GBlue,
        if (lim != null) "limite $lim km/h" else null, modifier)
}

private fun wxText(code: Int): String = when (code) {
    0 -> "Céu limpo"; 1, 2 -> "Poucas nuvens"; 3 -> "Nublado"; 45, 48 -> "Neblina"
    in 51..57 -> "Garoa"; in 61..67 -> "Chuva"; in 71..77 -> "Neve"; in 80..82 -> "Pancadas de chuva"
    in 95..99 -> "Tempestade"; else -> ""
}

@Composable
private fun WeatherGauge(wx: Weather.Now?, modifier: Modifier) {
    val t = wx?.tempC
    Gauge("CLIMA AGORA", if (t != null) "${Math.round(t)}°" else "--", "°C", if (t != null) ((t - 10.0) / 32.0).toFloat() else 0f,
        GOrange, wx?.let { wxText(it.code) } ?: "buscando…", modifier)
}

@Composable
private fun TodayGauge(engine: NavEngine, modifier: Modifier) {
    @Suppress("UNUSED_VARIABLE") val tick = engine.tripDist   // re-read as you drive
    val (m, secs) = engine.kmToday()
    val km = m / 1000.0
    val mins = (secs / 60L).toInt()
    Gauge("KM HOJE", if (km < 10) String.format(java.util.Locale("pt", "BR"), "%.1f", km) else "${Math.round(km)}", "km",
        (km / 100.0).toFloat(), GGreen, if (mins > 0) (if (mins >= 60) "${mins / 60} h ${mins % 60} min dirigindo" else "$mins min dirigindo") else "nenhuma viagem ainda", modifier)
}

@Composable
private fun FuelGauge(engine: NavEngine, modifier: Modifier) {
    val fuel = engine.carFuelPct
    val range = engine.carRangeM
    if (fuel != null || range != null) {
        val f = (fuel ?: 0f) / 100f
        Gauge("COMBUSTÍVEL", if (fuel != null) "${Math.round(fuel)}%" else "--", if (fuel != null) "do tanque" else "",
            f, if (f < 0.15f) GRed else if (f < 0.3f) GOrange else GGreen, range?.let { "autonomia ${Math.round(it / 1000f)} km" }, modifier)
    } else {
        // the car doesn't report its fuel to Android Auto: the road's limit and the next speed camera instead
        val lim = engine.speedLimit?.let { Math.round(it * 3.6).toInt() }
        val cam = engine.cameraDist
        Gauge("LIMITE DA VIA", lim?.toString() ?: "--", "km/h", ((lim ?: 0) / 120.0).toFloat(), GRed,
            if (cam != null) "radar a ${com.headunit.launcher.Geo.fmtDist(cam.toDouble(), false)}" else "sem radar perto", modifier)
    }
}

/** The car seen from above on its lane (tap: the full map). */
@Composable
private fun CarTile(modifier: Modifier) {
    DashCard(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0E1A3E), Color(0xFF1C2E66), Color(0xFF0E1A3E))))
            drawLine(Color(0xFF6F8BD6), Offset(w * 0.12f, 0f), Offset(w * 0.12f, h), strokeWidth = w * 0.02f)
            drawLine(Color(0xFF6F8BD6), Offset(w * 0.88f, 0f), Offset(w * 0.88f, h), strokeWidth = w * 0.02f)
            val dash = PathEffect.dashPathEffect(floatArrayOf(h * 0.07f, h * 0.06f))
            drawLine(Color(0xFFDDE6FF), Offset(w * 0.37f, 0f), Offset(w * 0.37f, h), strokeWidth = w * 0.018f, pathEffect = dash)
            drawLine(Color(0xFFDDE6FF), Offset(w * 0.63f, 0f), Offset(w * 0.63f, h), strokeWidth = w * 0.018f, pathEffect = dash)
            val cw = minOf(w * 0.22f, h * 0.18f); val ch = cw * 2.05f
            val x = w * 0.5f - cw / 2f; val y = h * 0.6f - ch / 2f
            drawRoundRect(Color.Black.copy(alpha = 0.35f), Offset(x + cw * 0.06f, y + ch * 0.04f), Size(cw, ch), CornerRadius(cw * 0.35f))
            drawRoundRect(Color(0xFFE9ECF2), Offset(x, y), Size(cw, ch), CornerRadius(cw * 0.35f))
            drawRoundRect(Color(0xFF26304A), Offset(x + cw * 0.12f, y + ch * 0.2f), Size(cw * 0.76f, ch * 0.2f), CornerRadius(cw * 0.12f))
            drawRoundRect(Color(0xFF26304A), Offset(x + cw * 0.14f, y + ch * 0.68f), Size(cw * 0.72f, ch * 0.14f), CornerRadius(cw * 0.1f))
            drawRoundRect(Color(0xFFC9CED8), Offset(x + cw * 0.16f, y + ch * 0.42f), Size(cw * 0.68f, ch * 0.24f), CornerRadius(cw * 0.08f))
            drawCircle(Color(0xFFFFF3B0), cw * 0.08f, Offset(x + cw * 0.22f, y + ch * 0.05f))
            drawCircle(Color(0xFFFFF3B0), cw * 0.08f, Offset(x + cw * 0.78f, y + ch * 0.05f))
            drawCircle(Color(0xFFFF5A5A), cw * 0.07f, Offset(x + cw * 0.2f, y + ch * 0.96f))
            drawCircle(Color(0xFFFF5A5A), cw * 0.07f, Offset(x + cw * 0.8f, y + ch * 0.96f))
        }
        Text("MAPA", color = DText.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = DFont, letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF0B1636).copy(alpha = 0.7f)).padding(horizontal = 6.dp, vertical = 1.dp))
    }
}

@Composable
private fun DashButton(icon: ImageVector, label: String, id: String, modifier: Modifier, action: () -> Unit) {
    DashCard(modifier.fillMaxHeight().carTap(id, action)) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val h = maxHeight.value
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(icon, null, tint = DBlue, modifier = Modifier.size((h * 0.38f).coerceIn(14f, 26f).dp))
                Text(label.uppercase(), color = DText, fontSize = (h * 0.17f).coerceIn(7f, 11f).tsp(), fontWeight = FontWeight.Bold, fontFamily = DFont,
                    letterSpacing = 0.5.sp, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun MusicCard(ctx: android.content.Context, modifier: Modifier) {
    DashCard(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(12.dp)) {
            val h = maxHeight.value
            if (!NowPlaying.allowed) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MusicNote, null, tint = DBlue, modifier = Modifier.size(20.dp))
                        Text("MÚSICA", color = DBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = DFont, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                    Text("Para mostrar e controlar a música aqui, permita no celular: Configurações › Sobre e backup › Android Auto › Música no painel.",
                        color = DSub, fontSize = 12.sp, fontFamily = DFont, modifier = Modifier.padding(top = 6.dp))
                }
                return@BoxWithConstraints
            }
            val artSize = (h * 0.42f).coerceIn(32f, 64f)
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val art = NowPlaying.art
                    Box(Modifier.size(artSize.dp).clip(RoundedCornerShape(10.dp)).background(DTrack), contentAlignment = Alignment.Center) {
                        if (art != null) Image(art.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        else Icon(Icons.Rounded.MusicNote, null, tint = DSub, modifier = Modifier.size((artSize * 0.45f).dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text((NowPlaying.app.ifBlank { "Música" }).uppercase(), color = DBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = DFont, letterSpacing = 1.sp, maxLines = 1)
                        Text(NowPlaying.title.ifBlank { "Nada tocando" }, color = DText, fontSize = (h * 0.13f).coerceIn(11f, 18f).tsp(), fontWeight = FontWeight.Bold, fontFamily = DFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (NowPlaying.artist.isNotBlank()) Text(NowPlaying.artist, color = DSub, fontSize = (h * 0.1f).coerceIn(9f, 13f).tsp(), fontFamily = DFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                val d = NowPlaying.duration
                val f = if (d > 0) (NowPlaying.position.toFloat() / d).coerceIn(0f, 1f) else 0f
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DTrack)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(f).background(Brush.horizontalGradient(GBlue)))
                }
                val btn = (h * 0.3f).coerceIn(30f, 50f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    MusicBtn(Icons.Rounded.SkipPrevious, "music-prev", false, btn * 0.85f) { NowPlaying.prev(ctx) }
                    MusicBtn(if (NowPlaying.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "music-play", true, btn) { NowPlaying.playPause(ctx) }
                    MusicBtn(Icons.Rounded.SkipNext, "music-next", false, btn * 0.85f) { NowPlaying.next(ctx) }
                }
            }
        }
    }
}

@Composable
private fun MusicBtn(icon: ImageVector, id: String, main: Boolean, sizeDp: Float, action: () -> Unit) {
    Box(Modifier.size(sizeDp.dp).clip(CircleShape).background(if (main) Brush.verticalGradient(GBlue) else Brush.verticalGradient(listOf(DTrack, DTrack)))
        .border(1.dp, DEdge.copy(alpha = 0.7f), CircleShape).carTap(id, action), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size((sizeDp * 0.56f).dp))
    }
}

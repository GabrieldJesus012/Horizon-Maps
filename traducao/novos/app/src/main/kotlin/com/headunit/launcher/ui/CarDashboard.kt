package com.headunit.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
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
    val c = LocalVw.current
    val ctx = LocalContext.current.applicationContext
    var win by remember { mutableStateOf<Rect?>(null) }
    val corner = 22.dp

    // the weather where you are (refreshed every 20 minutes)
    var wx by remember { mutableStateOf<com.headunit.launcher.Weather.Now?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            val p: LL? = engine.fix?.pos
            if (p != null) {
                val w = withContext(Dispatchers.IO) { try { Weather.fetch(p) } catch (e: Exception) { null } }
                if (w != null) wx = w
                delay(20 * 60_000L)
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
            drawRect(c.bg)
            win?.let { r -> drawRoundRect(Color.Transparent, r.topLeft, r.size, CornerRadius(corner.toPx()), blendMode = BlendMode.Clear) }
        },
    ) {
        Row(
            Modifier.fillMaxSize().padding(start = left, top = top, end = right, bottom = bottom),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ---- left: clock, gauges with the car in the middle, buttons ----
            Column(Modifier.weight(0.62f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ClockCard(now, Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SpeedGauge(engine, Modifier.weight(1f).fillMaxWidth())
                        WeatherGauge(wx, Modifier.weight(1f).fillMaxWidth())
                    }
                    CarTile(Modifier.weight(0.8f).fillMaxHeight().carTap("dash-car") { engine.carDash = false })
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TodayGauge(engine, Modifier.weight(1f).fillMaxWidth())
                        FuelGauge(engine, Modifier.weight(1f).fillMaxWidth())
                    }
                }
                Row(Modifier.fillMaxWidth().height(66.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashButton(Icons.Rounded.Search, "Buscar", "dash-search", Modifier.weight(1f)) { actions.search() }
                    DashButton(Icons.Rounded.Star, "Locais", "dash-places", Modifier.weight(1f)) { actions.places() }
                    DashButton(Icons.Rounded.Map, "Mapa", "dash-map", Modifier.weight(1f)) { engine.carDash = false }
                    DashButton(Icons.Rounded.LocalGasStation, "Postos", "dash-fuel", Modifier.weight(1f)) { actions.nearby("fuel") }
                    DashButton(Icons.Rounded.Settings, "Ajustes", "dash-settings", Modifier.weight(1f)) { actions.settings() }
                }
            }
            // ---- right: the music, and the live map window ----
            Column(Modifier.weight(0.38f).fillMaxHeight().padding(top = 58.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MusicCard(ctx, Modifier.fillMaxWidth())
                Box(
                    Modifier.fillMaxWidth().weight(1f).border(2.dp, c.line, RoundedCornerShape(corner))
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
private fun DashCard(modifier: Modifier, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    val c = LocalVw.current
    Box(modifier.clip(RoundedCornerShape(20.dp)).background(c.surface).border(1.dp, c.line.copy(alpha = 0.6f), RoundedCornerShape(20.dp))) { content() }
}

@Composable
private fun ClockCard(now: Long, modifier: Modifier) {
    val c = LocalVw.current
    val br = java.util.Locale("pt", "BR")
    val time = java.text.SimpleDateFormat("HH:mm", br).format(java.util.Date(now))
    val day = java.text.SimpleDateFormat("EEEE", br).format(java.util.Date(now)).uppercase(br)
    val date = java.text.SimpleDateFormat("d 'de' MMMM 'de' yyyy", br).format(java.util.Date(now))
    DashCard(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(time, color = c.text, fontSize = 46.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Box(Modifier.padding(horizontal = 18.dp).width(2.dp).height(44.dp).background(c.accent))
            Column(Modifier.weight(1f)) {
                Text(day, color = c.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
                Text(date, color = c.text, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** A round gauge: a 240° track, the value's arc in [color], the figure in the middle and the label above. */
@Composable
private fun Gauge(label: String, value: String, unit: String, frac: Float, color: Color, foot: String?, modifier: Modifier) {
    val c = LocalVw.current
    DashCard(modifier) {
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = c.sub, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().padding(4.dp)) {
                    val s = minOf(size.width, size.height)
                    val stroke = s * 0.09f
                    val tl = Offset((size.width - s) / 2f + stroke / 2f, (size.height - s) / 2f + stroke / 2f)
                    val sz = Size(s - stroke, s - stroke)
                    drawArc(c.line, 150f, 240f, false, tl, sz, style = Stroke(stroke, cap = StrokeCap.Round))
                    val f = frac.coerceIn(0f, 1f)
                    if (f > 0.005f) drawArc(color, 150f, 240f * f, false, tl, sz, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    if (unit.isNotEmpty()) Text(unit, color = c.sub, fontSize = 11.sp, maxLines = 1)
                }
            }
            if (!foot.isNullOrEmpty()) Text(foot, color = c.sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SpeedGauge(engine: NavEngine, modifier: Modifier) {
    val kmh = ((engine.fix?.speed ?: 0.0) * 3.6).coerceAtLeast(0.0)
    val lim = engine.speedLimit?.let { Math.round(it * 3.6).toInt() }
    val over = lim != null && kmh > lim + 3
    Gauge("VELOCIDADE", "${Math.round(kmh)}", "km/h", (kmh / 160.0).toFloat(), if (over) Bad else LocalVw.current.accent,
        if (lim != null) "limite $lim" else null, modifier)
}

private fun wxText(code: Int): String = when (code) {
    0 -> "Céu limpo"; 1, 2 -> "Parcialmente nublado"; 3 -> "Nublado"; 45, 48 -> "Neblina"
    in 51..57 -> "Garoa"; in 61..67 -> "Chuva"; in 71..77 -> "Neve"; in 80..82 -> "Pancadas de chuva"
    in 95..99 -> "Tempestade"; else -> ""
}

@Composable
private fun WeatherGauge(wx: Weather.Now?, modifier: Modifier) {
    val t = wx?.tempC
    Gauge("CLIMA AGORA", if (t != null) "${Math.round(t)}°" else "--", "°C", if (t != null) ((t - 10.0) / 32.0).toFloat() else 0f,
        if (t != null && t >= 34) Bad else Warn, wx?.let { wxText(it.code) }, modifier)
}

@Composable
private fun TodayGauge(engine: NavEngine, modifier: Modifier) {
    @Suppress("UNUSED_VARIABLE") val tick = engine.tripDist   // re-read as you drive
    val (m, secs) = engine.kmToday()
    val km = m / 1000.0
    val mins = (secs / 60L).toInt()
    Gauge("KM HOJE", if (km < 10) String.format(java.util.Locale("pt", "BR"), "%.1f", km) else "${Math.round(km)}", "km",
        (km / 100.0).toFloat(), Good, if (mins > 0) (if (mins >= 60) "${mins / 60} h ${mins % 60} min ao volante" else "$mins min ao volante") else null, modifier)
}

@Composable
private fun FuelGauge(engine: NavEngine, modifier: Modifier) {
    val fuel = engine.carFuelPct
    val range = engine.carRangeM
    if (fuel != null || range != null) {
        val f = (fuel ?: 0f) / 100f
        Gauge("COMBUSTÍVEL", if (fuel != null) "${Math.round(fuel)}%" else "--", if (fuel != null) "do tanque" else "",
            f, if (f < 0.15f) Bad else if (f < 0.3f) Warn else Good, range?.let { "autonomia ${Math.round(it / 1000f)} km" }, modifier)
    } else {
        // the car doesn't report its fuel to Android Auto: the road's limit and the next speed camera instead
        val lim = engine.speedLimit?.let { Math.round(it * 3.6).toInt() }
        val cam = engine.cameraDist
        Gauge("LIMITE DA VIA", lim?.toString() ?: "--", "km/h", ((lim ?: 0) / 120.0).toFloat(), Bad,
            if (cam != null) "radar a ${com.headunit.launcher.Geo.fmtDist(cam.toDouble(), false)}" else "sem radar perto", modifier)
    }
}

/** The car seen from above on its lane (tap: the full map). */
@Composable
private fun CarTile(modifier: Modifier) {
    val c = LocalVw.current
    DashCard(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFF1B2231), Color(0xFF2B3550), Color(0xFF1B2231))))
            // lane edges and the dashed centre lines
            val edge = Stroke(w * 0.02f)
            drawLine(Color(0xFF8E98B0), Offset(w * 0.12f, 0f), Offset(w * 0.12f, h), strokeWidth = edge.width)
            drawLine(Color(0xFF8E98B0), Offset(w * 0.88f, 0f), Offset(w * 0.88f, h), strokeWidth = edge.width)
            val dash = PathEffect.dashPathEffect(floatArrayOf(h * 0.07f, h * 0.06f))
            drawLine(Color(0xFFDDE3F0), Offset(w * 0.37f, 0f), Offset(w * 0.37f, h), strokeWidth = w * 0.018f, pathEffect = dash)
            drawLine(Color(0xFFDDE3F0), Offset(w * 0.63f, 0f), Offset(w * 0.63f, h), strokeWidth = w * 0.018f, pathEffect = dash)
            // the car (top view)
            val cw = w * 0.22f; val ch = cw * 2.05f
            val x = w * 0.5f - cw / 2f; val y = h * 0.62f - ch / 2f
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
        Text("MAPA", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
    }
}

@Composable
private fun DashButton(icon: ImageVector, label: String, id: String, modifier: Modifier, action: () -> Unit) {
    val c = LocalVw.current
    DashCard(modifier.fillMaxHeight().carTap(id, action)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = c.accent, modifier = Modifier.size(24.dp))
            Text(label.uppercase(), color = c.text, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, maxLines = 1,
                modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun MusicCard(ctx: android.content.Context, modifier: Modifier) {
    val c = LocalVw.current
    DashCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            if (!NowPlaying.allowed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.MusicNote, null, tint = c.accent, modifier = Modifier.size(22.dp))
                    Text("MÚSICA", color = c.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Text("Para mostrar e controlar a música aqui, permita no celular: Configurações › Sobre e backup › Android Auto › Música no painel.",
                    color = c.sub, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val art = NowPlaying.art
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(c.tile), contentAlignment = Alignment.Center) {
                    if (art != null) Image(art.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Rounded.MusicNote, null, tint = c.sub, modifier = Modifier.size(26.dp))
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text((NowPlaying.app.ifBlank { "Música" }).uppercase(), color = c.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
                    Text(NowPlaying.title.ifBlank { "Nada tocando" }, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (NowPlaying.artist.isNotBlank()) Text(NowPlaying.artist, color = c.sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            // progress
            val d = NowPlaying.duration
            val f = if (d > 0) (NowPlaying.position.toFloat() / d).coerceIn(0f, 1f) else 0f
            Box(Modifier.padding(top = 10.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.line)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(f).background(c.accent))
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                MusicBtn(Icons.Rounded.SkipPrevious, "music-prev", false) { NowPlaying.prev(ctx) }
                MusicBtn(if (NowPlaying.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "music-play", true) { NowPlaying.playPause(ctx) }
                MusicBtn(Icons.Rounded.SkipNext, "music-next", false) { NowPlaying.next(ctx) }
            }
        }
    }
}

@Composable
private fun MusicBtn(icon: ImageVector, id: String, main: Boolean, action: () -> Unit) {
    val c = LocalVw.current
    Box(Modifier.size(if (main) 50.dp else 44.dp).clip(CircleShape).background(if (main) c.accent else c.tile).carTap(id, action), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = if (main) Color.White else c.text, modifier = Modifier.size(if (main) 28.dp else 24.dp))
    }
}

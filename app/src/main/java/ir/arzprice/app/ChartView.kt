package ir.arzprice.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View

/** نمودار ناحیه‌ای ساده */
class ChartView(ctx: Context) : View(ctx) {

    var values: List<Double> = emptyList()
    var labels: List<String> = emptyList()
    var up: Boolean = true

    private val density = ctx.resources.displayMetrics.density
    private val line = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        line.style = Paint.Style.STROKE
        line.strokeWidth = 2.5f * density
        line.strokeJoin = Paint.Join.ROUND
        fill.style = Paint.Style.FILL
        txt.textSize = 12f * ctx.resources.displayMetrics.scaledDensity
    }

    fun setData(v: List<Double>, l: List<String>, isUp: Boolean) {
        values = v
        labels = l
        up = isUp
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val n = values.size
        if (n < 2) return
        val w = width.toFloat()
        val h = height.toFloat()
        val ch = h - 28f * density
        var mn = Double.MAX_VALUE
        var mx = -Double.MAX_VALUE
        for (v in values) {
            if (v < mn) mn = v
            if (v > mx) mx = v
        }
        val span = if (mx - mn < 1e-12) 1.0 else mx - mn
        val pad = ch * 0.08f

        val path = Path()
        val area = Path()
        for (i in 0 until n) {
            val x = w * i / (n - 1)
            val y = (pad + (ch - 2 * pad) * (1.0 - (values[i] - mn) / span)).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
                area.moveTo(x, ch)
                area.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                area.lineTo(x, y)
            }
        }
        area.lineTo(w, ch)
        area.close()

        val color = if (up) Theme.up else Theme.down
        val clear = color and 0x00FFFFFF
        fill.shader = LinearGradient(0f, 0f, 0f, ch, clear or 0x66000000, clear, Shader.TileMode.CLAMP)
        canvas.drawPath(area, fill)
        line.color = color
        canvas.drawPath(path, line)

        txt.color = Theme.sub
        val m = labels.size
        for (i in 0 until m) {
            val x = if (m == 1) 0f else w * i / (m - 1)
            txt.textAlign = when (i) {
                0 -> Paint.Align.LEFT
                m - 1 -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            canvas.drawText(labels[i], x, h - 6f * density, txt)
        }
    }
}

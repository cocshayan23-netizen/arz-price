package ir.arzprice.app

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** هر ۱۵ دقیقه: قیمت‌ها را می‌گیرد، ویجت‌ها را بروز می‌کند و هشدارها را بررسی می‌کند. */
class AlertWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val app = applicationContext
        try {
            Data.refresh(app)
            WidgetUpdater.updateAll(app)
            Alerts.check(app)
        } catch (e: Exception) {
            // اینترنت نبود؛ دفعه‌ی بعد دوباره تلاش می‌شود
        }
        return Result.success()
    }
}

package androidx.car.app.sample.showcase.common.misc

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.Speed
import androidx.car.app.model.*
import androidx.car.app.sample.showcase.common.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

data class UiModel(private val speed: Speed? = null) {
    val speedRow: Row?
        get() = speed?.let { speed ->
            val row = Row.Builder()
            row.setTitle("Speed in m/s")
            row.addText("${speed.rawSpeedMetersPerSecond.value} m/s.")
            row.build()
        }
}

class CarSpeedScreen(carContext: CarContext) : Screen(carContext) {
    private val hardwareExecutor = ContextCompat.getMainExecutor(carContext)
    private val speedListener = OnCarDataAvailableListener<Speed> {
        synchronized(this) {
            model = model.copy(speed = it)
            invalidate()
        }
    }

    private var model: UiModel = UiModel()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                val carHardwareManager = carContext.getCarService(CarHardwareManager::class.java)
                try {
                    carHardwareManager.carInfo.addSpeedListener(hardwareExecutor, speedListener)
                } catch (e: SecurityException) {
                    Log.d("CarSpeedScreen", e.message, e)
                }
            }
        })
    }

    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()

        model.speedRow?.let {
            pane.addRow(it)
        } ?: run {
            pane.setLoading(true)
        }

        return PaneTemplate.Builder(pane.build())
            .setTitle(carContext.getString(R.string.speed_demo_title))
            .setHeaderAction(Action.BACK)
            .build()
    }
}

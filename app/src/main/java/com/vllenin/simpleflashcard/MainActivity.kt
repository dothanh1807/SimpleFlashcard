package com.vllenin.simpleflashcard

import android.graphics.Insets
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.WindowInsets
import android.view.WindowMetrics
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val screenDisplaySize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            Size(windowMetrics.bounds.width(), windowMetrics.bounds.height() - insets.bottom)
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

        val listData = ArrayList<Pair<String, String>>()
        for (i in 0..10) {
            listData.add(Pair("FRONT $i", "BACK $i"))
        }

        flashCardView.setData(listData, screenDisplaySize)
            .setCallbackProgress { position, total ->
                Log.d("XXX", "$position / $total")
            }

    }

}
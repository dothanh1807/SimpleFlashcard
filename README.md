# SimpleFlashcard

![xxx](https://user-images.githubusercontent.com/52622713/104834519-7c7ee700-58d2-11eb-9d48-ab329f15afb9.gif)

```javascript
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

  <com.vllenin.simpleflashcard.FlashCardView
      android:id="@+id/flashCardView"
      android:layout_width="match_parent"
      android:layout_height="match_parent"/>

</RelativeLayout>
```

```javascript
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
```

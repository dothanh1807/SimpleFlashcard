package com.vllenin.simpleflashcard

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import kotlinx.android.synthetic.main.item_flash_card.view.*

/**
 * Created by Vllenin on 9/16/20.
 */
@SuppressLint("ClickableViewAccessibility")
class FlashCardView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {

    companion object {
        const val AMOUNT_VIEW_DEFAULT = 3
        const val MODE_FRONT = 64
        const val MODE_BACK = 65
        const val TIME_ANIMATION_SHORT = 300L
        const val TIME_ANIMATION_NORMAL = 400L
        const val TIME_ANIMATION_LONG = 500L
    }

    private var callbackProgress: (position: Int, total: Int) -> Unit = {_, _ -> }
    private lateinit var screenSize: Size
    private lateinit var arrayFlashCardView : Array<View>
    private lateinit var listData: List<Pair<String, String>>
    private val numberBouncingX = resources.getDimensionPixelSize(R.dimen.default_margin_normal)
    private val numberBouncingY = resources.getDimensionPixelSize(R.dimen.default_margin_small)

    private var currentMode: Int = MODE_FRONT
    private var countingFlashcard = 0
    private var countingSwiped = 0
    private var oldX = 0f
    private var oldY = 0f
    private var dX = 0f
    private var dY = 0f
    private var cameraDistanceFC = 0f

    private val gestureDetector = object : GestureDetector.OnGestureListener {
        override fun onShowPress(e: MotionEvent?) {}
        override fun onDown(e: MotionEvent?): Boolean = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = false
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = false
        override fun onLongPress(e: MotionEvent?) {}

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            flipView(getViewOnTop())
            return false
        }
    }

    private var detector = GestureDetectorCompat(context, gestureDetector)

    private val onTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = v.x - event.rawX
                dY = v.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                v.x = event.rawX + dX
                v.y = event.rawY + dY
                if ((event.rawX + dX + v.width/2) < screenSize.width / 2) {
                    val delta = (event.rawX + dX + v.width/2) / (screenSize.width / 2) * 30
                    v.rotation = delta + 330
                    if (delta > 15) {
                        v.iconNope.alpha = 1 - (delta - 15) / 15
                    }
                } else {
                    val delta = ((event.rawX + dX + v.width/2) - (screenSize.width / 2)) / (screenSize.width / 2) * 30
                    v.rotation = delta
                    if (delta < 15) {
                        v.iconLike.alpha = delta / 15
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                v.isEnabled = false
                if (v.x <= (-v.width / 4) || v.x >= (screenSize.width - (v.width * 3 / 4))) {
                    if (v.x <= (-v.width / 4)) {
                        animateRemoveViewToLeft(v)
                    } else {
                        animateRemoveViewToRight(v)
                    }
                } else {
                    var formLeft = false
                    val rotationOrigin = if (v.rotation <= 30) {
                        0f
                    } else {
                        formLeft = true
                        360f
                    }
                    val numberBouncingX = if (formLeft) {
                        this.numberBouncingX
                    } else {
                        -this.numberBouncingX
                    }
                    val numberBouncingY = if (v.y + v.height/2 > height/2) {
                        -this.numberBouncingY
                    } else {
                        this.numberBouncingY
                    }
                    v.animate().x(oldX + numberBouncingX).y(oldY + numberBouncingY).rotation(rotationOrigin).setDuration(200)
                        .withEndAction {
                            v.animate().x(oldX - numberBouncingX / 2).y(oldY - numberBouncingY / 2).setDuration(100).withEndAction {
                                v.animate().x(oldX).y(oldY).setDuration(100).withEndAction {
                                    v.isEnabled = true
                                }.start()
                            }.start()
                        }
                        .start()
                    if (v.iconNope.alpha != 0f) {
                        v.iconNope.animate().alpha(0f).setDuration(TIME_ANIMATION_SHORT).start()
                    }
                    if (v.iconLike.alpha != 0f) {
                        v.iconLike.animate().alpha(0f).setDuration(TIME_ANIMATION_SHORT).start()
                    }
                }
            }
        }

        detector.onTouchEvent(event)

        true
    }

    fun setCallbackProgress(callback: (position: Int, total: Int) -> Unit) = apply {
        callbackProgress = callback
    }

    fun setData(flashcards: List<Pair<String, String>>, screenSize: Size) = apply {
        listData = flashcards
        this.screenSize = screenSize
        countingFlashcard = 0
        countingSwiped = 0
        callbackProgress.invoke(countingSwiped, flashcards.size)
        val amountView = if (listData.size < AMOUNT_VIEW_DEFAULT) {
            listData.size
        } else {
            AMOUNT_VIEW_DEFAULT
        }
        arrayFlashCardView = Array(amountView) {
            val flashCardView = LayoutInflater.from(context).inflate(R.layout.item_flash_card, this, false)
            flashCardView.id = it
            flashCardView.elevation = it.toFloat()
            val lp = flashCardView.layoutParams as LayoutParams
            lp.width = (screenSize.width * 0.75).toInt()
            lp.height = (lp.width * 1.5).toInt()
            lp.gravity = Gravity.CENTER
            lp.topMargin = (amountView - 1 - it) * resources.getDimensionPixelSize(R.dimen.default_margin_normal)
            flashCardView.layoutParams = lp

            bindDataToFlashCard(flashCardView, listData[(amountView - 1 - it)])

            ++countingFlashcard
            addView(flashCardView)
            if ((it + 10) % 2 == 0) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.from_left)
                animation.interpolator = DecelerateInterpolator()
                animation.duration = TIME_ANIMATION_LONG
                flashCardView.startAnimation(animation)
            } else {
                val animation = AnimationUtils.loadAnimation(context, R.anim.from_right)
                animation.interpolator = DecelerateInterpolator()
                animation.duration = TIME_ANIMATION_LONG
                flashCardView.startAnimation(animation)
            }
            if (it == amountView - 1) {
                setActionForFlashCard(flashCardView)
                flashCardView.post {
                    oldX = flashCardView.x
                    oldY = flashCardView.y
                }
            }

            flashCardView
        }
    }

    private fun bindDataToFlashCard(view: View, itemFlashCard: Pair<String, String>) {
        view.textFrontSide.text = itemFlashCard.first
        view.textBackSide.text = itemFlashCard.second

        if (currentMode == MODE_FRONT) {
            view.textFrontSide.visibility = View.VISIBLE
            view.textBackSide.visibility = View.INVISIBLE
        } else {
            view.textFrontSide.visibility = View.INVISIBLE
            view.textBackSide.visibility = View.VISIBLE
        }
    }

    private fun animateRemoveViewToLeft(view: View) {
        view.animate().x(-view.width * 1.1f).setDuration(TIME_ANIMATION_SHORT)
            .withEndAction {
                handleFlashCard(view)
            }
            .start()
    }

    private fun animateRemoveViewToRight(view: View) {
        view.animate().x(screenSize.width * 1.1f).setDuration(TIME_ANIMATION_SHORT)
            .withEndAction {
                handleFlashCard(view)
            }
            .start()
    }

    private fun handleFlashCard(view: View) {
        view.iconNope.alpha = 0f
        view.iconLike.alpha = 0f
        if (arrayFlashCardView.size < AMOUNT_VIEW_DEFAULT) {
            view.isEnabled = false
            view.setOnTouchListener(null)
            removeView(view)
            countingSwiped++
            callbackProgress.invoke(countingSwiped, listData.size)
            setActionForFlashCard(arrayFlashCardView[0])
            return
        }
        removeView(arrayFlashCardView[2])
        countingSwiped++
        callbackProgress.invoke(countingSwiped, listData.size)
        arrayFlashCardView[2].x = oldX
        arrayFlashCardView[2].y = oldY
        arrayFlashCardView[2].rotation = 0f

        val cardTop = arrayFlashCardView[2]
        val cardMid = arrayFlashCardView[1]
        val cardBot = arrayFlashCardView[0]
        arrayFlashCardView[2] = cardMid
        arrayFlashCardView[1] = cardBot
        arrayFlashCardView[0] = cardTop

        for (i in arrayFlashCardView.indices) {
            arrayFlashCardView[i].isEnabled = false
            arrayFlashCardView[i].setOnTouchListener(null)
            arrayFlashCardView[i].elevation = i.toFloat()
            val lp = arrayFlashCardView[i].layoutParams as LayoutParams
            lp.topMargin = (AMOUNT_VIEW_DEFAULT - 1 - i) * resources.getDimensionPixelSize(R.dimen.default_margin_normal)
            arrayFlashCardView[i].layoutParams = lp
        }

        val positionData = (++countingFlashcard - 1)
        if (positionData < listData.size) {
            bindDataToFlashCard(arrayFlashCardView[0], listData[positionData])
            addView(arrayFlashCardView[0])
            val animation = AnimationUtils.loadAnimation(context,
                R.anim.item_animation_from_bottom_scale
            )
            animation.duration = TIME_ANIMATION_NORMAL
            arrayFlashCardView[0].startAnimation(animation)
        }

        post {
            setActionForFlashCard(arrayFlashCardView[2])
        }
    }

    private fun setActionForFlashCard(view: View) {
        view.isEnabled = true
        view.setOnTouchListener(onTouchListener)
    }

    private fun getViewOnTop(): View {
        var viewOnTop = getChildAt(0)
        for (i in 0 until childCount) {
            if (getChildAt(i).elevation > viewOnTop.elevation) {
                viewOnTop = getChildAt(i)
            }
        }

        return viewOnTop
    }

    private fun flipView(view: View) {
        if (cameraDistanceFC == 0f) {
            cameraDistanceFC = view.cameraDistance * 3f
        }
        view.cameraDistance = cameraDistanceFC
        view.animate().withLayer().rotationY(90f).scaleY(0.85f).setDuration(TIME_ANIMATION_SHORT)
            .setInterpolator(DecelerateInterpolator())
            .withStartAction {
                view.overlayView.visibility = View.VISIBLE
            }
            .withEndAction {
                view.rotationY = -90f
                view.animate().withLayer().rotationY(0f).scaleY(1f).setDuration(TIME_ANIMATION_SHORT)
                    .setInterpolator(DecelerateInterpolator())
                    .withStartAction {
                        view.overlayView.visibility = View.INVISIBLE
                    }
                    .withEndAction {

                    }
                    .start()
                changeViewWhenFlip(view)
            }
            .start()
    }

    private fun changeViewWhenFlip(view: View) {
        if (view.textFrontSide.visibility == View.VISIBLE) {
            view.textFrontSide.visibility = View.INVISIBLE
            view.textBackSide.visibility = View.VISIBLE
        } else {
            view.textFrontSide.visibility = View.VISIBLE
            view.textBackSide.visibility = View.INVISIBLE
        }
    }

}
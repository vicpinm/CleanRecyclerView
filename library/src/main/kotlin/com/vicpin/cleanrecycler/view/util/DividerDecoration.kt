package com.vicpin.cleanrecycler.view.util
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout


class DividerDecoration(context: Context, orientation: Int) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null
    /**
     * Current orientation. Either [.HORIZONTAL] or [.VERTICAL].
     */
    private var mOrientation: Int = 0
    private val mBounds = Rect()

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * [RecyclerView.LayoutManager] changes orientation.

     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL")
        }
        mOrientation = orientation
    }

    /**
     * Sets the [Drawable] for this divider.

     * @param drawable Drawable that should be used as a divider.
     */
    fun setDrawable(drawable: Drawable) {
        mDivider = drawable
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getLayoutManager() == null) {
            return
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    @SuppressLint("NewApi")
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft()
            right = parent.getWidth() - parent.getPaddingRight()
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom())
        } else {
            left = 0
            right = parent.getWidth()
        }
        val childCount = parent.getChildCount()
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child))
            val top = bottom - mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    @SuppressLint("NewApi")
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop()
            bottom = parent.getHeight() - parent.getPaddingBottom()
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom)
        } else {
            top = 0
            bottom = parent.getHeight()
        }
        val childCount = parent.getChildCount()
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            parent.layoutManager?.getDecoratedBoundsWithMargins(child, mBounds)
            val right = mBounds.right + Math.round(ViewCompat.getTranslationX(child))
            val left = right - mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (mOrientation == VERTICAL) {
            outRect.set(0, 0, 0, mDivider!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        }
    }

    companion object {
        val HORIZONTAL = LinearLayout.HORIZONTAL
        val VERTICAL = LinearLayout.VERTICAL
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}
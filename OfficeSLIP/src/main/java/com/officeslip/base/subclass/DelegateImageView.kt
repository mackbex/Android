package com.officeslip.base.subclass

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.MotionEvent
import com.officeslip.CIRCLE_ALPHA_DEFAULT
import com.officeslip.PEN_ALPHA_DEFAULT
import com.officeslip.RECT_ALPHA_DEFAULT
import com.officeslip.data.model.Bookmark
import com.officeslip.data.model.BookmarkType
import com.officeslip.data.model.RectPosition
import com.officeslip.data.model.Slip
import com.officeslip.px

class DelegateImageView(
        var imageView: TouchImageView,
        var slip:Slip,
        var bookmakList:List<Bookmark>?
) : TouchImageView.ImageViewDelegate  {

    var m_paint = Paint()
    var m_textPaint = TextPaint()
    var m_rect = RectF()


    override fun onDraw(canvas: Canvas?) {
        if(bookmakList == null) return
        var horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2f
        var verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2f


        var zoomedWRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
        var zoomedHRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()

//            var zoomedWRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.imageWidth
//            var zoomedHRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.imageHeight

        //   shape.leftPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio

        bookmakList?.forEach { bookmark ->
            canvas?.apply {

                with(bookmark) {
//                        var leftPoint = (shape.leftPoint -  horizontalspace + (imageView.currentRect.left * wRatio)) * zoomedWRatio //activity.pointToViewPoint(shape.leftPoint, imageView, zoomedWRatio, LEFT_POINT)
//                        var topPoint = (shape.topPoint -  verticalspace + (imageView.currentRect.top * hRatio)) * zoomedHRatio//activity.pointToViewPoint(shape.topPoint, imageView, zoomedHRatio, TOP_POINT)
//                        var rightPoint = (shape.rightPoint -  horizontalspace + (imageView.currentRect.right * wRatio)) * zoomedWRatio//activity.pointToViewPoint(shape.rightPoint, imageView, zoomedWRatio, RIGHT_POINT)
//                        var bottomPoint = (shape.bottomPoint -  verticalspace + (imageView.currentRect.bottom * hRatio)) * zoomedHRatio//activity.pointToViewPoint(shape.bottomPoint, imageView, zoomedHRatio, BOTTOM_POINT)

                    var leftPoint = pointToViewPoint(leftPoint, imageView, RectPosition.LEFT)
                    var topPoint = pointToViewPoint(topPoint, imageView, RectPosition.TOP)
                    var rightPoint = pointToViewPoint(rightPoint, imageView, RectPosition.RIGHT)
                    var bottomPoint = pointToViewPoint(bottomPoint, imageView, RectPosition.BOTTOM)
//
//                         leftPoint = (shape.leftPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
//                         topPoint = (shape.topPoint * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
//                         rightPoint = (shape.rightPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
//                         bottomPoint = (shape.bottomPoint * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedWRatio)

                    when (bookmarkType) {
                        BookmarkType.PEN -> {
                            m_paint.apply {
                                color = Color.parseColor(bgColor)
                                alpha = PEN_ALPHA_DEFAULT
                                isAntiAlias = true
                                style = Paint.Style.FILL
                            }
                            drawRect(leftPoint, topPoint, rightPoint, bottomPoint, m_paint)
                        }
                        BookmarkType.RECT -> {
                            if (backFlag == "0") {
                                m_paint.apply {
                                    isAntiAlias = true
                                    style = Paint.Style.FILL
                                    color = Color.parseColor(bgColor)
                                    alpha = RECT_ALPHA_DEFAULT
                                }
                                drawRect(leftPoint, topPoint, rightPoint, bottomPoint, m_paint)
                            }

                            m_paint.apply {
                                color = Color.parseColor(lineColor)
                                alpha = RECT_ALPHA_DEFAULT
                                isAntiAlias = true
                                style = Paint.Style.STROKE
                                strokeWidth = weight.toInt().px.toFloat()
                            }
                            drawRect(leftPoint, topPoint, rightPoint, bottomPoint, m_paint)
                        }
                        BookmarkType.MEMO -> {
                            m_paint.apply {
                                color = Color.parseColor(bgColor)
                                alpha = (255 * alpha).toInt()
                                isAntiAlias = true
                                // strokeWidth = shape.weight.px.toFloat()
                                style = Paint.Style.FILL
                            }
                            drawRect(leftPoint, topPoint, rightPoint, bottomPoint, m_paint)

                            m_paint.apply {
                                color = Color.parseColor(lineColor)
                                alpha = (255 * alpha).toInt()
                                isAntiAlias = true
                                strokeWidth = weight.toInt().px.toFloat()
                                style = Paint.Style.STROKE
                            }
                            drawRect(leftPoint, topPoint, rightPoint, bottomPoint, m_paint)

                            val fTextSize = (fontSize.px.toFloat() * imageView.currentZoom) * 2
                            val text = text
                            m_textPaint.apply {
                                textSize = fTextSize
                                color = Color.parseColor(fontColor)
                                style = Paint.Style.FILL
                                if (bold == "1" && italic == "1") {
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                                } else {
                                    if (bold == "1") typeface = Typeface.DEFAULT_BOLD
                                    if (italic == "1") typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                                }
                            }

                            var width = rightPoint - leftPoint
                            var startLeft = leftPoint
                            var startTop = topPoint
                            //sort horizontal points
                            if (leftPoint > rightPoint) {
                                width = leftPoint - rightPoint
                                startLeft = rightPoint
                                startTop = bottomPoint
                            }

//                                val ellipsize = TextUtils.ellipsize(text, m_textPaint, width, TextUtils.TruncateAt.END)
//                                val sl = StaticLayout(ellipsize, m_textPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)

                            val sl = StaticLayout(text, m_textPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)
                            canvas.save()
                            canvas.translate(startLeft, startTop)
                            sl.draw(canvas)
                            canvas.restore()
                        }
                        BookmarkType.CIRCLE -> {
                            if (backFlag == "0") {
                                m_rect.set(leftPoint, topPoint, rightPoint, bottomPoint)
                                m_paint.apply {
                                    color = Color.parseColor(bgColor)
                                    alpha = CIRCLE_ALPHA_DEFAULT
                                    isAntiAlias = true
                                    // strokeWidth = shape.weight.px.toFloat()
                                    style = Paint.Style.FILL
                                }
                                drawOval(m_rect, m_paint)
                            }

                            m_rect.set(leftPoint, topPoint, rightPoint, bottomPoint)
                            m_paint.apply {
                                color = Color.parseColor(lineColor)
                                alpha = CIRCLE_ALPHA_DEFAULT
                                isAntiAlias = true
                                strokeWidth = weight.toInt().px.toFloat()
                                style = Paint.Style.STROKE
                            }
                            drawOval(m_rect, m_paint)
                        }
                    }
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, ildh: Int) {
    }

    override fun onTouchEvent(e: MotionEvent?) {
    }


    fun pointToViewPoint(point:Float, imageView: TouchImageView, pos:RectPosition ):Float
    {
        var space = 0f
        var curRectPoint = 0f
        var zoomedRatio = 0f

        //(shape.leftPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
        when(pos)
        {
             RectPosition.LEFT -> {
                zoomedRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth
                space = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.left
            }

            RectPosition.RIGHT -> {
                zoomedRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth
                space = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.left
            }

            RectPosition.TOP -> {
                zoomedRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight
                space = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.top
            }

            RectPosition.BOTTOM -> {
                zoomedRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight
                space = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.top
            }
        }

        return  (point * zoomedRatio) + space - (curRectPoint * zoomedRatio)
    }
}
package com.officeslip.util

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.exifinterface.media.ExifInterface
import com.officeslip.DETECT_RECTANGLE_LIMIT
import com.officeslip.base.subclass.TouchImageView
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class Crop {

    private lateinit var detectPoints:ArrayList<PointF>
    lateinit var leftUpper:PointF
    lateinit var rightUpper:PointF
    lateinit var rightBottom:PointF
    lateinit var leftBottom:PointF
    var verticalSpace:Float = 0f
    var horizontalSpace:Float = 0f
    var lastTouchedPoint: PointF? = null
    private val m_C = Common()



    fun bitmapToMat(bitmap: Bitmap): Mat {

        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4, Scalar(4.0))

        Utils.bitmapToMat(bitmap, mat)

        val mat2 = Mat()
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2RGB)
        mat.release()

        return mat2
    }

    /**
     * transform ja.view m_thumbData to pointed.
     */
    fun transformImage(bitmap: Bitmap, path:String) :Bitmap {


        val exif = ExifInterface(path)

        val matOriginal = Mat()
        org.opencv.android.Utils.bitmapToMat(bitmap, matOriginal)

        val listPoints = listOf<PointF>(leftUpper, rightUpper, rightBottom, leftBottom)

        val matTransFormed = perspectiveTransform(matOriginal, listPoints)
        val res = Bitmap.createBitmap(matTransFormed.size().width.toInt(), matTransFormed.size().height.toInt(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matTransFormed, res)

        FileOutputStream(path).use {
            res.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        m_C.copyExif(exif, path)

//        imageView.setImageBitmap(bitmap)
        matOriginal.release()
        matTransFormed.release()
//        imageView.invalidate()

        return res
    }

    /**
     * Transform the coordinates on the given Mat to correct the perspective.
     *
     * @param src A valid Mat
     * @param points A list of coordinates from the given Mat to adjust the perspective
     * @return A perspective transformed Mat
     */
    private fun perspectiveTransform(src:Mat, points:List<PointF>):Mat {
        var point1 = Point(points.get(0).x.toDouble(), points.get(0).y.toDouble())
        var point2 = Point(points.get(1).x.toDouble(), points.get(1).y.toDouble())
        var point3 = Point(points.get(2).x.toDouble(), points.get(2).y.toDouble())
        var point4 = Point(points.get(3).x.toDouble(), points.get(3).y.toDouble())
        var pts = listOf(point1, point2, point3, point4)

        return fourPointTransform(src, sortPoints(pts))
    }

    /**
     * NOTE:
     * Based off of http://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *
     * @param src
     * @param pts
     * @return
     */
    private fun fourPointTransform(src:Mat, pts:Array<Point?>):Mat {
        var ratio = 1.0

        var ul = pts[0]!!
        var ur = pts[1]!!
        var lr = pts[2]!!
        var ll = pts[3]!!

        var widthA = Math.sqrt(Math.pow(lr.x - ll.x, 2.0) + Math.pow(lr.y - ll.y, 2.0))
        var widthB = Math.sqrt(Math.pow(ur.x - ul.x, 2.0) + Math.pow(ur.y - ul.y, 2.0))
        var maxWidth = Math.max(widthA, widthB) * ratio;

        var heightA = Math.sqrt(Math.pow(ur.x - lr.x, 2.0) + Math.pow(ur.y - lr.y, 2.0))
        var heightB = Math.sqrt(Math.pow(ul.x - ll.x, 2.0) + Math.pow(ul.y - ll.y, 2.0))
        var maxHeight = Math.max(heightA, heightB) * ratio

        var resultMat = Mat(maxHeight.toInt(), maxWidth.toInt(), CvType.CV_8UC4)

        var srcMat = Mat(4, 1, CvType.CV_32FC2)
        var dstMat = Mat(4, 1, CvType.CV_32FC2)
        srcMat.put(0, 0, ul.x * ratio, ul.y * ratio, ur.x * ratio, ur.y * ratio, lr.x * ratio, lr.y * ratio, ll.x * ratio, ll.y * ratio)
        dstMat.put(0, 0, 0.0, 0.0, maxWidth, 0.0, maxWidth, maxHeight, 0.0, maxHeight)

        var M = Imgproc.getPerspectiveTransform(srcMat, dstMat)
        Imgproc.warpPerspective(src, resultMat, M, resultMat.size())

        srcMat.release()
        dstMat.release()
        M.release()

        return resultMat
    }

    fun getDetectPoints():ArrayList<PointF> {
        return detectPoints
    }

    fun setDetectPoints(list:ArrayList<PointF>) {
        detectPoints = list
    }

    fun setPoints(points: List<PointF>?, imageView: TouchImageView) {
        imageView?.apply {
            if (points != null && points.size == 4) {
                leftUpper   = PointF(points[0].x, points[0].y)
                rightUpper  = PointF(points[1].x, points[1].y)
                rightBottom = PointF(points[2].x, points[2].y)
                leftBottom  = PointF(points[3].x, points[3].y)
//                mUpperLeftPoint = imagePointToViewPoint(points[0])
//                mUpperRightPoint = imagePointToViewPoint(points[1])
//                mLowerRightPoint = imagePointToViewPoint(points[2])
//                mLowerLeftPoint = imagePointToViewPoint(points[3])

                verticalSpace       = (imageView.viewHeight - imageView.imageHeight) / 2
                horizontalSpace     = (imageView.viewWidth - imageView.imageWidth) / 2
                if(
                    (leftBottom.y - leftUpper.y) < DETECT_RECTANGLE_LIMIT
                    || (rightBottom.y - rightUpper.y) < DETECT_RECTANGLE_LIMIT
                    || (rightUpper.x - leftUpper.x) < DETECT_RECTANGLE_LIMIT
                    || (rightBottom.x - leftBottom.x) < DETECT_RECTANGLE_LIMIT
                )
                {
                    setDefaultSelection(imageView)
                }
            } else {
                setDefaultSelection(imageView)
                verticalSpace       = (imageView.viewHeight - imageView.imageHeight) / 2
                horizontalSpace     = (imageView.viewWidth - imageView.imageWidth) / 2
            }

            invalidate()
        }
    }

    /**
     * Determine if the given points are a convex quadrilateral.  This is used to prevent the
     * selection from being dragged into an invalid state.
     *
     * @param ul The upper left point
     * @param ur The upper right point
     * @param lr The lower right point
     * @param ll The lower left point
     * @return True is the quadrilateral is convex
     */
    fun isConvexQuadrilateral(view: TouchImageView, ul:PointF, ur:PointF, lr:PointF, ll:PointF):Boolean {
        // http://stackoverflow.com/questions/9513107/find-if-4-points-form-a-quadrilateral

        var p = ll
        var q = lr
        var r = subtractPoints(ur, ll)
        var s = subtractPoints(ul, lr)

        var s_r_crossProduct = crossProduct(r, s)
        var t = crossProduct(subtractPoints(q, p), s) / s_r_crossProduct
        var u = crossProduct(subtractPoints(q, p), r) / s_r_crossProduct

        if (t < 0 || t > 1.0 || u < 0 || u > 1.0) {
            return false
        } else {
            return isOverflow(view, ul, ur, lr, ll)
            //return true
        }
    }

    fun isOverflow(view: TouchImageView, ul:PointF, ur:PointF, lr:PointF, ll:PointF):Boolean
    {
        var mMargin = getImageMargin(view)

        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        var nMargin = 10
        if(ul.x < nLeftMargin - view.paddingLeft - nMargin
            || ul.x > (view.width - nLeftMargin - view.paddingRight + nMargin)
            || ul.y < nTopMargin - view.paddingTop - nMargin
            || ul.y > (view.height - nTopMargin - view.paddingBottom + nMargin))
        {
            return false
        }

        else if(ur.x < nLeftMargin - view.paddingLeft - nMargin
            || ur.x > (view.width - nLeftMargin - view.paddingRight + nMargin)
            || ur.y < nTopMargin - view.paddingTop  - nMargin
            || ur.y > (view.height - nTopMargin - view.paddingBottom  + nMargin))
        {
            return false
        }

        else if(lr.x < nLeftMargin - view.paddingLeft - nMargin
            || lr.x > (view.width - nLeftMargin - view.paddingRight + nMargin)
            || lr.y < nTopMargin-view.paddingTop  - nMargin
            || lr.y > (view.height - nTopMargin - view.paddingBottom  + nMargin))
        {
            return false
        }

        else if(ll.x < nLeftMargin - view.paddingLeft - nMargin
            || ll.x > (view.width - nLeftMargin - view.paddingRight + nMargin)
            || ll.y < nTopMargin-view.paddingTop  - nMargin
            || ll.y > (view.height - nTopMargin - view.paddingBottom  + nMargin))
        {
            return false
        }

        return true
    }

    fun subtractPoints(p1:PointF, p2:PointF):PointF {
        return PointF(p1.x - p2.x, p1.y - p2.y)
    }

    fun crossProduct(v1:PointF, v2:PointF):Float {
        return v1.x * v2.y - v1.y * v2.x
    }

    fun setDefaultSelection(imageView: TouchImageView) {
//        val mMargin = getImageMargin(imageView)

//        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
//        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        imageView.apply {
            leftUpper   = PointF(0f, 0f)
            rightUpper  = PointF(drawable.intrinsicWidth.toFloat(), 0f)
            rightBottom = PointF(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            leftBottom  = PointF(0f, drawable.intrinsicHeight.toFloat())
        }
    }

    //returns offset
    fun getImageMargin(imageView: TouchImageView):Map<String, Int>
    {
        var mRes: HashMap<String, Int> = HashMap()
        var fValues = FloatArray(9)

        var matrix = imageView.imageMatrix
        matrix.getValues(fValues)

        var nPaddingTop = imageView.paddingTop
        var nPaddingLeft = imageView.paddingLeft

        mRes["Left"] = fValues[2].toInt() + nPaddingLeft
        mRes["Top"] = fValues[5].toInt() + nPaddingTop

        return mRes
    }
    /**
     * Sort the points
     *
     * The order of the points after sorting:
     * 0------->1
     * ^        |
     * |        v
     * 3<-------2
     *
     * NOTE:
     * Based off of http://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *
     * @param src The points to sort
     * @return An array of sorted points
     */
    fun sortPoints(src:List<Point>):Array<Point?> {
        var srcPoints = src
        var result = arrayOfNulls<Point>(4)


        var sumComp = kotlin.Comparator { lhs: Point, rhs: Point ->

            (lhs.y + lhs.x).compareTo(rhs.y + rhs.x)
        }

        var diffComp = kotlin.Comparator { lhs: Point, rhs: Point ->
            (lhs.y - lhs.x).compareTo(rhs.y - rhs.x)
        }

        result[0] = Collections.min(srcPoints, sumComp)
        result[2] = Collections.max(srcPoints, sumComp)        // Lower right has the maximal sum
        result[1] = Collections.min(srcPoints, diffComp) // Upper right has the minimal difference
        result[3] = Collections.max(srcPoints, diffComp) // Lower left has the maximal difference

        // result = getScaledPoint(result)
        return result
    }

}
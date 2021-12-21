package com.officeslip.ui.qr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.officeslip.*
import com.officeslip.base.BaseFragment
import com.officeslip.databinding.FragmentQrScanBinding
import com.officeslip.ui.main.MainViewModel
import com.officeslip.ui.main.SharedMainViewModel
import com.officeslip.ui.search.user.SelectUserActivity
import com.officeslip.ui.viewer.editslip.EditSlipFragment
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess


typealias BarcodeListener = (barcode: String) -> Unit

@AndroidEntryPoint
class QRScanFragment : BaseFragment<FragmentQrScanBinding, QRScanViewModel>() {

    private val m_C = Common()
    private lateinit var progress: AlertDialog
    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService

    override val layoutResourceId: Int
        get() =  R.layout.fragment_qr_scan
    override val viewModel by viewModels<QRScanViewModel>()


    companion object {
        const val BARCODE_CODE = "BARCODDE_CODE"
    }


    override fun initStartView() {

        activity?.let {
            binding.mainViewModel = viewModels<MainViewModel>({ requireActivity() }).value
            binding.sharedViewModel = activityViewModels<SharedMainViewModel>().value
            binding.fragment = this@QRScanFragment
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    override fun initDataBinding() {

        binding.sharedViewModel?.currentPageType?.observe(viewLifecycleOwner, {
            if (it == PageType.QR) {
                startCamera()
            }
        })

//        openQRResultActivity("101010-woonam-20210510-9453B")
    }

    override fun initAfterBinding() {}

    private fun openQRResultActivity(barcode:String) {
        qrResultRegistry.launch(Intent(activity, QRReceiveActivity::class.java).apply {
            putExtra(BARCODE_CODE, barcode)
        })
    }

    private val qrResultRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult -> if (result.resultCode == Activity.RESULT_CANCELED) { processingBarcode.set(false) }
    }


    private fun startCamera() {
        // Create an instance of the ProcessCameraProvider,
        // which will be used to bind the use cases to a lifecycle owner.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Add a listener to the cameraProviderFuture.
        // The first argument is a Runnable, which will be where the magic actually happens.
        // The second argument (way down below) is an Executor that runs on the main thread.
        cameraProviderFuture.addListener({
            // Add a ProcessCameraProvider, which binds the lifecycle of your camera to
            // the LifecycleOwner within the application's life.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Initialize the Preview object, get a surface provider from your PreviewView,
            // and set it on the preview instance.
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                        binding.svQrcode.surfaceProvider
                )
            }
            // Setup the ImageAnalyzer for the ImageAnalysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, QRAnalyzer { barcode ->
                            if (processingBarcode.compareAndSet(false, true)) {
                                //earchBarcode(barcode)
//                                viewModel.showSnackbar(barcode)
                                openQRResultActivity(barcode)
                            }
                        })
                    }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to lifecycleOwner
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Logger.error("PreviewUseCase - Binding failed.", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }




    private class QRAnalyzer(private val barcodeListener: BarcodeListener) : ImageAnalysis.Analyzer {


        private val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                .build()

        private val scanner = BarcodeScanning.getClient(options)

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to the scanner and have it do its thing
                scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            // Task completed successfully
                            for (barcode in barcodes) {
                                barcodeListener(barcode.rawValue ?: "")
                            }
                        }
                        .addOnFailureListener {
                            // You should really do something about Exceptions
                        }
                        .addOnCompleteListener {
                            // It's important to close the imageProxy
                            imageProxy.close()
                        }
            }
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }

}
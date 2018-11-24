package com.kotlindev.noam.faceattendance

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.kotlindev.noam.faceattendance.ManageStudentsActivity.Companion.CURRENT_STUDENT_DIR
import com.kotlindev.noam.faceattendance.camera.Camera2Fragment
import com.kotlindev.noam.faceattendance.camera.Camera2Fragment.OnCameraFragmentInteractionListener
import com.kotlindev.noam.faceattendance.operations.BmpOperations
import com.kotlindev.noam.faceattendance.operations.FaceDetector
import kotlinx.android.synthetic.main.activity_image_capture.*
import org.jetbrains.anko.toast
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class ImageCaptureActivity : AppCompatActivity(), OnCameraFragmentInteractionListener,
        OnSuccessListener<Bitmap>, OnFailureListener {


    companion object {
        private const val TAG = "ImageCaptureActivity"
    }

    private lateinit var picFile : File
    private lateinit var studentDir : File
    private var faceInd = 0
    private val shouldThrottle = AtomicBoolean(false)
    private var takingPicturesState = false
    private var numOfPicsTaken = 0
    private var camera2Fragment : Camera2Fragment? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)
        camera2Fragment = Camera2Fragment.newInstance()
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.camera_frag_container, camera2Fragment!!)
                .commit()
        picture_btn.setOnClickListener { startTakingPictures() }
        studentDir = intent.extras.get(CURRENT_STUDENT_DIR) as File
        if (! studentDir.exists() )
            studentDir.mkdir()
        if (!studentDir.listFiles().isEmpty())
            faceInd = studentDir.listFiles().map { it.nameWithoutExtension.toInt()}.max()!! + 1
        picFile = File(studentDir, "$faceInd.jpg")
    }

    private fun startTakingPictures(){
        takingPicturesState = !takingPicturesState
        if (takingPicturesState){
            pics_left.text = "10"
            take10Pictures()
        }else{
            pics_left.text = ""
        }
    }

    private fun take10Pictures(){
        if (numOfPicsTaken >= 10 || !takingPicturesState ){
            picture_btn.text = getString(R.string.picture)
            toast("Successfully took $numOfPicsTaken pictures")
            numOfPicsTaken = 0
            return
        }
        camera2Fragment ?: return
        camera2Fragment!!.captureStillPicture()
        picture_btn.text = getString(R.string.stop_btn)
    }

    override fun onImageAvailable(image: Image, rotationValue: Int) {
        if (shouldThrottle.get()){
            toast("Still working on previous images.")
            image.close()
            return
        }

        shouldThrottle.set(true)
        FaceDetector(image, rotationValue, this, this).start()
    }

    override fun onFailure(p0: Exception) {
        shouldThrottle.set(false)
        toast(p0.message.toString())
        take10Pictures()
    }

    override fun onSuccess(croppedFace: Bitmap) {
        shouldThrottle.set(false)
        if (cropped_face_view.drawable != null)
            (cropped_face_view.drawable as BitmapDrawable).bitmap.recycle()
        cropped_face_view.setImageBitmap(croppedFace)
        BmpOperations.writeBmpToFile(croppedFace, picFile)
        toast("Saved face under:${picFile.absolutePath}")
        picFile = File(studentDir, "${++faceInd}.jpg")
        numOfPicsTaken++
        pics_left.text = "${10-numOfPicsTaken}"
        take10Pictures()
    }
}

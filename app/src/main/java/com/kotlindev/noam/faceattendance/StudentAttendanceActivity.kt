package com.kotlindev.noam.faceattendance

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.kotlindev.noam.faceattendance.camera.Camera2Fragment
import com.kotlindev.noam.faceattendance.camera.Camera2Fragment.OnCameraFragmentInteractionListener
import com.kotlindev.noam.faceattendance.datasets.ClassObj
import com.kotlindev.noam.faceattendance.datasets.StudentSet
import com.kotlindev.noam.faceattendance.operations.BmpOperations
import com.kotlindev.noam.faceattendance.operations.FaceDetector
import com.kotlindev.noam.faceattendance.operations.FisherFaces
import com.kotlindev.noam.faceattendance.operations.OnModelReadyListener
import kotlinx.android.synthetic.main.activity_student_attandence.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean

class StudentAttendanceActivity : AppCompatActivity(), OnCameraFragmentInteractionListener,
    OnModelReadyListener, OnSuccessListener<Bitmap>, OnFailureListener {

    private val modelReady = AtomicBoolean(false)
    private val shouldThrottle = AtomicBoolean(false)
    private val arrivedStudents = ArrayList<StudentSet>()
    private lateinit var classObj: ClassObj
    private lateinit var fisherFaces: FisherFaces


    override fun onImageAvailable(image: Image, rotationValue: Int) {
        if (shouldThrottle.get()){
            toast("Still processing previous image.")
            image.close()
            return
        }
        shouldThrottle.set(true)
        Log.d(TAG, "OnImageAvailable!.")
        FaceDetector(image, rotationValue, this, this).start()
    }

    override fun onCreate(savedInstanceState: Bundle?) { savedInstanceState
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attandence)
        val cameraFragment = Camera2Fragment.newInstance()
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.camera_frag_container, cameraFragment)
                .commit()

        classObj = intent.extras.getSerializable(SelectClassActivity.CLASS_OBJ_TAG) as ClassObj
        finish_btn.setOnClickListener { finishSigning() }
        fix_btn.setOnClickListener { fixLastAttendance() }
        sign_btn.setOnClickListener { cameraFragment.captureStillPicture() }
        sign_btn.isClickable = false
        fisherFaces = FisherFaces(classObj.studentList, this)
        doAsync {
            fisherFaces.readAllStudentsFaces()
            fisherFaces.trainModel()
        }
    }


    override fun onModelReady() {
        modelReady.set(true)
        runOnUiThread {
            val readyString  = arrayOf("Model Ready", "Model Ready")
            loading_ftv.setTexts(readyString)
            doAsync {
                sleep(2000)
                runOnUiThread {
                    loading_ftv.stop()
                    loading_ftv.visibility = View.GONE
                }
            }
            sign_btn.isClickable = true
        }
    }

    private fun finishSigning() {
        val arrivedStudentsIntent = Intent(this, ArrivedStudentsActivity::class.java)
        arrivedStudentsIntent.putExtra(ARRIVED_STUDENTS_LIST, arrivedStudents)
        startActivity(arrivedStudentsIntent)
    }

    private fun fixLastAttendance() {
        arrivedStudents.removeAt(arrivedStudents.lastIndex)
        fix_btn.visibility = View.GONE
    }

    override fun onFailure(p0: Exception) {
        Log.d(TAG, "Failed to detect Face.")
        toast(p0.message.toString())
        shouldThrottle.set(false)
    }

    override fun onSuccess(bmp: Bitmap) {
        shouldThrottle.set(false)
        if (!modelReady.get()) {
            toast("Recognition model not ready yet!")
            return
        }
        Log.d(TAG, "Detected Face successfully.")
        val tmpImgFile = BmpOperations.writeBmpToTmpFile(bmp, this)
        bmp.recycle()
        val studentId = fisherFaces.predictImage(tmpImgFile.absolutePath)
        if (studentId == -1) {
            onFailure(java.lang.Exception("I'm not sure, please try again."))
            return
        }
        val student = classObj.studentList.single { it.id == studentId }
        predicted_student_id.text = student.id.toString()
        predicted_student_name.text = student.name
        if (!arrivedStudents.contains(student)) {
            arrivedStudents.add(student)
            if (fix_btn.visibility == View.GONE) {
                fix_btn.visibility = View.VISIBLE
                doAsync {
                    Thread.sleep(4000)
                    runOnUiThread {
                        fix_btn.visibility = View.GONE
                    }
                }
            }
        } else {
            toast("${student.name} is Already Registered :)")
            fix_btn.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "StudentAttnActivity"
        const val ARRIVED_STUDENTS_LIST = "arrived_student_list"
    }
}

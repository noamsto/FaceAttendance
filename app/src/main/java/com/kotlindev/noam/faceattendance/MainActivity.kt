package com.kotlindev.noam.faceattendance

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val APP_DIR_NAME = "Face Attendance"
    }
    private var rootDir : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manage_students_btn.setOnClickListener { startManageStudentsActivity() }
        pick_class_btn.setOnClickListener { pickClass() }
    }

    private fun startManageStudentsActivity() = runWithPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)  {
        val manageStudentsIntent = Intent(this, ManageStudentsActivity::class.java)
        if (rootDir == null)
            initAppDir()
        manageStudentsIntent.putExtra(APP_DIR_NAME, rootDir)
        startActivity(manageStudentsIntent)
    }

    private fun pickClass() = runWithPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        val selectClassIntent = Intent(this, SelectClassActivity::class.java)
        if (rootDir == null)
            initAppDir()
        selectClassIntent.putExtra(APP_DIR_NAME, rootDir)
        startActivity(selectClassIntent)

    }

    private fun initAppDir() = runWithPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)  {
        // Get the directory for the user's public pictures directory.
        rootDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), APP_DIR_NAME
        )
        if (!rootDir!!.mkdir()) {
            Log.d(TAG, "App root dir exists: ${rootDir!!.path}")
        }else{
            Log.d(TAG, "App root dir created: ${rootDir!!.path}")
        }
    }
}



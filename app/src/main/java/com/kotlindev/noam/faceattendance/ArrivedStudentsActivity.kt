package com.kotlindev.noam.faceattendance

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import com.kotlindev.noam.faceattendance.StudentAttendanceActivity.Companion.ARRIVED_STUDENTS_LIST
import com.kotlindev.noam.faceattendance.adapters.StudentsViewAdapter
import com.kotlindev.noam.faceattendance.datasets.StudentSet
import kotlinx.android.synthetic.main.activity_arrived_students.*


@Suppress("UNCHECKED_CAST")
class ArrivedStudentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrived_students)
        val arrivedStudents = intent.getSerializableExtra(ARRIVED_STUDENTS_LIST) as ArrayList<StudentSet>
        val adapter = StudentsViewAdapter(this, arrivedStudents)
        arrived_student_list_view.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
        super.onBackPressed()
    }
}

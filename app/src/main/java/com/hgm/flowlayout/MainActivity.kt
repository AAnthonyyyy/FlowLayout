package com.hgm.flowlayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.hgm.flowlayout.widget.FlowLayout

class MainActivity : AppCompatActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            val flowLayout = findViewById<FlowLayout>(R.id.flow_layout)

            //准备模拟数据
            val data = arrayListOf(
                  "Android",
                  "Kotlin",
                  "OOM",
                  "Compose",
                  "组件化",
                  "MVVM-MVI-MVP-MVC-MVVM-MVI-MVP-MVVM-MVI-MVP-",
                  "模块化",
                  "Kotlin",
                  "Jetpack",
                  "MVI架构模式",
                  "大厂面试指南",
            )

            flowLayout.setData(data)

            flowLayout.setOnItemClickListener(object :FlowLayout.OnItemClickListener{
                  override fun onItemClick(v: View, text: String) {
                        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
                  }

            })
      }
}
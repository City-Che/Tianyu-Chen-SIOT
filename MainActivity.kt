package com.example.siot

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var lineChartTemperature: LineChart
    private lateinit var lineChartHeartRate: LineChart
    private lateinit var lineChartHRV: LineChart
    private lateinit var lineChartTindexSleepIndex: LineChart

    private lateinit var deepSleepTimeTindexText: TextView
    private lateinit var lightSleepTimeTindexText: TextView
    private lateinit var deepSleepTimeSleepIndexText: TextView
    private lateinit var lightSleepTimeSleepIndexText: TextView
    private lateinit var targetSleepTimeTindexText: TextView
    private lateinit var targetSleepTimeSleepIndexText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化图表和文本框
        lineChartTemperature = findViewById(R.id.lineChartTemperature)
        lineChartHeartRate = findViewById(R.id.lineChartHeartRate)
        lineChartHRV = findViewById(R.id.lineChartHRV)
        lineChartTindexSleepIndex = findViewById(R.id.lineChartTindexSleepIndex)

        deepSleepTimeTindexText = findViewById(R.id.deepSleepTimeTindex)
        lightSleepTimeTindexText = findViewById(R.id.lightSleepTimeTindex)
        deepSleepTimeSleepIndexText = findViewById(R.id.deepSleepTimeSleepIndex)
        lightSleepTimeSleepIndexText = findViewById(R.id.lightSleepTimeSleepIndex)
        targetSleepTimeTindexText = findViewById(R.id.targetSleepTimeTindex)
        targetSleepTimeSleepIndexText = findViewById(R.id.targetSleepTimeSleepIndex)

        // 获取数据并显示图表
        fetchDataAndDisplayCharts()
    }

    private fun fetchDataAndDisplayCharts() {
        val db = FirebaseFirestore.getInstance()

        db.collection("sensor_data")
            .limit(200) // 获取最近的 200 条数据
            .get()
            .addOnSuccessListener { documents ->
                val temperatureEntries = mutableListOf<Entry>()
                val heartRateEntries = mutableListOf<Entry>()
                val hrvEntries = mutableListOf<Entry>()
                val tindexEntries = mutableListOf<Entry>()
                val sleepIndexEntries = mutableListOf<Entry>()

                var index = 0f
                var deepSleepTimeTindex = 0f
                var lightSleepTimeTindex = 0f
                var deepSleepTimeSleepIndex = 0f
                var lightSleepTimeSleepIndex = 0f

                for (document in documents) {
                    val temperature = document.getDouble("Temperature")?.toFloat() ?: 0f
                    val heartRate = document.getDouble("HeartRate")?.toFloat() ?: 0f
                    val hrv = document.getDouble("HRV")?.toFloat() ?: 0f
                    val tindex = document.getDouble("Tindex")?.toInt() ?: 0
                    val sleepIndex = document.getDouble("SleepIndex")?.toInt() ?: 0

                    temperatureEntries.add(Entry(index, temperature))
                    heartRateEntries.add(Entry(index, heartRate))
                    hrvEntries.add(Entry(index, hrv))
                    tindexEntries.add(Entry(index, tindex.toFloat()))
                    sleepIndexEntries.add(Entry(index, sleepIndex.toFloat()))

                    // 计算深睡和浅睡时间
                    if (tindex == 1) deepSleepTimeTindex++ else lightSleepTimeTindex++
                    if (sleepIndex == 1) deepSleepTimeSleepIndex++ else lightSleepTimeSleepIndex++

                    index++
                }

                // 分别显示图表
                setupChart(lineChartTemperature, temperatureEntries, "Temperature")
                setupChart(lineChartHeartRate, heartRateEntries, "Heart Rate")
                setupChart(lineChartHRV, hrvEntries, "HRV")
                setupDualChart(lineChartTindexSleepIndex, tindexEntries, sleepIndexEntries, "Tindex", "SleepIndex")

                // 计算并显示深浅睡眠时间
                deepSleepTimeTindexText.text = "Deep Sleep (Tindex): %.1f hours".format(deepSleepTimeTindex * 0.04f)
                lightSleepTimeTindexText.text = "Light Sleep (Tindex): %.1f hours".format(lightSleepTimeTindex * 0.04f)
                deepSleepTimeSleepIndexText.text = "Deep Sleep (SleepIndex): %.1f hours".format(deepSleepTimeSleepIndex * 0.04f)
                lightSleepTimeSleepIndexText.text = "Light Sleep (SleepIndex): %.1f hours".format(lightSleepTimeSleepIndex * 0.04f)

                // 计算并显示 Target Sleep Time
                val targetSleepTimeTindex = calculateTargetSleepTime(deepSleepTimeTindex, lightSleepTimeTindex)
                val targetSleepTimeSleepIndex = calculateTargetSleepTime(deepSleepTimeSleepIndex, lightSleepTimeSleepIndex)

                targetSleepTimeTindexText.text = "Target Sleep Time (Tindex): %.1f hours".format(targetSleepTimeTindex)
                targetSleepTimeSleepIndexText.text = "Target Sleep Time (SleepIndex): %.1f hours".format(targetSleepTimeSleepIndex)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error fetching data", e)
            }
    }

    private fun setupChart(chart: LineChart, entries: List<Entry>, label: String) {
        val dataSet = LineDataSet(entries, label)
        dataSet.color = resources.getColor(R.color.teal_200, null)
        dataSet.valueTextColor = resources.getColor(R.color.black, null)

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate() // 刷新图表
    }

    private fun setupDualChart(
        chart: LineChart,
        entries1: List<Entry>,
        entries2: List<Entry>,
        label1: String,
        label2: String
    ) {
        val dataSet1 = LineDataSet(entries1, label1)
        val dataSet2 = LineDataSet(entries2, label2)

        dataSet1.color = resources.getColor(R.color.teal_200, null)
        dataSet1.valueTextColor = resources.getColor(R.color.black, null)

        dataSet2.color = resources.getColor(R.color.purple_200, null)
        dataSet2.valueTextColor = resources.getColor(R.color.black, null)

        val lineData = LineData(dataSet1, dataSet2)
        chart.data = lineData
        chart.invalidate()
    }

    private fun calculateTargetSleepTime(deepSleepTime: Float, lightSleepTime: Float): Float {
        val alpha = 0.3f
        val beta = 0.2f
        val gamma = 0.5f
        val delta = 0.3f
        val activityLevel = 3f
        val errorTerm = 2f

        return alpha * (deepSleepTime * 0.04f) +
                beta * (lightSleepTime * 0.04f) +
                gamma * ((deepSleepTime + lightSleepTime) * 0.04f) +
                delta * activityLevel +
                errorTerm
    }
}

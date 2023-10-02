package com.android.walletscan.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.walletscan.R
import com.android.walletscan.databinding.FragmentVisualBinding
import com.android.walletscan.documents.WalletHistInfo
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.DateAndTime.DAY_MONTH_YEAR_FORMAT
import com.android.walletscan.supporting.DateAndTime.MONTH_YEAR_FORMAT
import com.android.walletscan.supporting.DateAndTime.YEAR_FORMAT
import com.android.walletscan.supporting.DateAndTime.pastSevenDates
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.FirebaseUtil
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.ktx.toObject
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit


class VisualFragment : Fragment() {
    lateinit var binding: FragmentVisualBinding
    lateinit var con: Activity
    lateinit var walletHistInfoList: MutableList<WalletHistInfo>
    lateinit var walletInfo: WalletInfo
    lateinit var txtNoRecords: TextView
    lateinit var barChart: BarChart
    lateinit var noDataAnim: LottieAnimationView
    lateinit var noInternetAnim: LottieAnimationView
    lateinit var loadingAnim: LottieAnimationView
    lateinit var txtDateRange: TextView
    lateinit var imgDateRange: ImageView
    lateinit var viewLine: View
    val graphAddDaysData = hashMapOf<String, Double>()
    val graphDeductDaysData = hashMapOf<String, Double>()
    val graphAddMonthsData = hashMapOf<String, Double>()
    val graphDeductMonthsData = hashMapOf<String, Double>()
    val graphAddYearsData = hashMapOf<String, Double>()
    val graphDeductYearsData = hashMapOf<String, Double>()

    var dateComparator = Comparator<WalletHistInfo> { one, two ->
        two.transactionDoneOn!!.compareTo(one.transactionDoneOn)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisualBinding.inflate(inflater, container, false)
        txtNoRecords = binding.txtNoRecords
        noDataAnim = binding.noDataAnim
        loadingAnim = binding.loadingAnim
        noInternetAnim = binding.noInternetAnim
        txtDateRange = binding.txtDateRange
        imgDateRange = binding.imgDateRange
        viewLine = binding.view
        imgDateRange.setOnClickListener {
            val dateRangePicker =MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select a date range")
                .build()
            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = Date(selection.first)
                val endDate = Date(selection.second)
                showBarChart(startDate, endDate)
            }
            dateRangePicker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        }
        barChart = binding.barChart
        con = activity as Activity
        initData()
        return binding.root
    }

    private fun initData() {
        walletInfo = arguments?.getSerializable(WalletScanConstants.INTENT_PARAM_WALLET_INFO)!! as WalletInfo
        retrieveHistory(walletInfo.id)
    }

    private fun retrieveHistory(walletId: String) {
        if (ConnectionManager.isNetworkAvailable(con)) {
            setVisible(1)
            FirebaseUtil.getWalletsHistReference().whereEqualTo("walletId", walletId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.size() != 0) {
                        walletHistInfoList = mutableListOf()
                        snapshot.documents.forEach {
                            if (it.exists()) {
                                val walletInfo = it.toObject<WalletHistInfo>()!!
                                walletHistInfoList.add(walletInfo)
                            }
                        }
                        clearGraphData()
                        Collections.sort(walletHistInfoList, dateComparator)
                        setGraphData()
                        showBarChart(pastSevenDates[0], pastSevenDates[6])
                        setVisible(2)
                    } else
                        setVisible(3)
                }
        } else
            setVisible(4)
    }

    private fun clearGraphData() {
        graphAddDaysData.clear()
        graphDeductDaysData.clear()
        graphAddMonthsData.clear()
        graphDeductMonthsData.clear()
        graphAddYearsData.clear()
        graphDeductYearsData.clear()
    }

    private fun showBarChart(startDate: Date, endDate: Date) {
        txtDateRange.text = "${DAY_MONTH_YEAR_FORMAT.format(startDate)} - ${DAY_MONTH_YEAR_FORMAT.format(endDate)}"
        val days = TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time)+1
        if (days<=31){
            showDaysGraph(startDate, endDate)
        }
        else{
            val startLocal = Instant.ofEpochMilli(startDate.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val endLocal = Instant.ofEpochMilli(endDate.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val y1 = startLocal.year
            val y2 = endLocal.year
            if (y1==y2){
                showMonthsGraph( y1)
            }
            else{
                showYearsGraph(y1, y2)
            }
        }
    }

    private fun showMonthsGraph(year: Int) {
        val allMonths = mutableListOf("Jan $year", "Feb $year", "Mar $year", "Apr $year", "May $year",
            "Jun $year", "Jul $year", "Aug $year", "Sept $year", "Oct $year", "Nov $year", "Dec $year")
        val barDataAdd = BarDataSet(getBarEntriesAddByMonths(allMonths), "Amount added")
        barDataAdd.color = ContextCompat.getColor(con, R.color.green)
        val barDataDeduct = BarDataSet(getBarEntriesDeductByMonths(allMonths), "Amount deducted")
        barDataDeduct.color = ContextCompat.getColor(con, R.color.red)
        val data = BarData(barDataAdd, barDataDeduct)
        setBarChartProps(data, allMonths)
        barChart.invalidate()
    }

    private fun getBarEntriesAddByYears(allYears: MutableList<String>):
            ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        var xValue = 0.325f
        allYears.forEach {
            var yValue = 0f
            if (graphAddYearsData[it] != null) {
                yValue = graphAddYearsData[it]!!.toFloat()
            }
            barEntries.add(BarEntry(xValue++, yValue))
        }
        return barEntries
    }

    // array list for second set.
    private fun getBarEntriesDeductByYears(allYears: MutableList<String>):
            ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        var xValue = 0.525f
        allYears.forEach {
            var yValue = 0f
            if (graphDeductYearsData[it] != null) {
                yValue = graphDeductYearsData[it]!!.toFloat()
            }
            barEntries.add(BarEntry(xValue++, yValue))
        }
        return barEntries
    }

    private fun getBarEntriesAddByMonths(allMonths: MutableList<String>):
            ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        var xValue = 0.325f
        allMonths.forEach {
            var yValue = 0f
            if (graphAddMonthsData[it] != null) {
                yValue = graphAddMonthsData[it]!!.toFloat()
            }
            barEntries.add(BarEntry(xValue++, yValue))
        }
        return barEntries
    }

    // array list for second set.
    private fun getBarEntriesDeductByMonths(allMonths: MutableList<String>):
            ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        var xValue = 0.525f
        allMonths.forEach {
            var yValue = 0f
            if (graphDeductMonthsData[it] != null) {
                yValue = graphDeductMonthsData[it]!!.toFloat()
            }
            barEntries.add(BarEntry(xValue++, yValue))
        }
        return barEntries
    }

    private fun showYearsGraph(startYear: Int, endYear:Int) {
        val allYears = (startYear until endYear+1).map { it.toString() }.toMutableList()
        val barDataAdd = BarDataSet(getBarEntriesAddByYears(allYears), "Amount added")
        barDataAdd.color = ContextCompat.getColor(con, R.color.green)
        val barDataDeduct = BarDataSet(getBarEntriesDeductByYears(allYears), "Amount deducted")
        barDataDeduct.color = ContextCompat.getColor(con, R.color.red)
        val data = BarData(barDataAdd, barDataDeduct)
        setBarChartProps(data, allYears)
        barChart.invalidate()
    }

    private fun showDaysGraph(startDate: Date, endDate: Date) {
        val allDays = mutableListOf<String>()
        var date = Date(startDate.time)
        while (date.before(endDate) || date == endDate){
            allDays.add(DAY_MONTH_YEAR_FORMAT.format(date))
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, 1)
            date = cal.time
        }
        val barDataAdd = BarDataSet(getBarEntriesAddByDays(allDays), "Amount added")
        barDataAdd.color = ContextCompat.getColor(con, R.color.green)
        val barDataDeduct = BarDataSet(getBarEntriesDeductByDays(allDays), "Amount deducted")
        barDataDeduct.color = ContextCompat.getColor(con, R.color.red)
        val data = BarData(barDataAdd, barDataDeduct)
        setBarChartProps(data, allDays)
        barChart.invalidate()
    }

    private fun setBarChartProps(data: BarData, xValues: MutableList<String>) {
        barChart.data = data
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(xValues)
        xAxis.setCenterAxisLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        barChart.isDragEnabled = true
        barChart.setVisibleXRangeMaximum(4f)
        data.barWidth = 0.15f
        barChart.xAxis.axisMinimum = 0f
        barChart.animate()
    }

    private fun getBarEntriesAddByDays(allDays: MutableList<String>): ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        var xValue = 0.325f
        allDays.forEach {
            var yValue = 0f
            if (graphAddDaysData[it] != null) {
                yValue = graphAddDaysData[it]!!.toFloat()
            }
            barEntries.add(BarEntry(xValue++, yValue))
        }
        return barEntries
    }

    // array list for second set.
    private fun getBarEntriesDeductByDays(allDays: MutableList<String>): ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        var xValue = 0.525f
        allDays.forEach {
            var yValue = 0f
            if (graphDeductDaysData[it] != null) {
                yValue = graphDeductDaysData[it]!!.toFloat()
            }
            barEntries.add(BarEntry(xValue++, yValue))
        }
        return barEntries
    }

    private fun setGraphData() {
        walletHistInfoList.forEach { walletHistInfo ->
            val dateObj = walletHistInfo.transactionDoneOn!!
            val dayMonYear = DAY_MONTH_YEAR_FORMAT.format(dateObj)
            val monYear = MONTH_YEAR_FORMAT.format(dateObj)
            val year = YEAR_FORMAT.format(dateObj)
            val transType = walletHistInfo.transactionType
            if (transType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
                graphAddDaysData[dayMonYear] =
                    graphAddDaysData.getOrDefault(dayMonYear, 0.00) + walletHistInfo.amtAdded!!
                graphAddMonthsData[monYear] =
                    graphAddMonthsData.getOrDefault(monYear, 0.00) + walletHistInfo.amtAdded!!
                graphAddYearsData[year] =
                    graphAddYearsData.getOrDefault(year, 0.00) + walletHistInfo.amtAdded!!
            } else if (transType == WalletScanConstants.TRANSACTION_TYPE_DEDUCT) {
                graphDeductDaysData[dayMonYear] =
                    graphDeductDaysData.getOrDefault(dayMonYear, 0.00) + walletHistInfo.amtDeducted!!
                graphDeductMonthsData[monYear] =
                    graphDeductMonthsData.getOrDefault(monYear, 0.00) + walletHistInfo.amtDeducted!!
                graphDeductYearsData[year] =
                    graphDeductYearsData.getOrDefault(year, 0.00) + walletHistInfo.amtDeducted!!
            }
        }
        Log.i("graphAddData ", "$graphAddDaysData")
        Log.i("graphAddMonthsData ", "$graphAddMonthsData")
        Log.i("graphAddYearsData ", "$graphAddYearsData")
        Log.i("graphDeductData ", "$graphDeductDaysData")
        Log.i("graphDeductMonthsData ", "$graphDeductMonthsData")
        Log.i("graphDeductYearsData ", "$graphDeductYearsData")
    }

    private fun setVisible(case: Int) {
        when (case) {
            1 -> {
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                loadingAnim.visibility = View.VISIBLE
                txtNoRecords.visibility = View.GONE
                barChart.visibility = View.GONE
                txtDateRange.visibility = View.GONE
                imgDateRange.visibility = View.GONE
                viewLine.visibility= View.GONE
            }

            2 -> {
                loadingAnim.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                txtNoRecords.visibility = View.GONE
                barChart.visibility = View.VISIBLE
                txtDateRange.visibility = View.VISIBLE
                imgDateRange.visibility = View.VISIBLE
                viewLine.visibility= View.VISIBLE
            }

            3 -> {
                loadingAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                noDataAnim.visibility = View.VISIBLE
                txtNoRecords.visibility = View.VISIBLE
                barChart.visibility = View.GONE
                txtDateRange.visibility = View.GONE
                imgDateRange.visibility = View.GONE
                viewLine.visibility= View.GONE
            }

            4 -> {
                loadingAnim.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.VISIBLE
                txtNoRecords.visibility = View.GONE
                barChart.visibility = View.GONE
                txtDateRange.visibility = View.GONE
                imgDateRange.visibility = View.GONE
                viewLine.visibility= View.GONE
            }
        }
    }
}
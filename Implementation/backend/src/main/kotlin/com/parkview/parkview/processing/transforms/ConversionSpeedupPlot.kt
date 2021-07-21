package com.parkview.parkview.processing.transforms

import com.parkview.parkview.benchmark.ConversionBenchmarkResult
import com.parkview.parkview.git.BenchmarkResult
import com.parkview.parkview.processing.PlotOption
import com.parkview.parkview.processing.PlotType

class ConversionSpeedupPlot : ConversionPlotTransform {
    override val numInputsRange = 2..2
    override val plottableAs = listOf(PlotType.Line)
    override val name = "conversionSpeedup"
    override fun getAvailableOptions(results: List<BenchmarkResult>): List<PlotOption> = listOf(
        PlotOption(
            name = "xAxis",
            options = listOf("nonzeros")
        )
    )

    override fun transformConversion(
        benchmarkResults: List<ConversionBenchmarkResult>,
        xAxis: Map<String, String>
    ): PlottableData {
        val seriesByName: MutableMap<String, MutableList<PlotPoint>> = mutableMapOf()

        val datapointsA = benchmarkResults[0].datapoints
        val datapointsB = benchmarkResults[1].datapoints

        for (datapointA in datapointsA) {
            val datapointB = datapointsB.find {
                (it.nonzeros == datapointA.nonzeros) and
                        (it.rows == datapointA.rows) and
                        (it.columns == datapointA.columns)
            } ?: continue

            for (conversionA in datapointA.conversions) {
                val conversionB = datapointB.conversions.find { it.name == conversionA.name } ?: continue
                if (!conversionA.completed or !conversionB.completed) continue

                seriesByName.getOrPut(conversionA.name) { mutableListOf() } += PlotPoint(
                    x = datapointA.nonzeros.toDouble(),
                    y = conversionA.time / conversionB.time
                )
            }
        }


        return DatasetSeries(
            seriesByName.map { (key, value) -> Dataset(label = key, data = value.sortedBy { it.x }) }
        )
    }
}
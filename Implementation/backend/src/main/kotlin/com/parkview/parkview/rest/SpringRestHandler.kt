package com.parkview.parkview.rest

import com.google.gson.Gson
import com.parkview.parkview.benchmark.JsonParser
import com.parkview.parkview.benchmark.SpmvBenchmarkResult
import com.parkview.parkview.database.DatabaseHandler
import com.parkview.parkview.database.ExposedHandler
import com.parkview.parkview.git.*
import com.parkview.parkview.processing.AvailablePlots
import com.parkview.parkview.processing.transforms.SpmvSingleScatterPlot
import com.parkview.parkview.processing.transforms.SpmvSingleScatterPlotYAxis
import com.parkview.parkview.processing.transforms.SpmvSpeedupPlot
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Class that implements a RestHandler using the Spring framework
 */
@RestController
class SpringRestHandler : RestHandler {
    private val repHandler = CachingRepositoryHandler(GitApiHandler("ginkgo", "ginkgo-project"))

//    private val url = "jdbc:postgresql://localhost:5432/parkview"
//    private val url = "jdbc:postgresql://parkview-postgres:5432/parkview"

    private val databaseHandler: DatabaseHandler = ExposedHandler.withConnectionPool()

    @PostMapping("/post")
    override fun handlePost(
        @RequestParam sha: String,
        @RequestParam device: String,
        @RequestParam benchmark: String,
        @RequestParam(defaultValue = "false") blas: Boolean,
        @RequestBody json: String,
    ) {
        val benchmarkResults = JsonParser.benchmarkResultsFromJson(sha, benchmark, device, json, blas = blas)

        databaseHandler.insertBenchmarkResults(benchmarkResults)
    }

    @GetMapping("/history")
    override fun handleGetHistory(
        @RequestParam branch: String,
        @RequestParam page: Int,
        @RequestParam benchmark: String,
    ): String {

        val commits = repHandler.fetchGitHistory(branch, page)
        val benchmarkInfo = Benchmark(benchmark, databaseHandler.getBenchmarkTypeForName(benchmark))

        for (commit in commits) {
            val devices = databaseHandler.getAvailableDevices(
                commit,
                benchmark = benchmarkInfo,
            )
            if (commit.device.isEmpty()) {
                for (device in devices) {
                    if (databaseHandler.hasDataAvailable(
                            commit,
                            device,
                            benchmarkInfo,
                        )
                    ) commit.addDevice(device)
                }
            }
        }

        return Gson().toJson(commits)
    }


    @GetMapping("/plot")
    override fun handleGetPlotData(
        @RequestParam benchmark: String,
        @RequestParam shas: List<String>,
        @RequestParam devices: List<String>,
        @RequestParam plotType: String,
    ): String {
        // dummy implementation
        val results: List<BenchmarkResult> = shas.zip(devices).map { (sha, device) ->
            databaseHandler.fetchBenchmarkResult(
                Commit(sha, "", Date(), ""),
                Device(device),
                Benchmark(benchmark, databaseHandler.getBenchmarkTypeForName(benchmark)),
//                nonzerosLim = 5000000,
            )
        }

        return when (plotType) {
            "spmvSingleScatter" -> SpmvSingleScatterPlot(SpmvSingleScatterPlotYAxis.Time).transform(results as List<SpmvBenchmarkResult>)
                .toJson()
            "spmvSpeedup" -> SpmvSpeedupPlot().transform(results as List<SpmvBenchmarkResult>).toJson()
            else -> ""
        }
    }

    @GetMapping("/branches")
    override fun getAvailableBranches(): String {
        val branches = repHandler.getAvailableBranches()
        val gson = Gson()
        return gson.toJson(branches)
    }

    @GetMapping("/benchmarks")
    override fun getAvailableBenchmarks(): String {
        val benchmarks = databaseHandler.getAvailableBenchmarks()
        return Gson().toJson(benchmarks)
    }

    @GetMapping("/availablePlots")
    override fun getAvailablePlots(
        @RequestParam benchmark: String,
        @RequestParam shas: List<String>,
        @RequestParam devices: List<String>,
    ): String = Gson().toJson(
        AvailablePlots.getPlotList(
            Benchmark(
                benchmark,
                databaseHandler.getBenchmarkTypeForName(benchmark)
            ), shas.size
        )
    )
}
import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ChartOptions, ChartType} from "chart.js";

@Component({
  selector: 'app-average-performance',
  templateUrl: './average-performance.component.html',
  styleUrls: ['./average-performance.component.scss']
})
export class AveragePerformanceComponent implements OnInit {

  public chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    legend: {display: false},
  };

  public chartData = Array();
  public chartType: ChartType = 'line';
  public chartLabels = Array();

  constructor() {
  }

  update() {
    this.chartData = data;
    this.chartType = 'line';
    this.chartLabels = labels;
  }

  ngOnInit() {
    this.update();
  }
}

const data = [
  {
    label: 'poggers',
    data: [11, 15, 73, 21, 3, 29]
  }
];

const labels = ['1', '2', '3', '4', '5', '6'];

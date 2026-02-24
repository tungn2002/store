import React, { useState } from "react";
import PageTitle from "../components/Typography/PageTitle";
import ChartCard from "../components/Chart/ChartCard";
import { Line } from "react-chartjs-2";
import ChartLegend from "../components/Chart/ChartLegend";
import { Input, Label, Button } from "@windmill/react-ui";

// Sample revenue data generator
function generateRevenueData(fromDate, toDate) {
  const start = new Date(fromDate);
  const end = new Date(toDate);
  const labels = [];
  const data = [];

  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    labels.push(d.toLocaleDateString("en-US", { month: "short", day: "numeric" }));
    data.push(Math.floor(Math.random() * 5000) + 1000);
  }

  return { labels, data };
}

function Reports() {
  const today = new Date().toISOString().split("T")[0];
  const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    .toISOString()
    .split("T")[0];

  const [fromDate, setFromDate] = useState(thirtyDaysAgo);
  const [toDate, setToDate] = useState(today);
  const [chartData, setChartData] = useState(generateRevenueData(thirtyDaysAgo, today));

  const lineChartData = {
    labels: chartData.labels,
    datasets: [
      {
        label: "Revenue",
        backgroundColor: "#0694a2",
        borderColor: "#0694a2",
        data: chartData.data,
        fill: true,
      },
    ],
  };

  const lineChartOptions = {
    responsive: true,
    tooltips: {
      mode: "index",
      intersect: false,
    },
    hover: {
      mode: "nearest",
      intersect: true,
    },
    scales: {
      x: {
        display: true,
        scaleLabel: {
          display: true,
          labelString: "Date",
        },
      },
      y: {
        display: true,
        scaleLabel: {
          display: true,
          labelString: "Revenue ($)",
        },
      },
    },
  };

  const lineLegends = [{ title: "Revenue", color: "bg-teal-600" }];

  function handleGenerateReport() {
    const data = generateRevenueData(fromDate, toDate);
    setChartData(data);
  }

  return (
    <div>
      <PageTitle>Revenue Report</PageTitle>

      {/* Date Filter */}
      <div className="mb-6 p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
        <div className="grid gap-4 md:grid-cols-3 items-end">
          <div>
            <Label>From Date</Label>
            <Input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>
          <div>
            <Label>To Date</Label>
            <Input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>
          <div>
            <Button block onClick={handleGenerateReport}>
              Generate Report
            </Button>
          </div>
        </div>
      </div>

      {/* Revenue Chart */}
      <ChartCard title="Revenue Overview">
        <Line data={lineChartData} options={lineChartOptions} />
        <ChartLegend legends={lineLegends} />
      </ChartCard>

      {/* Summary Cards */}
      <div className="grid gap-6 mt-8 md:grid-cols-3">
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <p className="text-sm text-gray-600 dark:text-gray-400">Total Revenue</p>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            ${chartData.data.reduce((a, b) => a + b, 0).toLocaleString()}
          </p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <p className="text-sm text-gray-600 dark:text-gray-400">Average Daily Revenue</p>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            ${Math.round(chartData.data.reduce((a, b) => a + b, 0) / chartData.data.length).toLocaleString()}
          </p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <p className="text-sm text-gray-600 dark:text-gray-400">Highest Day</p>
          <p className="text-2xl font-bold text-gray-800 dark:text-gray-200">
            ${Math.max(...chartData.data).toLocaleString()}
          </p>
        </div>
      </div>
    </div>
  );
}

export default Reports;

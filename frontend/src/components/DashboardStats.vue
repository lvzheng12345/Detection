<template>
  <div class="stats-container">
    <!-- Summary Numbers Row -->
    <div class="summary-row">
        <div class="summary-item">
            <div class="label">总检测数</div>
            <div class="value">{{ stats.total }}</div>
            <div class="sub-label">API 请求次数</div>
        </div>
        <div class="summary-item">
            <div class="label">安全内容</div>
            <div class="value success">{{ stats.safeCount }}</div>
            <div class="sub-label">合规文本</div>
        </div>
        <div class="summary-item">
            <div class="label">违规内容</div>
            <div class="value danger">{{ stats.unsafeCount }}</div>
            <div class="sub-label">需人工复核</div>
        </div>
        <div class="summary-item">
            <div class="label">安全率</div>
            <div class="value">{{ stats.safeRate.toFixed(1) }}%</div>
            <div class="sub-label">整体合规度</div>
        </div>
    </div>

    <el-divider style="margin: 24px 0;" />

    <!-- Charts Row -->
    <div class="charts-row">
      <div class="chart-wrapper">
          <div class="chart-title">违规类型分布</div>
          <div ref="pieChartRef" style="height: 300px; width: 100%;"></div>
      </div>
      <div class="chart-wrapper">
          <div class="chart-title">检测量统计</div>
          <div ref="barChartRef" style="height: 300px; width: 100%;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const props = defineProps({
  userId: {
    type: Number,
    default: null
  }
})

const stats = ref({
  total: 0,
  safeCount: 0,
  unsafeCount: 0,
  safeRate: 0,
  categoryStats: {}
})

const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChartInstance = null
let barChartInstance = null

const fetchStats = async () => {
  try {
    let url = '/api/audit/stats'
    if (props.userId) {
      url += `?userId=${props.userId}`
    }
    const response = await axios.get(url)
    stats.value = response.data
    updateCharts()
  } catch (error) {
    console.error(error)
  }
}

const updateCharts = () => {
  if (pieChartRef.value) {
    if (!pieChartInstance) pieChartInstance = echarts.init(pieChartRef.value)
    
    const pieData = Object.entries(stats.value.categoryStats).map(([name, value]) => ({
      name,
      value
    }))

    pieChartInstance.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '违规类型',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: pieData
        }
      ]
    })
  }

  if (barChartRef.value) {
    if (!barChartInstance) barChartInstance = echarts.init(barChartRef.value)
    
    barChartInstance.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: ['总计', '安全', '违规'],
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value'
        }
      ],
      series: [
        {
          name: '数量',
          type: 'bar',
          barWidth: '40%',
          data: [stats.value.total, stats.value.safeCount, stats.value.unsafeCount],
          itemStyle: {
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
          }
        }
      ]
    })
  }
}

watch(() => props.userId, () => {
  fetchStats()
})

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', () => {
    pieChartInstance?.resize()
    barChartInstance?.resize()
  })
})

defineExpose({ fetchStats })
</script>

<style scoped>
.stats-container {
  width: 100%;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0;
}

.summary-item {
  flex: 1;
  padding: 0 20px;
  border-right: 1px solid var(--el-border-color-light);
}

.summary-item:last-child {
  border-right: none;
}

.label {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}

.value {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.value.success {
  color: var(--el-color-success);
}

.value.danger {
  color: var(--el-color-danger);
}

.sub-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.charts-row {
  display: flex;
  gap: 24px;
}

.chart-wrapper {
  flex: 1;
  min-width: 0; /* Prevent flex overflow */
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}
</style>

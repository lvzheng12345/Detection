<template>
  <div class="sentiment-analysis">
    <div class="filter-bar" style="margin-bottom: 20px;">
        <span style="margin-right: 10px; font-weight: bold;">选择时间段：</span>
        <el-select v-model="selectedWeek" placeholder="请选择周次" @change="handleWeekChange" style="width: 240px">
            <el-option
                v-for="(item, index) in weeklyData"
                :key="index"
                :label="formatWeekLabel(item.weekStart)"
                :value="index"
            />
        </el-select>
    </div>

    <el-row :gutter="20" v-if="currentData">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>情感倾向分布 ({{ formatWeekLabel(currentData.weekStart) }})</span>
            </div>
          </template>
          <div ref="chartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="advice-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><ChatLineRound /></el-icon> 智能分析建议</span>
            </div>
          </template>
          <div class="advice-content" v-loading="loading">
            <p v-if="currentData.advice">{{ currentData.advice }}</p>
            <el-empty v-else description="暂无数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else description="暂无分析数据" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ChatLineRound } from '@element-plus/icons-vue'

const props = defineProps({
  userId: {
    type: Number,
    default: null
  }
})

const chartRef = ref(null)
const loading = ref(false)
const weeklyData = ref([])
const selectedWeek = ref(0)
let chartInstance = null

const currentData = computed(() => {
    if (weeklyData.value && weeklyData.value.length > 0) {
        return weeklyData.value[selectedWeek.value]
    }
    return null
})

const formatWeekLabel = (dateStr) => {
    return `周起始日: ${dateStr}`
}

const fetchAnalysis = async () => {
  loading.value = true
  try {
    let url = '/api/audit/analysis'
    if (props.userId) {
      url += `?userId=${props.userId}`
    }
    const response = await axios.get(url)
    weeklyData.value = response.data
    if (weeklyData.value.length > 0) {
        selectedWeek.value = 0
        // Wait for DOM update
        setTimeout(() => {
            updateChart(weeklyData.value[0].sentimentCounts)
        }, 100)
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleWeekChange = (index) => {
    const data = weeklyData.value[index]
    if (data) {
        updateChart(data.sentimentCounts)
    }
}

const updateChart = (counts) => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '情感倾向',
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
        data: [
          { value: counts.positive, name: '正面', itemStyle: { color: '#67C23A' } },
          { value: counts.neutral, name: '中性', itemStyle: { color: '#909399' } },
          { value: counts.negative, name: '负面', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  }
  chartInstance.setOption(option)
}

watch(() => props.userId, () => {
  fetchAnalysis()
})

onMounted(() => {
  fetchAnalysis()
  window.addEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
})
</script>

<style scoped>
.advice-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.advice-content p {
    text-align: left;
    width: 100%;
}
</style>

<template>
  <div>
    <div class="filter-container" style="margin-bottom: 15px">
      <el-radio-group v-model="filterStatus" @change="fetchHistory">
        <el-radio-button label="ALL">全部</el-radio-button>
        <el-radio-button label="SAFE">安全</el-radio-button>
        <el-radio-button label="UNSAFE">违规</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="filteredData" style="width: 100%" height="400" v-loading="loading">
      <el-table-column prop="createTime" label="时间" width="160">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin" prop="username" label="用户" width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="isSafe" label="结果" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isSafe ? 'success' : 'danger'">
            {{ scope.row.isSafe ? '安全' : '违规' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.category" :type="getCategoryType(scope.row.category)" effect="plain">
            {{ scope.row.category }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import axios from 'axios'

const props = defineProps({
  userId: {
    type: Number,
    default: null
  },
  isAdmin: {
    type: Boolean,
    default: false
  }
})

const tableData = ref([])
const loading = ref(false)
const filterStatus = ref('ALL')

const filteredData = computed(() => {
  if (filterStatus.value === 'ALL') return tableData.value
  const isSafe = filterStatus.value === 'SAFE'
  return tableData.value.filter(item => item.isSafe === isSafe)
})

const getCategoryType = (category) => {
  const map = {
    '正常': 'success',
    '色情': 'warning',
    '暴力': 'danger',
    '辱骂': 'danger',
    '政治敏感': 'danger',
    '广告': 'info'
  }
  return map[category] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const fetchHistory = async () => {
  loading.value = true
  try {
    let url = '/api/audit/history'
    if (props.userId) {
      url += `?userId=${props.userId}`
    }
    const response = await axios.get(url)
    tableData.value = response.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

watch(() => props.userId, (newVal) => {
  if (newVal) {
    fetchHistory()
  }
})

onMounted(() => {
  fetchHistory()
})

defineExpose({
  fetchHistory
})
</script>

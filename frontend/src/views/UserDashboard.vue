<template>
  <div class="common-layout">
    <el-container class="layout-container">
      <!-- Sidebar -->
      <el-aside width="240px" class="aside-menu">
        <div class="logo-area">
          <h2 class="app-title">DeepAudit</h2>
        </div>
        <el-menu
          :default-active="currentTab"
          class="el-menu-vertical"
          :border="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="1">
            <el-icon><Odometer /></el-icon>
            <span>概览统计</span>
          </el-menu-item>
          <el-menu-item index="2">
            <el-icon><Edit /></el-icon>
            <span>在线检测</span>
          </el-menu-item>
          <el-menu-item index="3">
            <el-icon><List /></el-icon>
            <span>检测历史</span>
          </el-menu-item>
          <el-menu-item index="6">
            <el-icon><ChatLineRound /></el-icon>
            <span>情感分析</span>
          </el-menu-item>
          <el-menu-item index="7">
            <el-icon><Document /></el-icon>
            <span>文档审核</span>
          </el-menu-item>
          <el-menu-item index="4">
            <el-icon><Document /></el-icon>
            <span>API 文档</span>
          </el-menu-item>
          <el-menu-item index="5">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </el-menu-item>
        </el-menu>
        
        <div class="user-profile-area">
           <div class="user-info">
             <el-avatar :size="32" :src="avatarUrl" icon="UserFilled" class="user-avatar" />
             <span class="username">{{ username }}</span>
           </div>
           <el-button link type="danger" size="small" @click="logout">
             <el-icon><SwitchButton /></el-icon>
           </el-button>
        </div>
      </el-aside>
      
      <!-- Main Content -->
      <el-main class="main-content">
        <div class="content-wrapper">
            <!-- Header -->
            <div class="page-header">
                <div class="header-left">
                    <h2>{{ getPageTitle() }}</h2>
                    <span class="subtitle">{{ getPageSubtitle() }}</span>
                </div>
                <div class="header-actions" v-if="currentTab === '1'">
                     <el-date-picker v-model="dateRange" type="month" placeholder="2025-12" size="default" style="width: 140px" />
                     <el-button type="primary" color="#000" class="export-btn">导出</el-button>
                </div>
            </div>
            
            <!-- Tab 1: Overview -->
            <div v-if="currentTab === '1'">
                <div class="stats-section">
                     <DashboardStats ref="statsRef" :user-id="userId" />
                </div>
                <el-row :gutter="24" style="margin-top: 24px;">
                    <el-col :span="14">
                        <div class="section-card">
                            <div class="section-header">
                                <h3><el-icon><Edit /></el-icon> 快速检测</h3>
                            </div>
                            <div class="card-body">
                                <AuditForm @check-completed="refreshData" />
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="10">
                         <div class="section-card">
                            <div class="section-header">
                                <h3><el-icon><List /></el-icon> 最近记录</h3>
                                <el-button link type="primary" @click="currentTab = '3'">查看全部</el-button>
                            </div>
                            <div class="card-body">
                                <AuditHistory ref="historyRef" :user-id="userId" />
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </div>

            <!-- Tab 2: Online Check -->
            <div v-if="currentTab === '2'">
                <el-card class="box-card">
                    <template #header>
                        <div class="card-header">
                            <span>文本内容检测</span>
                        </div>
                    </template>
                    <AuditForm @check-completed="refreshData" />
                </el-card>
            </div>

            <!-- Tab 3: History -->
            <div v-if="currentTab === '3'">
                <el-card class="box-card">
                    <template #header>
                        <div class="card-header">
                            <span>完整检测记录</span>
                            <el-button class="button" text @click="refreshData">刷新</el-button>
                        </div>
                    </template>
                    <AuditHistory ref="historyRef" :user-id="userId" />
                </el-card>
            </div>

            <!-- Tab 6: Sentiment Analysis -->
            <div v-if="currentTab === '6'">
                <SentimentAnalysis :user-id="userId" />
            </div>

            <!-- Tab 7: Document Audit -->
            <div v-if="currentTab === '7'">
                <el-card class="box-card">
                    <template #header>
                        <div class="card-header">
                            <span>文档内容审核</span>
                        </div>
                    </template>
                    <DocumentAudit />
                </el-card>
            </div>

            <!-- Tab 4: API Docs -->
            <div v-if="currentTab === '4'">
                <ApiDocs />
            </div>

            <!-- Tab 5: Settings -->
            <div v-if="currentTab === '5'">
                <Settings @profile-updated="loadUserProfile" />
            </div>

        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Odometer, Edit, List, Document, Setting, UserFilled, SwitchButton, ChatLineRound } from '@element-plus/icons-vue'
import AuditForm from '../components/AuditForm.vue'
import AuditHistory from '../components/AuditHistory.vue'
import DashboardStats from '../components/DashboardStats.vue'
import SentimentAnalysis from '../components/SentimentAnalysis.vue'
import DocumentAudit from '../components/DocumentAudit.vue'
import ApiDocs from '../components/ApiDocs.vue'
import Settings from '../components/Settings.vue'

const router = useRouter()
const historyRef = ref(null)
const statsRef = ref(null)
const userId = ref(null)
const username = ref('User')
const avatarUrl = ref('')
const dateRange = ref('')
const currentTab = ref('1')

const loadUserProfile = () => {
  const user = JSON.parse(localStorage.getItem('user'))
  if (user) {
    userId.value = user.id
    username.value = user.username || 'User'
    avatarUrl.value = user.avatarUrl || ''
  }
}

onMounted(() => {
  loadUserProfile()
  
  // Apply Dark Mode if enabled
  const isDark = localStorage.getItem('darkMode') === 'true'
  if (isDark) {
    document.documentElement.classList.add('dark')
  }
})

onUnmounted(() => {
  // Clean up Dark Mode when leaving User Dashboard
  document.documentElement.classList.remove('dark')
})

const handleMenuSelect = (index) => {
    currentTab.value = index
}

const getPageTitle = () => {
    const titles = {
        '1': '每月用量',
        '2': '在线检测',
        '3': '检测历史',
        '4': 'API 文档',
        '5': '系统设置',
        '6': '情感分析',
        '7': '文档审核'
    }
    return titles[currentTab.value]
}

const getPageSubtitle = () => {
    const subtitles = {
        '1': '检测统计与分析',
        '2': '实时文本内容审核',
        '3': '查看所有历史检测记录',
        '4': '接口调用说明',
        '5': '账户与偏好设置',
        '6': '用户发言情感倾向与心理建议',
        '7': '上传文档进行违规检测与修改建议'
    }
    return subtitles[currentTab.value]
}

const refreshData = () => {
  if (historyRef.value) {
    historyRef.value.fetchHistory()
  }
  if (statsRef.value) {
    statsRef.value.fetchStats()
  }
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.common-layout {
  height: 100vh;
  background-color: var(--el-bg-color-page);
  color: var(--el-text-color-primary);
}

.layout-container {
  height: 100%;
}

.aside-menu {
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
  position: relative;
}

.logo-area {
  padding: 20px 24px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.app-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.el-menu-vertical {
  border-right: none;
  padding-top: 10px;
  background-color: var(--el-bg-color);
}

.el-menu-item {
  font-size: 14px;
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  color: var(--el-text-color-regular);
}

.el-menu-item.is-active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 500;
}

.user-profile-area {
  margin-top: auto;
  padding: 20px;
  border-top: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.main-content {
  padding: 0;
  background-color: var(--el-bg-color-page);
}

.content-wrapper {
  padding: 32px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.export-btn {
  background-color: var(--el-text-color-primary);
  border-color: var(--el-text-color-primary);
  color: var(--el-bg-color);
}

.stats-section {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--el-box-shadow-light);
  margin-bottom: 24px;
}

.section-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);
  height: 100%;
  overflow: hidden;
}

.section-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-primary);
}

.card-body {
  padding: 24px;
}
</style>

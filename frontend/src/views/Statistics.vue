<template>
  <div class="page-grid">
    <el-row :gutter="18">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <el-card class="stat-card">
          <div class="muted">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="muted">{{ item.tip }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18">
      <el-col :span="16">
        <el-card class="stat-card">
          <template #header>
            <div class="header-row">
              <strong>学习成长趋势</strong>
              <el-radio-group v-model="days" size="small" @change="loadTrend">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
                <el-radio-button :value="90">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <strong>薄弱词库分类分析</strong>
          </template>
          <div ref="weakChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="stat-card">
      <template #header>
        <strong>关键学习指标</strong>
      </template>
      <el-row :gutter="18">
        <el-col :span="8">
          <el-progress type="dashboard" :percentage="overview?.masteryRate || 0">
            <template #default>
              <strong>{{ overview?.masteryRate || 0 }}%</strong>
              <span>掌握率</span>
            </template>
          </el-progress>
        </el-col>
        <el-col :span="8">
          <el-progress type="dashboard" status="warning" :percentage="overview?.forgettingRate || 0">
            <template #default>
              <strong>{{ overview?.forgettingRate || 0 }}%</strong>
              <span>遗忘率</span>
            </template>
          </el-progress>
        </el-col>
        <el-col :span="8">
          <el-progress type="dashboard" status="success" :percentage="overview?.reviewCompletionRate || 0">
            <template #default>
              <strong>{{ overview?.reviewCompletionRate || 0 }}%</strong>
              <span>复习完成率</span>
            </template>
          </el-progress>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import { getStatisticsOverview, getStatisticsTrend, getWeakAnalysis, type StatisticsOverview, type TrendPoint, type WeakWordAnalysis } from '@/api/statistics';

const overview = ref<StatisticsOverview>();
const trend = ref<TrendPoint[]>([]);
const weakAnalysis = ref<WeakWordAnalysis[]>([]);
const days = ref(7);
const trendChartRef = ref<HTMLDivElement>();
const weakChartRef = ref<HTMLDivElement>();
let trendChart: echarts.ECharts | undefined;
let weakChart: echarts.ECharts | undefined;

function resizeCharts() {
  trendChart?.resize();
  weakChart?.resize();
}

const statCards = computed(() => [
  { label: '已学习单词', value: overview.value?.learnedWordCount || 0, tip: '累计进入复习计划' },
  { label: '已掌握单词', value: overview.value?.masteredWordCount || 0, tip: `掌握率 ${overview.value?.masteryRate || 0}%` },
  { label: '待复习任务', value: overview.value?.dueReviewCount || 0, tip: `完成率 ${overview.value?.reviewCompletionRate || 0}%` },
  { label: '薄弱生词', value: overview.value?.wrongWordCount || 0, tip: `遗忘率 ${overview.value?.forgettingRate || 0}%` },
]);

async function loadOverview() {
  const res = await getStatisticsOverview();
  overview.value = res.data.data;
}

async function loadTrend() {
  const res = await getStatisticsTrend(days.value);
  trend.value = res.data.data;
  await nextTick();
  renderTrendChart();
}

async function loadWeakAnalysis() {
  const res = await getWeakAnalysis();
  weakAnalysis.value = res.data.data;
  await nextTick();
  renderWeakChart();
}

function renderTrendChart() {
  if (!trendChartRef.value) return;
  trendChart = trendChart || echarts.init(trendChartRef.value);
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新学单词', '复习单词', '已掌握', '错词', '学习时长(分钟)'] },
    grid: { left: 45, right: 35, top: 55, bottom: 35 },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: trend.value.map((item) => item.date.slice(5)),
      axisTick: { alignWithLabel: true },
    },
    yAxis: [
      { type: 'value', name: '单词数' },
      { type: 'value', name: '分钟' },
    ],
    series: [
      { name: '新学单词', type: 'line', smooth: true, data: trend.value.map((item) => item.newWordCount) },
      { name: '复习单词', type: 'line', smooth: true, data: trend.value.map((item) => item.reviewWordCount) },
      { name: '已掌握', type: 'bar', stack: 'wordStatus', barWidth: 28, data: trend.value.map((item) => item.masteredWordCount) },
      { name: '错词', type: 'bar', stack: 'wordStatus', barWidth: 28, data: trend.value.map((item) => item.wrongWordCount) },
      { name: '学习时长(分钟)', type: 'line', yAxisIndex: 1, smooth: true, data: trend.value.map((item) => Math.round(item.studyDuration / 60)) },
    ],
  });
}

function renderWeakChart() {
  if (!weakChartRef.value) return;
  weakChart = weakChart || echarts.init(weakChartRef.value);
  weakChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: '薄弱分类',
        type: 'pie',
        radius: ['42%', '68%'],
        data: weakAnalysis.value.map((item) => ({ name: item.category, value: item.count })),
        label: { formatter: '{b}: {c}' },
      },
    ],
  });
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadTrend(), loadWeakAnalysis()]);
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  trendChart?.dispose();
  weakChart?.dispose();
});
</script>

<style scoped>
.chart {
  height: 420px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-progress__text) {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

@media (max-width: 768px) {
  .header-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .chart {
    height: 320px;
    min-width: 520px;
  }

  .stat-card :deep(.el-card__body) {
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .chart {
    height: 280px;
  }
}
</style>

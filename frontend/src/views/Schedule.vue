<template>
  <div class="schedule-page">
    <section class="schedule-hero">
      <div>
        <div class="eyebrow">学习打卡与日程</div>
        <h1>把每天的背词节奏，变成可视化的学习习惯</h1>
        <p>设置每日目标、完成打卡、查看月历和连续天数，帮助你稳步推进艾宾浩斯复习计划。</p>
      </div>
      <div class="hero-actions">
        <el-button :disabled="overview?.checkedInToday" @click="openCheckin">今日打卡</el-button>
      </div>
    </section>

    <el-row :gutter="18">
      <el-col :span="6">
        <el-card class="stat-card metric-card">
          <div class="muted">今日背词</div>
          <div class="stat-value">{{ overview?.todayWordCount || 0 }}</div>
          <el-progress :percentage="overview?.wordProgress || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card metric-card">
          <div class="muted">学习时长</div>
          <div class="stat-value">{{ Math.round((overview?.todayStudyDuration || 0) / 60) }} 分钟</div>
          <el-progress :percentage="overview?.durationProgress || 0" status="success" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card metric-card">
          <div class="muted">目标完成率</div>
          <div class="stat-value">{{ Math.round(Math.max(overview?.wordProgress || 0, overview?.durationProgress || 0)) }}%</div>
          <el-progress :percentage="Math.round(Math.max(overview?.wordProgress || 0, overview?.durationProgress || 0))" status="warning" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card metric-card">
          <div class="muted">今日状态</div>
          <div class="stat-value">{{ overview?.checkedInToday ? '已打卡' : '未打卡' }}</div>
          <el-button type="primary" :disabled="overview?.checkedInToday" @click="openCheckin">今日打卡</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18">
      <el-col :span="8">
        <el-card class="stat-card goal-card">
          <template #header>
            <strong>每日目标设置</strong>
          </template>
          <el-form :model="goalForm" label-width="90px" class="compact-goal-form">
            <el-form-item label="目标词数">
              <el-input-number v-model="goalForm.targetWordCount" :min="1" :max="500" size="small" />
            </el-form-item>
            <el-form-item label="目标时长">
              <el-input-number v-model="goalForm.targetStudyMinutes" :min="1" :max="600" size="small" />
              <span class="muted" style="margin-left: 8px">分钟</span>
            </el-form-item>
            <el-button type="primary" size="small" @click="saveGoal">保存目标</el-button>
          </el-form>
        </el-card>

        <el-card class="stat-card plan-card">
          <template #header>
            <strong>学习计划</strong>
          </template>
          <div class="plan-panel">
            <div class="field-label">选择词库</div>
            <el-input v-model="bookKeyword" placeholder="搜索词库名称..." clearable size="small" />
            <div class="book-list inline-book-list">
              <div v-for="book in filteredBooks" :key="book.id" class="book-item" :class="{ active: planForm.bookId === book.id }" @click="planForm.bookId = book.id">
                <div>
                  <div class="book-title">{{ book.name }}</div>
                  <div class="book-subtitle">{{ book.category }} · {{ book.description || '暂无描述' }}</div>
                </div>
                <div class="book-count">{{ book.wordCount.toLocaleString() }} 词</div>
              </div>
            </div>

            <div class="slider-row">
              <div class="slider-head">
                <span>每日新词</span>
                <strong>{{ planForm.planWordCount }} 词</strong>
              </div>
              <el-slider v-model="planForm.planWordCount" :min="5" :max="80" :step="1" />
            </div>

            <div class="slider-row">
              <div class="slider-head">
                <span>复习倍数</span>
                <strong>×{{ planForm.reviewMultiplier }}</strong>
              </div>
              <el-slider v-model="planForm.reviewMultiplier" :min="1" :max="5" :step="1" />
            </div>

            <div class="plan-summary">
              每日复习 {{ planForm.planWordCount * planForm.reviewMultiplier }} 词，
              每日总量 {{ planForm.planWordCount + planForm.planWordCount * planForm.reviewMultiplier }} 词
            </div>

            <el-button type="primary" size="small" :disabled="!planForm.bookId" @click="createPlan">{{ hasStudyPlan ? '保存调整' : '创建计划' }}</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="stat-card calendar-card">
          <template #header>
            <div class="header-row">
              <strong>学习日历</strong>
              <el-date-picker v-model="monthValue" type="month" format="YYYY-MM" value-format="YYYY-MM" @change="loadCalendar" />
            </div>
          </template>
          <el-calendar v-model="calendarDate">
            <template #date-cell="{ data }">
              <div class="calendar-cell" :class="{ checked: dayMap[data.day]?.checkedIn }">
                <strong>{{ data.day.split('-').pop() }}</strong>
                <template v-if="dayMap[data.day]">
                  <span>词 {{ dayMap[data.day].newWordCount + dayMap[data.day].reviewWordCount }}</span>
                  <span>{{ Math.round(dayMap[data.day].studyDuration / 60) }} 分</span>
                </template>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="checkinVisible" title="今日学习打卡" width="460px">
      <el-form :model="checkinForm" label-width="90px">
        <el-form-item label="学习时长">
          <el-input-number v-model="checkinForm.studyDurationMinutes" :min="0" :max="1440" />
          <span class="muted" style="margin-left: 8px">分钟</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkinForm.remark" type="textarea" :rows="3" placeholder="记录今天的学习感受" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkinVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckin">确认打卡</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { checkin, getCalendar, getDailyGoal, getScheduleOverview, updateDailyGoal, type CalendarDay, type ScheduleOverview } from '@/api/schedule';
import { getWordBooks, type WordBook } from '@/api/wordBook';
import { getStudyPlan, saveStudyPlan } from '@/api/studyPlan';

const overview = ref<ScheduleOverview>();
const books = ref<WordBook[]>([]);
const days = ref<CalendarDay[]>([]);
const calendarDate = ref(new Date());
const monthValue = ref(new Date().toISOString().slice(0, 7));
const checkinVisible = ref(false);
const bookKeyword = ref('');

const goalForm = reactive({ targetWordCount: 30, targetStudyMinutes: 30 });
const planForm = reactive({ bookId: 0 as number, planWordCount: 20, reviewMultiplier: 2 });
const hasStudyPlan = ref(false);
const checkinForm = reactive({ studyDurationMinutes: 0, remark: '' });
const dayMap = computed(() => Object.fromEntries(days.value.map((day) => [day.date, day])));
const filteredBooks = computed(() => {
  const text = bookKeyword.value.trim().toLowerCase();
  return books.value.filter((book) => !text || [book.name, book.category, book.description].some((v) => v?.toLowerCase().includes(text)));
});

async function loadBooks() {
  const res = await getWordBooks();
  books.value = res.data.data;
  if (!planForm.bookId && books.value.length) planForm.bookId = books.value[0].id;
}

async function loadStudyPlan() {
  const res = await getStudyPlan();
  const plan = res.data.data;
  hasStudyPlan.value = Boolean(plan);
  if (!plan) return;
  planForm.bookId = plan.bookId;
  planForm.planWordCount = plan.planWordCount;
  planForm.reviewMultiplier = plan.reviewMultiplier;
}

async function loadOverview() {
  const res = await getScheduleOverview();
  overview.value = res.data.data;
}

async function loadGoal() {
  const res = await getDailyGoal();
  goalForm.targetWordCount = res.data.data.targetWordCount;
  goalForm.targetStudyMinutes = res.data.data.targetStudyMinutes;
}

async function saveGoal() {
  await updateDailyGoal(goalForm);
  ElMessage.success('每日目标已更新');
  loadOverview();
}

async function createPlan() {
  if (!planForm.bookId) {
    ElMessage.warning('请选择词库');
    return;
  }
  await saveStudyPlan({
    bookId: planForm.bookId,
    planWordCount: planForm.planWordCount,
    reviewMultiplier: planForm.reviewMultiplier,
  });
  hasStudyPlan.value = true;
  ElMessage.success('学习计划已保存，可在学习总览查看');
}

async function loadCalendar() {
  const [year, month] = monthValue.value.split('-').map(Number);
  const res = await getCalendar({ year, month });
  days.value = res.data.data;
  calendarDate.value = new Date(year, month - 1, 1);
}

function openCheckin() {
  checkinForm.studyDurationMinutes = Math.round((overview.value?.todayStudyDuration || 0) / 60);
  checkinForm.remark = '';
  checkinVisible.value = true;
}

async function submitCheckin() {
  await checkin({ studyDuration: checkinForm.studyDurationMinutes * 60, remark: checkinForm.remark });
  ElMessage.success('打卡成功，继续保持');
  checkinVisible.value = false;
  await loadOverview();
  await loadCalendar();
}

onMounted(async () => {
  await Promise.all([loadBooks(), loadStudyPlan(), loadOverview(), loadGoal(), loadCalendar()]);
});
</script>

<style scoped>
.schedule-page {
  display: grid;
  gap: 18px;
}

.schedule-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #ececea;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff, #fafafa);
  padding: 28px;
  box-shadow: 0 10px 30px rgba(15, 15, 15, 0.04);
}

.hero-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.eyebrow {
  color: #8a8a86;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.schedule-hero h1 {
  margin: 10px 0;
  font-size: 32px;
  letter-spacing: -0.04em;
}

.schedule-hero p {
  max-width: 720px;
  margin: 0;
  color: #6d6d68;
  line-height: 1.7;
}

.metric-card,
.goal-card,
.plan-card,
.calendar-card {
  border: 1px solid #ececea;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 15, 15, 0.04);
}

.goal-card {
  margin-bottom: 0;
}

.goal-card :deep(.el-card__body),
.plan-card :deep(.el-card__body) {
  padding: 16px 18px;
}

.goal-card :deep(.el-card__header),
.plan-card :deep(.el-card__header) {
  padding: 16px 18px;
}

.compact-goal-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.compact-goal-form :deep(.el-form-item__label) {
  height: 28px;
  line-height: 28px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-card {
  margin-top: 16px;
}

.plan-panel {
  display: grid;
  gap: 12px;
}

.inline-book-list {
  max-height: 170px;
}

.field-label {
  font-weight: 700;
  color: #202124;
}

.book-list {
  max-height: 170px;
  overflow: auto;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #fff;
}

.book-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
}

.book-item:last-child {
  border-bottom: none;
}

.book-item.active {
  background: #f8fafc;
}

.book-title {
  font-weight: 700;
  color: #202124;
}

.book-subtitle {
  margin-top: 2px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.35;
}

.book-count {
  color: #6b7280;
  white-space: nowrap;
}

.slider-row {
  padding: 3px 2px;
}

.slider-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.slider-head span {
  color: #202124;
  font-weight: 600;
}

.slider-head strong {
  color: #202124;
}

.plan-summary {
  color: #6b7280;
  font-size: 13px;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px;
  border-radius: 8px;
  color: #64748b;
}

.calendar-cell.checked {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.14), rgba(34, 197, 94, 0.14));
  color: #1d4ed8;
}

.calendar-cell strong {
  color: #1f2937;
}

@media (max-width: 768px) {
  .schedule-page {
    gap: 12px;
  }

  .schedule-hero {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .schedule-hero h1 {
    margin: 8px 0;
    font-size: 23px;
    line-height: 1.25;
  }

  .schedule-hero p {
    font-size: 14px;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-actions .el-button {
    width: 100%;
  }

  .header-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .header-row :deep(.el-date-editor) {
    width: 100%;
  }

  .book-item {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .book-count {
    white-space: normal;
  }

  .compact-goal-form :deep(.el-form-item) {
    display: block;
  }

  .compact-goal-form :deep(.el-form-item__label) {
    justify-content: flex-start;
    width: auto !important;
    margin-bottom: 6px;
  }

  .compact-goal-form :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }

  .calendar-card :deep(.el-calendar__body) {
    padding: 8px 0 0;
  }

  .calendar-card :deep(.el-calendar-table .el-calendar-day) {
    min-height: 48px;
    padding: 2px;
  }

  .calendar-cell {
    gap: 2px;
    font-size: 11px;
    padding: 3px;
  }
}

@media (max-width: 480px) {
  .schedule-hero h1 {
    font-size: 22px;
  }

  .calendar-cell span {
    display: none;
  }
}
</style>

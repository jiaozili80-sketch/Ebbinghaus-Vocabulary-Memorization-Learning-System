<template>
  <div class="page-grid">
    <el-row :gutter="18">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card class="stat-card">
          <div class="muted">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="muted">{{ item.tip }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="announcements.length" class="stat-card announcement-card">
      <el-carousel height="100px" :interval="4000" indicator-position="outside" arrow="never">
        <el-carousel-item v-for="item in announcements" :key="item.id">
          <div class="announcement-item">
            <el-tag :type="item.type === '公告' ? 'danger' : item.type === '贴士' ? 'success' : 'primary'" size="small" effect="dark" round>{{ item.type }}</el-tag>
            <h3>{{ item.title }}</h3>
            <p>{{ item.content }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <el-card class="stat-card plan-overview-card">
      <template #header>
        <div class="header-row">
          <strong>当前学习计划</strong>
          <el-button v-if="!studyPlan" type="primary" @click="goSchedule">创建计划</el-button>
          <el-button v-else @click="goSchedule">调整计划</el-button>
        </div>
      </template>
      <el-empty v-if="!studyPlan" description="暂无学习计划，请先在学习日程中创建" :image-size="80">
        <el-button type="primary" @click="goSchedule">去创建计划</el-button>
      </el-empty>
      <div v-else class="plan-overview">
        <div>
          <div class="muted">目标词库</div>
          <h3>{{ studyPlan.bookName }}</h3>
          <p>{{ studyPlan.category }} · 共 {{ studyPlan.wordCount.toLocaleString() }} 词</p>
        </div>
        <div class="plan-metrics">
          <div class="plan-metric">
            <span>每日新词</span>
            <strong>{{ studyPlan.planWordCount }} 词</strong>
          </div>
          <div class="plan-metric">
            <span>每日复习</span>
            <strong>{{ studyPlan.dailyReviewCount }} 词</strong>
          </div>
          <div class="plan-metric">
            <span>每日总量</span>
            <strong>{{ studyPlan.dailyTotalCount }} 词</strong>
          </div>
          <div class="plan-metric">
            <span>复习倍数</span>
            <strong>×{{ studyPlan.reviewMultiplier }}</strong>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="stat-card hero-card">
      <div>
        <h2>今日艾宾浩斯复习任务</h2>
        <p>系统将按照 5分钟、30分钟、12小时、1天、2天、4天、7天、15天周期智能提醒复习。</p>
      </div>
      <el-progress type="dashboard" :percentage="todayProgress" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getScheduleOverview, type ScheduleOverview } from '@/api/schedule';
import { getStatisticsOverview, type StatisticsOverview } from '@/api/statistics';
import { getStudyPlan, type StudyPlan } from '@/api/studyPlan';
import { getAnnouncements, type Announcement } from '@/api/admin';

const router = useRouter();
const scheduleOverview = ref<ScheduleOverview>();
const statisticsOverview = ref<StatisticsOverview>();
const studyPlan = ref<StudyPlan>();
const announcements = ref<Announcement[]>([]);

const stats = computed(() => {
  const learned = statisticsOverview.value?.learnedWordCount || 0;
  const mastered = statisticsOverview.value?.masteredWordCount || 0;
  const dueReview = statisticsOverview.value?.dueReviewCount || 0;
  const checkinDays = scheduleOverview.value?.continuousCheckinDays || 0;
  const masteryRate = statisticsOverview.value?.masteryRate || 0;
  return [
    { label: '累计背词', value: learned.toLocaleString(), tip: learned > 0 ? `已建立 ${learned} 条学习记录` : '还没开始学习，快去背第一个单词吧' },
    { label: '已掌握', value: mastered.toLocaleString(), tip: learned > 0 ? `掌握率 ${masteryRate}%` : '完成多轮复习后会在这里累计' },
    { label: '待复习', value: dueReview.toLocaleString(), tip: dueReview > 0 ? '今日需完成' : '当前没有到期复习任务' },
    { label: '连续打卡', value: `${checkinDays} 天`, tip: checkinDays > 0 ? '保持学习节奏' : '完成今日学习后即可开始累计' },
  ];
});

const todayProgress = computed(() => {
  const value = scheduleOverview.value?.wordProgress || 0;
  return Math.max(0, Math.min(100, Math.round(value)));
});

function goSchedule() {
  router.push('/schedule');
}

async function loadData() {
  const [planRes, scheduleRes, statisticsRes, announcementRes] = await Promise.all([getStudyPlan(), getScheduleOverview(), getStatisticsOverview(), getAnnouncements()]);
  studyPlan.value = planRes.data.data || undefined;
  scheduleOverview.value = scheduleRes.data.data;
  statisticsOverview.value = statisticsRes.data.data;
  announcements.value = announcementRes.data.data || [];
}

onMounted(loadData);
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-card :deep(.el-card__body) {
  padding: 8px 16px;
}

.announcement-item {
  padding: 8px 4px;
}

.announcement-item h3 {
  margin: 8px 0 4px;
  font-size: 16px;
  color: #202124;
}

.announcement-item p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plan-overview-card :deep(.el-card__body) {
  min-height: 150px;
}

.plan-overview {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
}

.plan-overview h3 {
  margin: 8px 0;
  font-size: 26px;
  color: #202124;
}

.plan-overview p {
  margin: 0;
  color: #64748b;
}

.plan-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(110px, 1fr));
  gap: 12px;
  min-width: 520px;
}

.plan-metric {
  padding: 16px;
  border: 1px solid #ececea;
  border-radius: 14px;
  background: #fbfbfa;
}

.plan-metric span {
  display: block;
  color: #8a8a86;
  font-size: 13px;
  margin-bottom: 8px;
}

.plan-metric strong {
  color: #202124;
  font-size: 22px;
}

.hero-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-card h2 {
  margin: 0 0 10px;
}

.hero-card p {
  margin: 0;
  color: #64748b;
}

@media (max-width: 768px) {
  .header-row {
    align-items: stretch;
    flex-direction: column;
    gap: 10px;
  }

  .header-row .el-button {
    width: 100%;
  }

  .plan-overview-card :deep(.el-card__body) {
    min-height: 0;
  }

  .plan-overview {
    align-items: stretch;
    flex-direction: column;
    gap: 16px;
  }

  .plan-overview h3 {
    font-size: 22px;
  }

  .plan-metrics {
    min-width: 0;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .plan-metric {
    padding: 10px;
  }

  .plan-metric span {
    margin-bottom: 4px;
    font-size: 12px;
  }

  .plan-metric strong {
    font-size: 18px;
  }

  .hero-card h2 {
    font-size: 20px;
  }

  .hero-card p {
    font-size: 14px;
    line-height: 1.6;
  }

  .hero-card :deep(.el-card__body) {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .plan-metrics {
    grid-template-columns: 1fr;
  }
}
</style>

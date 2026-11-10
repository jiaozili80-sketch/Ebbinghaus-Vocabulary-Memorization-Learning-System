<template>
  <div class="resource-page">
    <section class="resource-hero">
      <div>
        <el-tag effect="dark" round>资源中心</el-tag>
        <h1>精选词库，一个页面开启科学背词</h1>
        <p>覆盖四六级、考研、雅思、高中、初中与私人词库，结合艾宾浩斯复习计划持续巩固。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="router.push('/study')">开始单词练习</el-button>
          <el-button size="large" round @click="router.push('/wrong-words')">查看生词本</el-button>
        </div>
      </div>
      <div class="hero-stats">
        <div>
          <strong>{{ uniqueBooks.length }}+</strong>
          <span>可用词库</span>
        </div>
        <div>
          <strong>{{ totalWords }}</strong>
          <span>词条资源</span>
        </div>
        <div>
          <strong>8</strong>
          <span>复习周期</span>
        </div>
      </div>
    </section>

    <section class="filter-panel">
      <el-input v-model="keyword" size="large" clearable placeholder="搜索词库名称、分类或描述" class="search-input" />
      <div class="category-tabs">
        <button v-for="item in categories" :key="item.value" :class="{ active: category === item.value }" @click="selectCategory(item.value)">
          {{ item.label }}
        </button>
      </div>
    </section>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="12" :lg="8" v-for="book in filteredBooks" :key="book.id">
        <article class="dict-card">
          <div class="dict-card-top">
            <div class="dict-icon">{{ book.category || '词库' }}</div>
            <div>
              <el-tag size="small" :type="book.type === 1 ? 'success' : 'warning'">{{ book.type === 1 ? '官方词库' : '私人词库' }}</el-tag>
            </div>
          </div>
          <h3>{{ book.name }}</h3>
          <p>{{ book.description || '暂无简介，适合加入艾宾浩斯复习计划进行长期巩固。' }}</p>
          <div class="dict-meta">
            <span>{{ book.category }}</span>
            <span>{{ book.wordCount }} 词</span>
            <span>{{ book.status === 1 ? '可学习' : '已停用' }}</span>
          </div>
          <div class="dict-progress">
            <span>推荐指数</span>
            <el-rate :model-value="recommendScore(book)" disabled size="small" />
          </div>
          <div class="dict-actions">
            <el-button type="primary" round @click="goStudy(book.id)">开始学习</el-button>
            <el-button text @click="preview(book)">预览详情</el-button>
          </div>
        </article>
      </el-col>
    </el-row>

    <el-empty v-if="!filteredBooks.length" description="暂无匹配词库，换个关键词试试" />

    <el-dialog v-model="previewVisible" title="词库详情" width="520px">
      <template v-if="currentBook">
        <h2 class="preview-title">{{ currentBook.name }}</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="分类">{{ currentBook.category }}</el-descriptions-item>
          <el-descriptions-item label="单词数量">{{ currentBook.wordCount }}</el-descriptions-item>
          <el-descriptions-item label="词库类型">{{ currentBook.type === 1 ? '官方公共词库' : '私人自建词库' }}</el-descriptions-item>
          <el-descriptions-item label="简介">{{ currentBook.description || '暂无简介' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="currentBook && goStudy(currentBook.id)">开始学习</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getWordBooks, type WordBook } from '@/api/wordBook';

const router = useRouter();
const category = ref('');
const keyword = ref('');
const books = ref<WordBook[]>([]);
const previewVisible = ref(false);
const currentBook = ref<WordBook>();

const uniqueBooks = computed(() => {
  const map = new Map<string, WordBook>();
  books.value.forEach((book) => {
    const key = `${book.type}-${book.userId || 0}-${book.name}-${book.category}`;
    const existing = map.get(key);
    if (!existing || book.id < existing.id) {
      map.set(key, book);
    }
  });
  return Array.from(map.values()).sort((a, b) => a.id - b.id);
});
const categories = computed(() => {
  const unique = new Set(uniqueBooks.value.map((book) => book.category).filter(Boolean));
  if (category.value && !unique.has(category.value)) {
    category.value = '';
  }
  return [{ label: '全部', value: '' }, ...Array.from(unique).map((c) => ({ label: c as string, value: c as string }))];
});
const totalWords = computed(() => uniqueBooks.value.reduce((sum, book) => sum + (book.wordCount || 0), 0));
const filteredBooks = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  return uniqueBooks.value.filter((book) => {
    const matchCategory = !category.value || book.category === category.value;
    const matchKeyword = !text || [book.name, book.category, book.description].some((value) => value?.toLowerCase().includes(text));
    return matchCategory && matchKeyword;
  });
});

async function loadData() {
  const res = await getWordBooks();
  books.value = res.data.data;
}

function selectCategory(value: string) {
  category.value = value;
}

function recommendScore(book: WordBook) {
  if (book.wordCount >= 1000) return 5;
  if (book.wordCount >= 500) return 4;
  return book.type === 1 ? 4 : 3;
}

function preview(book: WordBook) {
  currentBook.value = book;
  previewVisible.value = true;
}

function goStudy(bookId: number) {
  router.push({ path: '/study', query: { bookId } });
}

onMounted(loadData);
</script>

<style scoped>
.resource-page {
  display: grid;
  gap: 16px;
}

.resource-hero {
  min-height: 210px;
  border-radius: 18px;
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  position: relative;
  background: #ffffff;
  color: #171717;
  border: 1px solid #ececea;
  box-shadow: 0 10px 30px rgba(15, 15, 15, 0.04);
}

.resource-hero::after {
  content: '';
  position: absolute;
  right: 34px;
  bottom: 24px;
  width: 150px;
  height: 110px;
  opacity: 0.08;
  background: linear-gradient(90deg, transparent 28%, #202124 28% 38%, transparent 38% 62%, #202124 62% 72%, transparent 72%);
  border-bottom: 8px solid #202124;
  border-radius: 0 0 18px 18px;
}

.resource-hero h1 {
  max-width: 680px;
  margin: 18px 0 12px;
  font-size: 34px;
  line-height: 1.18;
  letter-spacing: -0.04em;
}

.resource-hero p {
  max-width: 650px;
  margin: 0;
  color: #777772;
  font-size: 15px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}

.hero-stats {
  width: 240px;
  display: grid;
  gap: 10px;
  z-index: 1;
}

.hero-stats div {
  padding: 14px 16px;
  border-radius: 14px;
  background: #f7f7f6;
  border: 1px solid #ececea;
}

.hero-stats strong {
  display: block;
  font-size: 24px;
  color: #202124;
}

.hero-stats span {
  color: #8a8a86;
  font-size: 12px;
}

.filter-panel {
  border-radius: 16px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1px solid #ececea;
  box-shadow: 0 10px 30px rgba(15, 15, 15, 0.04);
}

.search-input {
  max-width: 360px;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tabs button {
  border: 1px solid transparent;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  color: #62625e;
  background: #f5f5f3;
  transition: all 0.18s ease;
}

.category-tabs button.active,
.category-tabs button:hover {
  color: #fff;
  background: #202124;
  border-color: #202124;
  box-shadow: none;
}

.dict-card {
  min-height: 280px;
  margin-bottom: 18px;
  padding: 20px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f0f0ee;
  box-shadow: 0 10px 30px rgba(15, 15, 15, 0.035);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.dict-card:hover {
  transform: translateY(-3px);
  border-color: #202124;
  box-shadow: 0 14px 36px rgba(15, 15, 15, 0.07);
}

.dict-card-top,
.dict-actions,
.dict-progress,
.dict-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dict-icon {
  min-width: 72px;
  height: 48px;
  padding: 0 12px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #202124;
  font-size: 16px;
  font-weight: 900;
  background: #f1f1ef;
  white-space: nowrap;
}

.dict-card h3 {
  margin: 18px 0 8px;
  font-size: 17px;
  letter-spacing: -0.02em;
  line-height: 1.45;
  min-height: 0;
  white-space: normal;
  overflow: visible;
  word-break: break-word;
}

.dict-card p {
  min-height: 44px;
  margin: 0;
  color: #777772;
  line-height: 1.6;
  overflow: hidden;
  font-size: 13px;
}

.dict-meta {
  margin: 16px 0;
  color: #777772;
  font-size: 12px;
}

.dict-progress {
  margin-bottom: 16px;
  color: #62625e;
  font-size: 12px;
}

.preview-title {
  margin-top: 0;
}

@media (max-width: 768px) {
  .resource-hero {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    min-height: 0;
    padding: 16px;
  }

  .resource-hero::after {
    display: none;
  }

  .resource-hero h1 {
    margin: 12px 0 8px;
    font-size: 23px;
    line-height: 1.25;
  }

  .resource-hero p {
    font-size: 14px;
    line-height: 1.6;
  }

  .hero-actions {
    align-items: stretch;
    flex-direction: column;
    width: 100%;
  }

  .hero-actions .el-button {
    width: 100%;
    margin-left: 0;
  }

  .hero-stats {
    width: 100%;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .filter-panel {
    align-items: stretch;
    flex-direction: column;
    gap: 10px;
    padding: 12px;
  }

  .search-input {
    max-width: none;
  }

  .category-tabs {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 2px;
  }

  .category-tabs button {
    flex: 0 0 auto;
  }

  .dict-card {
    min-height: 0;
    margin-bottom: 12px;
    padding: 16px;
  }

  .dict-card h3 {
    margin: 14px 0 8px;
  }

  .dict-meta {
    margin: 12px 0;
  }

  .dict-actions,
  .dict-progress,
  .dict-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>

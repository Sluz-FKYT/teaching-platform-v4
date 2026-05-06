<template>
  <div class="page dashboard-page workbench-page">
    <section class="workbench-header dashboard-header">
      <div class="workbench-header__top">
        <div class="workbench-header__main">
          <div class="dashboard-header__eyebrow">教师控制台 / 工作台总览</div>
          <h1>教师工作台</h1>
          <p>{{ workbenchHeadline }}</p>
        </div>
        <div class="workbench-header__actions">
          <el-button type="primary" @click="loadDashboard" :loading="loading">刷新工作台</el-button>
        </div>
      </div>

      <div class="workbench-meta dashboard-meta">
        <span class="workbench-meta__item"><strong>{{ recentTasks.length }}</strong> 待办</span>
        <span class="workbench-meta__item"><strong>{{ urgentTaskCount }}</strong> 优先</span>
        <span class="workbench-meta__item"><strong>{{ distinctActorsCount }}</strong> 对象</span>
        <span class="workbench-meta__item"><strong>{{ trendTotalCount }}</strong> 近 7 日评分</span>
        <span class="workbench-meta__item workbench-meta__item--accent"><strong>{{ riskAlertCount }}</strong> 风险</span>
        <span class="workbench-meta__item">{{ taskPressureLevel }}</span>
        <span class="workbench-meta__item">{{ heroStatusText }}</span>
      </div>
    </section>

    <el-skeleton v-if="loading && !loaded" :rows="8" animated />

    <template v-else>
      <div v-if="loadError" class="state-card">
        <el-result icon="error" title="教师工作台加载失败" :sub-title="loadError">
          <template #extra>
            <el-button type="primary" @click="loadDashboard">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <div v-else class="dashboard-grid dashboard-grid--refactored">
        <section class="dashboard-main dashboard-main--refactored">
          <section class="workbench-panel dashboard-panel dashboard-panel--summary">
            <div class="workbench-panel__header">
              <div>
                <h2>关键指标</h2>
                <p>用更紧凑的指标排布快速判断当前工作量、风险与近期评分节奏。</p>
              </div>
              <span class="dashboard-inline-note">{{ taskPressureText }}</span>
            </div>
            <div class="dashboard-summary-strip">
              <article v-for="(card, index) in localizedSummaryCards" :key="card.key" class="dashboard-kpi-row">
                <div class="dashboard-kpi-row__icon">
                  <el-icon :size="18"><component :is="summaryIcons[index]" /></el-icon>
                </div>
                <div class="dashboard-kpi-row__body">
                  <div class="dashboard-kpi-row__topline">
                    <span>{{ card.title }}</span>
                    <span class="workbench-chip">{{ summaryCardPills[index] }}</span>
                  </div>
                  <strong>{{ card.value }}</strong>
                  <small>{{ localizedSummarySignal(card.key) }}</small>
                </div>
              </article>
            </div>
          </section>

          <el-card class="dashboard-card dashboard-card--trend" shadow="never">
            <template #header>
              <div class="dashboard-card__header dashboard-card__header--stacked">
                <div>
                  <span class="section-title">近期评分趋势</span>
                  <p>聚焦最近 7 天评分节奏与高峰日。</p>
                </div>
                <div class="dashboard-card__metrics">
                  <div class="dashboard-card__metric">
                    <span>峰值</span>
                    <strong>{{ peakTrendValue }}</strong>
                  </div>
                  <div class="dashboard-card__metric">
                    <span>日均</span>
                    <strong>{{ trendAverageText }}</strong>
                  </div>
                  <div class="dashboard-card__metric dashboard-card__metric--accent">
                    <span>节奏</span>
                    <strong>{{ trendPulseShortText }}</strong>
                  </div>
                </div>
              </div>
            </template>

            <div v-if="trend.length" class="trend-panel">
              <div class="trend-chart-shell">
                <div class="trend-y-axis">
                  <span v-for="tick in trendAxisTicks" :key="tick">{{ tick }}</span>
                </div>

                <div class="trend-chart">
                  <div class="trend-chart__grid">
                    <span v-for="tick in trendAxisTicks" :key="`grid-${tick}`" class="trend-chart__grid-line"></span>
                  </div>

                  <svg
                    v-if="trendChartPoints.length"
                    class="trend-chart__line"
                    viewBox="0 0 100 100"
                    preserveAspectRatio="none"
                    aria-hidden="true"
                  >
                    <path v-if="trendLinePath" :d="trendLinePath" class="trend-chart__line-path" />
                    <circle
                      v-for="point in trendChartPoints"
                      :key="`${point.date}-dot`"
                      :cx="point.x"
                      :cy="point.y"
                      r="1.8"
                      class="trend-chart__line-dot"
                    />
                  </svg>

                  <div v-for="point in trendChartPoints" :key="point.date" class="trend-bar-item">
                    <strong class="trend-bar-value">{{ point.scoreCount }}</strong>
                    <div class="trend-bar-track">
                      <div class="trend-bar-fill" :style="{ height: `${Math.max((point.scoreCount / maxTrendCount) * 100, 8)}%` }"></div>
                    </div>
                    <span class="trend-bar-label">{{ point.date.slice(5) }}</span>
                    <small class="trend-bar-meta">均分 {{ point.avgScore.toFixed(1) }}</small>
                  </div>
                </div>
              </div>

              <div class="trend-summary-grid trend-summary-grid--compact">
                <article class="trend-summary-card">
                  <span class="trend-summary-card__label">高峰日</span>
                  <strong>{{ peakTrendDate }}</strong>
                  <p>{{ peakTrendHint }}</p>
                </article>
                <article class="trend-summary-card">
                  <span class="trend-summary-card__label">近期关注</span>
                  <strong>{{ trendFocusTitle }}</strong>
                  <p>{{ trendFocusDescription }}</p>
                </article>
              </div>
            </div>
            <el-empty v-else description="暂无趋势数据" />
          </el-card>
        </section>

        <aside class="dashboard-side dashboard-side--refactored">
          <el-card class="dashboard-card" shadow="never">
            <template #header>
              <div class="dashboard-card__header">
                <div>
                  <span class="section-title">待办与提醒</span>
                  <p>直接列出要处理的任务，不做额外大摘要。</p>
                </div>
                <el-tag :type="recentTasks.length ? 'danger' : 'success'" effect="plain">{{ recentTasks.length }} 项</el-tag>
              </div>
            </template>

            <div class="task-panel task-panel--compact">
              <div class="task-overview-strip task-overview-strip--compact">
                <article class="task-overview-item">
                  <span class="task-overview-item__label">优先</span>
                  <strong>{{ urgentTaskCount }}</strong>
                </article>
                <article class="task-overview-item">
                  <span class="task-overview-item__label">对象</span>
                  <strong>{{ distinctActorsCount }}</strong>
                </article>
                <article class="task-overview-item">
                  <span class="task-overview-item__label">节奏</span>
                  <strong>{{ taskPressureLevel }}</strong>
                </article>
              </div>

              <div v-if="recentTasks.length" class="task-list">
                <div v-for="(task, index) in recentTasks" :key="`${task.label}-${task.title}-${index}`" class="task-item">
                  <div class="task-item__badge"></div>
                  <div class="task-item__content">
                    <div class="task-item__header">
                      <strong>{{ localizedTaskLabel(task.label) }}</strong>
                      <span class="task-item__index">{{ padIndex(index + 1) }}</span>
                    </div>
                    <p class="task-item__title">{{ localizedTaskTitle(task.title) }}</p>
                    <div class="task-item__meta-row">
                      <span>{{ localizedActorText(task.actor) }}</span>
                      <button type="button" class="task-item__link" @click="router.push(task.route)">处理</button>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="当前没有待处理任务" />
            </div>
          </el-card>

          <el-card class="dashboard-card" shadow="never">
            <template #header>
              <div class="dashboard-card__header">
                <div>
                  <span class="section-title">快捷入口</span>
                  <p>保留真实跳转路径，压成高密度入口列表。</p>
                </div>
                <el-tag effect="plain">{{ quickLinks.length }} 个入口</el-tag>
              </div>
            </template>

            <div v-if="quickLinks.length" class="quick-link-grid quick-link-grid--single">
              <button v-for="link in quickLinks" :key="link.route" type="button" class="quick-link-card quick-link-card--compact" @click="router.push(link.route)">
                <div class="quick-link-card__topline">
                  <span class="quick-link-card__title">{{ localizedQuickLinkLabel(link.label) }}</span>
                  <span class="quick-link-card__badge">{{ link.badge }}</span>
                </div>
                <div class="quick-link-card__footer">
                  <span>{{ quickLinkDescription(link) }}</span>
                  <span class="material-symbols-outlined">arrow_forward</span>
                </div>
              </button>
            </div>
            <el-empty v-else description="暂无可用快捷入口" />
          </el-card>

          <el-card class="dashboard-card" shadow="never">
            <template #header>
              <div class="dashboard-card__header">
                <div>
                  <span class="section-title">风险与建议</span>
                  <p>只保留会影响下一步动作的结论。</p>
                </div>
              </div>
            </template>

            <div class="guidance-list guidance-list--compact">
              <article class="guidance-item">
                <span class="guidance-item__title">风险提醒</span>
                <strong>{{ riskAlertSummary }}</strong>
                <p>{{ riskAlertDetail }}</p>
              </article>
              <article class="guidance-item">
                <span class="guidance-item__title">推荐动作</span>
                <strong>{{ recommendedActionTitle }}</strong>
                <p>{{ recommendedActionDescription }}</p>
              </article>
              <article class="guidance-item guidance-item--warn">
                <span class="guidance-item__title">处置建议</span>
                <strong>{{ riskActionTitle }}</strong>
                <p>{{ riskActionDescription }}</p>
              </article>
            </div>
          </el-card>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { DataLine, List, Opportunity, Warning } from '@element-plus/icons-vue';
import { getTeacherDashboard } from '@/api/teacher';

type SummaryCard = {
  key: string;
  title: string;
  value: number | string;
  description: string;
};

type TrendItem = {
  date: string;
  scoreCount: number;
  avgScore: number;
};

type RecentTask = {
  label: string;
  title: string;
  actor: string;
  route: string;
};

type QuickLink = {
  label: string;
  route: string;
  badge: number;
};

const router = useRouter();
const loading = ref(false);
const loaded = ref(false);
const loadError = ref('');
const summaryCards = ref<SummaryCard[]>([]);
const trend = ref<TrendItem[]>([]);
const recentTasks = ref<RecentTask[]>([]);
const quickLinks = ref<QuickLink[]>([]);
const summaryIcons = [Opportunity, Warning, DataLine, List];
const summaryCardPills = ['总览', '提醒', '进度', '入口'];

const textLooksChinese = (text: string) => /[\u4e00-\u9fff]/.test(text);

const normalizeEnglishText = (text: string) => text.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();

const translateDashboardText = (text: string) => {
  if (!text) return '';
  if (textLooksChinese(text)) return text;

  const normalized = normalizeEnglishText(text);
  const dictionary: Array<[RegExp, string]> = [
    [/dashboard overview|instructor dashboard|teacher workbench|teacher dashboard|dashboard/i, '教师工作台'],
    [/total students|active students|student count|students/i, '学生总数'],
    [/avg grade|average grade|avg score|average score|class average/i, '平均成绩'],
    [/pending submissions|pending grading|to grade|submissions/i, '待批改提交'],
    [/plagiarism alerts|risk alert|alerts|high risk/i, '风险提醒'],
    [/recent submission trend|student performance trends|engagement trend|trend/i, '近期趋势'],
    [/my tasks|tasks reminders|tasks alerts|tasks/i, '待办与提醒'],
    [/recent submissions/i, '近期提交'],
    [/view all/i, '查看全部'],
    [/action required/i, '需要处理'],
    [/stable/i, '整体稳定'],
    [/priority/i, '优先处理'],
    [/office hours/i, '答疑安排'],
    [/grade/i, '评分'],
    [/course|class/i, '课程/班级'],
    [/student/i, '学生'],
    [/assignment|homework/i, '作业'],
    [/lab/i, '实验'],
    [/exam/i, '考试'],
    [/report/i, '报告'],
    [/material/i, '资料'],
    [/resource/i, '资源'],
    [/settings/i, '设置']
  ];

  for (const [pattern, replacement] of dictionary) {
    if (pattern.test(normalized)) {
      return replacement;
    }
  }

  return text;
};

const localizedSummaryCards = computed(() => summaryCards.value.map(card => ({
  ...card,
  title: translateDashboardText(card.title),
  description: translateDashboardText(card.description)
})));

const maxTrendCount = computed(() => Math.max(...trend.value.map(item => item.scoreCount), 1));
const trendTotalCount = computed(() => trend.value.reduce((sum, item) => sum + item.scoreCount, 0));
const trendAverageValue = computed(() => {
  if (!trend.value.length) {
    return 0;
  }
  return trendTotalCount.value / trend.value.length;
});
const trendAverageText = computed(() => trend.value.length ? trendAverageValue.value.toFixed(1) : '0');
const trendChartPoints = computed(() => {
  const items = trend.value;
  if (!items.length) {
    return [];
  }

  return items.map((item, index) => {
    const avgScore = Math.min(Math.max(item.avgScore, 0), 100);
    const x = items.length === 1 ? 50 : 4 + (index / (items.length - 1)) * 92;
    const y = 94 - (avgScore / 100) * 88;

    return {
      ...item,
      avgScore,
      x,
      y
    };
  });
});
const trendLinePath = computed(() => trendChartPoints.value.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x.toFixed(2)} ${point.y.toFixed(2)}`).join(' '));
const trendAxisTicks = computed(() => {
  const maxValue = maxTrendCount.value;
  return [maxValue, Math.ceil(maxValue * 0.66), Math.ceil(maxValue * 0.33), 0];
});
const trendPulseShortText = computed(() => {
  if (trendPulseTitle.value === '存在明显评分高峰') return '有高峰';
  if (trendPulseTitle.value === '评分节奏总体平稳') return '平稳';
  if (trendPulseTitle.value === '近期评分活跃度偏低') return '偏低';
  return '暂无';
});
const peakTrendItem = computed(() => {
  if (!trend.value.length) {
    return null;
  }
  return trend.value.reduce((peak, item) => item.scoreCount > peak.scoreCount ? item : peak, trend.value[0]);
});
const peakTrendValue = computed(() => peakTrendItem.value?.scoreCount ?? 0);
const peakTrendDate = computed(() => peakTrendItem.value ? peakTrendItem.value.date.slice(5) : '暂无');
const peakTrendHint = computed(() => {
  if (!peakTrendItem.value) {
    return '当前还没有形成可对比的评分峰值。';
  }
  return `${peakTrendItem.value.date.slice(5)} 共完成 ${peakTrendItem.value.scoreCount} 条评分记录，均分 ${peakTrendItem.value.avgScore.toFixed(1)}。`;
});
const urgentTaskCount = computed(() => recentTasks.value.filter(task => /待|催|预警|逾期|异常|未/.test(`${task.label}${task.title}`)).length);
const distinctActorsCount = computed(() => new Set(recentTasks.value.map(task => task.actor).filter(Boolean)).size);
const riskAlertCount = computed(() => recentTasks.value.filter(task => /待|催|预警|逾期|异常|未|风险/.test(`${task.label}${task.title}`)).length);
const taskPressureLevel = computed(() => {
  if (!recentTasks.value.length) return '低负载';
  if (recentTasks.value.length >= 5 || urgentTaskCount.value >= 3) return '高负载';
  if (recentTasks.value.length >= 3 || urgentTaskCount.value >= 1) return '需跟进';
  return '稳态巡视';
});
const taskPressureText = computed(() => {
  if (!recentTasks.value.length) {
    return '当前任务列表为空，可优先巡视班级、作业和实验数据。';
  }
  if (taskPressureLevel.value === '高负载') {
    return '待办数量较高，建议先处理红色风险或临近截止事项。';
  }
  if (taskPressureLevel.value === '需跟进') {
    return '已有待办堆积，建议优先进入提交队列或相关管理页清理。';
  }
  return '当前待办压力可控，适合按既定节奏处理。';
});
const workbenchHeadline = computed(() => {
  if (urgentTaskCount.value) {
    return `当前有 ${urgentTaskCount.value} 项优先任务需要尽快处理`;
  }
  if (recentTasks.value.length) {
    return `当前共 ${recentTasks.value.length} 项待办，工作台已为你集中收口`;
  }
  if (trendTotalCount.value) {
    return `近 7 日已完成 ${trendTotalCount.value} 条评分记录，工作节奏整体稳定`;
  }
  return '当前暂无集中待办，可从快捷入口继续巡视班级与教学活动';
});
const workbenchSubline = computed(() => {
  if (recentTasks.value.length) {
    return `涉及 ${distinctActorsCount.value || 1} 个对象与 ${quickLinks.value.length} 个高频入口，适合从首页直接切入处理。`;
  }
  if (quickLinks.value.length) {
    return `已收口 ${quickLinks.value.length} 个常用管理入口，可继续进入班级、作业、实验或考试相关页面。`;
  }
  return '当前教师工作台已加载完成，但暂未返回更多任务或聚合摘要。';
});
const heroStatusText = computed(() => {
  if (loading.value) return '工作台同步中';
  if (loadError.value) return '数据加载异常';
  if (!loaded.value) return '等待数据回流';
  if (riskAlertCount.value) return `当前风险 ${riskAlertCount.value} 项`;
  return '当前状态稳定';
});
const trendInsightText = computed(() => {
  if (!trend.value.length) {
    return '暂无近 7 日评分趋势。';
  }
  return `最高峰出现在 ${peakTrendDate.value}，单日 ${peakTrendValue.value} 条。`;
});
const trendPulseTitle = computed(() => {
  if (!trend.value.length) return '暂无评分波动';
  if (peakTrendValue.value >= trendAverageValue.value * 1.5 && peakTrendValue.value >= 3) return '存在明显评分高峰';
  if (trendAverageValue.value >= 1) return '评分节奏总体平稳';
  return '近期评分活跃度偏低';
});
const trendPulseDescription = computed(() => {
  if (!trend.value.length) {
    return '等待真实趋势数据回流后再形成节奏判断。';
  }
  if (trendPulseTitle.value === '存在明显评分高峰') {
    return '建议结合待办列表复核高峰日前后的批改集中区段，避免遗漏异常提交。';
  }
  if (trendPulseTitle.value === '评分节奏总体平稳') {
    return '近一周评分记录分布较均衡，可继续按照日常批改节奏推进。';
  }
  return '近期评分记录较少，建议结合作业、实验和考试入口检查是否存在未进入队列的数据。';
});
const trendFocusTitle = computed(() => {
  if (urgentTaskCount.value) return '先清理优先任务';
  if (peakTrendItem.value) return `${peakTrendDate.value} 评分峰值需复盘`;
  return '优先巡视高频入口';
});
const trendFocusDescription = computed(() => {
  if (urgentTaskCount.value) {
    return '当前首页已出现明确待办压力，建议先从待办与提醒面板进入对应页面处理。';
  }
  if (peakTrendItem.value) {
    return `峰值日均分 ${peakTrendItem.value.avgScore.toFixed(1)}，可进一步结合业务页核对评分结果和提交质量。`;
  }
  return '如果暂无显著峰值或任务，可从快捷入口继续巡视班级、题库和提交列表。';
});
const quickLinkSummary = computed(() => {
  if (!quickLinks.value.length) return '暂无入口摘要';
  const totalBadge = quickLinks.value.reduce((sum, item) => sum + item.badge, 0);
  return `共 ${quickLinks.value.length} 个入口，累计 ${totalBadge} 项待处理`; 
});
const quickLinkLoadText = computed(() => {
  if (!quickLinks.value.length) {
    return '当前没有返回快捷入口数据。';
  }
  const busiest = quickLinks.value.reduce((target, item) => item.badge > target.badge ? item : target, quickLinks.value[0]);
  return `${busiest.label} 当前待处理 ${busiest.badge} 项，适合作为下一步进入点。`;
});
const riskAlertSummary = computed(() => {
  if (!recentTasks.value.length) return '当前无待处理风险';
  if (!riskAlertCount.value) return '待办存在但风险可控';
  return `当前识别出 ${riskAlertCount.value} 项重点提醒`;
});
const riskAlertDetail = computed(() => {
  if (!recentTasks.value.length) {
    return '当前首页未返回待办任务，可继续按模块巡视整体状态。';
  }
  if (!riskAlertCount.value) {
    return '虽然已有待办，但暂未出现明显逾期、催办或异常类风险。';
  }
  return '建议优先处理带有待办、预警、逾期或异常语义的事项，减少后续积压。';
});
const actorCoverageText = computed(() => {
  if (!recentTasks.value.length) {
    return '暂无任务对象需要跟进。';
  }
  if (distinctActorsCount.value <= 1) {
    return '任务集中在少量对象上，适合快速清理。';
  }
  if (distinctActorsCount.value >= 4) {
    return '任务分散在多个对象上，建议按任务标签先做优先级分层。';
  }
  return '任务覆盖多个对象，适合先按业务模块进入处理。';
});
const recommendedActionTitle = computed(() => {
  if (quickLinks.value.length) {
    const busiest = quickLinks.value.reduce((target, item) => item.badge > target.badge ? item : target, quickLinks.value[0]);
    return `优先进入“${busiest.label}”`;
  }
  if (recentTasks.value.length) {
    return '优先处理首页待办';
  }
  return '继续巡视教学模块';
});
const recommendedActionDescription = computed(() => {
  if (quickLinks.value.length) {
    const busiest = quickLinks.value.reduce((target, item) => item.badge > target.badge ? item : target, quickLinks.value[0]);
    return `${busiest.label} 的待处理量当前最高，可先从该入口切入，缩短后续回访成本。`;
  }
  if (recentTasks.value.length) {
    return '虽然缺少入口负载摘要，但首页待办已经给出明确处理方向。';
  }
  return '当前没有明显积压项，可按班级、作业、实验或分析页顺序继续巡视。';
});
const riskActionTitle = computed(() => {
  if (!riskAlertCount.value) return '维持常规巡视';
  if (riskAlertCount.value >= 3) return '先清理高风险事项';
  return '优先处理提醒项';
});
const riskActionDescription = computed(() => {
  if (!riskAlertCount.value) {
    return '当前没有明显风险告警，可按既定节奏推进班级、作业和实验管理。';
  }
  if (riskAlertCount.value >= 3) {
    return '风险提醒已形成堆积，建议优先进入待办列表逐项消化，再回到常规巡视。';
  }
  return '当前风险数量有限，适合结合首页待办快速清理，避免升级为更高优先级问题。';
});

const localizedSummarySignal = (key: string) => {
  if (/student|class|member/i.test(key)) return '在册概览';
  if (/grade|score|trend/i.test(key)) return '教学节奏';
  if (/submission|task|todo/i.test(key)) return '等待处理';
  if (/alert|risk|warn/i.test(key)) return '风险关注';
  return '工作摘要';
};

const loadDashboard = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const data = await getTeacherDashboard();
    summaryCards.value = data.summaryCards ?? [];
    trend.value = data.trend ?? [];
    recentTasks.value = data.recentTasks ?? [];
    quickLinks.value = data.quickLinks ?? [];
    loaded.value = true;
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '教师工作台加载失败';
  } finally {
    loading.value = false;
  }
};

const padIndex = (value: number) => String(value).padStart(2, '0');

const quickLinkDescription = (link: QuickLink) => {
  if (link.badge > 0) {
    return `当前待处理 ${link.badge} 项，可直接进入对应管理页继续处理。`;
  }
  return '当前没有积压项，可进入该模块继续巡视整体状态。';
};

const localizedQuickLinkLabel = (label: string) => translateDashboardText(label);
const localizedTaskLabel = (label: string) => translateDashboardText(label);
const localizedTaskTitle = (title: string) => translateDashboardText(title);
const localizedActorText = (actor: string) => translateDashboardText(actor);

onMounted(loadDashboard);
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dashboard-header__eyebrow {
  margin-bottom: 6px;
  color: #0f766e;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.dashboard-grid--refactored {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(340px, 0.95fr);
  gap: 14px;
}

.dashboard-main--refactored,
.dashboard-side--refactored {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dashboard-panel {
  border: 1px solid #dbe4f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  overflow: hidden;
}

.dashboard-inline-note {
  max-width: 20rem;
  color: #64748b;
  font-size: 12px;
  line-height: 1.55;
  text-align: right;
}

.dashboard-summary-strip {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 14px 16px 16px;
}

.dashboard-kpi-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}

.dashboard-kpi-row__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #e2e8f0;
  color: #0f766e;
  flex-shrink: 0;
}

.dashboard-kpi-row__body {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.dashboard-kpi-row__topline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.dashboard-kpi-row__body strong {
  margin-top: 6px;
  color: #0f172a;
  font-size: 22px;
  font-weight: 800;
}

.dashboard-kpi-row__body small {
  margin-top: 4px;
  color: #64748b;
  font-size: 11px;
}

.dashboard-card,
.state-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.dashboard-card :deep(.el-card__header) {
  border-bottom: 1px solid #e2e8f0;
  padding: 14px 16px;
}

.dashboard-card :deep(.el-card__body) {
  padding: 14px 16px 16px;
}

.dashboard-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.dashboard-card__header--stacked {
  align-items: flex-start;
}

.dashboard-card__header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.dashboard-card__metrics {
  display: flex;
  gap: 10px;
}

.dashboard-card__metric {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 82px;
  padding: 9px 11px;
  border-radius: 16px;
  background: #f8fafc;
}

.dashboard-card__metric--accent {
  background: linear-gradient(180deg, #ecfeff, #ccfbf1);
}

.dashboard-card__metric span {
  font-size: 11px;
  color: #64748b;
}

.dashboard-card__metric strong {
  margin-top: 4px;
  font-size: 20px;
  color: #0f172a;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.trend-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trend-chart-shell {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 12px;
  height: 220px;
  padding: 12px 10px 10px 0;
  border-radius: 18px;
  border: 1px solid #dbe4f0;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(255, 255, 255, 0.98));
  overflow: hidden;
}

.trend-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  height: 100%;
  padding: 2px 0 24px;
}

.trend-y-axis span {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
}

.trend-chart {
  position: relative;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 100%;
  min-height: 0;
  padding-top: 2px;
}

.trend-chart__grid {
  position: absolute;
  inset: 0 0 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  pointer-events: none;
}

.trend-chart__grid-line {
  display: block;
  width: 100%;
  border-top: 1px dashed rgba(148, 163, 184, 0.34);
}

.trend-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 0;
  z-index: 1;
}

.trend-bar-value {
  font-size: 16px;
  color: #0f172a;
}

.trend-bar-track {
  width: 22px;
  height: 148px;
  display: flex;
  align-items: flex-end;
  border-radius: 999px;
  border: 1px solid rgba(203, 213, 225, 0.7);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(241, 245, 249, 0.45));
  padding: 0 0 6px;
}

.trend-bar-fill {
  width: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(20, 184, 166, 0.5), rgba(15, 118, 110, 0.86));
  box-shadow: 0 10px 18px rgba(15, 118, 110, 0.14);
}

.trend-chart__line {
  position: absolute;
  inset: 0 0 24px;
  pointer-events: none;
  z-index: 2;
}

.trend-chart__line-path {
  fill: none;
  stroke: rgba(15, 118, 110, 0.88);
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.trend-chart__line-dot {
  fill: #ffffff;
  stroke: #0f766e;
  stroke-width: 1.5;
  filter: drop-shadow(0 2px 5px rgba(15, 118, 110, 0.18));
}

.trend-bar-label {
  font-size: 12px;
  color: #334155;
  font-weight: 700;
}

.trend-bar-meta {
  font-size: 11px;
  color: #64748b;
}

.trend-summary-grid {
  display: grid;
  gap: 12px;
}

.trend-summary-grid--compact {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.trend-summary-card {
  padding: 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fafc, #ffffff);
  border: 1px solid #e2e8f0;
}

.trend-summary-card__label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #64748b;
}

.trend-summary-card strong {
  display: block;
  margin-top: 8px;
  font-size: 17px;
  color: #0f172a;
}

.trend-summary-card p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #64748b;
}

.quick-link-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-link-grid--single {
  grid-template-columns: 1fr;
}

.quick-link-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 112px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #ffffff, #f8fafc);
  border-radius: 14px;
  padding: 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.quick-link-card--compact {
  min-height: 88px;
}

.quick-link-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.quick-link-card__topline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.quick-link-card__title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.quick-link-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
  font-size: 13px;
  font-weight: 700;
}

.quick-link-card p {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.65;
}

.quick-link-card__footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  gap: 12px;
}

.task-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-panel--compact {
  gap: 10px;
}

.task-overview-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.task-overview-strip--compact {
  gap: 8px;
}

.task-overview-item {
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.task-overview-item__label {
  font-size: 11px;
  color: #64748b;
}

.task-overview-item strong {
  display: block;
  margin-top: 4px;
  font-size: 16px;
  color: #0f172a;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.task-item__badge {
  width: 10px;
  border-radius: 999px;
  background: linear-gradient(180deg, #ef4444, #f97316);
  flex-shrink: 0;
}

.task-item__content {
  min-width: 0;
  flex: 1;
}

.task-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.task-item__header strong {
  color: #0f172a;
  font-size: 13px;
}

.task-item__index {
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
}

.task-item__title {
  margin: 6px 0 0;
  color: #334155;
  font-size: 12px;
  line-height: 1.45;
}

.task-item__meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  font-size: 12px;
  color: #64748b;
}

.task-item__link {
  padding: 0;
  border: none;
  background: transparent;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.guidance-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.guidance-list--compact {
  gap: 10px;
}

.guidance-item {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.guidance-item__title {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #64748b;
}

.guidance-item strong {
  display: block;
  margin-top: 6px;
  font-size: 16px;
  line-height: 1.35;
  color: #0f172a;
}

.guidance-item p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.guidance-item--warn {
  background: linear-gradient(180deg, #fff7ed, #fffbeb);
}

.state-card {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
}

.dashboard-side--refactored {
  align-self: start;
}

@media (max-width: 1240px) {
  .dashboard-summary-strip,
  .trend-summary-grid--compact,
  .dashboard-grid--refactored {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .dashboard-inline-note {
    max-width: none;
    text-align: left;
  }

  .dashboard-card__header,
  .dashboard-card__header--stacked {
    flex-direction: column;
    align-items: flex-start;
  }

  .dashboard-card__metrics,
  .quick-link-grid,
  .trend-summary-grid,
  .task-overview-strip,
  .dashboard-summary-strip {
    grid-template-columns: 1fr;
  }

  .dashboard-card__metrics {
    width: 100%;
    display: grid;
  }

  .trend-chart-shell {
    grid-template-columns: 1fr;
    height: auto;
    padding-left: 14px;
  }

  .trend-y-axis {
    display: none;
  }

  .trend-chart {
    height: 220px;
  }
}

@media (max-width: 768px) {
  .quick-link-grid,
  .trend-summary-grid,
  .task-overview-strip,
  .dashboard-summary-strip {
    grid-template-columns: 1fr;
  }

  .dashboard-card :deep(.el-card__header),
  .dashboard-card :deep(.el-card__body) {
    padding-left: 18px;
    padding-right: 18px;
  }

  .trend-chart {
    gap: 10px;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .trend-bar-item {
    min-width: 66px;
  }
}
</style>

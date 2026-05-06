export interface AnalysisSummaryCard {
  key: string;
  title: string;
  value: number | string;
  description: string;
}

export interface TeacherTrendItem {
  date: string;
  scoreCount: number;
  avgScore: number;
}

export interface TeacherComparisonItem {
  className: string;
  avgScore: number;
  gradedCount: number;
}

export interface TeacherBusinessBreakdownItem {
  businessType: 'LAB' | 'HOMEWORK' | 'EXAM';
  label: string;
  avgScore: number;
  scoreCount: number;
}

export interface AnalysisInsight {
  tone: 'positive' | 'highlight' | 'warning';
  title: string;
  description: string;
}

export interface TeacherAnalysisOverview {
  summaryCards: AnalysisSummaryCard[];
  trend: TeacherTrendItem[];
  recentTasks: Array<{
    label: string;
    title: string;
    actor: string;
    eventTime: string | null;
    route: string;
  }>;
  quickLinks: Array<{
    label: string;
    route: string;
    badge: number;
  }>;
  sectionComparison: TeacherComparisonItem[];
  businessBreakdown: TeacherBusinessBreakdownItem[];
  insights: AnalysisInsight[];
}

export interface StudentScoreRecord {
  id: number;
  businessType: 'LAB' | 'HOMEWORK' | 'EXAM';
  businessName: string;
  score: number;
  gradedAt: string | null;
}

export interface StudentGroupedScore {
  businessType: 'LAB' | 'HOMEWORK' | 'EXAM';
  label: string;
  avgScore: number;
  scoreCount: number;
}

export interface StudentScoreOverview {
  summaryCards: AnalysisSummaryCard[];
  groupedScores: StudentGroupedScore[];
  recentRecords: StudentScoreRecord[];
  feedbackNotes: AnalysisInsight[];
  completedItems?: number;
  totalItems?: number;
}

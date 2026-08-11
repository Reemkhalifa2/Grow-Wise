import { GoalStatus } from '../models/portfolio-models';

export interface GoalStatusResult {
  status: GoalStatus;
  label: string;
}

/**
 * Compares the monthly contribution actually required to reach a goal's
 * target date against the linked plan's current monthly investment
 * amount. Returns null when there isn't enough real data to make the
 * call (no target date, no linked plan) rather than guessing.
 */
export function computeGoalStatus(
  targetAmount: number | null | undefined,
  currentAmount: number | null | undefined,
  targetDate: string | null | undefined,
  monthlyInvestmentAmount: number | null | undefined
): GoalStatusResult | null {

  if (!targetAmount || targetAmount <= 0 || !targetDate) {
    return null;
  }

  const remaining = Math.max(targetAmount - (currentAmount ?? 0), 0);

  if (remaining <= 0) {
    return { status: 'ON_TRACK', label: 'On Track' };
  }

  const now = new Date();
  const target = new Date(targetDate);

  const monthsRemaining =
    (target.getFullYear() - now.getFullYear()) * 12 +
    (target.getMonth() - now.getMonth());

  if (monthsRemaining <= 0) {
    return { status: 'BEHIND', label: 'Behind' };
  }

  if (!monthlyInvestmentAmount || monthlyInvestmentAmount <= 0) {
    return null;
  }

  const requiredMonthly = remaining / monthsRemaining;

  if (requiredMonthly <= monthlyInvestmentAmount * 1.05) {
    return { status: 'ON_TRACK', label: 'On Track' };
  }

  if (requiredMonthly <= monthlyInvestmentAmount * 1.5) {
    return { status: 'NEEDS_ATTENTION', label: 'Needs Attention' };
  }

  return { status: 'BEHIND', label: 'Behind' };
}

/** Best-effort icon keyword match for a goal name, falling back to a generic target icon. */
export function goalIconKey(goalName: string | null | undefined): 'car' | 'home' | 'travel' | 'education' | 'savings' | 'target' {
  const name = (goalName ?? '').toLowerCase();

  if (/\bcar\b|vehicle|auto/.test(name)) return 'car';
  if (/home|house|apartment|property/.test(name)) return 'home';
  if (/travel|trip|vacation|holiday|flight/.test(name)) return 'travel';
  if (/education|school|university|college|tuition|study/.test(name)) return 'education';
  if (/saving|emergency|fund/.test(name)) return 'savings';

  return 'target';
}

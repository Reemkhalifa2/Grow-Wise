declare const Chart3: any;

const API_BASE_URL3 = "http://localhost:3000";
const DASHBOARD_ENDPOINT = `${API_BASE_URL}/api/dashboard`;


interface DashboardResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  stats: {
    totalInvested: { value: number; deltaLabel: string };
    portfolioValue: { value: number; deltaLabel: string };
    totalProfit: { value: number; deltaLabel: string };
    monthlyCapacity: { value: number; deltaLabel: string };
  };
  growthChart: {
    labels: string[];
    portfolioValues: number[];
    contributionValues: number[];
  };
  allocation: {
    label: string;
    percent: number;
    color: string;
  }[];
  goal: {
    label: string;
    currentAmount: number;
    targetAmount: number;
    percent: number;
    status: string;
    estimate: string;
  };
  aiPlan: {
    monthlyAmount: number;
    description: string;
    split: { label: string; percent: number }[];
  };
}

// ---- DOM references ----

const userNameEl3 = document.getElementById("userName") as HTMLDivElement;
const userRoleEl3 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl3 = document.getElementById("userAvatar") as HTMLDivElement;
const topbarDateEl3 = document.getElementById("topbarDate") as HTMLDivElement;
const greetingEl = document.getElementById("greeting") as HTMLHeadingElement;
const greetingSubEl = document.getElementById("greetingSub") as HTMLParagraphElement;

const statInvestedValue = document.getElementById("statInvestedValue") as HTMLDivElement;
const statInvestedDelta = document.getElementById("statInvestedDelta") as HTMLDivElement;
const statPortfolioValue = document.getElementById("statPortfolioValue") as HTMLDivElement;
const statPortfolioDelta = document.getElementById("statPortfolioDelta") as HTMLDivElement;
const statProfitValue = document.getElementById("statProfitValue") as HTMLDivElement;
const statProfitDelta = document.getElementById("statProfitDelta") as HTMLDivElement;
const statCapacityValue = document.getElementById("statCapacityValue") as HTMLDivElement;
const statCapacityDelta = document.getElementById("statCapacityDelta") as HTMLDivElement;

const allocationLegend = document.getElementById("allocationLegend") as HTMLUListElement;

const goalAmountEl = document.getElementById("goalAmount") as HTMLDivElement;
const goalOfEl = document.getElementById("goalOf") as HTMLSpanElement;
const goalBadgeEl = document.getElementById("goalBadge") as HTMLDivElement;
const goalProgressFillEl = document.getElementById("goalProgressFill") as HTMLDivElement;
const goalStatusEl = document.getElementById("goalStatus") as HTMLSpanElement;
const goalEstimateEl = document.getElementById("goalEstimate") as HTMLSpanElement;

const aiHeadlineEl3 = document.getElementById("aiHeadline") as HTMLDivElement;
const aiSubEl3 = document.getElementById("aiSub") as HTMLParagraphElement;
const aiSplitEl3 = document.getElementById("aiSplit") as HTMLDivElement;

// ---- Helpers ----

function formatCurrency(value: number): string {
  return `$${value.toLocaleString()}`;
}

function formatTopbarDate3(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "Good morning";
  if (hour < 18) return "Good afternoon";
  return "Good evening";
}

async function fetchDashboardData(): Promise<DashboardResponse> {
  const response = await fetch(DASHBOARD_ENDPOINT, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Server responded with ${response.status}`);
  }

  return (await response.json()) as DashboardResponse;
}

// ---- Render functions ----

function renderUser1(data: DashboardResponse): void {
  userNameEl.textContent = data.user.name;
  userRoleEl.textContent = data.user.role;
  userAvatarEl.textContent = data.user.initials;
  greetingEl.textContent = `${getGreeting()}, ${data.user.name.split(" ")[0]}`;
}

function renderStats(data: DashboardResponse): void {
  statInvestedValue.textContent = formatCurrency(data.stats.totalInvested.value);
  statInvestedDelta.textContent = data.stats.totalInvested.deltaLabel;

  statPortfolioValue.textContent = formatCurrency(data.stats.portfolioValue.value);
  statPortfolioDelta.textContent = data.stats.portfolioValue.deltaLabel;

  statProfitValue.textContent = `+${formatCurrency(data.stats.totalProfit.value)}`;
  statProfitDelta.textContent = data.stats.totalProfit.deltaLabel;

  statCapacityValue.textContent = formatCurrency(data.stats.monthlyCapacity.value);
  statCapacityDelta.textContent = data.stats.monthlyCapacity.deltaLabel;
}

function renderAllocationLegend(data: DashboardResponse): void {
  allocationLegend.innerHTML = "";
  data.allocation.forEach((item) => {
    const li = document.createElement("li");
    li.innerHTML = `<span class="dot" style="background:${item.color}"></span>${item.label} <b>${item.percent}%</b>`;
    allocationLegend.appendChild(li);
  });
}

function renderGoal(data: DashboardResponse): void {
  goalAmountEl.childNodes[0].textContent = formatCurrency(data.goal.currentAmount) + " ";
  goalOfEl.textContent = `of ${formatCurrency(data.goal.targetAmount)}`;
  goalBadgeEl.textContent = `${data.goal.percent}%`;
  goalProgressFillEl.style.width = `${data.goal.percent}%`;
  goalStatusEl.textContent = data.goal.status;
  goalEstimateEl.textContent = `Estimated: ${data.goal.estimate}`;
}

function renderAiPlan(data: DashboardResponse): void {
  aiHeadlineEl.textContent = `Invest ${formatCurrency(data.aiPlan.monthlyAmount)} monthly`;
  aiSubEl.textContent = data.aiPlan.description;
  aiSplitEl.innerHTML = "";
  data.aiPlan.split.forEach((item) => {
    const div = document.createElement("div");
    div.className = "ai-split-item";
    div.innerHTML = `<div class="ai-split-value">${item.percent}%</div><div class="ai-split-label">${item.label}</div>`;
    aiSplitEl.appendChild(div);
  });
}

function renderGrowthChart(data: DashboardResponse): void {
  const growthCanvas = document.getElementById("growthChart") as HTMLCanvasElement;
  const growthCtx = growthCanvas.getContext("2d") as CanvasRenderingContext2D;

  const growthGradient = growthCtx.createLinearGradient(0, 0, 0, 220);
  growthGradient.addColorStop(0, "rgba(20, 106, 79, 0.18)");
  growthGradient.addColorStop(1, "rgba(20, 106, 79, 0)");

  new Chart(growthCtx, {
    type: "line",
    data: {
      labels: data.growthChart.labels,
      datasets: [
        {
          label: "Portfolio value",
          data: data.growthChart.portfolioValues,
          borderColor: "#146a4f",
          backgroundColor: growthGradient,
          borderWidth: 2.5,
          pointRadius: 0,
          pointHoverRadius: 4,
          tension: 0.35,
          fill: true,
        },
        {
          label: "Contributions",
          data: data.growthChart.contributionValues,
          borderColor: "#c7cdd6",
          borderDash: [4, 4],
          borderWidth: 1.5,
          pointRadius: 0,
          tension: 0.2,
          fill: false,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: "index", intersect: false },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: "#1a2233",
          padding: 10,
          cornerRadius: 8,
          callbacks: {
            label: (ctx: any) => ` $${ctx.parsed.y.toLocaleString()}`,
          },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: "#7b8494", font: { size: 11 } },
        },
        y: {
          ticks: {
            color: "#7b8494",
            font: { size: 11 },
            callback: (value: number) => `$${value / 1000}k`,
          },
          grid: { color: "#eef0f3" },
        },
      },
    },
  });
}

function renderAllocationChart(data: DashboardResponse): void {
  const allocationCanvas = document.getElementById("allocationChart") as HTMLCanvasElement;
  const allocationCtx = allocationCanvas.getContext("2d") as CanvasRenderingContext2D;

  new Chart(allocationCtx, {
    type: "doughnut",
    data: {
      labels: data.allocation.map((a) => a.label),
      datasets: [
        {
          data: data.allocation.map((a) => a.percent),
          backgroundColor: data.allocation.map((a) => a.color),
          borderWidth: 0,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: "72%",
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: "#1a2233",
          padding: 10,
          cornerRadius: 8,
          callbacks: {
            label: (ctx: any) => ` ${ctx.label}: ${ctx.parsed}%`,
          },
        },
      },
    },
  });
}

// ---- active state ----

function setupNav1(): void {
  const navItems = document.querySelectorAll<HTMLAnchorElement>(".nav-item");
  navItems.forEach((item) => {
    item.addEventListener("click", (event: MouseEvent) => {
      event.preventDefault();
      navItems.forEach((el) => el.classList.remove("active"));
      item.classList.add("active");
    });
  });
}

// ---- Init ----

async function init3(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav1();

  try {
    const data = await fetchDashboardData();
    //renderUser(data);//
    renderStats(data);
    renderAllocationLegend(data);
    renderGoal(data);
    renderAiPlan(data);
    renderGrowthChart(data);
    renderAllocationChart(data);
  } catch (err) {
    console.error("Failed to load dashboard data:", err);
    greetingEl.textContent = "Could not load your dashboard";
    greetingSubEl.textContent = "Check that the backend is running and reachable, then refresh.";
  }
}

init1();
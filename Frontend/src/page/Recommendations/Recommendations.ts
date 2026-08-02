declare const Chart: any;

const API_BASE_URL11 = "http://localhost:3000";
const AI_PLAN_ENDPOINT = `${API_BASE_URL}/api/ai-plan`;


interface AiPlanResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  subtitle: string;
  plan: {
    monthlyAmount: number;
    description: string;
    split: { label: string; percent: number }[];
  };
  allocation: {
    label: string;
    percent: number;
    color: string;
  }[];
  whyThisFits: string[];
  historicalScenario: {
    rangeLabel: string; 
    rangeSub: string; 
    note: string;
  };
}

// ---- DOM references ----

const topbarDateEl = document.getElementById("topbarDate") as HTMLDivElement;
const userNameEl = document.getElementById("userName") as HTMLDivElement;
const userRoleEl = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl = document.getElementById("userAvatar") as HTMLDivElement;
const planSubtitleEl = document.getElementById("planSubtitle") as HTMLParagraphElement;

const aiHeadlineEl = document.getElementById("aiHeadline") as HTMLDivElement;
const aiSubEl = document.getElementById("aiSub") as HTMLParagraphElement;
const aiSplitEl = document.getElementById("aiSplit") as HTMLDivElement;

const allocationLegendEl = document.getElementById("allocationLegend") as HTMLUListElement;

const fitListEl = document.getElementById("fitList") as HTMLUListElement;

const scenarioRangeEl = document.getElementById("scenarioRange") as HTMLDivElement;
const scenarioSubEl = document.getElementById("scenarioSub") as HTMLDivElement;
const scenarioNoteEl = document.getElementById("scenarioNote") as HTMLParagraphElement;

// ---- Helpers ----

function formatCurrency11(value: number): string {
  return `$${value.toLocaleString()}`;
}

function formatTopbarDate11(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

// ---- API call ----

async function fetchAiPlan(): Promise<AiPlanResponse> {
  const response = await fetch(AI_PLAN_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as AiPlanResponse;
}

// ---- Rendering ----

function renderUser11(user: AiPlanResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderPlan(data: AiPlanResponse): void {
  planSubtitleEl.textContent = data.subtitle;

  aiHeadlineEl.textContent = `Invest ${formatCurrency(data.plan.monthlyAmount)} monthly`;
  aiSubEl.textContent = data.plan.description;

  aiSplitEl.innerHTML = "";
  data.plan.split.forEach((item) => {
    const div = document.createElement("div");
    div.className = "ai-split-item";
    div.innerHTML = `<div class="ai-split-value">${item.percent}%</div><div class="ai-split-label">${item.label}</div>`;
    aiSplitEl.appendChild(div);
  });
}

function renderAllocationLegend11(allocation: AiPlanResponse["allocation"]): void {
  allocationLegendEl.innerHTML = "";
  allocation.forEach((item) => {
    const li = document.createElement("li");
    li.innerHTML = `<span class="dot" style="background:${item.color}"></span>${item.label} <b>${item.percent}%</b>`;
    allocationLegendEl.appendChild(li);
  });
}

function renderAllocationChart11(allocation: AiPlanResponse["allocation"]): void {
  const canvas = document.getElementById("allocationChart") as HTMLCanvasElement;
  const ctx = canvas.getContext("2d") as CanvasRenderingContext2D;

  new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: allocation.map((a) => a.label),
      datasets: [
        {
          data: allocation.map((a) => a.percent),
          backgroundColor: allocation.map((a) => a.color),
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

function renderFitList(items: string[]): void {
  fitListEl.innerHTML = "";
  items.forEach((text) => {
    const li = document.createElement("li");
    li.innerHTML = `
      <svg class="fit-check" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.6"/><path d="M8 12.5l2.5 2.5L16 9.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      <span>${text}</span>
    `;
    fitListEl.appendChild(li);
  });
}

function renderScenario(scenario: AiPlanResponse["historicalScenario"]): void {
  scenarioRangeEl.textContent = scenario.rangeLabel;
  scenarioSubEl.textContent = scenario.rangeSub;
  scenarioNoteEl.textContent = scenario.note;
}

// ---- Sidebar navigation (active state) ----

function setupNav11(): void {
  const navItems = document.querySelectorAll<HTMLAnchorElement>(".nav-item");
  navItems.forEach((item) => {
    item.addEventListener("click", (event: MouseEvent) => {
      if (item.getAttribute("href") !== "#") return; // allow real page links to navigate
      event.preventDefault();
      navItems.forEach((el) => el.classList.remove("active"));
      item.classList.add("active");
    });
  });
}

// ---- Init ----

async function init11(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();

  try {
    const data = await fetchAiPlan();
    renderUser(data.user);
    renderPlan(data);
    renderAllocationLegend11(data.allocation);
    renderAllocationChart11(data.allocation);
    renderFitList(data.whyThisFits);
    renderScenario(data.historicalScenario);
  } catch (err) {
    console.error("Failed to load AI plan data:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
    planSubtitleEl.textContent = "Could not load your plan. Check that the backend is running, then refresh.";
  }
}

init();
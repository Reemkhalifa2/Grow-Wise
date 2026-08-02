declare const Chart10: any;

const API_BASE_URL10 = "http://localhost:3000";
const PORTFOLIO_ENDPOINT = `${API_BASE_URL}/api/portfolio`;


interface Holding {
  symbol: string;
  name: string;
  subtitle: string; 
  amount: number;
  changePercent: number; 
}

interface PortfolioResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  stats: {
    invested: number;
    currentValue: number;
    currentValueDeltaLabel: string;
    profit: number;
    profitDeltaLabel: string;
  };
  growthChart: {
    labels: string[];
    portfolioValues: number[];
    contributionValues: number[];
  };
  holdings: Holding[];
}

// ---- DOM references ----

const topbarDateEl10 = document.getElementById("topbarDate") as HTMLDivElement;
const userNameEl10 = document.getElementById("userName") as HTMLDivElement;
const userRoleEl10 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl10 = document.getElementById("userAvatar") as HTMLDivElement;

const investedValueEl = document.getElementById("investedValue") as HTMLDivElement;
const currentValueEl = document.getElementById("currentValue") as HTMLDivElement;
const currentValueDeltaEl = document.getElementById("currentValueDelta") as HTMLDivElement;
const profitValueEl = document.getElementById("profitValue") as HTMLDivElement;
const profitDeltaEl = document.getElementById("profitDelta") as HTMLDivElement;

const holdingsListEl = document.getElementById("holdingsList") as HTMLUListElement;

// ---- Helpers ----

function formatCurrency10(value: number): string {
  return `$${Math.round(value).toLocaleString()}`;
}

function formatTopbarDate10(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

// ---- API call ----

async function fetchPortfolio(): Promise<PortfolioResponse> {
  const response = await fetch(PORTFOLIO_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as PortfolioResponse;
}

// ---- Rendering ----

function renderUser10(user: PortfolioResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderStats10(stats: PortfolioResponse["stats"]): void {
  investedValueEl.textContent = formatCurrency(stats.invested);
  currentValueEl.textContent = formatCurrency(stats.currentValue);
  currentValueDeltaEl.textContent = stats.currentValueDeltaLabel;
  profitValueEl.textContent = `+${formatCurrency(stats.profit)}`;
  profitDeltaEl.textContent = stats.profitDeltaLabel;
}

function renderHoldingRow(holding: Holding): HTMLLIElement {
  const li = document.createElement("li");
  li.className = "holding-row";
  const changeClass = holding.changePercent >= 0 ? "positive" : "negative";
  const changeSign = holding.changePercent >= 0 ? "+" : "";

  li.innerHTML = `
    <div class="holding-badge">${holding.symbol}</div>
    <div class="holding-info">
      <div class="holding-name">${holding.name}</div>
      <div class="holding-sub">${holding.subtitle}</div>
    </div>
    <div class="holding-figures">
      <div class="holding-amount">${formatCurrency(holding.amount)}</div>
      <div class="holding-change ${changeClass}">${changeSign}${holding.changePercent.toFixed(1)}%</div>
    </div>
  `;
  return li;
}

function renderHoldings(holdings: Holding[]): void {
  holdingsListEl.innerHTML = "";

  if (holdings.length === 0) {
    const li = document.createElement("li");
    li.className = "table-message";
    li.textContent = "No holdings yet.";
    holdingsListEl.appendChild(li);
    return;
  }

  holdings.forEach((holding) => {
    holdingsListEl.appendChild(renderHoldingRow(holding));
  });
}

function renderGrowthChart10(growthChart: PortfolioResponse["growthChart"]): void {
  const growthCanvas = document.getElementById("growthChart") as HTMLCanvasElement;
  const growthCtx = growthCanvas.getContext("2d") as CanvasRenderingContext2D;

  const growthGradient = growthCtx.createLinearGradient(0, 0, 0, 220);
  growthGradient.addColorStop(0, "rgba(20, 106, 79, 0.18)");
  growthGradient.addColorStop(1, "rgba(20, 106, 79, 0)");

  new Chart(growthCtx, {
    type: "line",
    data: {
      labels: growthChart.labels,
      datasets: [
        {
          label: "Portfolio value",
          data: growthChart.portfolioValues,
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
          data: growthChart.contributionValues,
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

// ---- Sidebar navigation (active state) ----

function setupNav10(): void {
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

async function init10(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();

  try {
    const data = await fetchPortfolio();
    renderUser(data.user);
    renderStats10(data.stats);
    renderHoldings(data.holdings);
    renderGrowthChart10(data.growthChart);
  } catch (err) {
    console.error("Failed to load portfolio data:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
    holdingsListEl.innerHTML = `<li class="table-message">Could not load holdings. Check that the backend is running, then refresh.</li>`;
  }
}

init();
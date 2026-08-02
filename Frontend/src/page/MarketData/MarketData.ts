const MARKET_DATA_ENDPOINT = `${API_BASE_URL}/api/market-data`;
const REFRESH_ENDPOINT = `${API_BASE_URL}/api/market-data/refresh`;


type SourceStatus = "Healthy" | "Unhealthy";

interface MarketSource {
  id: string;
  assetName: string;
  provider: string;
  source: string; // e.g. "API"
  endpoint: string;
  latestPrice: number;
  status: SourceStatus;
}

interface MarketDataResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  stats: {
    connectedSources: number;
    lastUpdatedLabel: string; // e.g. "2 minutes ago"
    historyRecords: number;
  };
  sources: MarketSource[];
}

interface RefreshResponse {
  success: boolean;
  lastUpdatedLabel: string;
  sources: MarketSource[];
}

// ---- DOM references ----

const topbarDateEl9 = document.getElementById("topbarDate") as HTMLDivElement;
const userNameEl9 = document.getElementById("userName") as HTMLDivElement;
const userRoleEl9 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl9 = document.getElementById("userAvatar") as HTMLDivElement;

const connectedSourcesEl = document.getElementById("connectedSources") as HTMLDivElement;
const latestUpdateEl = document.getElementById("latestUpdate") as HTMLDivElement;
const historyRecordsEl = document.getElementById("historyRecords") as HTMLDivElement;

const marketTableBody = document.getElementById("marketTableBody") as HTMLTableSectionElement;

const refreshBtn = document.getElementById("refreshBtn") as HTMLButtonElement;
const refreshBtnLabel = document.getElementById("refreshBtnLabel") as HTMLSpanElement;

// ---- Helpers ----

function formatTopbarDate9(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

function formatPrice(value: number): string {
  return `$${value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

// ---- API calls ----

async function fetchMarketData(): Promise<MarketDataResponse> {
  const response = await fetch(MARKET_DATA_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as MarketDataResponse;
}

async function refreshSources(): Promise<RefreshResponse> {
  const response = await fetch(REFRESH_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as RefreshResponse;
}

// ---- Rendering ----

function renderUser9(user: MarketDataResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderStats9(stats: MarketDataResponse["stats"]): void {
  connectedSourcesEl.textContent = String(stats.connectedSources);
  latestUpdateEl.textContent = stats.lastUpdatedLabel;
  historyRecordsEl.textContent = stats.historyRecords.toLocaleString();
}

function renderSourceRow(source: MarketSource): HTMLTableRowElement {
  const tr = document.createElement("tr");
  const statusClass = source.status === "Healthy" ? "status-healthy" : "status-unhealthy";

  tr.innerHTML = `
    <td class="market-asset">${source.assetName}</td>
    <td class="market-provider">${source.provider}</td>
    <td><span class="source-pill">${source.source}</span></td>
    <td class="market-endpoint">${source.endpoint}</td>
    <td class="market-price">${formatPrice(source.latestPrice)}</td>
    <td><span class="${statusClass}">${source.status}</span></td>
  `;
  return tr;
}

function renderSourcesTable(sources: MarketSource[]): void {
  marketTableBody.innerHTML = "";

  if (sources.length === 0) {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td colspan="6" class="table-message">No connected sources.</td>`;
    marketTableBody.appendChild(tr);
    return;
  }

  sources.forEach((source) => {
    marketTableBody.appendChild(renderSourceRow(source));
  });
}

function showTableMessage9(message: string): void {
  marketTableBody.innerHTML = `<tr><td colspan="6" class="table-message">${message}</td></tr>`;
}

// ---- Refresh button ----

refreshBtn.addEventListener("click", async () => {
  refreshBtn.disabled = true;
  refreshBtnLabel.textContent = "Refreshing...";

  try {
    const result = await refreshSources();
    latestUpdateEl.textContent = result.lastUpdatedLabel;
    renderSourcesTable(result.sources);
    refreshBtnLabel.textContent = "Refreshed ✓";
    setTimeout(() => {
      refreshBtnLabel.textContent = "Refresh sources";
    }, 1800);
  } catch (err) {
    console.error("Failed to refresh sources:", err);
    refreshBtnLabel.textContent = "Refresh sources";
    alert("Could not refresh sources. Please check the server and try again.");
  } finally {
    refreshBtn.disabled = false;
  }
});

// ---- Sidebar navigation (active state) ----

function setupNav9(): void {
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

async function init9(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();
  showTableMessage("Loading market sources...");

  try {
    const data = await fetchMarketData();
    renderUser(data.user);
    renderStats9(data.stats);
    renderSourcesTable(data.sources);
  } catch (err) {
    console.error("Failed to load market data:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
    showTableMessage("Could not load market data. Check that the backend is running and reachable, then refresh.");
  }
}

init();
const API_BASE_URL1 = "http://localhost:3000";
const ADMIN_OVERVIEW_ENDPOINT = `${API_BASE_URL}/api/admin/overview`;


type FeedStatus = "Healthy" | "Degraded" | "Down";

interface AssetCategory {
  label: string;
  assetCount: number;
  percentOfMax: number; 
}

interface MarketFeed {
  name: string;
  updatedLabel: string; 
}

interface AdminOverviewResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  stats: {
    totalUsers: number;
    totalUsersDeltaLabel: string;
    totalAssets: number;
    totalAssetsDeltaLabel: string;
    activeAssets: number;
    activeAssetsPercentLabel: string;
    categoriesCount: number;
    categoriesListLabel: string;
  };
  categories: AssetCategory[];
  marketStatus: {
    overallStatus: FeedStatus;
    feeds: MarketFeed[];
  };
}

// ---- DOM references ----

const topbarDateEl1 = document.getElementById("topbarDate") as HTMLDivElement;
const userNameEl1 = document.getElementById("userName") as HTMLDivElement;
const userRoleEl1 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl1 = document.getElementById("userAvatar") as HTMLDivElement;

const totalUsersEl = document.getElementById("totalUsers") as HTMLDivElement;
const totalUsersDeltaEl = document.getElementById("totalUsersDelta") as HTMLDivElement;
const totalAssetsEl = document.getElementById("totalAssets") as HTMLDivElement;
const totalAssetsDeltaEl = document.getElementById("totalAssetsDelta") as HTMLDivElement;
const activeAssetsEl = document.getElementById("activeAssets") as HTMLDivElement;
const activeAssetsPercentEl = document.getElementById("activeAssetsPercent") as HTMLDivElement;
const categoriesCountEl = document.getElementById("categoriesCount") as HTMLDivElement;
const categoriesListEl = document.getElementById("categoriesList") as HTMLDivElement;

const categoryBarsEl = document.getElementById("categoryBars") as HTMLDivElement;
const overallHealthPillEl = document.getElementById("overallHealthPill") as HTMLSpanElement;
const feedListEl = document.getElementById("feedList") as HTMLUListElement;

// ---- Helpers ----

function formatTopbarDate8(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

// ---- API call ----

async function fetchAdminOverview(): Promise<AdminOverviewResponse> {
  const response = await fetch(ADMIN_OVERVIEW_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as AdminOverviewResponse;
}

// ---- Rendering ----

function renderUser10(user: AdminOverviewResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderStats1(stats: AdminOverviewResponse["stats"]): void {
  totalUsersEl.textContent = stats.totalUsers.toLocaleString();
  totalUsersDeltaEl.textContent = stats.totalUsersDeltaLabel;

  totalAssetsEl.textContent = String(stats.totalAssets);
  totalAssetsDeltaEl.textContent = stats.totalAssetsDeltaLabel;

  activeAssetsEl.textContent = String(stats.activeAssets);
  activeAssetsPercentEl.textContent = stats.activeAssetsPercentLabel;

  categoriesCountEl.textContent = String(stats.categoriesCount);
  categoriesListEl.textContent = stats.categoriesListLabel;
}

function renderCategoryBars(categories: AssetCategory[]): void {
  categoryBarsEl.innerHTML = "";
  categories.forEach((category) => {
    const div = document.createElement("div");
    div.className = "category-bar-item";
    div.innerHTML = `
      <div class="category-bar-header">
        <span class="category-bar-label">${category.label}</span>
        <span class="category-bar-count">${category.assetCount} assets</span>
      </div>
      <div class="category-bar-track">
        <div class="category-bar-fill" style="width: ${category.percentOfMax}%"></div>
      </div>
    `;
    categoryBarsEl.appendChild(div);
  });
}

function renderMarketStatus(marketStatus: AdminOverviewResponse["marketStatus"]): void {
  overallHealthPillEl.textContent = marketStatus.overallStatus;

  feedListEl.innerHTML = "";
  marketStatus.feeds.forEach((feed) => {
    const li = document.createElement("li");
    li.innerHTML = `
      <span class="feed-name">${feed.name}</span>
      <span class="feed-updated">${feed.updatedLabel}</span>
    `;
    feedListEl.appendChild(li);
  });
}

// ---- Sidebar navigation (active state) ----

function setupNav8(): void {
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

async function init1(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();

  try {
    const data = await fetchAdminOverview();
    renderUser(data.user);
    renderStats1(data.stats);
    renderCategoryBars(data.categories);
    renderMarketStatus(data.marketStatus);
  } catch (err) {
    console.error("Failed to load admin overview data:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
  }
}

init();
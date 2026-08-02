const API_BASE_URL7 = "http://localhost:3000";
const HISTORY_ENDPOINT = `${API_BASE_URL}/api/investment-history`;


type TransactionType = "Buy" | "Sell";

interface HistoryEntry {
  id: string;
  date: string; // ISO date string, e.g. "2026-07-20"
  assetName: string;
  type: TransactionType;
  amount: number;
}

interface HistoryResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  history: HistoryEntry[];
}

// ---- DOM references ----

const topbarDateEl7 = document.getElementById("topbarDate") as HTMLDivElement;
const userNameEl7 = document.getElementById("userName") as HTMLDivElement;
const userRoleEl7 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl7 = document.getElementById("userAvatar") as HTMLDivElement;
const historyTableBody = document.getElementById("historyTableBody") as HTMLTableSectionElement;

// ---- Helpers ----

function formatTopbarDate7(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

function formatHistoryDate(isoDate: string): string {
  const date = new Date(isoDate);
  return date.toLocaleDateString("en-US", {
    month: "short",
    day: "2-digit",
    year: "numeric",
  });
}

function formatCurrency7(value: number): string {
  return `$${value.toFixed(2)}`;
}

// ---- API call ----

async function fetchHistory(): Promise<HistoryResponse> {
  const response = await fetch(HISTORY_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as HistoryResponse;
}

// ---- Rendering ----

function renderUser7(user: HistoryResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderHistoryRow(entry: HistoryEntry): HTMLTableRowElement {
  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td class="history-date">${formatHistoryDate(entry.date)}</td>
    <td class="history-asset">${entry.assetName}</td>
    <td><span class="type-pill">${entry.type}</span></td>
    <td class="align-right history-amount">${formatCurrency(entry.amount)}</td>
  `;
  return tr;
}

function renderHistoryTable(history: HistoryEntry[]): void {
  historyTableBody.innerHTML = "";

  if (history.length === 0) {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td colspan="4" class="table-message">No investment activity yet.</td>`;
    historyTableBody.appendChild(tr);
    return;
  }

  history.forEach((entry) => {
    historyTableBody.appendChild(renderHistoryRow(entry));
  });
}

function showTableMessage7(message: string): void {
  historyTableBody.innerHTML = `<tr><td colspan="4" class="table-message">${message}</td></tr>`;
}

// ---- Sidebar navigation (active state) ----

function setupNav7(): void {
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

async function init7(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();
  showTableMessage("Loading history...");

  try {
    const data = await fetchHistory();
    renderUser(data.user);
    renderHistoryTable(data.history);
  } catch (err) {
    console.error("Failed to load investment history:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
    showTableMessage("Could not load history. Check that the backend is running and reachable, then refresh.");
  }
}

init();
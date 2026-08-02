
const API_BASE_URL2 = "http://localhost:3000";
const ASSETS_PAGE_ENDPOINT = `${API_BASE_URL}/api/assets-page`;
const ASSETS_ENDPOINT = `${API_BASE_URL}/api/assets`;


type AssetType = "STOCK" | "GOLD" | "FUND";
type AssetRisk = "Low" | "Medium" | "High";
type AssetStatus = "Active" | "Inactive";
type AssetApproval = "Approved" | "Pending";

interface Asset {
  id: string;
  name: string;
  symbol: string;
  type: AssetType;
  risk: AssetRisk;
  status: AssetStatus;
  approval: AssetApproval;
}

interface AssetsPageResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  assets: Asset[];
}

// ---- DOM references ----

const topbarDateE2 = document.getElementById("topbarDate") as HTMLDivElement;
const userNameE2 = document.getElementById("userName") as HTMLDivElement;
const userRoleE2 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarE2 = document.getElementById("userAvatar") as HTMLDivElement;

const addAssetForm = document.getElementById("addAssetForm") as HTMLFormElement;
const assetNameInput = document.getElementById("assetNameInput") as HTMLInputElement;
const assetSymbolInput = document.getElementById("assetSymbolInput") as HTMLInputElement;
const assetTypeInput = document.getElementById("assetTypeInput") as HTMLSelectElement;
const assetRiskInput = document.getElementById("assetRiskInput") as HTMLSelectElement;
const addAssetBtn = document.getElementById("addAssetBtn") as HTMLButtonElement;

const assetsTableBody = document.getElementById("assetsTableBody") as HTMLTableSectionElement;



function formatTopbarDate1(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

// ---- API calls ----

async function fetchAssetsPage(): Promise<AssetsPageResponse> {
  const response = await fetch(ASSETS_PAGE_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as AssetsPageResponse;
}

async function createAsset(payload: {
  name: string;
  symbol: string;
  type: AssetType;
  risk: AssetRisk;
}): Promise<Asset> {
  const response = await fetch(ASSETS_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as Asset;
}

async function toggleAssetStatus(id: string): Promise<Asset> {
  const response = await fetch(`${ASSETS_ENDPOINT}/${id}/toggle-status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as Asset;
}

async function approveAsset(id: string): Promise<Asset> {
  const response = await fetch(`${ASSETS_ENDPOINT}/${id}/approve`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as Asset;
}

async function deleteAsset(id: string): Promise<void> {
  const response = await fetch(`${ASSETS_ENDPOINT}/${id}`, {
    method: "DELETE",
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
}

// ---- Rendering ----

function renderUser(user: AssetsPageResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderAssetRow(asset: Asset): HTMLTableRowElement {
  const tr = document.createElement("tr");
  tr.dataset.assetId = asset.id;

  tr.innerHTML = `
    <td>
      <div class="asset-name">${asset.name}</div>
      <div class="asset-symbol">${asset.symbol}</div>
    </td>
    <td><span class="asset-type">${asset.type}</span></td>
    <td><span class="asset-risk">${asset.risk}</span></td>
    <td><span class="status-pill ${asset.status === "Active" ? "active" : "inactive"}">${asset.status}</span></td>
    <td><span class="approval-pill ${asset.approval === "Approved" ? "approved" : "pending"}">${asset.approval}</span></td>
    <td>
      <div class="action-icons">
        <button class="action-icon icon-edit" title="Edit" data-action="edit">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 20h4L18.5 9.5a2 2 0 000-2.8l-1.2-1.2a2 2 0 00-2.8 0L4 15v5z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
        </button>
        <button class="action-icon icon-toggle" title="Toggle status" data-action="toggle">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 3v8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/><path d="M7 6a7 7 0 105-1.6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
        </button>
        ${
          asset.approval === "Pending"
            ? `<button class="action-icon icon-approve" title="Approve" data-action="approve">
                 <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 12l5 5L20 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
               </button>`
            : ""
        }
        <button class="action-icon icon-delete" title="Delete" data-action="delete">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 7h16M9 7V5a1 1 0 011-1h4a1 1 0 011 1v2m-8 0l1 12a1 1 0 001 1h6a1 1 0 001-1l1-12" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
      </div>
    </td>
  `;

  return tr;
}

function renderAssetsTable(assets: Asset[]): void {
  assetsTableBody.innerHTML = "";

  if (assets.length === 0) {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td colspan="6" class="table-message">No assets yet. Add one above to get started.</td>`;
    assetsTableBody.appendChild(tr);
    return;
  }

  assets.forEach((asset) => {
    assetsTableBody.appendChild(renderAssetRow(asset));
  });
}

function showTableMessage(message: string): void {
  assetsTableBody.innerHTML = `<tr><td colspan="6" class="table-message">${message}</td></tr>`;
}

// ---- Event handling (delegated to the table body) ----

assetsTableBody.addEventListener("click", async (event: MouseEvent) => {
  const button = (event.target as HTMLElement).closest("button.action-icon") as HTMLButtonElement | null;
  if (!button) return;

  const row = button.closest("tr") as HTMLTableRowElement;
  const assetId = row.dataset.assetId as string;
  const action = button.dataset.action;

  try {
    if (action === "toggle") {
      const updated = await toggleAssetStatus(assetId);
      row.replaceWith(renderAssetRow(updated));
    } else if (action === "approve") {
      const updated = await approveAsset(assetId);
      row.replaceWith(renderAssetRow(updated));
    } else if (action === "delete") {
      const confirmed = confirm(`Delete "${row.querySelector(".asset-name")?.textContent}"?`);
      if (!confirmed) return;
      await deleteAsset(assetId);
      row.remove();
    } else if (action === "edit") {
      // Hook up your edit flow here (e.g. open a modal pre-filled with the asset's data).
      console.log("Edit asset:", assetId);
    }
  } catch (err) {
    console.error("Action failed:", err);
    alert("Something went wrong talking to the server. Please try again.");
  }
});

// ---- Add asset form ----

addAssetForm.addEventListener("submit", async (event: SubmitEvent) => {
  event.preventDefault();

  const name = assetNameInput.value.trim();
  const symbol = assetSymbolInput.value.trim().toUpperCase();
  const type = assetTypeInput.value as AssetType;
  const risk = assetRiskInput.value as AssetRisk;

  if (!name || !symbol) return;

  addAssetBtn.disabled = true;

  try {
    const newAsset = await createAsset({ name, symbol, type, risk });

    // Remove the empty-state row if present before appending the new one.
    const emptyRow = assetsTableBody.querySelector(".table-message");
    if (emptyRow) emptyRow.closest("tr")?.remove();

    assetsTableBody.appendChild(renderAssetRow(newAsset));
    addAssetForm.reset();
    assetRiskInput.value = "Medium";
  } catch (err) {
    console.error("Failed to create asset:", err);
    alert("Could not add the asset. Please check the server and try again.");
  } finally {
    addAssetBtn.disabled = false;
  }
});

// ---- Sidebar navigation (active state) ----

function setupNav2(): void {
  const navItems = document.querySelectorAll<HTMLAnchorElement>(".nav-item");
  navItems.forEach((item) => {
    item.addEventListener("click", (event: MouseEvent) => {
      if (item.getAttribute("href") !== "#") return; // allow real page links (e.g. Overview) to navigate
      event.preventDefault();
      navItems.forEach((el) => el.classList.remove("active"));
      item.classList.add("active");
    });
  });
}

// ---- Init ----

async function init2(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();
  showTableMessage("Loading assets...");

  try {
    const data = await fetchAssetsPage();
    renderUser(data.user);
    renderAssetsTable(data.assets);
  } catch (err) {
    console.error("Failed to load assets page:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
    showTableMessage("Could not load assets. Check that the backend is running and reachable, then refresh.");
  }
}

init();
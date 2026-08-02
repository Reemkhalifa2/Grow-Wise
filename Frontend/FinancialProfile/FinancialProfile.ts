const API_BASE_URL14 = "http://localhost:3000";
const PROFILE_ENDPOINT = `${API_BASE_URL}/api/financial-profile`;


type RiskLevel = "Low" | "Medium" | "High";

interface FinancialProfileResponse {
  user: {
    name: string;
    role: string;
    initials: string;
  };
  profile: {
    monthlySalary: number;
    monthlyExpenses: number;
    currentSavings: number;
    desiredSavingAmount: number;
    investmentDuration: number;
    financialGoal: string;
    riskLevel: RiskLevel;
  };
}

interface FinancialProfilePayload {
  monthlySalary: number;
  monthlyExpenses: number;
  currentSavings: number;
  desiredSavingAmount: number;
  investmentDuration: number;
  financialGoal: string;
  riskLevel: RiskLevel;
}

// ---- DOM references ----

const topbarDateEl4 = document.getElementById("topbarDate") as HTMLDivElement;
const userNameEl4 = document.getElementById("userName") as HTMLDivElement;
const userRoleEl4 = document.getElementById("userRole") as HTMLDivElement;
const userAvatarEl4 = document.getElementById("userAvatar") as HTMLDivElement;

const profileForm = document.getElementById("profileForm") as HTMLFormElement;
const monthlySalaryInput4 = document.getElementById("monthlySalary") as HTMLInputElement;
const monthlyExpensesInput4 = document.getElementById("monthlyExpenses") as HTMLInputElement;
const currentSavingsInput4 = document.getElementById("currentSavings") as HTMLInputElement;
const desiredSavingAmountInput = document.getElementById("desiredSavingAmount") as HTMLInputElement;
const investmentDurationInput = document.getElementById("investmentDuration") as HTMLInputElement;
const financialGoalInput = document.getElementById("financialGoal") as HTMLInputElement;
const riskToggle = document.getElementById("riskToggle") as HTMLDivElement;
const riskOptions = riskToggle.querySelectorAll<HTMLButtonElement>(".risk-option");
const saveBtn = document.getElementById("saveBtn") as HTMLButtonElement;
const saveBtnLabel = saveBtn.querySelector(".btn-save-label") as HTMLSpanElement;

let selectedRisk: RiskLevel = "Medium";

// ---- Date: computed locally from the user's own device clock ----

function formatTopbarDate(date: Date): string {
  return date.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

// ---- Risk toggle behavior ----

function setSelectedRisk(risk: RiskLevel): void {
  selectedRisk = risk;
  riskOptions.forEach((btn) => {
    btn.classList.toggle("selected", btn.dataset.risk === risk);
  });
}

riskOptions.forEach((btn) => {
  btn.addEventListener("click", () => {
    setSelectedRisk(btn.dataset.risk as RiskLevel);
  });
});

// ---- API calls ----

async function fetchProfile(): Promise<FinancialProfileResponse> {
  const response = await fetch(PROFILE_ENDPOINT, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
  return (await response.json()) as FinancialProfileResponse;
}

async function saveProfile(payload: FinancialProfilePayload): Promise<void> {
  const response = await fetch(PROFILE_ENDPOINT, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error(`Server responded with ${response.status}`);
}

// ---- Rendering ----

function renderUser4(user: FinancialProfileResponse["user"]): void {
  userNameEl.textContent = user.name;
  userRoleEl.textContent = user.role;
  userAvatarEl.textContent = user.initials;
}

function renderProfile(profile: FinancialProfileResponse["profile"]): void {
  monthlySalaryInput.value = String(profile.monthlySalary);
  monthlyExpensesInput.value = String(profile.monthlyExpenses);
  currentSavingsInput.value = String(profile.currentSavings);
  desiredSavingAmountInput.value = String(profile.desiredSavingAmount);
  investmentDurationInput.value = String(profile.investmentDuration);
  financialGoalInput.value = profile.financialGoal;
  setSelectedRisk(profile.riskLevel);
}

// ---- Form submit ----

profileForm.addEventListener("submit", async (event: SubmitEvent) => {
  event.preventDefault();

  const payload: FinancialProfilePayload = {
    monthlySalary: Number(monthlySalaryInput.value),
    monthlyExpenses: Number(monthlyExpensesInput.value),
    currentSavings: Number(currentSavingsInput.value),
    desiredSavingAmount: Number(desiredSavingAmountInput.value),
    investmentDuration: Number(investmentDurationInput.value),
    financialGoal: financialGoalInput.value.trim(),
    riskLevel: selectedRisk,
  };

  saveBtn.disabled = true;
  saveBtnLabel.textContent = "Saving...";

  try {
    await saveProfile(payload);
    saveBtnLabel.textContent = "Saved ✓";
    setTimeout(() => {
      saveBtnLabel.textContent = "Save financial profile";
    }, 1800);
  } catch (err) {
    console.error("Failed to save financial profile:", err);
    saveBtnLabel.textContent = "Save financial profile";
    alert("Could not save your profile. Please check the server and try again.");
  } finally {
    saveBtn.disabled = false;
  }
});

// ---- Sidebar navigation (active state) ----

function setupNav(): void {
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

async function init(): Promise<void> {
  topbarDateEl.textContent = formatTopbarDate(new Date());
  setupNav();
  setSelectedRisk("Medium"); // sensible default while data loads

  try {
    const data = await fetchProfile();
    renderUser(data.user);
    renderProfile(data.profile);
  } catch (err) {
    console.error("Failed to load financial profile:", err);
    userNameEl.textContent = "—";
    userRoleEl.textContent = "";
  }
}

init();
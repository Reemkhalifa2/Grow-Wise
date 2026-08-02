const API_BASE_URL5 = "http://localhost:3000";
const RESET_ENDPOINT = `${API_BASE_URL}/api/auth/forgot-password`;

interface ResetResponse {
  success: boolean;
  message?: string;
}

// ---- DOM references ----

const form5 = document.getElementById("resetForm") as HTMLFormElement;
const emailInput5 = document.getElementById("email") as HTMLInputElement;
const emailWrap5 = document.getElementById("emailWrap") as HTMLDivElement;
const emailError5 = document.getElementById("emailError") as HTMLDivElement;
const sendBtn5 = document.getElementById("sendBtn") as HTMLButtonElement;
const btnLabel5 = sendBtn5.querySelector(".btn-label") as HTMLSpanElement;
const toast5 = document.getElementById("toast") as HTMLDivElement;

// ---- Helpers ----

function isValidEmail5(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function showToast4(message: string, isError: boolean = false): void {
  toast.textContent = message;
  toast.classList.toggle("error-toast", isError);
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2600);
}

function clearError(): void {
  emailWrap.classList.remove("error");
  emailError.textContent = "";
}

// ---- API call ----

async function requestPasswordReset(email: string): Promise<ResetResponse> {
  const response = await fetch(RESET_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });

  let data: ResetResponse;
  try {
    data = (await response.json()) as ResetResponse;
  } catch {
    data = { success: false, message: "Unexpected server response" };
  }

  if (!response.ok) {
    return { success: false, message: data.message || `Server error (${response.status})` };
  }

  return data;
}

// ---- Form submit ----

form.addEventListener("submit", async (event: SubmitEvent) => {
  event.preventDefault();
  clearError();

  const email = emailInput.value.trim();

  if (!isValidEmail(email)) {
    emailWrap.classList.add("error");
    emailError.textContent = "Please enter a valid email address";
    return;
  }

  sendBtn5.disabled = true;
  btnLabel.textContent = "Sending...";

  try {
    const result = await requestPasswordReset(email);

    if (result.success) {
      btnLabel.textContent = "Link sent ✓";
      showToast("If that email exists, a reset link is on its way.");
      emailInput.value = "";
      setTimeout(() => {
        btnLabel.textContent = "Send reset link";
      }, 2200);
    } else {
      btnLabel.textContent = "Send reset link";
      showToast(result.message || "Could not send the reset link.", true);
    }
  } catch (err) {
    btnLabel.textContent = "Send reset link";
    showToast("Could not reach the server. Please try again.", true);
  } finally {
    sendBtn5.disabled = false;
  }
});

emailInput.addEventListener("input", clearError);
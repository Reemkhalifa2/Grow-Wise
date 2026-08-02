const API_BASE_URL = "http://localhost:3000";
const VERIFY_TOKEN_ENDPOINT = `${API_BASE_URL}/api/auth/verify-reset-token`;

interface VerifyTokenResponse {
  valid: boolean;
  message?: string;
}

// ---- DOM references ----

const pageTitleEl = document.getElementById("pageTitle") as HTMLHeadingElement;
const pageSubtitleEl = document.getElementById("pageSubtitle") as HTMLParagraphElement;
const messageTextEl = document.getElementById("messageText") as HTMLParagraphElement;

// ---- Helpers ----

function getTokenFromUrl(): string | null {
  const params = new URLSearchParams(window.location.search);
  return params.get("token");
}

// ---- API call ----

async function verifyResetToken(token: string): Promise<VerifyTokenResponse> {
  const response = await fetch(`${VERIFY_TOKEN_ENDPOINT}?token=${encodeURIComponent(token)}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });

  let data: VerifyTokenResponse;
  try {
    data = (await response.json()) as VerifyTokenResponse;
  } catch {
    data = { valid: false, message: "Unexpected server response" };
  }

  if (!response.ok) {
    return { valid: false, message: data.message || `Server error (${response.status})` };
  }

  return data;
}

// ---- Rendering states ----

function showMissingTokenState(): void {
  // No token at all in the URL — this is the state shown in the design.
  pageTitleEl.textContent = "Invalid reset link";
  pageSubtitleEl.textContent = "This password reset link is missing or invalid";
  messageTextEl.textContent = "The link you used appears to be incomplete. Please request a new password reset email.";
}

function showExpiredOrUsedState(message?: string): void {
  pageTitleEl.textContent = "Invalid reset link";
  pageSubtitleEl.textContent = "This password reset link is missing or invalid";
  messageTextEl.textContent = message || "This link has expired or has already been used. Please request a new password reset email.";
}

function redirectToSetNewPassword(token: string): void {
  // Hook this up once you have a "set new password" page, e.g.:
  window.location.href = `set-new-password.html?token=${encodeURIComponent(token)}`;
}

// ---- Init ----

async function init13(): Promise<void> {
  const token = getTokenFromUrl();

  if (!token) {
    showMissingTokenState();
    return;
  }

  try {
    const result = await verifyResetToken(token);
    if (result.valid) {
      redirectToSetNewPassword(token);
    } else {
      showExpiredOrUsedState(result.message);
    }
  } catch (err) {
    console.error("Failed to verify reset token:", err);
    showExpiredOrUsedState("Could not verify this link right now. Please try again or request a new one.");
  }
}

init();
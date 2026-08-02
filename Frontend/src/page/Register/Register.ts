const API_BASE_URL12 = "http://localhost:3000";
const SIGNUP_ENDPOINT = `${API_BASE_URL}/api/auth/signup`;

interface SignupResponse {
  success: boolean;
  token?: string;
  message?: string;
}

// ---- DOM references ----

const form = document.getElementById("signupForm") as HTMLFormElement;
const emailInput = document.getElementById("email") as HTMLInputElement;
const passwordInput = document.getElementById("password") as HTMLInputElement;
const confirmPasswordInput = document.getElementById("confirmPassword") as HTMLInputElement;

const emailWrap = document.getElementById("emailWrap") as HTMLDivElement;
const passwordWrap = document.getElementById("passwordWrap") as HTMLDivElement;
const confirmPasswordWrap = document.getElementById("confirmPasswordWrap") as HTMLDivElement;

const emailError = document.getElementById("emailError") as HTMLDivElement;
const passwordError = document.getElementById("passwordError") as HTMLDivElement;
const confirmPasswordError = document.getElementById("confirmPasswordError") as HTMLDivElement;

const signupBtn = document.getElementById("signupBtn") as HTMLButtonElement;
const btnLabel = signupBtn.querySelector(".btn-label") as HTMLSpanElement;
const googleBtn = document.getElementById("googleBtn") as HTMLButtonElement;
const toast = document.getElementById("toast") as HTMLDivElement;

// ---- Helpers ----

function isValidEmail12(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function showToast12(message: string, isError: boolean = false): void {
  toast.textContent = message;
  toast.classList.toggle("error-toast", isError);
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2600);
}

function clearErrors12(): void {
  emailWrap.classList.remove("error");
  passwordWrap.classList.remove("error");
  confirmPasswordWrap.classList.remove("error");
  emailError.textContent = "";
  passwordError.textContent = "";
  confirmPasswordError.textContent = "";
}

// ---- API call ----

async function signupRequest(email: string, password: string): Promise<SignupResponse> {
  const response = await fetch(SIGNUP_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  let data: SignupResponse;
  try {
    data = (await response.json()) as SignupResponse;
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
  clearErrors();

  const email = emailInput.value.trim();
  const password = passwordInput.value;
  const confirmPassword = confirmPasswordInput.value;
  let hasError = false;

  if (!isValidEmail(email)) {
    emailWrap.classList.add("error");
    emailError.textContent = "Please enter a valid email address";
    hasError = true;
  }

  if (password.length < 6) {
    passwordWrap.classList.add("error");
    passwordError.textContent = "Password must be at least 6 characters";
    hasError = true;
  }

  if (confirmPassword !== password || !confirmPassword) {
    confirmPasswordWrap.classList.add("error");
    confirmPasswordError.textContent = "Passwords do not match";
    hasError = true;
  }

  if (hasError) return;

  signupBtn.disabled = true;
  btnLabel.textContent = "Creating account...";

  try {
    const result = await signupRequest(email, password);

    if (result.success) {
      if (result.token) {
        localStorage.setItem("authToken", result.token);
      }
      btnLabel.textContent = "Account created ✓";
      showToast("Your account has been created.");
      // Redirect after a successful signup, e.g.:
      // window.location.href = "index.html";
    } else {
      btnLabel.textContent = "Create account";
      if (result.message && result.message.toLowerCase().includes("email")) {
        emailWrap.classList.add("error");
        emailError.textContent = result.message;
      }
      showToast(result.message || "Could not create your account.", true);
    }
  } catch (err) {
    btnLabel.textContent = "Create account";
    showToast("Could not reach the server. Please try again.", true);
  } finally {
    signupBtn.disabled = false;
  }
});

[emailInput, passwordInput, confirmPasswordInput].forEach((input) => {
  input.addEventListener("input", clearErrors);
});

googleBtn.addEventListener("click", () => {
  showToast("Continue with Google (demo)");
});
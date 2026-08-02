// login page 

const form = document.getElementById("loginForm") as HTMLFormElement;
const emailInput = document.getElementById("email") as HTMLInputElement;
const passwordInput = document.getElementById("password") as HTMLInputElement;
const emailWrap = document.getElementById("emailWrap") as HTMLDivElement;
const passwordWrap = document.getElementById("passwordWrap") as HTMLDivElement;
const emailError = document.getElementById("emailError") as HTMLDivElement;
const passwordError = document.getElementById("passwordError") as HTMLDivElement;
const loginBtn = document.getElementById("loginBtn") as HTMLButtonElement;
const btnLabel = loginBtn.querySelector(".btn-label") as HTMLSpanElement;
const googleBtn = document.getElementById("googleBtn") as HTMLButtonElement;
const forgotLink = document.getElementById("forgotLink") as HTMLAnchorElement;
const createAccountLink = document.getElementById("createAccountLink") as HTMLAnchorElement;
const toast = document.getElementById("toast") as HTMLDivElement;

interface User {
  email: string;
  password: string;
}

// ---- Helpers ----

function isValidEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function getUsers(): User[] {
  const raw = localStorage.getItem("users");
  return raw ? (JSON.parse(raw) as User[]) : [];
}

function saveUsers(users: User[]): void {
  localStorage.setItem("users", JSON.stringify(users));
}

function findUser(email: string, password: string): User | undefined {
  return getUsers().find((u) => u.email === email && u.password === password);
}

function showToast(message: string, isError: boolean = false): void {
  toast.textContent = message;
  toast.classList.toggle("error-toast", isError);
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2200);
}

function clearErrors(): void {
  emailWrap.classList.remove("error");
  passwordWrap.classList.remove("error");
  emailError.textContent = "";
  passwordError.textContent = "";
}

// ---- Ensure a demo account exists so the form is testable right away ----

function ensureDemoUser(): void {
  const users = getUsers();
  if (users.length === 0) {
    saveUsers([{ email: "demo@example.com", password: "demo123" }]);
  }
}
ensureDemoUser();

// ---- Form submit ----

form.addEventListener("submit", (event: SubmitEvent) => {
  event.preventDefault();
  clearErrors();

  const email = emailInput.value.trim();
  const password = passwordInput.value;
  let hasError = false;

  if (!isValidEmail(email)) {
    emailWrap.classList.add("error");
    emailError.textContent = "Please enter a valid email address";
    hasError = true;
  }

  if (!password) {
    passwordWrap.classList.add("error");
    passwordError.textContent = "Please enter your password";
    hasError = true;
  }

  if (hasError) return;

  loginBtn.disabled = true;
  btnLabel.textContent = "Logging in...";

  setTimeout(() => {
    loginBtn.disabled = false;
    btnLabel.textContent = "Log in";

    const match = findUser(email, password);

    if (match) {
      localStorage.setItem("currentUser", email);
      showToast("Logged in successfully ✓");
    } else {
      passwordWrap.classList.add("error");
      passwordError.textContent = "Incorrect email or password";
      showToast("Invalid login credentials", true);
    }
  }, 600);
});

// ---- Clear field errors while typing ----

[emailInput, passwordInput].forEach((input) => {
  input.addEventListener("input", clearErrors);
});

// ---- Other actions (demo handlers) ----

googleBtn.addEventListener("click", () => {
  showToast("Continue with Google (demo)");
});

forgotLink.addEventListener("click", (event: MouseEvent) => {
  event.preventDefault();
  showToast("Password reset link would be sent (demo)");
});

createAccountLink.addEventListener("click", (event: MouseEvent) => {
  event.preventDefault();
  showToast("Redirect to sign up page (demo)");
});
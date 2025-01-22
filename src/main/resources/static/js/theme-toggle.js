// theme-toggle.js

document.addEventListener("DOMContentLoaded", () => {
  const toggleBtn = document.getElementById("themeToggleBtn");
  const body = document.body;

  let isDarkMode = localStorage.getItem("darkMode") === "true";
  // If darkMode isn't set, default to true
  if (localStorage.getItem("darkMode") === null) {
    isDarkMode = true;
    localStorage.setItem("darkMode", "true");
  }

  if (isDarkMode) {
    enableDarkMode();
  }

  toggleBtn.addEventListener("click", () => {
    isDarkMode = !isDarkMode;
    localStorage.setItem("darkMode", isDarkMode.toString());
    isDarkMode ? enableDarkMode() : disableDarkMode();
  });

  function enableDarkMode() {
    body.classList.add("dark-mode");
    document.getElementById("mainNavbar").classList.remove("bg-light");
    document.getElementById("mainNavbar").classList.add("navbar-dark", "bg-dark");
    toggleBtn.textContent = "Switch to Light";
  }

  function disableDarkMode() {
    body.classList.remove("dark-mode");
    document.getElementById("mainNavbar").classList.remove("navbar-dark", "bg-dark");
    document.getElementById("mainNavbar").classList.add("bg-light");
    toggleBtn.textContent = "Switch to Dark";
  }
});

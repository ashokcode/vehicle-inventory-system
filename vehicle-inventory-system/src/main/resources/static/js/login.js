document.getElementById("login-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const alertBox = document.getElementById("alert-box");
  alertBox.innerHTML = "";

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  try {
    const response = await Api.login(username, password);
    Api.setSession(response.token, response.username);
    window.location.href = "/dashboard.html";
  } catch (err) {
    alertBox.innerHTML = `<div class="alert error">${err.message}</div>`;
  }
});

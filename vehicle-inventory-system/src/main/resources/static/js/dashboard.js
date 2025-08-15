Api.requireAuth();

const money = (n) => (n == null ? "$0" : "$" + Number(n).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
const dateStr = (iso) => new Date(iso).toLocaleDateString();

document.getElementById("logout-btn").addEventListener("click", () => {
  Api.clearSession();
  window.location.href = "/login.html";
});

async function load() {
  try {
    const summary = await Api.get("/api/dashboard/summary");
    renderStats(summary);
    renderRecent(summary.recentlyAdded);
  } catch (err) {
    document.querySelector(".main").insertAdjacentHTML("afterbegin", `<div class="alert error">${err.message}</div>`);
  }
}

function renderStats(summary) {
  const cards = [
    { label: "Total vehicles", value: summary.totalVehicles },
    { label: "Available", value: summary.countByStatus.AVAILABLE || 0 },
    { label: "Reserved", value: summary.countByStatus.RESERVED || 0 },
    { label: "Sold", value: summary.countByStatus.SOLD || 0 },
    { label: "Inventory on hand (cost)", value: money(summary.totalInventoryValue) },
    { label: "Potential revenue", value: money(summary.totalPotentialRevenue) },
    { label: "Revenue to date (sold)", value: money(summary.soldRevenueToDate) },
    { label: "Gross profit to date", value: money(summary.soldGrossProfitToDate) },
  ];
  document.getElementById("stats").innerHTML = cards
    .map((c) => `<div class="card"><div class="label">${c.label}</div><div class="value">${c.value}</div></div>`)
    .join("");
}

function renderRecent(vehicles) {
  const body = document.getElementById("recent-body");
  if (!vehicles.length) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state">No vehicles yet — add your first one.</td></tr>`;
    return;
  }
  body.innerHTML = vehicles
    .map(
      (v) => `
      <tr onclick="window.location.href='/vehicle-form.html?id=${v.id}'" style="cursor:pointer">
        <td><img class="thumb" src="${v.primaryPhotoUrl || ""}" onerror="this.style.visibility='hidden'"></td>
        <td>${v.year} ${v.brand} ${v.model}</td>
        <td>${v.vin}</td>
        <td><span class="badge ${v.status}">${v.status.replace("_", " ")}</span></td>
        <td>${money(v.sellingPrice)}</td>
        <td>${dateStr(v.dateAdded)}</td>
      </tr>`
    )
    .join("");
}

load();

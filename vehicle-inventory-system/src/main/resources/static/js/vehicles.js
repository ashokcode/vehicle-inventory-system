Api.requireAuth();

const money = (n) => (n == null ? "—" : "$" + Number(n).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));

let page = 0;
const pageSize = 15;

document.getElementById("logout-btn").addEventListener("click", () => {
  Api.clearSession();
  window.location.href = "/login.html";
});

async function loadLookups() {
  const [statuses, conditions] = await Promise.all([Api.get("/api/lookups/statuses"), Api.get("/api/lookups/conditions")]);
  const statusSelect = document.getElementById("f-status");
  statuses.forEach((s) => statusSelect.insertAdjacentHTML("beforeend", `<option value="${s}">${s.replace("_", " ")}</option>`));
  const conditionSelect = document.getElementById("f-condition");
  conditions.forEach((c) => conditionSelect.insertAdjacentHTML("beforeend", `<option value="${c}">${c.replace(/_/g, " ")}</option>`));
}

function currentFilters() {
  const params = new URLSearchParams();
  const q = document.getElementById("f-q").value.trim();
  const brand = document.getElementById("f-brand").value.trim();
  const year = document.getElementById("f-year").value.trim();
  const status = document.getElementById("f-status").value;
  const condition = document.getElementById("f-condition").value;
  if (q) params.set("q", q);
  if (brand) params.set("brand", brand);
  if (year) params.set("year", year);
  if (status) params.set("status", status);
  if (condition) params.set("condition", condition);
  params.set("page", page);
  params.set("size", pageSize);
  return params;
}

async function load() {
  const params = currentFilters();
  try {
    const result = await Api.get(`/api/vehicles?${params.toString()}`);
    renderRows(result.content);
    document.getElementById("page-info").textContent = `Page ${result.page + 1} of ${Math.max(result.totalPages, 1)} — ${result.totalElements} vehicles`;
    document.getElementById("prev-page").disabled = result.page === 0;
    document.getElementById("next-page").disabled = result.last;
  } catch (err) {
    document.getElementById("vehicle-body").innerHTML = `<tr><td colspan="8" class="empty-state">${err.message}</td></tr>`;
  }
}

function renderRows(vehicles) {
  const body = document.getElementById("vehicle-body");
  if (!vehicles.length) {
    body.innerHTML = `<tr><td colspan="8" class="empty-state">No vehicles match these filters.</td></tr>`;
    return;
  }
  body.innerHTML = vehicles
    .map(
      (v) => `
      <tr>
        <td><img class="thumb" src="${v.primaryPhotoUrl || ""}" onerror="this.style.visibility='hidden'"></td>
        <td>${v.year} ${v.brand} ${v.model}</td>
        <td>${v.vin}</td>
        <td>${v.mileage.toLocaleString()} mi</td>
        <td>${money(v.sellingPrice)}</td>
        <td><span class="badge ${v.status}">${v.status.replace("_", " ")}</span></td>
        <td>${v.condition.replace(/_/g, " ")}</td>
        <td><a class="btn secondary" href="/vehicle-form.html?id=${v.id}">Edit</a></td>
      </tr>`
    )
    .join("");
}

document.getElementById("apply-filters").addEventListener("click", () => {
  page = 0;
  load();
});
document.getElementById("clear-filters").addEventListener("click", () => {
  document.getElementById("f-q").value = "";
  document.getElementById("f-brand").value = "";
  document.getElementById("f-year").value = "";
  document.getElementById("f-status").value = "";
  document.getElementById("f-condition").value = "";
  page = 0;
  load();
});
document.getElementById("prev-page").addEventListener("click", () => {
  if (page > 0) { page -= 1; load(); }
});
document.getElementById("next-page").addEventListener("click", () => {
  page += 1;
  load();
});

loadLookups().then(load);

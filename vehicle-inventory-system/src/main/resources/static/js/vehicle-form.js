Api.requireAuth();

const params = new URLSearchParams(window.location.search);
const vehicleId = params.get("id");
const isEdit = Boolean(vehicleId);

document.getElementById("logout-btn").addEventListener("click", () => {
  Api.clearSession();
  window.location.href = "/login.html";
});

async function loadLookups() {
  const [statuses, conditions] = await Promise.all([Api.get("/api/lookups/statuses"), Api.get("/api/lookups/conditions")]);
  const statusSelect = document.getElementById("status-select");
  statuses.forEach((s) => statusSelect.insertAdjacentHTML("beforeend", `<option value="${s}">${s.replace("_", " ")}</option>`));
  const conditionSelect = document.getElementById("condition-select");
  conditions.forEach((c) => conditionSelect.insertAdjacentHTML("beforeend", `<option value="${c}">${c.replace(/_/g, " ")}</option>`));
}

function fillForm(vehicle) {
  const form = document.getElementById("vehicle-form");
  form.brand.value = vehicle.brand;
  form.model.value = vehicle.model;
  form.year.value = vehicle.year;
  form.engine.value = vehicle.engine || "";
  form.vin.value = vehicle.vin;
  form.mileage.value = vehicle.mileage;
  form.purchasePrice.value = vehicle.purchasePrice;
  form.sellingPrice.value = vehicle.sellingPrice || "";
  form.status.value = vehicle.status;
  form.condition.value = vehicle.condition;
  form.notes.value = vehicle.notes || "";
}

function renderPhotos(photos) {
  const grid = document.getElementById("photo-grid");
  if (!photos.length) {
    grid.innerHTML = `<p class="muted">No photos uploaded yet.</p>`;
    return;
  }
  grid.innerHTML = photos
    .map(
      (p) => `
      <div class="photo ${p.primary ? "primary" : ""}">
        <img src="${p.url}">
        <div class="photo-actions">
          ${p.primary ? '<span class="muted">Primary</span>' : `<button class="secondary" data-make-primary="${p.id}">Make primary</button>`}
          <button class="danger" data-delete-photo="${p.id}">Delete</button>
        </div>
      </div>`
    )
    .join("");

  grid.querySelectorAll("[data-make-primary]").forEach((btn) =>
    btn.addEventListener("click", async () => {
      const updated = await Api.put(`/api/vehicles/${vehicleId}/photos/${btn.dataset.makePrimary}/primary`, {});
      renderPhotos(updated.photos);
    })
  );
  grid.querySelectorAll("[data-delete-photo]").forEach((btn) =>
    btn.addEventListener("click", async () => {
      if (!confirm("Delete this photo?")) return;
      await Api.del(`/api/vehicles/${vehicleId}/photos/${btn.dataset.deletePhoto}`);
      const refreshed = await Api.get(`/api/vehicles/${vehicleId}`);
      renderPhotos(refreshed.photos);
    })
  );
}

async function init() {
  await loadLookups();

  if (isEdit) {
    document.getElementById("form-title").textContent = "Edit vehicle";
    document.getElementById("delete-btn").style.display = "inline-block";
    document.getElementById("photos-card").style.display = "block";

    const vehicle = await Api.get(`/api/vehicles/${vehicleId}`);
    fillForm(vehicle);
    renderPhotos(vehicle.photos);
  } else {
    document.getElementById("status-select").value = "AVAILABLE";
  }
}

document.getElementById("vehicle-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const alertBox = document.getElementById("alert-box");
  alertBox.innerHTML = "";

  const form = event.target;
  const payload = {
    brand: form.brand.value.trim(),
    model: form.model.value.trim(),
    year: Number(form.year.value),
    engine: form.engine.value.trim() || null,
    vin: form.vin.value.trim().toUpperCase(),
    mileage: Number(form.mileage.value),
    purchasePrice: Number(form.purchasePrice.value),
    sellingPrice: form.sellingPrice.value ? Number(form.sellingPrice.value) : null,
    status: form.status.value || undefined,
    condition: form.condition.value,
    notes: form.notes.value.trim() || null,
  };

  try {
    if (isEdit) {
      await Api.put(`/api/vehicles/${vehicleId}`, payload);
      window.location.href = "/vehicles.html";
    } else {
      const created = await Api.post("/api/vehicles", payload);
      window.location.href = `/vehicle-form.html?id=${created.id}`;
    }
  } catch (err) {
    const details = err.payload && err.payload.fieldErrors
      ? "<ul>" + err.payload.fieldErrors.map((f) => `<li>${f.field}: ${f.message}</li>`).join("") + "</ul>"
      : "";
    alertBox.innerHTML = `<div class="alert error">${err.message}${details}</div>`;
  }
});

document.getElementById("delete-btn").addEventListener("click", async () => {
  if (!confirm("Delete this vehicle and all of its photos? This cannot be undone.")) return;
  await Api.del(`/api/vehicles/${vehicleId}`);
  window.location.href = "/vehicles.html";
});

document.getElementById("upload-photos-btn").addEventListener("click", async () => {
  const input = document.getElementById("photo-input");
  if (!input.files.length) return;
  const formData = new FormData();
  Array.from(input.files).forEach((file) => formData.append("files", file));

  try {
    const updated = await Api.post(`/api/vehicles/${vehicleId}/photos`, formData);
    renderPhotos(updated.photos);
    input.value = "";
  } catch (err) {
    document.getElementById("alert-box").innerHTML = `<div class="alert error">${err.message}</div>`;
  }
});

init();

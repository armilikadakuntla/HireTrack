const $ = (id) => document.getElementById(id);
let mode = "login";
let applications = [];

async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  const data = response.status === 204 ? null : await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.message || data.error || "Something went wrong.");
  return data;
}
function showAuth() {
  $("auth-view").classList.remove("hidden");
  $("dashboard-view").classList.add("hidden");
  $("user-area").innerHTML = "";
}
function showDashboard(user) {
  $("auth-view").classList.add("hidden");
  $("dashboard-view").classList.remove("hidden");
  $("user-area").innerHTML = `<span>Hi, ${escapeHtml(user.name)}</span><button class="link-btn" id="logout-btn">Log out</button>`;
  $("logout-btn").addEventListener("click", logout);
  loadApplications();
}
function escapeHtml(value = "") {
  return String(value).replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
}
function setMode(next) {
  mode = next;
  document.querySelectorAll(".tab").forEach(b => b.classList.toggle("active", b.dataset.tab === mode));
  $("name-wrap").classList.toggle("hidden", mode === "login");
  $("name").required = mode === "register";
  $("auth-title").textContent = mode === "login" ? "Welcome back" : "Create your account";
  $("auth-subtitle").textContent = mode === "login" ? "Log in to continue to your dashboard." : "Start organizing your job search today.";
  $("auth-submit").textContent = mode === "login" ? "Log in" : "Create account";
  $("password").autocomplete = mode === "login" ? "current-password" : "new-password";
  $("auth-error").textContent = "";
}
document.querySelectorAll(".tab").forEach(b => b.addEventListener("click", () => setMode(b.dataset.tab)));
$("auth-form").addEventListener("submit", async e => {
  e.preventDefault(); $("auth-error").textContent = "";
  const payload = { email: $("email").value.trim(), password: $("password").value };
  if (mode === "register") payload.name = $("name").value.trim();
  try {
    const user = await api(`/auth/${mode === "login" ? "login" : "register"}`, { method: "POST", body: JSON.stringify(payload) });
    showDashboard(user);
  } catch (err) { $("auth-error").textContent = err.message; }
});
async function logout() {
  try { await api("/auth/logout", { method: "POST", body: "{}" }); } catch (_) {}
  showAuth(); $("auth-form").reset(); setMode("login");
}
async function loadApplications() {
  try { applications = await api("/applications"); render(); }
  catch (err) { if (err.message.includes("log in")) showAuth(); else alert(err.message); }
}
function render() {
  const total = applications.length;
  const inProgress = applications.filter(a => ["Applied","Online Assessment","Interview"].includes(a.status)).length;
  $("total-count").textContent = total;
  $("progress-count").textContent = inProgress;
  $("interview-count").textContent = applications.filter(a => a.status === "Interview").length;
  $("offer-count").textContent = applications.filter(a => a.status === "Offer").length;
  const q = $("search").value.toLowerCase();
  const filter = $("status-filter").value;
  const rows = applications.filter(a => (`${a.company} ${a.role}`).toLowerCase().includes(q) && (!filter || a.status === filter));
  $("applications-body").innerHTML = rows.map(a => `
    <tr><td><strong>${escapeHtml(a.company)}</strong><small>${escapeHtml(a.role)}</small></td>
    <td>${a.appliedDate || "—"}</td><td><span class="status-pill ${a.status.toLowerCase().replaceAll(" ","-")}">${escapeHtml(a.status)}</span></td>
    <td>${escapeHtml(a.location || "—")}</td><td><div class="actions"><button data-edit="${a.id}">Edit</button><button class="delete" data-delete="${a.id}">Delete</button></div></td></tr>`).join("");
  $("empty-state").classList.toggle("hidden", rows.length !== 0);
  $("applications-body").classList.toggle("hidden", rows.length === 0);
  document.querySelectorAll("[data-edit]").forEach(b => b.addEventListener("click", () => openModal(applications.find(a => a.id == b.dataset.edit))));
  document.querySelectorAll("[data-delete]").forEach(b => b.addEventListener("click", () => removeApplication(b.dataset.delete)));
}
$("search").addEventListener("input", render);
$("status-filter").addEventListener("change", render);
function openModal(app = null) {
  $("application-form").reset();
  $("app-error").textContent = "";
  $("app-id").value = app?.id || "";
  $("modal-title").textContent = app ? "Edit application" : "Add application";
  $("company").value = app?.company || "";
  $("role").value = app?.role || "";
  $("location").value = app?.location || "";
  $("jobUrl").value = app?.jobUrl || "";
  $("status").value = app?.status || "Applied";
  $("appliedDate").value = app?.appliedDate || new Date().toISOString().slice(0,10);
  $("notes").value = app?.notes || "";
  $("modal").classList.remove("hidden");
}
function closeModal() { $("modal").classList.add("hidden"); }
$("add-open").addEventListener("click", () => openModal());
$("empty-add").addEventListener("click", () => openModal());
$("modal-close").addEventListener("click", closeModal);
$("cancel-btn").addEventListener("click", closeModal);
$("modal").addEventListener("click", e => { if (e.target === $("modal")) closeModal(); });
$("application-form").addEventListener("submit", async e => {
  e.preventDefault(); $("app-error").textContent = "";
  const id = $("app-id").value;
  const payload = {
    company: $("company").value.trim(), role: $("role").value.trim(),
    location: $("location").value.trim(), jobUrl: $("jobUrl").value.trim(),
    status: $("status").value, appliedDate: $("appliedDate").value,
    notes: $("notes").value.trim()
  };
  try {
    await api(id ? `/applications/${id}` : "/applications", { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
    closeModal(); await loadApplications();
  } catch (err) { $("app-error").textContent = err.message; }
});
async function removeApplication(id) {
  if (!confirm("Delete this application?")) return;
  try { await api(`/applications/${id}`, { method: "DELETE" }); await loadApplications(); }
  catch (err) { alert(err.message); }
}
(async function init() {
  try { const user = await api("/auth/me"); showDashboard(user); }
  catch (_) { showAuth(); }
})();

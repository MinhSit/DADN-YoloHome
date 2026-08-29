// CHUYỂN TAB
function switchPage(pageName) {
  if (pageName === "logout") {
    localStorage.removeItem("token");
    window.location.href = "login.html";
    return;
  }

  document.querySelectorAll(".page").forEach((el) => {
    el.classList.remove("active");
  });

  const targetPage = document.getElementById("page-" + pageName);
  if (targetPage) {
    targetPage.classList.add("active");
  }

  document.querySelectorAll(".nav-item").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.page === pageName);
  });

  const titles = {
    overview: "Overview",
    logs: "Activity Logs",
    alerts: "Alerts",
    settings: "Settings"
  };
  document.getElementById("topbar-title").textContent = titles[pageName] || "";
}

function initSidebarNavigation() {
  document.querySelectorAll(".nav-item[data-page]").forEach((btn) => {
    btn.addEventListener("click", () => switchPage(btn.dataset.page));
  });
}

// TAB OVERVIEW
async function renderOverview() {
  const [sensors, devices, alerts, lastUpdated, thresholds] = await Promise.all([
    SensorAPI.getSensorData(),
    DeviceAPI.getDevices(),
    AlertAPI.getAlerts(),
    SystemAPI.getLastUpdated(),
    SettingsAPI.getThresholds()
  ]);

  document.getElementById("overview-temp-threshold").value =
    thresholds.temperature;

  document.getElementById("overview-humidity-threshold").value =
    thresholds.humidity;

  document.getElementById("overview-light-threshold").value =
    thresholds.light ?? "";

  // Cảm biến
  document.getElementById("overview-temp-value").textContent =
    sensors.temperature.current + " " + sensors.temperature.unit;

  document.getElementById("overview-humidity-value").textContent =
    sensors.humidity.current + " " + sensors.humidity.unit;

  document.getElementById("overview-light-value").textContent =
    sensors.light.current + " " + sensors.light.unit;

  // Điều khiển thiết bị
  const deviceListEl = document.getElementById("overview-device-list");
  deviceListEl.innerHTML = devices.map((device) => renderDeviceControlRow(device)).join("");

  // Cảnh báo gần nhất
  const alertListEl = document.getElementById("overview-alert-list");
  alertListEl.innerHTML = alerts.slice(0, 5).map((alert) => `
    <li class="notification-item">
      <span class="notification-text">${alert.message}</span>
      <span class="notification-time">${alert.time}</span>
    </li>
  `).join("");

  document.getElementById("overview-last-updated").textContent = lastUpdated;

  attachDeviceControlListeners();
}

function renderDeviceControlRow(device) {
  const isOn = device.status === "on";
  return `
    <li class="device-item">
      <span class="device-name">${device.name}</span>
      <label class="toggle-switch">
        <input type="checkbox" data-device-id="${device.id}" ${isOn ? "checked" : ""}>
        <span class="toggle-slider"></span>
      </label>
    </li>
  `;
}

let devicePollTimer = null;
let suppressDevicePollUntil = 0;

async function refreshDeviceStates() {
  if (Date.now() < suppressDevicePollUntil) {
    return;
  }

  const devices = await DeviceAPI.getDevices();

  devices.forEach((device) => {
    const inputs = document.querySelectorAll("input[data-device-id]");

    inputs.forEach((input) => {
      if (input.dataset.deviceId === String(device.id)) {
        input.checked = device.status === "on";
      }
    });
  });
}

function attachDeviceControlListeners() {
  document.querySelectorAll("input[data-device-id]").forEach((input) => {
    input.addEventListener("change", async () => {
      const newStatus = input.checked ? "on" : "off";
      const previousChecked = !input.checked;

      input.disabled = true;

      try {
        const result = await DeviceAPI.setDeviceStatus(
          input.dataset.deviceId,
          newStatus
        );

        if (!result.success) {
          input.checked = previousChecked;
          return;
        }

        /*
         * Không đọc lại /devices ngay.
         *
         * Firmware gửi V6 khoảng mỗi 5 giây,
         * backend listener cần thêm thời gian cập nhật device_state.
         * Cho actual telemetry khoảng 7 giây để bắt kịp.
         */
        suppressDevicePollUntil = Date.now() + 7000;

      } finally {
        input.disabled = false;
      }
    });
  });
}

// ACTIVITY LOGS
async function renderLogs() {
  const typeFilter = document.getElementById("logs-filter-type").value;
  const logs = await LogAPI.getLogs();

  const filtered = logs.filter((log) => {
    if (typeFilter !== "all" && log.type !== typeFilter) return false;
    return true;
  });

  const tbody = document.querySelector("#logs-table tbody");
  tbody.innerHTML = filtered.map((log) => `
    <tr>
      <td>${log.time}</td>
      <td>${log.type}</td>
      <td>${log.description}</td>
      <td>${log.device}</td>
      <td>${log.status}</td>
    </tr>
  `).join("");
}

function initLogsFilters() {
  document.getElementById("logs-filter-type").addEventListener("change", renderLogs);
  document.getElementById("logs-filter-time").addEventListener("change", renderLogs);
}

// ALERTS
async function renderAlerts() {
  const alerts = await AlertAPI.getAlerts();
  const listEl = document.getElementById("alerts-list");

  listEl.innerHTML = alerts.map((alert) => `
    <li class="alert-item level-${alert.level} ${alert.read ? "" : "unread"}">
      <span class="alert-level">${alert.level}</span>
      <div class="alert-body">
        <span class="alert-message">${alert.message}</span>
        <span class="alert-time">${alert.time}</span>
      </div>
      <button type="button" class="mark-read-btn" data-alert-id="${alert.id}" ${alert.read ? "disabled" : ""}>
        ${alert.read ? "Read" : "Mark as read"}
      </button>
    </li>
  `).join("");

  document.querySelectorAll(".mark-read-btn[data-alert-id]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await AlertAPI.markAlertRead(Number(btn.dataset.alertId));
      await renderAlerts();
    });
  });
}

// OVERVIEW THRESHOLDS

async function refreshOverviewThresholds() {
  const thresholds = await SettingsAPI.getThresholds();

  const tempInput = document.getElementById("overview-temp-threshold");
  const humidityInput = document.getElementById("overview-humidity-threshold");
  const lightInput = document.getElementById("overview-light-threshold");

  // Không ghi đè lúc user đang nhập
  if (document.activeElement !== tempInput) {
    tempInput.value = thresholds.temperature;
  }

  if (document.activeElement !== humidityInput) {
    humidityInput.value = thresholds.humidity;
  }

  if (document.activeElement !== lightInput) {
    lightInput.value = thresholds.light ?? "";
  }
}

function initOverviewThresholdForm() {
  document
    .getElementById("overview-save-thresholds")
    .addEventListener("click", async () => {

      const temperature = Number(
        document.getElementById("overview-temp-threshold").value
      );

      const humidity = Number(
        document.getElementById("overview-humidity-threshold").value
      );

      const light = Number(
        document.getElementById("overview-light-threshold").value
      );

      const result = await SettingsAPI.saveThresholds(
        temperature,
        humidity,
        light
      );

      document.getElementById(
        "overview-threshold-message"
      ).textContent = result.message;

      if (result.success) {
        await refreshOverviewThresholds();
        await renderSettings();
      }
    });
}

// SETTINGS
async function renderSettings() {
  const [account, adafruit, thresholds] = await Promise.all([
    SettingsAPI.getAccount(),
    SettingsAPI.getAdafruitConfig(),
    SettingsAPI.getThresholds()
  ]);

  document.getElementById("settings-name").value = account.name;
  document.getElementById("settings-email").value = account.email;
  document.getElementById("settings-aio-username").value = adafruit.username;
  document.getElementById("settings-aio-key").value = adafruit.aioKey;
  document.getElementById("settings-temp-threshold").value = thresholds.temperature;
  document.getElementById("settings-humidity-threshold").value = thresholds.humidity;
  document.getElementById("settings-light-threshold").value = thresholds.light || "";
}

function initSettingsForm() {
  document.getElementById("settings-save-aio").addEventListener("click", async () => {
    const username = document.getElementById("settings-aio-username").value;
    const aioKey = document.getElementById("settings-aio-key").value;
    const result = await SettingsAPI.saveAdafruitConfig(username, aioKey);
    showSettingsMessage(result.message);
  });

  document.getElementById("settings-save-thresholds").addEventListener("click", async () => {
    const temperature = Number(document.getElementById("settings-temp-threshold").value);
    const humidity = Number(document.getElementById("settings-humidity-threshold").value);
    const light = Number(document.getElementById("settings-light-threshold").value);

    // Tạm thời vẫn dùng hàm cũ (sau này có thể mở rộng API)
    const result = await SettingsAPI.saveThresholds(
      temperature,
      humidity,
      light
    );

    showSettingsMessage(result.message);

    if (result.success) {
      await refreshOverviewThresholds();
    }
  });
}

function showSettingsMessage(text) {
  document.getElementById("settings-status-message").textContent = text;
}

async function refreshSensorCards() {
  const sensors = await SensorAPI.getSensorData();

  document.getElementById("overview-temp-value").textContent =
    sensors.temperature.current + " " + sensors.temperature.unit;

  document.getElementById("overview-humidity-value").textContent =
    sensors.humidity.current + " " + sensors.humidity.unit;

  document.getElementById("overview-light-value").textContent =
    sensors.light.current + " " + sensors.light.unit;
}

// INIT
async function init() {
  // Kiểm tra đăng nhập
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "login.html";
    return;
  }

  const account = await SettingsAPI.getAccount();
  document.getElementById("topbar-account-name").textContent = account.name;

  initSidebarNavigation();
  initLogsFilters();
  initSettingsForm();
  initOverviewThresholdForm();

  await Promise.all([
    renderOverview(),
    renderLogs(),
    renderAlerts(),
    renderSettings()
  ]);

    setInterval(refreshSensorCards, 2000);
  devicePollTimer = setInterval(() => {
    refreshDeviceStates();
  }, 2000);
  setInterval(() => {
    refreshOverviewThresholds();
  }, 2000);
}

document.addEventListener("DOMContentLoaded", init);
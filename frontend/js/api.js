// CONFIG
const API_CONFIG = {
  mode: "real",                      // Nếu sử dụng thì thay "mock" thành "real"
  baseUrl: "http://localhost:8080"   // used only when mode === "real"
};

//call api
async function request(path, options = {}) {
  try {
    const token = localStorage.getItem("token");

    const response = await fetch(`${API_CONFIG.baseUrl}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {})
      }
    });

    let body = null;
    try {
      body = await response.json();
    } catch (_) {
      // Response had no JSON body (or wasn't JSON) — leave body as null.
    }

    return { ok: response.ok, status: response.status, body, networkError: false };
  } catch (networkError) {
    return { ok: false, status: 0, body: null, networkError: true };
  }
}

// AUTHENTICATION
const AuthAPI = {
  // Mock
  async login(email, password) {
    if (API_CONFIG.mode === "mock") {
      const isValid =
        email === mockData.auth.email && password === mockData.auth.password;

      return isValid
        ? { success: true, message: "Login successful (mock).", token: mockData.auth.token }
        : { success: false, message: "Invalid email or password (mock).", token: null };
    }

    // Real Mode
    const { ok, body, networkError } = await request("/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });

    if (networkError) {
      return { success: false, message: "Unable to reach the server. Please try again later.", token: null };
    }

    return ok
      ? { success: true, message: body?.message || "Login successful.", token: body?.token || null }
      : { success: false, message: body?.message || "Invalid email or password.", token: null };
  }
};


// DEVICES
//Mock
const DeviceAPI = {
  async getDevices() {
    if (API_CONFIG.mode === "mock") {
      return mockData.devices;
    }
    //real
    const { ok, body } = await request("/devices");
    return ok && body?.devices ? body.devices : [];
  },

  async setDeviceStatus(deviceId, status) {
    if (API_CONFIG.mode === "mock") {
      const device = mockData.devices.find((d) => d.id === deviceId);
      if (device) device.status = status;
      return {
        success: !!device,
        message: device ? "Device updated (mock)." : "Device not found (mock)."
      };
    }
    //Real
    const { ok, body } = await request(`/devices/${deviceId}`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    });
    return { success: ok, message: body?.message || (ok ? "Device updated." : "Failed to update device.") };
  }
};

// SENSORS
const SensorAPI = {
  async getSensorData() {
    if (API_CONFIG.mode === "mock") {
      return mockData.sensors;
    }
    //real
    const { ok, body } = await request("/sensors");
    return ok && body
      ? body
      : {
          temperature: { current: 0, unit: "°C", history: [] },
          humidity: { current: 0, unit: "%", history: [] }
        };
  }
};

// ALERTS
const AlertAPI = {
  async getAlerts() {
    if (API_CONFIG.mode === "mock") {
      return mockData.alerts;
    }
    //real
    const { ok, body } = await request("/alerts");
    return ok && body?.alerts ? body.alerts : [];
  },

  async markAlertRead(alertId) {
    if (API_CONFIG.mode === "mock") {
      const alert = mockData.alerts.find((a) => a.id === alertId);
      if (alert) alert.read = true;
      return { success: !!alert };
    }
    //real
    const { ok } = await request(`/alerts/${alertId}/read`, { method: "PATCH" });
    return { success: ok };
  }
};

// ACTIVITY LOGS
const LogAPI = {
  async getLogs() {
    if (API_CONFIG.mode === "mock") {
      return mockData.logs;
    }
    //real
    const { ok, body } = await request("/logs");
    return ok && body?.logs ? body.logs : [];
  }
};

// SETTINGS
const SettingsAPI = {
  async getAccount() {
    if (API_CONFIG.mode === "mock") {
      return mockData.account;
    }
    //real
    const { ok, body } = await request("/account");
    return ok && body ? body : { name: "", email: "" };
  },

  async getAdafruitConfig() {
    if (API_CONFIG.mode === "mock") {
      return mockData.adafruit;
    }
    //real
    const { ok, body } = await request("/settings/adafruit");
    return ok && body ? body : { username: "", aioKey: "" };
  },

  async getThresholds() {
    if (API_CONFIG.mode === "mock") {
      return mockData.thresholds;
    }
    //real
    const { ok, body } = await request("/settings/thresholds");
    return ok && body ? body : { temperature: 0, humidity: 0 };
  },

  async saveAdafruitConfig(username, aioKey) {
    if (API_CONFIG.mode === "mock") {
      mockData.adafruit.username = username;
      mockData.adafruit.aioKey = aioKey;
      return { success: true, message: "Adafruit IO info saved locally (mock only)." };
    }
    //real
    const { ok, body } = await request("/settings/adafruit", {
      method: "PUT",
      body: JSON.stringify({ username, aioKey })
    });
    return { success: ok, message: body?.message || (ok ? "Adafruit IO settings saved." : "Failed to save settings.") };
  },

  async saveThresholds(temperature, humidity, light) {
    if (API_CONFIG.mode === "mock") {
      mockData.thresholds.temperature = temperature;
      mockData.thresholds.humidity = humidity;
      return { success: true, message: "Thresholds saved locally (mock only)." };
    }
    //real
    const { ok, body } = await request("/settings/thresholds", {
      method: "PUT",
      body: JSON.stringify({ temperature, humidity, light })
    });
    return { success: ok, message: body?.message || (ok ? "Thresholds saved." : "Failed to save thresholds.") };
  }
};

// SYSTEM (misc values like "last updated")
const SystemAPI = {
  async getLastUpdated() {
    if (API_CONFIG.mode === "mock") {
      return mockData.lastUpdated;
    }
    //real
    const { ok, body } = await request("/system/status");
    return ok && body?.lastUpdated ? body.lastUpdated : "--";
  }
};
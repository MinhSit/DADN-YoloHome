const mockData = {

  // Mock credentials used ONLY when API_CONFIG.mode === "mock" in api/api.js.
  // This is purely for frontend testing without a backend — not a real
  // authentication mechanism, and not used at all in "real" mode.
  auth: {
    email: "demo@yolohome.com",
    password: "demo-password",
    token: "mock-token"
  },

  // Thông tin tài khoản hiển thị ở Settings
  account: {
    name: "User Demo",
    email: "user@example.com"
  },

  // Thông tin kết nối Adafruit IO (rỗng vì chưa có thật)
  adafruit: {
    username: "",
    aioKey: ""
  },

  // Ngưỡng cảnh báo, chỉnh được ở trang Settings (chỉ lưu tạm trong bộ nhớ)
  thresholds: {
    temperature: 30,
    humidity: 70
  },

  // Dữ liệu cảm biến hiện tại + lịch sử 12 điểm gần nhất (dùng để vẽ biểu đồ)
  sensors: {
    temperature: {
      current: 24,
      unit: "°C",
      history: [22, 23, 24, 24, 25, 24, 23, 24, 25, 26, 24, 24]
    },
    humidity: {
      current: 58,
      unit: "%",
      history: [55, 56, 58, 60, 59, 58, 57, 58, 59, 60, 58, 58]
    },
    light: {                                         // ← THÊM MỚI
    current: 320,
    unit: "lux",
    history: [280, 300, 310, 320, 350, 340, 330, 320, 310, 300, 290, 320]
    }
  },

  // Danh sách thiết bị: id dùng để nối với nút bấm, status là trạng thái hiện tại
  devices: [
    { id: "light", name: "Light", type: "toggle", status: "on", online: true },
    { id: "fan", name: "Fan", type: "toggle", status: "off", online: true },
  ],

  // Lịch sử hoạt động, dùng cho trang Activity Logs
  logs: [
    { time: "2026-08-09 09:12", type: "Device", description: "Light turned ON", device: "Light", status: "Success" },
    { time: "2026-08-09 08:55", type: "Alert", description: "Humidity above threshold", device: "Sensor", status: "Warning" },
    { time: "2026-08-09 08:40", type: "Device", description: "Fan turned OFF", device: "Fan", status: "Success" },
    { time: "2026-08-08 22:10", type: "Device", description: "Light turned OFF", device: "Light", status: "Success" },
    { time: "2026-08-08 19:05", type: "Alert", description: "Temperature above threshold", device: "Sensor", status: "Critical" }
  ],

  // Danh sách cảnh báo, dùng cho trang Alerts và mục "cảnh báo gần nhất" ở Overview
  alerts: [
    { id: 1, level: "Critical", message: "Temperature above threshold (32°C)", time: "2 min ago", read: false },
    { id: 2, level: "Warning", message: "Humidity above threshold (75%)", time: "20 min ago", read: false },
    { id: 4, level: "Warning", message: "Fan offline", time: "3 hours ago", read: true },
    { id: 5, level: "Info", message: "System started", time: "5 hours ago", read: true }
  ],

  // Thời điểm cập nhật lần cuối — sau này sẽ lấy từ timestamp của API thật
  lastUpdated: "2026-08-09 09:15 AM"
};
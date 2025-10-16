# 📱 Shipper App - Android Application

Ứng dụng dành cho Shipper (người giao hàng) được xây dựng bằng Kotlin và Android SDK.

## 🎯 Tính năng chính

### 1. **Đăng nhập**
- Đăng nhập bằng email và mật khẩu
- Tự động lưu token và thông tin người dùng
- Kiểm tra quyền truy cập (chỉ shipper mới đăng nhập được)

### 2. **Danh sách đơn hàng có thể nhận**
- Hiển thị các đơn hàng đang ở trạng thái "Đang chuẩn bị"
- Xem thông tin khách hàng, địa chỉ, tổng tiền
- Pull-to-refresh để cập nhật danh sách
- Nhấn vào đơn hàng để xem chi tiết và nhận đơn

### 3. **Đơn hàng của tôi**
- Tab "Đang giao": Hiển thị các đơn đang giao
- Tab "Hoàn tất": Hiển thị các đơn đã giao xong
- Tab "Tất cả": Hiển thị tất cả đơn hàng của shipper
- Pull-to-refresh để cập nhật

### 4. **Chi tiết đơn hàng**
- Xem đầy đủ thông tin đơn hàng
- Thông tin khách hàng (tên, SĐT, địa chỉ)
- Danh sách món ăn và giá tiền
- Ghi chú từ khách hàng (nếu có)
- Nút "Nhận đơn" (cho đơn có thể nhận)
- Nút "Hoàn tất" (cho đơn đang giao)

### 5. **Tài khoản**
- Hiển thị thông tin shipper
- Đăng xuất

## 🏗️ Kiến trúc ứng dụng

### Công nghệ sử dụng:
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines
- **UI**: Material Design Components
- **Storage**: SharedPreferences

### Cấu trúc thư mục:
```
app/src/main/java/com/example/foodapp/
├── data/
│   ├── api/
│   │   ├── RetrofitClient.kt         # Khởi tạo Retrofit
│   │   └── ShipperApiService.kt      # Định nghĩa API endpoints
│   ├── models/                        # Data classes
│   │   ├── User.kt
│   │   ├── Order.kt
│   │   ├── OrderItem.kt
│   │   ├── Customer.kt
│   │   ├── LoginRequest.kt
│   │   └── ApiResponse.kt
│   └── repository/
│       └── ShipperRepository.kt      # Xử lý API calls
├── ui/
│   ├── login/                        # Màn hình đăng nhập
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── main/                         # Màn hình chính
│   │   └── MainActivity.kt
│   ├── available/                    # Đơn có thể nhận
│   │   ├── AvailableOrdersFragment.kt
│   │   └── AvailableOrdersViewModel.kt
│   ├── myorders/                     # Đơn của tôi
│   │   ├── MyOrdersFragment.kt
│   │   └── MyOrdersViewModel.kt
│   ├── detail/                       # Chi tiết đơn hàng
│   │   ├── OrderDetailActivity.kt
│   │   └── OrderDetailViewModel.kt
│   ├── profile/                      # Tài khoản
│   │   └── ProfileFragment.kt
│   └── adapter/                      # RecyclerView adapters
│       ├── OrderAdapter.kt
│       └── OrderItemAdapter.kt
└── utils/
    └── TokenManager.kt               # Quản lý token và user info
```

## 🚀 Cài đặt và chạy

### Yêu cầu:
- Android Studio Hedgehog trở lên
- Android SDK 24+
- JDK 8+

### Bước 1: Clone project
```bash
cd /Users/nguyentienkhoa/Desktop/PROJECT
```

### Bước 2: Cấu hình API URL
Mở file `RetrofitClient.kt` và cập nhật BASE_URL:
```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8000/api/shipper/"
```

**Lưu ý**: 
- Nếu test trên emulator: `http://10.0.2.2:8000/api/shipper/`
- Nếu test trên thiết bị thật: `http://YOUR_LOCAL_IP:8000/api/shipper/`

### Bước 3: Sync Gradle
Trong Android Studio, nhấn "Sync Now" để tải dependencies.

### Bước 4: Chạy ứng dụng
- Kết nối thiết bị Android hoặc khởi động emulator
- Nhấn nút "Run" trong Android Studio

## 📝 Thông tin đăng nhập test

Theo tài liệu API, sử dụng tài khoản:
- **Email**: `22004011@st.vlute.edu.vn`
- **Password**: `Monkay991@`

## 🔐 Bảo mật

- Token được lưu an toàn trong SharedPreferences
- Sử dụng HTTPS trong production
- Token tự động gửi kèm mọi request qua Interceptor
- Tự động đăng xuất khi token hết hạn

## 📱 Giao diện

### Màn hình đăng nhập
- Material Design với TextInputLayout
- Hiển thị loading khi đang xử lý
- Toast message cho thông báo lỗi/thành công

### Màn hình chính
- Bottom Navigation với 3 tab
- Toolbar với menu đăng xuất
- Fragment-based navigation

### Danh sách đơn hàng
- RecyclerView với CardView
- SwipeRefreshLayout
- Empty state khi không có đơn
- Loading indicator

### Chi tiết đơn hàng
- ScrollView để xem đầy đủ thông tin
- Material Cards cho từng phần thông tin
- Action buttons (Nhận đơn/Hoàn tất)
- Confirm dialog trước khi thực hiện action

## 🐛 Xử lý lỗi

Ứng dụng xử lý các trường hợp lỗi:
- Lỗi mạng (No internet)
- Server error (500)
- Unauthorized (401) - Token hết hạn
- Validation errors (422)
- Toast message thân thiện cho người dùng

## 🔄 Quy trình làm việc

1. **Shipper đăng nhập** → App lưu token
2. **Xem đơn có thể nhận** → Tab "Đơn có thể nhận"
3. **Nhấn vào đơn** → Xem chi tiết
4. **Nhấn "Nhận đơn"** → Đơn chuyển sang "Đang giao"
5. **Giao hàng** → Đơn xuất hiện ở tab "Đơn của tôi" > "Đang giao"
6. **Hoàn tất giao hàng** → Nhấn "Hoàn tất" → Đơn chuyển sang "Hoàn tất"

## 📚 Dependencies

```kotlin
// Retrofit - HTTP client
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp - Logging
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Coroutines - Async
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Lifecycle - ViewModel & LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

// Material Design
implementation("com.google.android.material:material:1.x.x")
```

## 🎨 Màu sắc và Theme

Ứng dụng sử dụng Material Design 3 với:
- Primary color: Material default
- Cards với elevation và rounded corners
- Bottom Navigation với ripple effect
- Material buttons

## 📱 Hỗ trợ

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Screen orientation**: Portrait (có thể thêm landscape)
- **Language**: Vietnamese

## 🔮 Tính năng có thể mở rộng

- [ ] Thông báo push khi có đơn mới
- [ ] Google Maps tích hợp để chỉ đường
- [ ] Gọi điện trực tiếp cho khách hàng
- [ ] Lịch sử thu nhập
- [ ] Thống kê đơn hàng theo ngày/tuần/tháng
- [ ] Dark mode
- [ ] Multi-language support

## 👨‍💻 Developer

Nguyễn Tiến Khoa
- GitHub: [Your GitHub]
- Email: 22004011@st.vlute.edu.vn

## 📄 License

MIT License

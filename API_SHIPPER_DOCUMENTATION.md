# 📱 API Documentation cho Shipper App

## 📋 Mục lục
- [Giới thiệu](#giới-thiệu)
- [Base URL](#base-url)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [1. Đăng nhập](#1-đăng-nhập)
  - [2. Lấy danh sách đơn hàng có thể nhận](#2-lấy-danh-sách-đơn-hàng-có-thể-nhận)
  - [3. Nhận đơn hàng](#3-nhận-đơn-hàng)
  - [4. Hoàn tất đơn hàng](#4-hoàn-tất-đơn-hàng)
  - [5. Lấy đơn hàng của shipper](#5-lấy-đơn-hàng-của-shipper)
  - [6. Đăng xuất](#6-đăng-xuất)
- [Response Codes](#response-codes)
- [Error Handling](#error-handling)

---

## 🌟 Giới thiệu

API này được thiết kế cho ứng dụng dành riêng cho Shipper (người giao hàng). Chỉ những tài khoản có `vai_tro_id = 5` (vai trò Shipper) mới có thể đăng nhập và sử dụng API này.

### Quy trình hoạt động:
1. Shipper đăng nhập → Nhận token
2. Nhân viên xác nhận đơn → Đơn chuyển sang "Đang chuẩn bị"
3. Shipper xem danh sách đơn có thể nhận
4. Shipper chọn nhận đơn → Đơn chuyển sang "Đang giao" + lưu `shipper_id`
5. Shipper giao hàng xong → Bấm hoàn tất → Đơn chuyển sang "Hoàn tất"

### Dữ liệu trả về:
API trả về dữ liệu đã được format sẵn để hiển thị trực tiếp trên app:
- **Tiền tệ**: Được format với dấu phân cách nghìn (ví dụ: "250.000 đ")
- **Ngày giờ**: Format dd/mm/yyyy HH:mm (ví dụ: "15/10/2023 14:30")
- **Chi tiết món**: Bao gồm tên món, số lượng, đơn giá và thành tiền
- **Thông tin thanh toán**: `payment_method` (COD, VNPay, etc.) và `payment_status` (pending, paid, etc.)
- **Raw values**: Các field có `_raw` suffix chứa giá trị gốc để tính toán (ví dụ: `tong_thanh_toan_raw`)

---

## 🌐 Base URL

```
http://localhost:8000/api/shipper
```

**Production:**
```
https://yourdomain.com/api/shipper
```

---

## 🔐 Authentication

API sử dụng **Bearer Token Authentication**. Sau khi đăng nhập thành công, bạn sẽ nhận được một token. Token này cần được gửi kèm trong header của mọi request (trừ API đăng nhập).

### Header Format:
```
Authorization: Bearer {your_token_here}
```

### Token Expiration:
Token có hiệu lực trong **30 ngày** kể từ khi được tạo.

---

## 📡 API Endpoints

### 1. Đăng nhập

Đăng nhập vào hệ thống với tài khoản shipper.

**Endpoint:**
```
POST /api/shipper/login
```

**Headers:**
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json"
}
```

**Request Body:**
```json
{
  "email": "shipper@example.com",
  "password": "password123"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "user": {
      "id": 42,
      "ho": "Nguyễn",
      "ten": "Văn A",
      "email": "shipper@example.com",
      "so_dien_thoai": "0123456789",
      "vai_tro_id": 5
    },
    "token": "eyJ1c2VyX2lkIjo0MiwiZW1haWwiOiJzaGlwcGVyQGV4YW1wbGUuY29tIiwidmFpX3Ryb19pZCI6NSwiZXhwaXJlc19hdCI6MTczMjU0MzIwMCwicmFuZG9tIjoiYWJjZGVmMTIzNDU2Nzg5MCJ9"
  }
}
```

**Response Error (401 - Email/Password sai):**
```json
{
  "success": false,
  "message": "Mật khẩu không chính xác"
}
```

**Response Error (403 - Không phải shipper):**
```json
{
  "success": false,
  "message": "Tài khoản không có quyền truy cập app shipper"
}
```

**Response Error (403 - Tài khoản bị khóa):**
```json
{
  "success": false,
  "message": "Tài khoản đã bị khóa"
}
```

---

### 2. Lấy danh sách đơn hàng có thể nhận

Lấy danh sách các đơn hàng đã được nhân viên xác nhận và đang ở trạng thái "Đang chuẩn bị", sẵn sàng cho shipper nhận.

**Endpoint:**
```
GET /api/shipper/orders/available
```

**Headers:**
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json",
  "Authorization": "Bearer {your_token}"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Lấy danh sách đơn hàng thành công",
  "data": [
    {
      "id": 123,
      "ma_don_hang": "DH20231015123456",
      "khach_hang": {
        "ten": "Trần Văn B",
        "so_dien_thoai": "0987654321"
      },
      "dia_chi_giao": "123 Đường ABC, Quận 1, TP.HCM",
      "tong_thanh_toan": "250.000 đ",
      "tong_thanh_toan_raw": "250000.00",
      "payment_method": "COD",
      "payment_status": "pending",
      "ghi_chu": "Gọi trước khi giao",
      "trang_thai": "Đang chuẩn bị",
      "ngay_tao": "15/10/2023 10:30",
      "chi_tiet": [
        {
          "ten_mon": "Bánh mì thịt",
          "so_luong": 2,
          "don_gia": "100.000 đ",
          "thanh_tien": "200.000 đ"
        },
        {
          "ten_mon": "Nước cam",
          "so_luong": 1,
          "don_gia": "50.000 đ",
          "thanh_tien": "50.000 đ"
        }
      ]
    }
  ]
}
```

**Response Error (401 - Không có token hoặc token không hợp lệ):**
```json
{
  "success": false,
  "message": "Unauthorized - Token không được cung cấp"
}
```

---

### 3. Nhận đơn hàng

Shipper nhận đơn hàng để giao. Khi nhận đơn, trạng thái đơn hàng sẽ chuyển sang "Đang giao" và `shipper_id` sẽ được gán.

**Endpoint:**
```
POST /api/shipper/orders/{id}/accept
```

**Parameters:**
- `id` (integer, required): ID của đơn hàng cần nhận

**Headers:**
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json",
  "Authorization": "Bearer {your_token}"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Nhận đơn hàng thành công",
  "data": {
    "id": 123,
    "ma_don_hang": "DH20231015123456",
    "shipper_id": 42,
    "trang_thai": "Đang giao",
    "dia_chi_giao": "123 Đường ABC, Quận 1, TP.HCM",
    "so_dien_thoai_nhan": "0987654321",
    "tong_thanh_toan": "250000.00",
    "nguoi_dung": {
      "id": 10,
      "ho": "Trần",
      "ten": "Văn B",
      "so_dien_thoai": "0987654321"
    }
  }
}
```

**Response Error (404):**
```json
{
  "success": false,
  "message": "Không tìm thấy đơn hàng"
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Đơn hàng này không thể nhận (đã có shipper khác nhận hoặc trạng thái không phù hợp)"
}
```

---

### 4. Hoàn tất đơn hàng

Shipper xác nhận đã giao hàng thành công. Trạng thái đơn hàng sẽ chuyển sang "Hoàn tất".

**Endpoint:**
```
POST /api/shipper/orders/{id}/complete
```

**Parameters:**
- `id` (integer, required): ID của đơn hàng cần hoàn tất

**Headers:**
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json",
  "Authorization": "Bearer {your_token}"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Hoàn tất đơn hàng thành công",
  "data": {
    "id": 123,
    "ma_don_hang": "DH20231015123456",
    "shipper_id": 42,
    "trang_thai": "Hoàn tất",
    "dia_chi_giao": "123 Đường ABC, Quận 1, TP.HCM",
    "tong_thanh_toan": "250000.00"
  }
}
```

**Response Error (404):**
```json
{
  "success": false,
  "message": "Không tìm thấy đơn hàng"
}
```

**Response Error (403):**
```json
{
  "success": false,
  "message": "Bạn không có quyền hoàn tất đơn hàng này (không phải shipper của đơn)"
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Đơn hàng này không thể hoàn tất (trạng thái không phù hợp)"
}
```

---

### 5. Lấy đơn hàng của shipper

Lấy danh sách tất cả đơn hàng mà shipper hiện tại đang giao hoặc đã giao.

**Endpoint:**
```
GET /api/shipper/orders/my-orders
```

**Headers:**
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json",
  "Authorization": "Bearer {your_token}"
}
```

**Query Parameters (Optional):**
- `status` (string): Lọc theo trạng thái (`Đang giao`, `Hoàn tất`)
  - Example: `/api/shipper/orders/my-orders?status=Đang giao`

**Response Success (200):**
```json
{
  "success": true,
  "message": "Lấy danh sách đơn hàng thành công",
  "data": [
    {
      "id": 123,
      "ma_don_hang": "DH20231015123456",
      "khach_hang": {
        "ten": "Trần Văn B",
        "so_dien_thoai": "0987654321"
      },
      "dia_chi_giao": "123 Đường ABC, Quận 1, TP.HCM",
      "tong_thanh_toan": "250.000 đ",
      "tong_thanh_toan_raw": "250000.00",
      "payment_method": "COD",
      "payment_status": "pending",
      "ghi_chu": "Gọi trước khi giao",
      "trang_thai": "Đang giao",
      "ngay_nhan": "15/10/2023 11:00",
      "chi_tiet": [
        {
          "ten_mon": "Bánh mì thịt",
          "so_luong": 2,
          "don_gia": "100.000 đ",
          "thanh_tien": "200.000 đ"
        },
        {
          "ten_mon": "Nước cam",
          "so_luong": 1,
          "don_gia": "50.000 đ",
          "thanh_tien": "50.000 đ"
        }
      ]
    }
  ]
}
```

---

### 6. Đăng xuất

Đăng xuất khỏi hệ thống (client nên xóa token đã lưu).

**Endpoint:**
```
POST /api/shipper/logout
```

**Headers:**
```json
{
  "Content-Type": "application/json",
  "Accept": "application/json",
  "Authorization": "Bearer {your_token}"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Đăng xuất thành công"
}
```

---

## � Response Fields Explanation

### Đơn hàng (Order):
| Field | Type | Description |
|-------|------|-------------|
| `id` | integer | ID đơn hàng |
| `ma_don_hang` | string | Mã đơn hàng (unique) |
| `khach_hang` | object | Thông tin khách hàng |
| `khach_hang.ten` | string | Tên đầy đủ khách hàng |
| `khach_hang.so_dien_thoai` | string | Số điện thoại người nhận |
| `dia_chi_giao` | string | Địa chỉ giao hàng |
| `tong_thanh_toan` | string | Tổng tiền đã format (ví dụ: "250.000 đ") |
| `tong_thanh_toan_raw` | decimal | Tổng tiền raw để tính toán |
| `payment_method` | string | Phương thức thanh toán (COD, VNPay, MoMo, etc.) |
| `payment_status` | string | Trạng thái thanh toán (pending, paid, failed) |
| `ghi_chu` | string/null | Ghi chú từ khách hàng |
| `trang_thai` | string | Trạng thái đơn hàng |
| `ngay_tao` | string | Ngày tạo đơn (format: dd/mm/yyyy HH:mm) |
| `ngay_nhan` | string | Ngày shipper nhận đơn |
| `chi_tiet` | array | Danh sách món ăn trong đơn |

### Chi tiết món (Order Items):
| Field | Type | Description |
|-------|------|-------------|
| `ten_mon` | string | Tên món ăn |
| `so_luong` | integer | Số lượng |
| `don_gia` | string | Đơn giá đã format (ví dụ: "100.000 đ") |
| `thanh_tien` | string | Thành tiền đã format (số lượng × đơn giá) |

### Trạng thái đơn hàng:
- **"Chờ xác nhận"**: Đơn mới, chờ nhân viên xác nhận
- **"Đang chuẩn bị"**: Nhân viên đã xác nhận, đơn sẵn sàng cho shipper nhận
- **"Đang giao"**: Shipper đã nhận và đang giao
- **"Hoàn tất"**: Đã giao thành công
- **"Đã hủy"**: Đơn bị hủy

### Phương thức thanh toán:
- **"COD"**: Thanh toán khi nhận hàng (Cash on Delivery)
- **"VNPay"**: Thanh toán qua VNPay
- **"MoMo"**: Thanh toán qua MoMo
- Các phương thức khác tùy hệ thống

### Trạng thái thanh toán:
- **"pending"**: Chưa thanh toán
- **"paid"**: Đã thanh toán
- **"failed"**: Thanh toán thất bại

---

## �📊 Response Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request thành công |
| 400 | Bad Request | Request không hợp lệ hoặc thiếu tham số |
| 401 | Unauthorized | Không có token hoặc token không hợp lệ |
| 403 | Forbidden | Không có quyền truy cập |
| 404 | Not Found | Không tìm thấy tài nguyên |
| 422 | Unprocessable Entity | Dữ liệu validation lỗi |
| 500 | Internal Server Error | Lỗi server |

---

## ⚠️ Error Handling

Tất cả response lỗi đều có format:

```json
{
  "success": false,
  "message": "Mô tả lỗi"
}
```

Hoặc với validation errors:

```json
{
  "message": "The given data was invalid.",
  "errors": {
    "email": [
      "Trường email là bắt buộc."
    ],
    "password": [
      "Trường password là bắt buộc."
    ]
  }
}
```

---

## 💡 Ví dụ sử dụng trong Mobile App

### Flutter Example:

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class ShipperAPI {
  static const String baseUrl = 'http://localhost:8000/api/shipper';
  String? token;

  // 1. Đăng nhập
  Future<Map<String, dynamic>> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/login'),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );

    final data = jsonDecode(response.body);
    
    if (response.statusCode == 200 && data['success']) {
      token = data['data']['token'];
      // Lưu token vào storage
      return data;
    } else {
      throw Exception(data['message']);
    }
  }

  // 2. Lấy đơn hàng có thể nhận
  Future<List<dynamic>> getAvailableOrders() async {
    final response = await http.get(
      Uri.parse('$baseUrl/orders/available'),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    final data = jsonDecode(response.body);
    
    if (response.statusCode == 200 && data['success']) {
      return data['data'];
    } else {
      throw Exception(data['message']);
    }
  }

  // 3. Nhận đơn hàng
  Future<Map<String, dynamic>> acceptOrder(int orderId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/orders/$orderId/accept'),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    final data = jsonDecode(response.body);
    
    if (response.statusCode == 200 && data['success']) {
      return data['data'];
    } else {
      throw Exception(data['message']);
    }
  }

  // 4. Hoàn tất đơn hàng
  Future<Map<String, dynamic>> completeOrder(int orderId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/orders/$orderId/complete'),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    final data = jsonDecode(response.body);
    
    if (response.statusCode == 200 && data['success']) {
      return data['data'];
    } else {
      throw Exception(data['message']);
    }
  }

  // 5. Lấy đơn hàng của tôi
  Future<List<dynamic>> getMyOrders({String? status}) async {
    String url = '$baseUrl/orders/my-orders';
    if (status != null) {
      url += '?status=$status';
    }

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    final data = jsonDecode(response.body);
    
    if (response.statusCode == 200 && data['success']) {
      return data['data'];
    } else {
      throw Exception(data['message']);
    }
  }
}
```

### React Native Example:

```javascript
import axios from 'axios';

const BASE_URL = 'http://localhost:8000/api/shipper';
let token = null;

// 1. Đăng nhập
export const login = async (email, password) => {
  try {
    const response = await axios.post(`${BASE_URL}/login`, {
      email,
      password
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    if (response.data.success) {
      token = response.data.data.token;
      // Lưu token vào AsyncStorage
      return response.data;
    }
  } catch (error) {
    throw error.response.data.message;
  }
};

// 2. Lấy đơn hàng có thể nhận
export const getAvailableOrders = async () => {
  try {
    const response = await axios.get(`${BASE_URL}/orders/available`, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    });

    return response.data.data;
  } catch (error) {
    throw error.response.data.message;
  }
};

// 3. Nhận đơn hàng
export const acceptOrder = async (orderId) => {
  try {
    const response = await axios.post(
      `${BASE_URL}/orders/${orderId}/accept`,
      {},
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      }
    );

    return response.data.data;
  } catch (error) {
    throw error.response.data.message;
  }
};

// 4. Hoàn tất đơn hàng
export const completeOrder = async (orderId) => {
  try {
    const response = await axios.post(
      `${BASE_URL}/orders/${orderId}/complete`,
      {},
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      }
    );

    return response.data.data;
  } catch (error) {
    throw error.response.data.message;
  }
};

// 5. Lấy đơn hàng của tôi
export const getMyOrders = async (status = null) => {
  try {
    let url = `${BASE_URL}/orders/my-orders`;
    if (status) {
      url += `?status=${status}`;
    }

    const response = await axios.get(url, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    });

    return response.data.data;
  } catch (error) {
    throw error.response.data.message;
  }
};
```

---

---

## 🧪 Hướng dẫn Test API với Postman

### Chuẩn bị:
1. Mở Postman
2. Tạo một Collection mới tên "Shipper API"
3. Đảm bảo Laravel server đang chạy (`php artisan serve`)

---

### Test 1: Đăng nhập Shipper

**Bước 1:** Tạo request mới trong Postman
- Click **New** → **HTTP Request**
- Đặt tên: "1. Login Shipper"

**Bước 2:** Cấu hình request
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/shipper/login`

**Bước 3:** Cấu hình Headers
- Click tab **Headers**
- Thêm:
  ```
  Key: Content-Type     | Value: application/json
  Key: Accept          | Value: application/json
  ```

**Bước 4:** Cấu hình Body
- Click tab **Body**
- Chọn **raw**
- Chọn **JSON** từ dropdown
- Nhập:
  ```json
  {
      "email": "22004011@st.vlute.edu.vn",
      "password": "Monkay991@"
  }
  ```

**Bước 5:** Gửi request
- Click **Send**
- Kiểm tra response status: `200 OK`
- **LƯU Ý QUAN TRỌNG:** Copy giá trị `token` từ response để dùng cho các API tiếp theo
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
      "user": {...},
      "token": "eyJ1c2VyX2lkIjo0MS..."  // ← COPY TOKEN NÀY
    }
  }
  ```

**Bước 6 (Optional):** Lưu token vào Environment Variable
- Click biểu tượng mắt (👁️) ở góc phải trên
- Click **Edit** trong Environment
- Thêm variable:
  ```
  Variable: shipper_token
  Initial Value: [paste token vừa copy]
  Current Value: [paste token vừa copy]
  ```

---

### Test 2: Lấy danh sách đơn hàng có thể nhận

**Bước 1:** Tạo request mới
- Đặt tên: "2. Get Available Orders"

**Bước 2:** Cấu hình request
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/shipper/orders/available`

**Bước 3:** Cấu hình Headers
- Click tab **Headers**
- Thêm:
  ```
  Key: Content-Type     | Value: application/json
  Key: Accept          | Value: application/json
  Key: Authorization   | Value: Bearer eyJ1c2VyX2lkIjo0MS...
  ```
  
  **HOẶC** nếu dùng Environment Variable:
  ```
  Key: Authorization   | Value: Bearer {{shipper_token}}
  ```

**Bước 4:** Gửi request
- Click **Send**
- Kiểm tra response status: `200 OK`
- Response sẽ trả về danh sách đơn hàng có trạng thái "Đang chuẩn bị"
- **Lưu ý:** Copy `id` của một đơn hàng để test API tiếp theo

---

### Test 3: Nhận đơn hàng

**Bước 1:** Tạo request mới
- Đặt tên: "3. Accept Order"

**Bước 2:** Cấu hình request
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/shipper/orders/123/accept`
  - Thay `123` bằng `id` đơn hàng từ Test 2

**Bước 3:** Cấu hình Headers
- Click tab **Headers**
- Thêm:
  ```
  Key: Content-Type     | Value: application/json
  Key: Accept          | Value: application/json
  Key: Authorization   | Value: Bearer {{shipper_token}}
  ```

**Bước 4:** Gửi request
- Click **Send**
- Kiểm tra response status: `200 OK`
- Response sẽ chứa thông tin đơn hàng với:
  - `shipper_id`: đã được gán
  - `trang_thai`: "Đang giao"

**Kiểm tra kết quả:**
- Gọi lại API Test 2 → Đơn hàng này sẽ không còn trong danh sách
- Hoặc gọi API Test 5 → Đơn hàng này sẽ xuất hiện trong "Đơn của tôi"

---

### Test 4: Lấy đơn hàng của shipper

**Bước 1:** Tạo request mới
- Đặt tên: "4. Get My Orders"

**Bước 2:** Cấu hình request
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/shipper/orders/my-orders`
  
  **Hoặc lọc theo trạng thái:**
  - `http://localhost:8000/api/shipper/orders/my-orders?status=Đang giao`
  - `http://localhost:8000/api/shipper/orders/my-orders?status=Hoàn tất`

**Bước 3:** Cấu hình Headers
- Click tab **Headers**
- Thêm:
  ```
  Key: Content-Type     | Value: application/json
  Key: Accept          | Value: application/json
  Key: Authorization   | Value: Bearer {{shipper_token}}
  ```

**Bước 4:** Gửi request
- Click **Send**
- Kiểm tra response status: `200 OK`
- Response sẽ trả về danh sách đơn hàng mà shipper hiện tại đang nhận

---

### Test 5: Hoàn tất đơn hàng

**Bước 1:** Tạo request mới
- Đặt tên: "5. Complete Order"

**Bước 2:** Cấu hình request
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/shipper/orders/123/complete`
  - Thay `123` bằng `id` đơn hàng từ Test 3 (đơn đã nhận)

**Bước 3:** Cấu hình Headers
- Click tab **Headers**
- Thêm:
  ```
  Key: Content-Type     | Value: application/json
  Key: Accept          | Value: application/json
  Key: Authorization   | Value: Bearer {{shipper_token}}
  ```

**Bước 4:** Gửi request
- Click **Send**
- Kiểm tra response status: `200 OK`
- Response sẽ chứa thông tin đơn hàng với:
  - `trang_thai`: "Hoàn tất"

**Kiểm tra kết quả:**
- Gọi lại API Test 4 với filter `?status=Hoàn tất`
- Đơn hàng này sẽ xuất hiện với trạng thái "Hoàn tất"

---

### Test 6: Đăng xuất

**Bước 1:** Tạo request mới
- Đặt tên: "6. Logout"

**Bước 2:** Cấu hình request
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/shipper/logout`

**Bước 3:** Cấu hình Headers
- Click tab **Headers**
- Thêm:
  ```
  Key: Content-Type     | Value: application/json
  Key: Accept          | Value: application/json
  Key: Authorization   | Value: Bearer {{shipper_token}}
  ```

**Bước 4:** Gửi request
- Click **Send**
- Kiểm tra response status: `200 OK`
- Response:
  ```json
  {
    "success": true,
    "message": "Đăng xuất thành công"
  }
  ```

---

## 🎯 Test Cases và Expected Results

### ✅ Test Case 1: Đăng nhập thành công
- **Input:** Email và password đúng, vai_tro_id = 5
- **Expected:** Status 200, nhận được token
- **Result:** ✓ Pass

### ✅ Test Case 2: Đăng nhập thất bại - Sai password
- **Input:** Email đúng, password sai
- **Expected:** Status 401, message "Mật khẩu không chính xác"
- **Result:** ✓ Pass

### ✅ Test Case 3: Đăng nhập thất bại - Không phải shipper
- **Input:** Email của user có vai_tro_id ≠ 5
- **Expected:** Status 403, message "Tài khoản không có quyền truy cập app shipper"
- **Result:** ✓ Pass

### ✅ Test Case 4: Lấy đơn hàng không có token
- **Input:** Không gửi header Authorization
- **Expected:** Status 401, message "Token không được cung cấp"
- **Result:** ✓ Pass

### ✅ Test Case 5: Nhận đơn hàng đã có shipper khác
- **Input:** ID đơn hàng đã có shipper_id
- **Expected:** Status 400, message "Đơn hàng này không thể nhận..."
- **Result:** ✓ Pass

### ✅ Test Case 6: Hoàn tất đơn hàng của shipper khác
- **Input:** ID đơn hàng của shipper khác
- **Expected:** Status 403, message "Bạn không có quyền hoàn tất đơn hàng này"
- **Result:** ✓ Pass

---

## 📦 Import Postman Collection

Bạn có thể tạo file JSON để import trực tiếp vào Postman:

**File: `Shipper_API.postman_collection.json`**

```json
{
  "info": {
    "name": "Shipper API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Login Shipper",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Accept",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"email\": \"22004011@st.vlute.edu.vn\",\n    \"password\": \"Monkay991@\"\n}"
        },
        "url": {
          "raw": "http://localhost:8000/api/shipper/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8000",
          "path": ["api", "shipper", "login"]
        }
      }
    },
    {
      "name": "2. Get Available Orders",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Accept",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{shipper_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:8000/api/shipper/orders/available",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8000",
          "path": ["api", "shipper", "orders", "available"]
        }
      }
    },
    {
      "name": "3. Accept Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Accept",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{shipper_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:8000/api/shipper/orders/123/accept",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8000",
          "path": ["api", "shipper", "orders", "123", "accept"]
        }
      }
    },
    {
      "name": "4. Get My Orders",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Accept",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{shipper_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:8000/api/shipper/orders/my-orders",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8000",
          "path": ["api", "shipper", "orders", "my-orders"]
        }
      }
    },
    {
      "name": "5. Complete Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Accept",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{shipper_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:8000/api/shipper/orders/123/complete",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8000",
          "path": ["api", "shipper", "orders", "123", "complete"]
        }
      }
    },
    {
      "name": "6. Logout",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Accept",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{shipper_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:8000/api/shipper/logout",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8000",
          "path": ["api", "shipper", "logout"]
        }
      }
    }
  ]
}
```

**Cách import:**
1. Mở Postman
2. Click **Import** ở góc trên bên trái
3. Chọn file `Shipper_API.postman_collection.json`
4. Click **Import**

---

## 🔒 Security Notes

1. **Luôn sử dụng HTTPS** trong môi trường production
2. **Không lưu token dưới dạng plain text** - sử dụng secure storage (Keychain/Keystore)
3. **Xử lý token hết hạn** - Khi nhận lỗi 401, yêu cầu người dùng đăng nhập lại
4. **Validate dữ liệu** trước khi gửi lên server
5. **Xóa token** khi người dùng đăng xuất

---

## 📞 Support

Nếu có vấn đề hoặc câu hỏi, vui lòng liên hệ team phát triển.

**Version:** 1.0.0  
**Last Updated:** October 15, 2025

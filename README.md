# Bach Hoa E-Commerce Platform

Nền tảng thương mại điện tử Bach Hoa với hệ thống quản trị admin, frontend web, mobile app, và chatbot AI.

## 📋 Cấu trúc Project

```
do_an_2/
├── backend/              # Node.js API Server
├── admin-web/           # Admin Management Web (React)
├── frontend2/           # Mobile App (Android)
├── chatbot/             # AI Chatbot (FastAPI)
└── README.md            # File này
```

## 🚀 Hướng Dẫn Setup

### 📦 Yêu Cầu Hệ Thống

- **Node.js**: v16+ (cho Backend và Admin Web)
- **Python**: v3.8+ (cho Chatbot)
- **MongoDB**: v4.4+ (cơ sở dữ liệu)
- **Git**: Cho việc clone repository
- **npm** hoặc **yarn**: Package manager cho Node.js

---

## 1️⃣ Backend Setup

### Bước 1: Vào thư mục Backend

```bash
cd backend
```

### Bước 2: Cài đặt Dependencies

```bash
npm install
```

### Bước 3: Tạo file `.env`

Tạo file `.env` trong thư mục `backend` với nội dung:

```env
# Server
PORT=5000
NODE_ENV=development

# Database
MONGODB_URI=mongodb://localhost:27017/bach_hoa
# Hoặc nếu dùng MongoDB Atlas:
# MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/bach_hoa

# JWT
JWT_SECRET=your_jwt_secret_key_here

# Cloudinary (cho upload ảnh)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Email (Gmail)
EMAIL_USER=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

# CORS
CORS_ORIGIN=http://localhost:3000,http://localhost:5173
```

### Bước 4: Chạy Backend

```bash
# Development mode (với auto-reload)
npm run dev

# Hoặc production mode
npm start
```

Backend sẽ chạy tại: **http://localhost:5000**

### Kiểm tra Backend

```bash
# Mở browser và truy cập:
http://localhost:5000

# Hoặc dùng curl để test API:
curl http://localhost:5000/api/products
```

---

## 2️⃣ Admin Web Setup

### Bước 1: Vào thư mục Admin Web

```bash
cd admin-web
```

### Bước 2: Cài đặt Dependencies

```bash
npm install
```

### Bước 3: Tạo file `.env.local`

Tạo file `.env.local` trong thư mục `admin-web`:

```env
VITE_API_BASE_URL=http://localhost:5000
```

### Bước 4: Chạy Admin Web

```bash
# Development mode
npm run dev

# Hoặc build production
npm run build
npm run preview
```

Admin Web sẽ chạy tại: **http://localhost:5173** (hoặc port khác nếu 5173 đã dùng)

### Đăng nhập Admin

- Email: `nguyenhaithien2k5@gmail.com`
- Password: `12345678`

---

## 3️⃣ Chatbot Setup

### Bước 1: Vào thư mục Chatbot

```bash
cd chatbot
```

### Bước 2: Tạo Virtual Environment (Python)

**Windows:**

```bash
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

**macOS/Linux:**

```bash
python3 -m venv .venv
source .venv/bin/activate
```

### Bước 3: Cài đặt Dependencies

```bash
pip install -r requirements.txt
```

### Bước 4: Tạo file `.env`

Tạo file `.env` trong thư mục `chatbot`:

```env
# OpenAI API
OPENAI_API_KEY=your_openai_api_key

# FastAPI
PORT=8001
HOST=127.0.0.1
```

### Bước 5: Build Database Chroma

```bash
python build_db.py
```

Lệnh này sẽ tạo vector database từ các file markdown trong thư mục `data/`.

### Bước 6: Chạy Chatbot

```bash
uvicorn main:app --host 127.0.0.1 --port 8001 --reload
```

Chatbot sẽ chạy tại: **http://127.0.0.1:8001**

### Test Chatbot

```bash
# Truy cập Swagger UI:
http://127.0.0.1:8001/docs

# Hoặc test bằng curl:
curl -X POST "http://127.0.0.1:8001/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "Có sản phẩm nào khuyến mãi không?"}'
```

---

## 📱 Mobile App (Android)

### Bước 1: Vào thư mục Frontend2

```bash
cd frontend2
```

### Bước 2: Build Android App

```bash
# Sử dụng Gradle
./gradlew build

# Hoặc trực tiếp mở Android Studio và build từ đó
```

### Bước 3: Configure API URL

Sửa file `app/src/main/java/.../ApiClient.java`:

```java
private static final String BASE_URL = "http://10.0.2.2:5000"; // Cho emulator
// Hoặc
private static final String BASE_URL = "http://your_local_ip:5000"; // Cho device thực
```

---

## 🔄 Chạy Tất Cả Cùng Lúc

### Option 1: Mở nhiều terminal

**Terminal 1 - Backend:**

```bash
cd backend
npm run dev
```

**Terminal 2 - Admin Web:**

```bash
cd admin-web
npm run dev
```

**Terminal 3 - Chatbot:**

```bash
cd chatbot
source .venv/bin/activate  # Hoặc .\.venv\Scripts\Activate.ps1 trên Windows
uvicorn main:app --port 8001 --reload
```

---

## 🗄️ Database Setup

### MongoDB Local

```bash
# Windows
mongod

# macOS
brew services start mongodb-community

# Linux
sudo systemctl start mongod
```

### MongoDB Atlas (Cloud)

1. Tạo tài khoản tại https://www.mongodb.com/cloud/atlas
2. Tạo cluster mới
3. Lấy connection string
4. Thêm vào `.env` của backend:

```env
MONGODB_URI=mongodb+srv://username:password@cluster-name.mongodb.net/bach_hoa
```

### Seed Database (tùy chọn)

```bash
cd backend
node scripts/seedCompleteDatabase.js
```

---

## 📝 API Documentation

Backend API documentation có sẵn trong file:

```
backend/API_COMPLETE_DOCUMENTATION.js
```

Hoặc truy cập:

- Swagger UI: http://localhost:5000/api-docs (nếu bật)
- Postman Collection: `backend/postman_collection.json`

---

## 🔧 Troubleshooting

### Backend không chạy

```bash
# Kiểm tra port 5000 có đang dùng
netstat -ano | findstr :5000

# Kill process trên port 5000
```

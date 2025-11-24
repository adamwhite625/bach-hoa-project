from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn

# Import hàm logic RAG từ file chatbot_core.py
from chatbot_core import get_rag_response

app = FastAPI(
    title="Chatbot RAG Demo API",
    description="API cho seminar demo RAG với FastAPI và GPT-4o"
)

# --- Cấu hình CORS ---
# Cho phép React (chạy ở port 5173) gọi đến API (chạy ở port 8000/8001)
origins = [
    "http://localhost:5173",  # Cổng mặc định của Vite
    "http://127.0.0.1:5173",
    "http://localhost:8000",  # Local testing
    "http://127.0.0.1:8000",
    "http://localhost:8001",  # Port 8001
    "http://127.0.0.1:8001",
    "http://localhost:3000",  # Nếu dùng port 3000
    "http://127.0.0.1:3000",
    "*",  # Cho phép tất cả origins (để test dễ hơn)
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"], # Cho phép tất cả methods (GET, POST, OPTIONS, v.v.)
    allow_headers=["*"], # Cho phép tất cả headers
)

# --- Định nghĩa cấu trúc Request Body ---
class ChatRequest(BaseModel):
    message: str

# --- API Endpoints ---
@app.get("/")
def read_root():
    return {"message": "Chào mừng bạn đến với Chatbot API!"}


@app.post("/api/chat")
def chat_endpoint(request: ChatRequest):
    """
    Đây là API endpoint chính mà React sẽ gọi tới.
    Nó nhận tin nhắn và trả về phản hồi từ hệ thống RAG.
    """
    try:
        response_text = get_rag_response(request.message)
        return {"response": response_text}
    except Exception as e:
        return {"response": f"Lỗi: {e}"}


# --- Lệnh chạy Server (chỉ để tham khảo) ---
if __name__ == "__main__":
    print("Khởi chạy FastAPI server tại http://localhost:8000")
    uvicorn.run(app, host="127.0.0.1", port=8000)
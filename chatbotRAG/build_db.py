#!/usr/bin/env python3
"""
Script để build Vector Store (ChromaDB) từ dữ liệu trong thư mục ./data
Sử dụng OpenAI Embeddings (text-embedding-3-small)
"""

import os
import sys
from dotenv import load_dotenv
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_openai import OpenAIEmbeddings

load_dotenv()

# Định nghĩa đường dẫn
DATA_PATH = "./data"
CHROMA_PATH = "./chroma_db"

# Model embedding nhỏ của OpenAI
OPENAI_EMBED_MODEL = "text-embedding-3-small"

def build_vector_store():
    # Kiểm tra nếu DB tồn tại rồi thì dừng
    if os.path.exists(CHROMA_PATH):
        print(f"Thư mục {CHROMA_PATH} đã tồn tại. Xóa đi nếu muốn build lại.")
        return

    print(f"Đang tạo Vector Store mới từ: {DATA_PATH}")

    # 1. Load data
    loader = DirectoryLoader(
        DATA_PATH,
        glob="**/*",
        loader_cls=TextLoader,
        loader_kwargs={'encoding': 'utf-8'},
        show_progress=True,
        use_multithreading=True
    )
    documents = loader.load()

    if not documents:
        print("❌ Lỗi: Không tìm thấy tài liệu.")
        return

    print(f"Đã tải {len(documents)} file. Bắt đầu phân mảnh...")

    # 2. Split data
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=1500,
        chunk_overlap=200
    )
    docs = text_splitter.split_documents(documents)
    print(f"Đã phân mảnh thành {len(docs)} chunks.")

    # 3. Khởi tạo OpenAI Embeddings
    print(f"Đang khởi tạo OpenAI Embeddings với model: {OPENAI_EMBED_MODEL}")

    try:
        embeddings = OpenAIEmbeddings(
            model=OPENAI_EMBED_MODEL,
            api_key=os.getenv("OPENAI_API_KEY")
        )
        test_vec = embeddings.embed_query("hello world")
        print(f"✅ Embedding OK! Vector size = {len(test_vec)}")

    except Exception as e:
        print("❌ LỖI khi khởi tạo OpenAI Embeddings!")
        print("   Kiểm tra API key trong .env (OPENAI_API_KEY)")
        print(f"   Chi tiết: {e}")
        return

    # 4. Build ChromaDB
    print("Đang xây dựng Vector Store (ChromaDB)...")
    Chroma.from_documents(
        documents=docs,
        embedding=embeddings,
        persist_directory=CHROMA_PATH
    )

    print("🎉 --- XÂY DỰNG VECTOR STORE THÀNH CÔNG! ---")
    print(f"Dữ liệu đã lưu tại: {CHROMA_PATH}")

if __name__ == "__main__":
    build_vector_store()

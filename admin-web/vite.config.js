import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  base: '/admin/',
  plugins: [react()],
  resolve: {
    preserveSymlinks: false,
  },
  build: {
    // Đảm bảo case sensitivity
    rollupOptions: {
      external: [],
    }
  }
})

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

try {
  console.log('📦 Installing dependencies in admin-web...');
  execSync('npm install', { cwd: 'admin-web', stdio: 'inherit' });
  
  console.log('🔨 Building admin-web...');
  execSync('npm run build', { cwd: 'admin-web', stdio: 'inherit' });
  
  console.log('📁 Copying dist folder...');
  const distSrc = path.join(__dirname, 'admin-web', 'dist');
  const distDest = path.join(__dirname, 'dist');
  
  // Xóa dist cũ nếu có
  if (fs.existsSync(distDest)) {
    fs.rmSync(distDest, { recursive: true });
  }
  
  // Copy dist mới
  fs.cpSync(distSrc, distDest, { recursive: true });
  
  console.log('✅ Build completed successfully!');
} catch (error) {
  console.error('❌ Build failed:', error.message);
  process.exit(1);
}

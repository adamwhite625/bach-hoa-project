import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";

// Layouts
import AdminLayout from './components/layout/AdminLayout';

// Removed user/public/auth pages to make admin-only

// Admin Pages
import AdminDashboard from "./pages/Admin/Dashboard";
import AdminProducts from "./pages/Admin/ProductManager";
import AdminOrders from "./pages/Admin/OrderManager";
import AdminCategories from "./pages/Admin/CategoryManager";
import AdminLogin from "./pages/auth/AdminLogin";
import AdminGuard from "./components/common/AdminGuard";

const App = () => {
  return (
    <Router>
      <Routes>
        {/* Redirect root to admin */}
        <Route path="/" element={<Navigate to="/admin" replace />} />

        {/* Admin login */}
        <Route path="/admin/login" element={<AdminLogin />} />

        {/* Admin-only routes */}
        <Route path="/admin" element={<AdminGuard><AdminLayout /></AdminGuard>}>
          <Route index element={<AdminDashboard />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="orders" element={<AdminOrders />} />
          <Route path="categories" element={<AdminCategories />} />
        </Route>

        {/* Catch all other routes */}
        <Route path="*" element={<Navigate to="/admin" replace />} />

        {/* Redirect root and any unknown path to /admin or login depending on auth */}
        <Route path="/" element={<Navigate to="/admin" replace />} />
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </Router>
  );
};

export default App;
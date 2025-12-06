import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";

// Layouts
import AdminLayout from './components/layout/AdminLayout.jsx';

// Removed user/public/auth pages to make admin-only

// Admin Pages
import AdminDashboard from "./pages/Admin/Dashboard.jsx";
import AdminProducts from "./pages/Admin/ProductManager.jsx";
import AdminOrders from "./pages/Admin/OrderManager.jsx";
import AdminCategories from "./pages/Admin/CategoryManager.jsx";
import AdminUsers from "./pages/Admin/UserManager.jsx";
import AdminDiscounts from "./pages/Admin/DiscountManager.jsx";
import AdminSettings from "./pages/Admin/AccountSettings.jsx";
import AdminLogin from "./pages/auth/AdminLogin.jsx";
import AdminGuard from "./components/common/AdminGuard.jsx";

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
          <Route path="users" element={<AdminUsers />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="discounts" element={<AdminDiscounts />} />
          <Route path="settings" element={<AdminSettings />} />
        </Route>

  {/* Catch all other routes */}
  <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </Router>
  );
};

export default App;
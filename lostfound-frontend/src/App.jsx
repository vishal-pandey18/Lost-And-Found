import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminRoute from "./components/AdminRoute";
import AppLayout from "./layouts/AppLayout";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import ItemsListPage from "./pages/ItemsListPage";
import ItemDetailPage from "./pages/ItemDetailPage";
import ItemFormPage from "./pages/ItemFormPage";
import MyClaimsPage from "./pages/MyClaimsPage";
import AdminClaimsPage from "./pages/AdminClaimsPage";
import NotificationsPage from "./pages/NotificationsPage";
import SubscriptionsPage from "./pages/SubscriptionsPage";
import NotFoundPage from "./pages/NotFoundPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="items" element={<ItemsListPage />} />
        <Route path="items/new" element={<ItemFormPage />} />
        <Route path="items/:id" element={<ItemDetailPage />} />
        <Route path="items/:id/edit" element={<ItemFormPage />} />
        <Route path="claims/mine" element={<MyClaimsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="subscriptions" element={<SubscriptionsPage />} />
        <Route
          path="admin/claims"
          element={
            <AdminRoute>
              <AdminClaimsPage />
            </AdminRoute>
          }
        />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
